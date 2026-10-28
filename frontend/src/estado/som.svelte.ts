import type { EventoDto } from "@/api/protocolo";
import { RANGIDO, receitaDoEvento, type Nota } from "@/sons/receitas";
import { tocarNotas } from "@/sons/sintetizador";
import { configuracoes } from "./configuracoes.svelte";

/**
 * Toca notas num volume de 0 a 1, agora ou a partir de um instante do relógio
 * do áudio (`quando`, em segundos). Os testes trocam por um falso.
 */
export type Tocador = (notas: readonly Nota[], volume: number, quando?: number) => void;

/** O instante atual do relógio do áudio, em segundos, ou `null` se não há áudio. */
export type Relogio = () => number | null;

/** O mínimo do `localStorage` que o som usa. */
export type Armazenamento = Pick<Storage, "getItem" | "setItem">;

/** Volume de 0 a 1 na hora de tocar. */
export type LerVolume = () => number;

const PORCENTO = 100;

const CHAVE = "canteiro.som";
const DESLIGADO = "desligado";

/**
 * Os efeitos sonoros da partida (RF28). Toca o som de cada evento e guarda,
 * no navegador, se o jogador desligou o som.
 */
export class Som {
    /** Se os sons tocam. Começa como o jogador deixou da última vez. */
    ligado = $state(true);

    readonly #tocador: Tocador;
    readonly #armazenamento: Armazenamento | null;
    readonly #volume: LerVolume;

    /**
     * @param tocador quem toca de fato; o padrão usa o Web Audio
     * @param armazenamento onde lembrar se o som está desligado
     * @param volume de onde vem o volume; o padrão é o das configurações (RF27)
     */
    constructor(
        tocador: Tocador = tocadorDoNavegador,
        armazenamento = armazenamentoDoNavegador(),
        volume: LerVolume = () => configuracoes.volume / PORCENTO,
    ) {
        this.#tocador = tocador;
        this.#armazenamento = armazenamento;
        this.#volume = volume;
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

    /**
     * Toca o som do evento, se houver um e se o som estiver ligado.
     *
     * @param sequencia peças seguidas que fecharam linha: o arpejo das linhas sobe com ela
     */
    tocar(evento: EventoDto, sequencia = 0): void {
        this.#tocarNotas(receitaDoEvento(evento, sequencia));
    }

    /** A estrutura rangendo, quando está para cair. */
    ranger(): void {
        this.#tocarNotas(RANGIDO);
    }

    #tocarNotas(notas: readonly Nota[]): void {
        if (this.ligado && notas.length > 0) {
            this.#tocador(notas, this.#volume());
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

let contexto: AudioContext | null = null;
let saida: GainNode | null = null;

/**
 * O contexto de áudio da página, o mesmo para os efeitos e a música. Só nasce
 * quando o primeiro som é pedido: o navegador não deixa tocar antes de o
 * jogador interagir, e a essa altura ele já clicou ou apertou teclas. Sem Web
 * Audio, devolve `null`, e o jogo fica mudo em vez de quebrar.
 */
function audioDaPagina(): { contexto: AudioContext; saida: GainNode } | null {
    if (!contexto || !saida) {
        if (typeof AudioContext === "undefined") {
            return null;
        }
        contexto = new AudioContext();
        saida = contexto.createGain();
        saida.connect(contexto.destination);
    }
    if (contexto.state === "suspended") {
        void contexto.resume();
    }
    return { contexto, saida };
}

/** Toca pelo Web Audio. Um som que falha não pode derrubar a partida. */
export const tocadorDoNavegador: Tocador = (notas, volume, quando) => {
    try {
        const audio = audioDaPagina();
        if (audio) {
            audio.saida.gain.value = volume;
            tocarNotas(audio.contexto, audio.saida, notas, quando ?? audio.contexto.currentTime);
        }
    } catch {
        // segue mudo
    }
};

/** O relógio do áudio da página, que a música usa para emendar os compassos. */
export const relogioDoNavegador: Relogio = () => {
    try {
        return audioDaPagina()?.contexto.currentTime ?? null;
    } catch {
        return null;
    }
};
