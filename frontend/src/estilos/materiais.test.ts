import { describe, expect, it } from "vitest";
import {
    COR_DESCONHECIDA,
    MATERIAIS,
    corDoMaterial,
    desenharBloco,
    infoDoMaterial,
} from "./materiais";

/** Contexto 2D falso que conta o que foi desenhado. */
function contextoFalso() {
    const chamadas: string[] = [];
    const anotar = (nome: string) => () => {
        chamadas.push(nome);
    };
    const ctx = {
        fillStyle: "",
        strokeStyle: "",
        lineWidth: 1,
        fillRect: anotar("fillRect"),
        strokeRect: anotar("strokeRect"),
        beginPath: anotar("beginPath"),
        moveTo: anotar("moveTo"),
        lineTo: anotar("lineTo"),
        quadraticCurveTo: anotar("quadraticCurveTo"),
        stroke: anotar("stroke"),
        arc: anotar("arc"),
        fill: anotar("fill"),
    };
    return { ctx: ctx as unknown as CanvasRenderingContext2D, chamadas };
}

/** Luminância relativa (WCAG 2) de uma cor #rrggbb. */
function luminancia(hex: string): number {
    const [r, g, b] = [1, 3, 5].map((i) => {
        const canal = parseInt(hex.slice(i, i + 2), 16) / 255;
        return canal <= 0.03928 ? canal / 12.92 : ((canal + 0.055) / 1.055) ** 2.4;
    });
    return 0.2126 * (r ?? 0) + 0.7152 * (g ?? 0) + 0.0722 * (b ?? 0);
}

describe("materiais", () => {
    it("vão do mais leve ao mais pesado, com nome e peso para a legenda", () => {
        expect(MATERIAIS.map((m) => m.codigo)).toEqual(["MADEIRA", "ALVENARIA", "CONCRETO", "ACO"]);
        expect(infoDoMaterial("ACO")).toMatchObject({ nome: "Aço", peso: "muito pesado" });
    });

    it("material desconhecido ganha a cor de aviso", () => {
        expect(corDoMaterial("OURO")).toBe(COR_DESCONHECIDA);
        expect(infoDoMaterial("OURO")).toBeUndefined();
    });

    it("nenhum par de materiais tem a mesma claridade: dá para separar em escala de cinza", () => {
        for (const a of MATERIAIS) {
            for (const b of MATERIAIS) {
                if (a !== b) {
                    const [clara, escura] = [luminancia(a.cor), luminancia(b.cor)].sort(
                        (x, y) => y - x,
                    );
                    expect(((clara ?? 0) + 0.05) / ((escura ?? 0) + 0.05)).toBeGreaterThan(1.4);
                }
            }
        }
    });
});

describe("desenharBloco", () => {
    it("cada material tem uma textura diferente, além da cor", () => {
        const assinaturas = MATERIAIS.map((m) => {
            const { ctx, chamadas } = contextoFalso();
            desenharBloco(ctx, 0, 0, 28, m.codigo);
            return chamadas.join(",");
        });
        expect(new Set(assinaturas).size).toBe(MATERIAIS.length);
    });

    it("pinta um único retângulo e contorna o bloco com a borda clara", () => {
        const { ctx, chamadas } = contextoFalso();
        desenharBloco(ctx, 0, 0, 28, "ACO");
        expect(chamadas.filter((c) => c === "fillRect")).toHaveLength(1);
        expect(chamadas.at(-1)).toBe("strokeRect");
    });
});
