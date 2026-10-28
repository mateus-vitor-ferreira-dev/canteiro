import { compasso, duracaoDoCompasso } from "@/sons/musica";
import { configuracoes } from "./configuracoes.svelte";
import {
    relogioDoNavegador,
    tocadorDoNavegador,
    type LerVolume,
    type Relogio,
    type Tocador,
} from "./som.svelte";

/** De quanto em quanto tempo o agendador acorda, em milissegundos. */
const INTERVALO_MS = 100;
/** Quanto do futuro já fica agendado, em segundos: folga para o navegador atrasar um pouco. */
const ANTECEDENCIA = 0.3;
/** Atraso do primeiro compasso, para ele não começar no passado. */
const PARTIDA = 0.05;
const PORCENTO = 100;
/**
 * A música fica abaixo dos efeitos: as notas saem a 60 % do ganho escrito. O
 * volume do controle é o mesmo dos efeitos, porque os dois saem pelo mesmo
 * ganho do áudio; por isso a música baixa as notas, e não o controle.
 */
const NIVEL_DA_MUSICA = 0.6;

/**
 * Toca a música da partida (RF28), que fica mais intensa perto de perder.
 *
 * É um agendador com antecedência: de 100 em 100 ms ele confere o relógio do
 * áudio e agenda os compassos que começam nos próximos 300 ms, cada um
 * emendado no fim do anterior. O relógio do áudio não atrasa como o
 * `setTimeout`, então os compassos não abrem buraco nem se atropelam. A
 * intensidade nova vale a partir do próximo compasso.
 */
export class Musica {
    readonly #tocador: Tocador;
    readonly #relogio: Relogio;
    readonly #ligado: () => boolean;
    readonly #volume: LerVolume;
    #intensidade = 0;
    #proximo: number | null = null;
    #numero = 0;
    #timer: ReturnType<typeof setInterval> | null = null;

    /**
     * @param ligado se o som está ligado; desligado, a música fica calada
     * @param tocador quem toca de fato; o padrão usa o Web Audio
     * @param relogio o relógio do áudio; o padrão é o da página
     * @param volume de onde vem o volume; o padrão é o das configurações (RF27)
     */
    constructor(
        ligado: () => boolean,
        tocador: Tocador = tocadorDoNavegador,
        relogio: Relogio = relogioDoNavegador,
        volume: LerVolume = () => configuracoes.volume / PORCENTO,
    ) {
        this.#ligado = ligado;
        this.#tocador = tocador;
        this.#relogio = relogio;
        this.#volume = volume;
    }

    /** Quão perto de perder, de 0 a 1. */
    set intensidade(valor: number) {
        this.#intensidade = Math.min(1, Math.max(0, valor));
    }

    get intensidade(): number {
        return this.#intensidade;
    }

    /** Se a música está rodando. */
    get tocando(): boolean {
        return this.#timer !== null;
    }

    /** Começa a tocar, se ainda não estava. */
    tocar(): void {
        if (this.#timer === null) {
            this.#timer = setInterval(() => this.agendar(), INTERVALO_MS);
            this.agendar();
        }
    }

    /** Para a música. O que já estava agendado termina em menos de meio segundo. */
    parar(): void {
        if (this.#timer !== null) {
            clearInterval(this.#timer);
            this.#timer = null;
        }
        this.#proximo = null;
    }

    /** Agenda os compassos que começam antes de agora mais a antecedência. */
    agendar(): void {
        const agora = this.#ligado() ? this.#relogio() : null;
        if (agora === null) {
            this.#proximo = null;
            return;
        }
        if (this.#proximo === null || this.#proximo < agora) {
            this.#proximo = agora + PARTIDA;
        }
        while (this.#proximo < agora + ANTECEDENCIA) {
            const notas = compasso(this.#intensidade, this.#numero).map((nota) => ({
                ...nota,
                ganho: nota.ganho * NIVEL_DA_MUSICA,
            }));
            this.#tocador(notas, this.#volume(), this.#proximo);
            this.#proximo += duracaoDoCompasso(this.#intensidade);
            this.#numero++;
        }
    }
}
