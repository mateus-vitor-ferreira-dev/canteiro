import { afterEach, describe, expect, it, vi } from "vitest";
import { densidadeDaTela, prefereMenosMovimento } from "./tela";

afterEach(() => vi.unstubAllGlobals());

describe("prefereMenosMovimento", () => {
    it("segue a preferência do sistema", () => {
        vi.stubGlobal("matchMedia", (consulta: string) => ({
            matches: consulta === "(prefers-reduced-motion: reduce)",
        }));
        expect(prefereMenosMovimento()).toBe(true);
    });

    it("sem matchMedia (como no jsdom), anima normalmente", () => {
        vi.stubGlobal("matchMedia", undefined);
        expect(prefereMenosMovimento()).toBe(false);
    });
});

describe("densidadeDaTela", () => {
    it("fica entre 1 e 3", () => {
        vi.stubGlobal("devicePixelRatio", 2);
        expect(densidadeDaTela()).toBe(2);
        vi.stubGlobal("devicePixelRatio", 4);
        expect(densidadeDaTela()).toBe(3);
        vi.stubGlobal("devicePixelRatio", 0);
        expect(densidadeDaTela()).toBe(1);
    });
});
