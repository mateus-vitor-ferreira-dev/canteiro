import { describe, expect, it, vi } from "vitest";
import {
    FECHAMENTO_PARTIDA_INEXISTENTE,
    conectarPartida,
    enderecoDaPartida,
    esperaDaTentativa,
} from "./conexao";

/** WebSocket falso, que dispara os eventos quando o teste manda. */
class SocketFalso extends EventTarget {
    readyState: number = WebSocket.CONNECTING;
    enviados: string[] = [];
    fechado = false;

    constructor(readonly url: string) {
        super();
    }

    send(dados: string) {
        this.enviados.push(dados);
    }

    close() {
        this.fechado = true;
    }

    abrir() {
        this.readyState = WebSocket.OPEN;
        this.dispatchEvent(new Event("open"));
    }

    receber(dados: string) {
        this.dispatchEvent(new MessageEvent("message", { data: dados }));
    }
}

function conectar() {
    const ouvinte = {
        aoReceber: vi.fn(),
        aoAbrir: vi.fn(),
        aoFechar: vi.fn(),
        aoReconectar: vi.fn(),
        aoDesistir: vi.fn(),
    };
    const sockets: SocketFalso[] = [];
    const agendadas: (() => void)[] = [];
    const conexao = conectarPartida(
        "abc123",
        ouvinte,
        (url) => {
            const socket = new SocketFalso(url);
            sockets.push(socket);
            return socket as unknown as WebSocket;
        },
        (tarefa) => agendadas.push(tarefa),
    );
    const atual = () => sockets[sockets.length - 1] as SocketFalso;
    const cair = (codigo = 1006) =>
        atual().dispatchEvent(new CloseEvent("close", { code: codigo }));
    return { conexao, ouvinte, sockets, agendadas, atual, cair, socket: atual() };
}

describe("enderecoDaPartida", () => {
    it("usa ws no http e wss no https, no mesmo servidor da página", () => {
        expect(
            enderecoDaPartida("x", { protocol: "http:", host: "localhost:5173" } as Location),
        ).toBe("ws://localhost:5173/ws/partidas/x");
        expect(enderecoDaPartida("x", { protocol: "https:", host: "jogo" } as Location)).toBe(
            "wss://jogo/ws/partidas/x",
        );
    });
});

describe("conectarPartida", () => {
    it("só manda comando com a conexão aberta", () => {
        const { conexao, socket } = conectar();
        conexao.enviar("ESQUERDA");
        expect(socket.enviados).toEqual([]);

        socket.abrir();
        conexao.enviar("ESQUERDA");

        expect(socket.enviados).toEqual(['{"tipo":"COMANDO","comando":"ESQUERDA"}']);
    });

    it("repassa as mensagens do servidor e avisa quando abre e fecha", () => {
        const { ouvinte, socket } = conectar();
        socket.abrir();
        socket.receber('{"tipo":"EVENTO","evento":"PECA_FIXADA","dados":{}}');
        socket.dispatchEvent(new CloseEvent("close", { code: 1000 }));

        expect(ouvinte.aoAbrir).toHaveBeenCalled();
        expect(ouvinte.aoReceber).toHaveBeenCalledWith({
            tipo: "EVENTO",
            evento: "PECA_FIXADA",
            dados: {},
        });
        expect(ouvinte.aoFechar).toHaveBeenCalled();
    });

    it("descarta mensagem que não é JSON", () => {
        const { ouvinte, socket } = conectar();
        socket.receber("isso não é json");
        expect(ouvinte.aoReceber).not.toHaveBeenCalled();
    });

    it("fechar fecha o socket", () => {
        const { conexao, socket } = conectar();
        conexao.fechar();
        expect(socket.fechado).toBe(true);
    });
});

describe("reconexão", () => {
    it("a espera dobra a cada tentativa, até 8 segundos", () => {
        expect([1, 2, 3, 4, 5, 6].map(esperaDaTentativa)).toEqual([
            500, 1000, 2000, 4000, 8000, 8000,
        ]);
    });

    it("quando a conexão cai, tenta de novo depois da espera", () => {
        const { ouvinte, sockets, agendadas, cair } = conectar();
        sockets[0]?.abrir();

        cair();

        expect(ouvinte.aoReconectar).toHaveBeenCalledWith(1, 500);
        expect(sockets).toHaveLength(1);
        agendadas[0]?.();
        expect(sockets).toHaveLength(2);
        expect(sockets[1]?.url).toBe(sockets[0]?.url);
    });

    it("sem conseguir voltar, espera cada vez mais", () => {
        const { ouvinte, agendadas, cair } = conectar();
        cair();
        agendadas[0]?.();
        cair();
        expect(ouvinte.aoReconectar).toHaveBeenLastCalledWith(2, 1000);
    });

    it("ao voltar, a contagem recomeça", () => {
        const { ouvinte, agendadas, atual, cair } = conectar();
        cair();
        agendadas[0]?.();
        atual().abrir();
        cair();
        expect(ouvinte.aoReconectar).toHaveBeenLastCalledWith(1, 500);
    });

    it("fechar de propósito não reconecta", () => {
        const { conexao, ouvinte, agendadas, cair } = conectar();
        conexao.fechar();
        cair(1000);
        expect(ouvinte.aoReconectar).not.toHaveBeenCalled();
        expect(agendadas).toHaveLength(0);
    });

    it("partida inexistente: desiste em vez de insistir", () => {
        const { ouvinte, agendadas, cair } = conectar();
        cair(FECHAMENTO_PARTIDA_INEXISTENTE);
        expect(ouvinte.aoDesistir).toHaveBeenCalled();
        expect(agendadas).toHaveLength(0);
    });
});
