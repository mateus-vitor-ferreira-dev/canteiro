import type { EstadoDto } from "@/api/protocolo";
import type { Nota } from "./receitas";

/**
 * A música da partida, descrita como dado, como os efeitos em `receitas.ts`:
 * um loop em lá menor, compasso a compasso, que fica mais cheio, mais rápido e
 * mais tenso conforme a **intensidade** sobe, de 0 (obra firme e pilha baixa)
 * a 1 (prestes a perder).
 */

/** Batidas por compasso. */
const BATIDAS = 4;
/** Andamento com a obra tranquila e no limite do perigo, em batidas por minuto. */
const ANDAMENTO_CALMO = 84;
const ANDAMENTO_MAXIMO = 144;

/** A partir de que intensidade cada camada entra. */
export const CAMADAS = {
    baixo: 0.2,
    bumbo: 0.35,
    chimbal: 0.5,
    arpejo: 0.6,
    tensao: 0.75,
    alarme: 0.9,
} as const;

/** O lá central da música, em MIDI. */
const LA = 57;

/** Lá menor, notas MIDI do grave ao agudo: o acorde em que a música começa e para onde volta. */
const LA_MENOR: readonly number[] = [LA, 60, 64];

/** Lá menor, fá, dó e sol: a volta tranquila. */
const CALMA: readonly (readonly number[])[] = [LA_MENOR, [53, 57, 60], [55, 60, 64], [55, 59, 62]];

/** Lá menor, fá e mi maior duas vezes: o mi puxa de volta para o lá e não deixa a música descansar. */
const TENSA: readonly (readonly number[])[] = [LA_MENOR, [53, 57, 60], [52, 56, 59], [52, 56, 59]];

/** Índice de estabilidade em que o perigo começa a contar e em que ele chega ao máximo. */
const INDICE_TRANQUILO = 0.7;
const INDICE_CRITICO = 0.1;
/** Altura da pilha, em linhas, em que o perigo começa a contar e em que ele chega ao máximo. */
const ALTURA_TRANQUILA = 8;
const ALTURA_CRITICA = 18;
const LINHAS_DA_GRADE = 22;

/**
 * O quanto a partida está perto de acabar, de 0 a 1: a pior entre a
 * estabilidade (índice caindo de 70 % para 10 %) e a altura da pilha (de 8
 * para 18 linhas, onde o colapso já encerra a partida).
 */
export function intensidadeDoPerigo(estado: EstadoDto): number {
    const equilibrio = entre0e1(
        (INDICE_TRANQUILO - estado.estabilidade.indice) / (INDICE_TRANQUILO - INDICE_CRITICO),
    );
    const topo = estado.tabuleiro.findIndex((linha) => linha.some(Boolean));
    const altura = topo < 0 ? 0 : LINHAS_DA_GRADE - topo;
    const pilha = entre0e1((altura - ALTURA_TRANQUILA) / (ALTURA_CRITICA - ALTURA_TRANQUILA));
    return Math.max(equilibrio, pilha);
}

/** Quanto dura um compasso nessa intensidade, em segundos: o andamento acelera com o perigo. */
export function duracaoDoCompasso(intensidade: number): number {
    const andamento =
        ANDAMENTO_CALMO + (ANDAMENTO_MAXIMO - ANDAMENTO_CALMO) * entre0e1(intensidade);
    return (BATIDAS * 60) / andamento;
}

/**
 * As notas de um compasso, com o início contado a partir do começo dele.
 *
 * @param intensidade de 0 a 1: decide as camadas, o andamento e a harmonia
 * @param numero quantos compassos já tocaram: escolhe o acorde da volta
 */
export function compasso(intensidade: number, numero: number): Nota[] {
    const i = entre0e1(intensidade);
    const volta = i >= CAMADAS.tensao ? TENSA : CALMA;
    const acorde = volta[numero % volta.length] ?? LA_MENOR;
    const batida = duracaoDoCompasso(i) / BATIDAS;
    const notas = fundo(acorde, batida);
    if (i >= CAMADAS.baixo) {
        notas.push(...baixo(acorde, batida, i >= CAMADAS.arpejo ? 2 : 1));
    }
    if (i >= CAMADAS.bumbo) {
        notas.push(...bumbo(batida, i >= CAMADAS.tensao ? [0, 1, 2, 3] : [0, 2]));
    }
    if (i >= CAMADAS.chimbal) {
        notas.push(...chimbal(batida, i >= CAMADAS.alarme ? 4 : 2));
    }
    if (i >= CAMADAS.arpejo) {
        notas.push(...arpejo(acorde, batida));
    }
    if (i >= CAMADAS.tensao) {
        notas.push(...caixa(batida));
    }
    if (i >= CAMADAS.alarme) {
        notas.push(...alarme(batida));
    }
    return notas;
}

/** O acorde segurado o compasso inteiro, baixinho: a cama de tudo. */
function fundo(acorde: readonly number[], batida: number): Nota[] {
    return acorde.map((midi) => ({
        onda: "triangle",
        inicio: 0,
        duracao: batida * BATIDAS,
        frequencia: hz(midi),
        ganho: 0.035,
    }));
}

/** A fundamental uma oitava abaixo, a cada batida ou a cada meia batida. */
function baixo(acorde: readonly number[], batida: number, porBatida: number): Nota[] {
    const passo = batida / porBatida;
    return Array.from({ length: BATIDAS * porBatida }, (_, k) => ({
        onda: "triangle" as const,
        inicio: k * passo,
        duracao: passo * 0.85,
        frequencia: hz((acorde[0] ?? LA) - 12),
        ganho: 0.13,
    }));
}

/** Um tom grave que despenca: o bumbo. */
function bumbo(batida: number, batidas: readonly number[]): Nota[] {
    return batidas.map((b) => ({
        onda: "sine",
        inicio: b * batida,
        duracao: 0.16,
        frequencia: 120,
        frequenciaFinal: 45,
        ganho: 0.3,
    }));
}

/** Chiado agudo e curtíssimo, em colcheias ou em semicolcheias. */
function chimbal(batida: number, porBatida: number): Nota[] {
    const passo = batida / porBatida;
    return Array.from({ length: BATIDAS * porBatida }, (_, k) => ({
        onda: "ruido" as const,
        inicio: k * passo,
        duracao: 0.035,
        frequencia: 9000,
        frequenciaFinal: 6500,
        ganho: k % porBatida === 0 ? 0.06 : 0.035,
    }));
}

/** As notas do acorde uma oitava acima, subindo em colcheias. */
function arpejo(acorde: readonly number[], batida: number): Nota[] {
    const passo = batida / 2;
    return Array.from({ length: BATIDAS * 2 }, (_, k) => ({
        onda: "square" as const,
        inicio: k * passo,
        duracao: passo * 0.7,
        frequencia: hz((acorde[k % acorde.length] ?? LA) + 12),
        ganho: 0.022,
    }));
}

/** Caixa nos tempos 2 e 4: o estalo do ruído com o corpo de um tom médio. */
function caixa(batida: number): Nota[] {
    return [1, 3].flatMap((b): Nota[] => [
        {
            onda: "ruido",
            inicio: b * batida,
            duracao: 0.1,
            frequencia: 3200,
            frequenciaFinal: 1400,
            ganho: 0.12,
        },
        { onda: "triangle", inicio: b * batida, duracao: 0.08, frequencia: 190, ganho: 0.06 },
    ]);
}

/** Dois bipes agudos no fim do compasso, como uma sirene distante. */
function alarme(batida: number): Nota[] {
    return [3, 3.5].map((b) => ({
        onda: "sine",
        inicio: b * batida,
        duracao: batida * 0.4,
        frequencia: 1318.5,
        frequenciaFinal: 1244.5,
        ganho: 0.04,
    }));
}

function hz(midi: number): number {
    return 440 * 2 ** ((midi - 69) / 12);
}

function entre0e1(valor: number): number {
    return Math.min(1, Math.max(0, valor));
}
