import { describe, expect, it, vi } from "vitest";
import { conectarPartida, enderecoDaPartida } from "./conexao";

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

function conectar(ouvinte = { aoReceber: vi.fn(), aoAbrir: vi.fn(), aoFechar: vi.fn() }) {
    let socket: SocketFalso | undefined;
    const conexao = conectarPartida("abc123", ouvinte, (url) => {
        socket = new SocketFalso(url);
        return socket as unknown as WebSocket;
    });
    return { conexao, ouvinte, socket: socket as unknown as SocketFalso };
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
        socket.dispatchEvent(new Event("close"));

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
