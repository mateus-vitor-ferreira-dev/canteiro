import { describe, expect, it } from "vitest";
import { TECLAS, comandoDaTecla } from "./teclado";

describe("comandoDaTecla", () => {
    it("traduz as setas, o espaço e as letras", () => {
        expect(comandoDaTecla("ArrowLeft", false, false)).toBe("ESQUERDA");
        expect(comandoDaTecla("ArrowRight", false, false)).toBe("DIREITA");
        expect(comandoDaTecla("ArrowDown", false, false)).toBe("DESCER");
        expect(comandoDaTecla("Space", false, false)).toBe("QUEDA_INSTANTANEA");
        expect(comandoDaTecla("ArrowUp", false, false)).toBe("GIRAR_HORARIO");
        expect(comandoDaTecla("KeyX", false, false)).toBe("GIRAR_HORARIO");
        expect(comandoDaTecla("KeyZ", false, false)).toBe("GIRAR_ANTI_HORARIO");
        expect(comandoDaTecla("KeyC", false, false)).toBe("RESERVAR");
    });

    it("ignora a tecla segurada: cada toque vale um comando", () => {
        expect(comandoDaTecla("ArrowLeft", true, false)).toBeNull();
    });

    it("a tecla de pausa alterna entre pausar e retomar", () => {
        expect(comandoDaTecla("KeyP", false, false)).toBe("PAUSAR");
        expect(comandoDaTecla("Escape", false, true)).toBe("RETOMAR");
    });

    it("tecla sem função não gera comando", () => {
        expect(comandoDaTecla("KeyQ", false, false)).toBeNull();
    });

    it("toda tecla da tabela tem rótulo e ação para a legenda", () => {
        for (const tecla of TECLAS) {
            expect(tecla.rotulo).not.toBe("");
            expect(tecla.acao).not.toBe("");
            expect(tecla.codigos.length).toBeGreaterThan(0);
        }
    });
});
