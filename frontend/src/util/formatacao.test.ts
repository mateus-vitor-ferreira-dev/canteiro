import { describe, expect, it } from "vitest";
import {
    corCss,
    formatarColunas,
    formatarData,
    formatarDataHora,
    formatarDuracao,
    formatarSegundos,
} from "./formatacao";

describe("formatarDuracao", () => {
    it("só segundos abaixo de um minuto", () => {
        expect(formatarDuracao(45)).toBe("45 s");
        expect(formatarDuracao(0)).toBe("0 s");
    });

    it("minutos e segundos com dois dígitos", () => {
        expect(formatarDuracao(185)).toBe("3 min 05 s");
    });
});

describe("formatarDataHora", () => {
    it("mostra o dia e a hora do relógio de quem jogou", () => {
        expect(formatarDataHora("2026-09-28T14:32:05")).toBe("28/09/2026 às 14:32");
        expect(formatarDataHora("2026-01-02T03:04:05.123")).toBe("02/01/2026 às 03:04");
    });

    it("sem hora, mostra só a data; texto estranho volta como veio", () => {
        expect(formatarDataHora("2026-09-28")).toBe("28/09/2026");
        expect(formatarDataHora("ontem")).toBe("ontem");
    });
});

describe("formatarData", () => {
    it("converte a data ISO para o formato brasileiro, sem mudar o dia pelo fuso", () => {
        expect(formatarData("2026-09-28")).toBe("28/09/2026");
    });

    it("devolve o texto como veio se ele não for uma data", () => {
        expect(formatarData("ontem")).toBe("ontem");
    });
});

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
