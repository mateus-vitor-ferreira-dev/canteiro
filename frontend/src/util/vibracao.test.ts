import { afterEach, describe, expect, it, vi } from "vitest";
import { vibrarNoEvento } from "./vibracao";

const evento = (tipo: "PECA_FIXADA" | "COLAPSO" | "NIVEL_SUBIU") =>
    ({ tipo: "EVENTO", evento: tipo, dados: {} }) as const;

describe("vibrarNoEvento", () => {
    afterEach(() => {
        vi.unstubAllGlobals();
    });

    it("sem suporte no navegador, não faz nada", () => {
        vi.stubGlobal("navigator", {});
        expect(vibrarNoEvento(evento("COLAPSO"))).toBe(false);
    });

    it("vibra curto ao fixar e longo no colapso", () => {
        const vibrate = vi.fn(() => true);
        vi.stubGlobal("navigator", { vibrate });
        vibrarNoEvento(evento("PECA_FIXADA"));
        vibrarNoEvento(evento("COLAPSO"));
        expect(vibrate.mock.calls).toEqual([[12], [[70, 50, 140]]]);
    });

    it("eventos sem padrão não vibram", () => {
        const vibrate = vi.fn(() => true);
        vi.stubGlobal("navigator", { vibrate });
        expect(vibrarNoEvento(evento("NIVEL_SUBIU"))).toBe(false);
        expect(vibrate).not.toHaveBeenCalled();
    });
});
