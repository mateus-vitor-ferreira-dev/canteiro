import { describe, expect, it } from "vitest";
import { estadoDeTeste } from "@/testes-apoio";
import { CAMADAS, compasso, duracaoDoCompasso, intensidadeDoPerigo } from "./musica";

/** Um estado com o índice dado e a pilha com a altura dada, em linhas. */
function estado(indice: number, altura = 0) {
    const base = estadoDeTeste();
    const tabuleiro = base.tabuleiro.map((linha, l) =>
        linha.map((_, c) => (c === 0 && l >= 22 - altura ? "MADEIRA" : null)),
    );
    return { ...base, tabuleiro, estabilidade: { ...base.estabilidade, indice } };
}

describe("intensidadeDoPerigo", () => {
    it("obra firme e pilha baixa não têm perigo", () => {
        expect(intensidadeDoPerigo(estado(1, 4))).toBe(0);
        expect(intensidadeDoPerigo(estado(0.7, 8))).toBe(0);
    });

    it("sobe com o índice caindo, até o máximo em 10 %", () => {
        expect(intensidadeDoPerigo(estado(0.4))).toBeCloseTo(0.5);
        expect(intensidadeDoPerigo(estado(0.1))).toBe(1);
        expect(intensidadeDoPerigo(estado(0))).toBe(1);
    });

    it("sobe com a pilha, até o máximo em 18 linhas", () => {
        expect(intensidadeDoPerigo(estado(1, 13))).toBeCloseTo(0.5);
        expect(intensidadeDoPerigo(estado(1, 18))).toBe(1);
    });

    it("vale o pior dos dois perigos", () => {
        expect(intensidadeDoPerigo(estado(0.4, 18))).toBe(1);
        expect(intensidadeDoPerigo(estado(0.1, 4))).toBe(1);
    });
});

describe("compasso", () => {
    it("o andamento acelera com o perigo", () => {
        expect(duracaoDoCompasso(0)).toBeCloseTo((4 * 60) / 84);
        expect(duracaoDoCompasso(1)).toBeCloseTo((4 * 60) / 144);
        expect(duracaoDoCompasso(0.5)).toBeLessThan(duracaoDoCompasso(0.2));
    });

    it("tranquilo, é só o acorde de fundo", () => {
        const notas = compasso(0, 0);
        expect(notas).toHaveLength(3);
        expect(notas.every((n) => n.onda === "triangle" && n.inicio === 0)).toBe(true);
    });

    it("cada faixa de perigo acrescenta camadas", () => {
        const intensidades = [0, CAMADAS.baixo, CAMADAS.bumbo, CAMADAS.chimbal, CAMADAS.arpejo];
        const tamanhos = intensidades.map((i) => compasso(i, 0).length);
        for (let k = 1; k < tamanhos.length; k++) {
            expect(tamanhos[k]).toBeGreaterThan(tamanhos[k - 1] ?? 0);
        }
        expect(compasso(CAMADAS.chimbal, 0).some((n) => n.onda === "ruido")).toBe(true);
        expect(compasso(CAMADAS.arpejo, 0).some((n) => n.onda === "square")).toBe(true);
        expect(compasso(1, 0).some((n) => n.frequencia > 1200)).toBe(true);
    });

    it("no perigo, a volta troca o sol pelo mi maior", () => {
        const mi = 164.81;
        const fundo = (i: number, numero: number) =>
            compasso(i, numero).filter(
                (n) => n.inicio === 0 && n.onda === "triangle" && n.ganho < 0.05,
            );
        expect(fundo(0, 2).some((n) => Math.abs(n.frequencia - mi) < 0.1)).toBe(false);
        expect(fundo(CAMADAS.tensao, 2).some((n) => Math.abs(n.frequencia - mi) < 0.1)).toBe(true);
    });

    it("nenhuma nota começa fora do compasso", () => {
        for (const i of [0, 0.3, 0.6, 0.8, 1]) {
            const fim = duracaoDoCompasso(i);
            for (const nota of compasso(i, 3)) {
                expect(nota.inicio).toBeGreaterThanOrEqual(0);
                expect(nota.inicio).toBeLessThan(fim);
            }
        }
    });
});
