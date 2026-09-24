import { describe, expect, it } from "vitest";
import { corCss, formatarColunas, formatarSegundos } from "./formatacao";

describe("formatarColunas", () => {
    it("usa vírgula decimal e plural a partir de duas colunas", () => {
        expect(formatarColunas(2.5)).toBe("2,5 colunas");
        expect(formatarColunas(3)).toBe("3,0 colunas");
    });

    it("usa singular abaixo de duas colunas", () => {
        expect(formatarColunas(1.7)).toBe("1,7 coluna");
    });
});

describe("formatarSegundos", () => {
    it("converte milissegundos em segundos sem perder os centésimos", () => {
        expect(formatarSegundos(800)).toBe("0,8 s");
        expect(formatarSegundos(650)).toBe("0,65 s");
        expect(formatarSegundos(1000)).toBe("1,0 s");
    });
});

describe("corCss", () => {
    it("converte o número RGB do backend em cor CSS", () => {
        expect(corCss(0x1f3864)).toBe("#1f3864");
    });

    it("completa com zeros à esquerda", () => {
        expect(corCss(0x0000ff)).toBe("#0000ff");
    });
});
