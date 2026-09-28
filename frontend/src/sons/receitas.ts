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

/**
 * O que tocar para cada evento da partida (RF28): fixação, linhas eliminadas
 * e colapso. Os outros eventos não têm som.
 *
 * @returns as notas, ou lista vazia se o evento é silencioso
 */
export function receitaDoEvento(evento: EventoDto): readonly Nota[] {
    switch (evento.evento) {
        case "PECA_FIXADA":
            return FIXACAO;
        case "LINHAS_ELIMINADAS":
            return arpejo(evento.dados.linhas?.length ?? 1);
        case "COLAPSO":
            return COLAPSO;
        default:
            return [];
    }
}

/** Uma nota subindo para cada linha: quatro de uma vez soam como um acorde completo. */
function arpejo(linhas: number): Nota[] {
    const quantas = Math.min(Math.max(linhas, 1), ARPEJO_HZ.length);
    return ARPEJO_HZ.slice(0, quantas).map((frequencia, i) => ({
        onda: "triangle",
        inicio: i * INTERVALO_ARPEJO,
        duracao: i === quantas - 1 ? 0.35 : 0.18,
        frequencia,
        ganho: 0.35,
    }));
}
