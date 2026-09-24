import { describe, expect, it } from "vitest";
import { estadoDeTeste } from "@/testes-apoio";
import { desenharTabuleiro, naTela } from "./desenho";

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
