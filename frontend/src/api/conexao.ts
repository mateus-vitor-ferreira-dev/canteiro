import type { Comando, ComandoDto, MensagemDoServidor } from "./protocolo";

/** Quem quer saber o que chega pela conexão da partida. */
export type OuvinteConexao = {
    aoReceber: (mensagem: MensagemDoServidor) => void;
    aoAbrir?: () => void;
    aoFechar?: () => void;
};

/** Uma conexão aberta com a partida, pelo WebSocket. */
export type ConexaoPartida = {
    /** Manda um comando. Se a conexão ainda não abriu ou já fechou, o comando é descartado. */
    enviar: (comando: Comando) => void;
    fechar: () => void;
};

/** Cria o WebSocket. Os testes trocam por um falso. */
export type CriarSocket = (url: string) => WebSocket;

/** Endereço do canal da partida, no mesmo servidor da página (o Vite repassa `/ws` em desenvolvimento). */
export function enderecoDaPartida(id: string, local: Location = window.location): string {
    const protocolo = local.protocol === "https:" ? "wss" : "ws";
    return `${protocolo}://${local.host}/ws/partidas/${encodeURIComponent(id)}`;
}

/** Abre a conexão com a partida (RF02) e repassa cada mensagem do servidor ao ouvinte. */
export function conectarPartida(
    id: string,
    ouvinte: OuvinteConexao,
    criarSocket: CriarSocket = (url) => new WebSocket(url),
): ConexaoPartida {
    const socket = criarSocket(enderecoDaPartida(id));
    socket.addEventListener("open", () => ouvinte.aoAbrir?.());
    socket.addEventListener("close", () => ouvinte.aoFechar?.());
    socket.addEventListener("message", (evento: MessageEvent) => {
        const mensagem = lerMensagem(evento.data);
        if (mensagem) {
            ouvinte.aoReceber(mensagem);
        }
    });
    return {
        enviar(comando) {
            if (socket.readyState === WebSocket.OPEN) {
                const dto: ComandoDto = { tipo: "COMANDO", comando };
                socket.send(JSON.stringify(dto));
            }
        },
        fechar() {
            socket.close();
        },
    };
}

function lerMensagem(dados: unknown): MensagemDoServidor | null {
    if (typeof dados !== "string") {
        return null;
    }
    try {
        return JSON.parse(dados) as MensagemDoServidor;
    } catch {
        return null;
    }
}
