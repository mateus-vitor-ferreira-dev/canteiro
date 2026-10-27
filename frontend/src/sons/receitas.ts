import type { EventoDto } from "@/api/protocolo";

/**
 * Um som curto, descrito como dado: a onda, quando começa, quanto dura, a
 * frequência (que pode deslizar) e o volume. O sintetizador toca; aqui só se
 * decide o que tocar, para os testes conferirem sem precisar de áudio.
 */
export type Nota = {
    /** Forma da onda, ou `"ruido"` para chiado (poeira, entulho). */
    onda: "sine" | "triangle" | "square" | "ruido";
    /** Segundos depois do evento. */
    inicio: number;
    /** Segundos de duração. */
    duracao: number;
    /** Frequência no começo, em Hz. No ruído, é o corte do filtro. */
    frequencia: number;
    /** Frequência no fim; sem ela, fica a mesma. */
    frequenciaFinal?: number;
    /** Volume de pico, de 0 a 1. */
    ganho: number;
};

/** Dó, mi, sol e dó: uma nota a mais para cada linha eliminada junto. */
const ARPEJO_HZ = [523.25, 659.25, 783.99, 1046.5] as const;
const INTERVALO_ARPEJO = 0.07;

/** Baque seco de peça assentando: um tom grave que cai, com um estalo. */
const FIXACAO: readonly Nota[] = [
    {
        onda: "triangle",
        inicio: 0,
        duracao: 0.12,
        frequencia: 150,
        frequenciaFinal: 55,
        ganho: 0.5,
    },
    {
        onda: "ruido",
        inicio: 0,
        duracao: 0.05,
        frequencia: 1800,
        frequenciaFinal: 400,
        ganho: 0.15,
    },
];

/** Desabamento: estrondo grave, entulho caindo e a poeira assentando. */
const COLAPSO: readonly Nota[] = [
    { onda: "sine", inicio: 0, duracao: 0.9, frequencia: 80, frequenciaFinal: 32, ganho: 0.55 },
    { onda: "ruido", inicio: 0, duracao: 0.9, frequencia: 1600, frequenciaFinal: 120, ganho: 0.5 },
    {
        onda: "ruido",
        inicio: 0.18,
        duracao: 0.12,
        frequencia: 2400,
        frequenciaFinal: 600,
        ganho: 0.3,
    },
    {
        onda: "ruido",
        inicio: 0.34,
        duracao: 0.12,
        frequencia: 2000,
        frequenciaFinal: 500,
        ganho: 0.25,
    },
    {
        onda: "ruido",
        inicio: 0.5,
        duracao: 0.12,
        frequencia: 1600,
        frequenciaFinal: 400,
        ganho: 0.2,
    },
];

/** Meta cumprida: o arpejo inteiro, rápido, e o dó de cima sustentado. */
const META: readonly Nota[] = [
    ...ARPEJO_HZ.map((frequencia, i): Nota => ({
        onda: "square",
        inicio: i * 0.05,
        duracao: 0.1,
        frequencia,
        ganho: 0.12,
    })),
    { onda: "triangle", inicio: 0.2, duracao: 0.6, frequencia: 1046.5, ganho: 0.3 },
];

/** Nível novo: sol, dó e mi subindo, em onda quadrada, com o mi de cima sustentado. */
const NIVEL: readonly Nota[] = [
    { onda: "square", inicio: 0, duracao: 0.12, frequencia: 392, ganho: 0.12 },
    { onda: "square", inicio: 0.12, duracao: 0.12, frequencia: 523.25, ganho: 0.12 },
    { onda: "triangle", inicio: 0.24, duracao: 0.5, frequencia: 659.25, ganho: 0.32 },
];

/**
 * A estrutura rangendo, quando está para cair: um gemido grave que desce,
 * com o chiado da madeira e do ferro forçados.
 */
export const RANGIDO: readonly Nota[] = [
    { onda: "triangle", inicio: 0, duracao: 0.5, frequencia: 95, frequenciaFinal: 62, ganho: 0.3 },
    {
        onda: "ruido",
        inicio: 0.05,
        duracao: 0.35,
        frequencia: 700,
        frequenciaFinal: 300,
        ganho: 0.12,
    },
];

/** Cada peça da sequência sobe o arpejo um semitom, até uma oitava menos dois. */
const SEMITONS_MAXIMOS = 10;

/**
 * O que tocar para cada evento da partida (RF28): fixação, linhas eliminadas,
 * colapso, nível novo e meta cumprida. Os outros eventos não têm som.
 *
 * @param sequencia quantas peças seguidas fecharam linha: o arpejo das linhas sobe com ela
 *
 * @returns as notas, ou lista vazia se o evento é silencioso
 */
export function receitaDoEvento(evento: EventoDto, sequencia = 0): readonly Nota[] {
    switch (evento.evento) {
        case "PECA_FIXADA":
            return FIXACAO;
        case "LINHAS_ELIMINADAS":
            return arpejo(evento.dados.linhas?.length ?? 1, sequencia);
        case "COLAPSO":
            return COLAPSO;
        case "META_CUMPRIDA":
            return META;
        case "NIVEL_SUBIU":
            return NIVEL;
        default:
            return [];
    }
}

/**
 * Uma nota subindo para cada linha: quatro de uma vez soam como um acorde
 * completo. A sequência sobe o arpejo inteiro, para a sequência longa soar
 * como tensão crescendo.
 */
function arpejo(linhas: number, sequencia: number): Nota[] {
    const quantas = Math.min(Math.max(linhas, 1), ARPEJO_HZ.length);
    const transposicao = 2 ** (Math.min(Math.max(sequencia, 0), SEMITONS_MAXIMOS) / 12);
    return ARPEJO_HZ.slice(0, quantas).map((frequencia, i) => ({
        onda: "triangle",
        inicio: i * INTERVALO_ARPEJO,
        duracao: i === quantas - 1 ? 0.35 : 0.18,
        frequencia: frequencia * transposicao,
        ganho: 0.35,
    }));
}
