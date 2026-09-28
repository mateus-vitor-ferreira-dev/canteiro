import type { EventoDto } from "@/api/protocolo";
import { receitaDoEvento, type Nota } from "@/sons/receitas";
import { tocarNotas } from "@/sons/sintetizador";

/** Toca notas num volume de 0 a 1. Os testes trocam por um falso. */
export type Tocador = (notas: readonly Nota[], volume: number) => void;

/** O mínimo do `localStorage` que o som usa. */
export type Armazenamento = Pick<Storage, "getItem" | "setItem">;

/** Volume até a tela de configurações (#38) deixar o jogador escolher. */
export const VOLUME_PADRAO = 0.6;

const CHAVE = "canteiro.som";
const DESLIGADO = "desligado";

/**
 * Os efeitos sonoros da partida (RF28). Toca o som de cada evento e guarda,
 * no navegador, se o jogador desligou o som.
 */
export class Som {
    /** Se os sons tocam. Começa como o jogador deixou da última vez. */
    ligado = $state(true);
    /** Volume, de 0 a 1. */
    volume = $state(VOLUME_PADRAO);

    readonly #tocador: Tocador;
    readonly #armazenamento: Armazenamento | null;

    constructor(
        tocador: Tocador = tocadorDoNavegador(),
        armazenamento = armazenamentoDoNavegador(),
    ) {
        this.#tocador = tocador;
        this.#armazenamento = armazenamento;
        this.ligado = this.#ler() !== DESLIGADO;
    }

    /** Liga ou desliga o som, e lembra a escolha. */
    alternar(): void {
        this.ligado = !this.ligado;
        try {
            this.#armazenamento?.setItem(CHAVE, this.ligado ? "ligado" : DESLIGADO);
        } catch {
            // Sem armazenamento (aba anônima, bloqueio): vale só até fechar a aba.
        }
    }

    /** Toca o som do evento, se houver um e se o som estiver ligado. */
    tocar(evento: EventoDto): void {
        const notas = receitaDoEvento(evento);
        if (this.ligado && notas.length > 0) {
            this.#tocador(notas, this.volume);
        }
    }

    #ler(): string | null {
        try {
            return this.#armazenamento?.getItem(CHAVE) ?? null;
        } catch {
            return null;
        }
    }
}

function armazenamentoDoNavegador(): Armazenamento | null {
    try {
        return window.localStorage;
    } catch {
        return null;
    }
}

/**
 * Toca pelo Web Audio. O contexto só nasce no primeiro som: o navegador não
 * deixa tocar antes de o jogador interagir, e a essa altura ele já apertou teclas.
 * Sem Web Audio, fica mudo em vez de quebrar a partida.
 */
function tocadorDoNavegador(): Tocador {
    let contexto: AudioContext | null = null;
    let saida: GainNode | null = null;
    return (notas, volume) => {
        try {
            if (!contexto || !saida) {
                if (typeof AudioContext === "undefined") {
                    return;
                }
                contexto = new AudioContext();
                saida = contexto.createGain();
                saida.connect(contexto.destination);
            }
            if (contexto.state === "suspended") {
                void contexto.resume();
            }
            saida.gain.value = volume;
            tocarNotas(contexto, saida, notas);
        } catch {
            // Um som que falha não pode derrubar a partida.
        }
    };
}
