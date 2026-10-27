import { describe, expect, it } from "vitest";
import type { RelatorioDto } from "@/api/protocolo";
import { distribuicao, pontosDaEstabilidade, pontosDosColapsos, resumir } from "./series";

const RELATORIO: RelatorioDto = {
    indices: [1, 0.8, 0.25, 0.9],
    colapsos: [3],
    materiais: { MADEIRA: 3, ACO: 1 },
};

describe("séries do relatório", () => {
    it("numera as peças a partir de 1 e passa o índice para %", () => {
        expect(pontosDaEstabilidade(RELATORIO)).toEqual([
            { peca: 1, estabilidade: 100 },
            { peca: 2, estabilidade: 80 },
            { peca: 3, estabilidade: 25 },
            { peca: 4, estabilidade: 90 },
        ]);
    });

    it("põe cada colapso no ponto da linha da peça que derrubou", () => {
        expect(pontosDosColapsos(RELATORIO)).toEqual([{ peca: 3, estabilidade: 25 }]);
    });

    it("ignora colapso numa peça que não existe", () => {
        expect(pontosDosColapsos({ ...RELATORIO, colapsos: [0, 9] })).toEqual([]);
    });

    it("resume peças, média, pior momento e colapsos", () => {
        expect(resumir(RELATORIO)).toEqual({
            pecas: 4,
            media: 74,
            pior: { peca: 3, estabilidade: 25 },
            colapsos: 1,
        });
    });

    it("partida sem peças não divide por zero", () => {
        expect(resumir({ indices: [], colapsos: [], materiais: {} })).toEqual({
            pecas: 0,
            media: 0,
            pior: null,
            colapsos: 0,
        });
    });

    it("mostra os quatro materiais em ordem de peso, mesmo com zero", () => {
        const fatias = distribuicao(RELATORIO);
        expect(fatias.map((f) => [f.codigo, f.pecas])).toEqual([
            ["MADEIRA", 3],
            ["ALVENARIA", 0],
            ["CONCRETO", 0],
            ["ACO", 1],
        ]);
        expect(fatias[0]?.fracao).toBe(0.75);
    });

    it("material desconhecido vai no fim, com o código como nome", () => {
        const fatias = distribuicao({ ...RELATORIO, materiais: { VIDRO: 2 } });
        expect(fatias.at(-1)).toEqual({ codigo: "VIDRO", nome: "VIDRO", pecas: 2, fracao: 1 });
    });
});
