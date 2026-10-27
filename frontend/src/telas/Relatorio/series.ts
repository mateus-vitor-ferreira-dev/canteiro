import type { RelatorioDto } from "@/api/protocolo";
import { MATERIAIS } from "@/estilos/materiais";

/** O índice vem de 0 a 1; a tela mostra em porcentagem. */
const PORCENTO = 100;

/** Um ponto do gráfico: a peça (a partir de 1) e a estabilidade logo depois dela, em %. */
export type Ponto = { peca: number; estabilidade: number };

/** Os números do topo do relatório. */
export type Resumo = {
    pecas: number;
    /** Estabilidade média, em %. */
    media: number;
    /** O momento mais perto de cair; `null` se nenhuma peça foi fixada. */
    pior: Ponto | null;
    colapsos: number;
};

/** Uma barra da distribuição: quantas peças de um material, e a fração do total. */
export type FatiaMaterial = {
    codigo: string;
    nome: string;
    pecas: number;
    /** De 0 a 1, sobre o total de peças. */
    fracao: number;
};

/** A estabilidade depois de cada peça, pronta para o gráfico de linha. */
export function pontosDaEstabilidade(relatorio: RelatorioDto): Ponto[] {
    return relatorio.indices.map((indice, i) => ({
        peca: i + 1,
        estabilidade: Math.round(indice * PORCENTO),
    }));
}

/** Os colapsos, no ponto da linha em que aconteceram. Peças fora da partida são ignoradas. */
export function pontosDosColapsos(relatorio: RelatorioDto): Ponto[] {
    const pontos = pontosDaEstabilidade(relatorio);
    return relatorio.colapsos
        .map((peca) => pontos[peca - 1])
        .filter((ponto): ponto is Ponto => ponto !== undefined);
}

/** Peças fixadas, estabilidade média, pior momento e colapsos. */
export function resumir(relatorio: RelatorioDto): Resumo {
    const pontos = pontosDaEstabilidade(relatorio);
    const soma = relatorio.indices.reduce((total, indice) => total + indice, 0);
    const pior = pontos.reduce<Ponto | null>(
        (menor, ponto) =>
            menor === null || ponto.estabilidade < menor.estabilidade ? ponto : menor,
        null,
    );
    return {
        pecas: pontos.length,
        media: pontos.length === 0 ? 0 : Math.round((soma / pontos.length) * PORCENTO),
        pior,
        colapsos: pontosDosColapsos(relatorio).length,
    };
}

/**
 * Quantas peças de cada material, do mais leve ao mais pesado. Os quatro
 * materiais aparecem sempre, mesmo com zero; um código desconhecido vai no fim.
 */
export function distribuicao(relatorio: RelatorioDto): FatiaMaterial[] {
    const total = Object.values(relatorio.materiais).reduce((soma, pecas) => soma + pecas, 0);
    const conhecidos = MATERIAIS.map((m) => ({ codigo: m.codigo, nome: m.nome }));
    const outros = Object.keys(relatorio.materiais)
        .filter((codigo) => !MATERIAIS.some((m) => m.codigo === codigo))
        .map((codigo) => ({ codigo, nome: codigo }));
    return [...conhecidos, ...outros].map(({ codigo, nome }) => {
        const pecas = relatorio.materiais[codigo] ?? 0;
        return { codigo, nome, pecas, fracao: total === 0 ? 0 : pecas / total };
    });
}
