import { describe, expect, it } from "vitest";
import { estadoDeTeste } from "@/testes-apoio";
import {
    CELULA_MAXIMA,
    CELULA_MINIMA,
    desenharColapso,
    desenharTabuleiro,
    naTela,
    posicaoNaQueda,
    tamanhoDaCelula,
} from "./desenho";

/** Contexto 2D falso que só anota os quadrados pintados depois do fundo. */
function contextoFalso() {
    const pintados: string[] = [];
    const ctx = {
        fillStyle: "",
        strokeStyle: "",
        lineWidth: 1,
        fillRect(x: number, y: number) {
            pintados.push(`${this.fillStyle}@${x},${y}`);
        },
        strokeRect() {},
        beginPath() {},
        moveTo() {},
        lineTo() {},
        stroke() {},
        setLineDash() {},
        quadraticCurveTo() {},
        arc() {},
        fill() {},
    };
    return { ctx: ctx as unknown as CanvasRenderingContext2D, pintados };
}

describe("desenharTabuleiro", () => {
    it("pinta o fundo, os blocos fixados e a peça atual; o bloco da linha oculta não aparece", () => {
        const tabuleiro = estadoDeTeste().tabuleiro.map((linha, l) =>
            linha.map((_, c) => ((l === 21 || l === 0) && c === 0 ? "ACO" : null)),
        );
        const estado = estadoDeTeste({ tabuleiro });
        const { ctx, pintados } = contextoFalso();

        desenharTabuleiro(ctx, estado, 10);

        const blocos = pintados.slice(1);
        expect(blocos).toContain("#4e5a6b@1,191");
        expect(blocos.filter((p) => p.startsWith("#e6c48f"))).toHaveLength(4);
        expect(blocos).toHaveLength(5);
    });

    it("sem estado, pinta só o fundo", () => {
        const { ctx, pintados } = contextoFalso();
        desenharTabuleiro(ctx, null, 10);
        expect(pintados).toHaveLength(1);
    });
});

describe("naTela", () => {
    it("desconta as duas linhas ocultas", () => {
        expect(naTela([2, 3], 10)).toEqual({ x: 30, y: 0 });
        expect(naTela([1, 3], 10)).toBeNull();
    });
});

describe("posicaoNaQueda", () => {
    const queda = { origem: [10, 5], destino: [20, 5] } as const;

    it("começa na origem e termina no destino, na mesma coluna", () => {
        expect(posicaoNaQueda({ origem: [10, 5], destino: [20, 5] }, 0)).toEqual({
            linha: 10,
            coluna: 5,
        });
        expect(posicaoNaQueda({ origem: [10, 5], destino: [20, 5] }, 1)).toEqual({
            linha: 20,
            coluna: 5,
        });
    });

    it("acelera como na gravidade: na metade do tempo percorreu só um quarto", () => {
        expect(
            posicaoNaQueda({ origem: [...queda.origem], destino: [...queda.destino] }, 0.5).linha,
        ).toBe(12.5);
    });

    it("limita o progresso entre 0 e 1", () => {
        expect(posicaoNaQueda({ origem: [10, 5], destino: [20, 5] }, 2).linha).toBe(20);
        expect(posicaoNaQueda({ origem: [10, 5], destino: [20, 5] }, -1).linha).toBe(10);
    });
});

describe("desenharColapso", () => {
    it("apaga o bloco no destino e o desenha a caminho, com o clarão por cima", () => {
        const tabuleiro = estadoDeTeste().tabuleiro.map((linha, l) =>
            linha.map((_, c) => (l === 21 && c === 5 ? "ACO" : null)),
        );
        const { ctx, pintados } = contextoFalso();

        desenharColapso(
            ctx,
            estadoDeTeste({ tabuleiro }),
            [{ origem: [11, 5], destino: [21, 5] }],
            0,
            10,
        );

        expect(pintados[0]).toBe("#1b2432@50,190");
        expect(pintados[1]).toBe("#4e5a6b@51,91");
        expect(pintados[2]).toMatch(/^rgba\(244, 112, 103, 0.45\)@0,0$/);
    });
});

describe("tamanhoDaCelula", () => {
    it("usa o que couber: 20 linhas na altura e 10 colunas na largura", () => {
        expect(tamanhoDaCelula(1000, 500)).toBe(25);
        expect(tamanhoDaCelula(220, 900)).toBe(22);
    });

    it("fica entre o mínimo e o máximo", () => {
        expect(tamanhoDaCelula(50, 50)).toBe(CELULA_MINIMA);
        expect(tamanhoDaCelula(5000, 5000)).toBe(CELULA_MAXIMA);
    });
});
