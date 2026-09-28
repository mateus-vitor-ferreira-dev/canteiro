import { describe, expect, it, vi } from "vitest";
import type { EventoDto } from "@/api/protocolo";
import { Som, VOLUME_PADRAO } from "./som.svelte";

const FIXADA: EventoDto = { tipo: "EVENTO", evento: "PECA_FIXADA", dados: {} };
const NIVEL: EventoDto = { tipo: "EVENTO", evento: "NIVEL_SUBIU", dados: {} };

function armazenamento(inicial: Record<string, string> = {}) {
    const dados = { ...inicial };
    return {
        dados,
        getItem: (chave: string) => dados[chave] ?? null,
        setItem: (chave: string, valor: string) => {
            dados[chave] = valor;
        },
    };
}

describe("Som", () => {
    it("toca o som do evento no volume atual", () => {
        const tocador = vi.fn();
        new Som(tocador, armazenamento()).tocar(FIXADA);
        expect(tocador).toHaveBeenCalledWith(expect.any(Array), VOLUME_PADRAO);
    });

    it("evento sem som não chama o tocador", () => {
        const tocador = vi.fn();
        new Som(tocador, armazenamento()).tocar(NIVEL);
        expect(tocador).not.toHaveBeenCalled();
    });

    it("desligado, fica mudo, e lembra a escolha na próxima partida", () => {
        const tocador = vi.fn();
        const guardado = armazenamento();
        const som = new Som(tocador, guardado);
        som.alternar();
        som.tocar(FIXADA);
        expect(tocador).not.toHaveBeenCalled();
        expect(new Som(tocador, guardado).ligado).toBe(false);
    });

    it("sem armazenamento no navegador, funciona só na partida atual", () => {
        const quebrado = {
            getItem: () => {
                throw new Error("bloqueado");
            },
            setItem: () => {
                throw new Error("bloqueado");
            },
        };
        const som = new Som(vi.fn(), quebrado);
        expect(som.ligado).toBe(true);
        expect(() => som.alternar()).not.toThrow();
        expect(som.ligado).toBe(false);
    });
});
