import type { Comando, ComandoDto, MensagemDoServidor } from "./protocolo";

/** Quem quer saber o que chega pela conexão da partida. */
export type OuvinteConexao = {
    aoReceber: (mensagem: MensagemDoServidor) => void;
    aoAbrir?: () => void;
    aoFechar?: () => void;
    /** A conexão caiu e uma nova tentativa vai acontecer daqui a `esperaMs` (RF31). */
    aoReconectar?: (tentativa: number, esperaMs: number) => void;
    /** O servidor recusou a partida (ela não existe mais): não adianta tentar de novo. */
    aoDesistir?: () => void;
};

/** Uma conexão aberta com a partida, pelo WebSocket. */
export type ConexaoPartida = {
    /** Manda um comando. Se a conexão ainda não abriu ou já fechou, o comando é descartado. */
    enviar: (comando: Comando) => void;
    /** Fecha de vez, sem tentar reconectar. */
    fechar: () => void;
};

/** Cria o WebSocket. Os testes trocam por um falso. */
export type CriarSocket = (url: string) => WebSocket;

/** Agenda uma tentativa de reconexão. Os testes trocam por um relógio falso. */
export type Agendar = (tarefa: () => void, esperaMs: number) => void;

/** Código com que o servidor fecha quando a partida não existe (política violada). */
export const FECHAMENTO_PARTIDA_INEXISTENTE = 1008;

const ESPERA_INICIAL_MS = 500;
const ESPERA_MAXIMA_MS = 8000;

/** Espera antes da tentativa `n` (a partir de 1): dobra a cada vez, até 8 s. */
export function esperaDaTentativa(tentativa: number): number {
    return Math.min(ESPERA_INICIAL_MS * 2 ** (tentativa - 1), ESPERA_MAXIMA_MS);
}

/** Endereço do canal da partida, no mesmo servidor da página (o Vite repassa `/ws` em desenvolvimento). */
export function enderecoDaPartida(id: string, local: Location = window.location): string {
    const protocolo = local.protocol === "https:" ? "wss" : "ws";
    return `${protocolo}://${local.host}/ws/partidas/${encodeURIComponent(id)}`;
}

/**
 * Abre a conexão com a partida e repassa cada mensagem do servidor ao
 * ouvinte. Se a conexão cair sem o jogador ter fechado, tenta de novo em
 * intervalos crescentes; ao voltar, o servidor manda o estado completo.
 */
export function conectarPartida(
    id: string,
    ouvinte: OuvinteConexao,
    criarSocket: CriarSocket = (url) => new WebSocket(url),
    agendar: Agendar = (tarefa, espera) => setTimeout(tarefa, espera),
): ConexaoPartida {
    let socket: WebSocket;
    let fechadaPeloJogador = false;
    let tentativa = 0;

    function abrir() {
        socket = criarSocket(enderecoDaPartida(id));
        socket.addEventListener("open", () => {
            tentativa = 0;
            ouvinte.aoAbrir?.();
        });
        socket.addEventListener("message", (evento: MessageEvent) => {
            const mensagem = lerMensagem(evento.data);
            if (mensagem) {
                ouvinte.aoReceber(mensagem);
            }
        });
        socket.addEventListener("close", (evento: CloseEvent) => aoFechar(evento.code));
    }

    function aoFechar(codigo: number) {
        ouvinte.aoFechar?.();
        if (fechadaPeloJogador) {
            return;
        }
        if (codigo === FECHAMENTO_PARTIDA_INEXISTENTE) {
            ouvinte.aoDesistir?.();
            return;
        }
        tentativa++;
        const espera = esperaDaTentativa(tentativa);
        ouvinte.aoReconectar?.(tentativa, espera);
        agendar(() => {
            if (!fechadaPeloJogador) {
                abrir();
            }
        }, espera);
    }

    abrir();
    return {
        enviar(comando) {
            if (socket.readyState === WebSocket.OPEN) {
                const dto: ComandoDto = { tipo: "COMANDO", comando };
                socket.send(JSON.stringify(dto));
            }
        },
        fechar() {
            fechadaPeloJogador = true;
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
