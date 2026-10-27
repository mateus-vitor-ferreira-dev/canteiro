import { describe, expect, it } from "vitest";
import {
    TECLAS,
    comandoDaTecla,
    ehAtalhoDesfazer,
    montarTeclas,
    rotuloDaTecla,
    teclaReservada,
    teclasDaRepeticao,
    teclasDoModo,
} from "./teclado";

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

    it("M liga e desliga o som, sem virar comando do backend", () => {
        expect(comandoDaTecla("KeyM", false, false)).toBe("SOM");
        expect(comandoDaTecla("KeyM", false, true)).toBe("SOM");
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

describe("modo treino", () => {
    it("fora do treino, a tecla de desfazer sai da tabela e não gera comando", () => {
        const teclas = teclasDoModo(TECLAS, false);
        expect(teclas.some((t) => t.comando === "DESFAZER")).toBe(false);
        expect(comandoDaTecla("KeyU", false, false, teclas)).toBeNull();
        expect(teclas).toHaveLength(TECLAS.length - 1);
    });

    it("no treino, U desfaz e a legenda mostra também o Ctrl+Z", () => {
        const teclas = teclasDoModo(TECLAS, true);
        expect(comandoDaTecla("KeyU", false, false, teclas)).toBe("DESFAZER");
        expect(teclas.find((t) => t.comando === "DESFAZER")?.rotulo).toBe("U ou Ctrl+Z");
    });

    it("a tecla de desfazer trocada pelo jogador continua só do treino", () => {
        const teclas = montarTeclas({ DESFAZER: ["KeyB"] });
        expect(teclasDoModo(teclas, false).some((t) => t.comando === "DESFAZER")).toBe(false);
        expect(teclasDoModo(teclas, true).find((t) => t.comando === "DESFAZER")?.rotulo).toBe(
            "B ou Ctrl+Z",
        );
    });

    it("na repetição sobram só a pausa e o som", () => {
        expect(teclasDaRepeticao(TECLAS).map((t) => t.comando)).toEqual(["PAUSA", "SOM"]);
    });

    it("o atalho de desfazer é o Z com Ctrl ou ⌘, e só ele", () => {
        expect(ehAtalhoDesfazer("KeyZ", true)).toBe(true);
        expect(ehAtalhoDesfazer("KeyZ", false)).toBe(false);
        expect(ehAtalhoDesfazer("KeyX", true)).toBe(false);
    });
});

describe("teclas personalizadas", () => {
    it("troca as teclas de uma ação e refaz o rótulo da legenda", () => {
        const teclas = montarTeclas({ ESQUERDA: ["KeyA"], PAUSA: ["KeyP", "Space"] });
        const esquerda = teclas.find((t) => t.comando === "ESQUERDA");
        expect(esquerda?.codigos).toEqual(["KeyA"]);
        expect(esquerda?.rotulo).toBe("A");
        expect(teclas.find((t) => t.comando === "PAUSA")?.rotulo).toBe("P ou Espaço");
    });

    it("ação sem troca fica com a tecla padrão", () => {
        const padrao = TECLAS.find((t) => t.comando === "DIREITA");
        expect(montarTeclas({}).find((t) => t.comando === "DIREITA")).toEqual(padrao);
        expect(montarTeclas({ DIREITA: [] }).find((t) => t.comando === "DIREITA")).toEqual(padrao);
    });

    it("o comando segue a tabela em uso", () => {
        const teclas = montarTeclas({ ESQUERDA: ["KeyA"] });
        expect(comandoDaTecla("KeyA", false, false, teclas)).toBe("ESQUERDA");
        expect(comandoDaTecla("ArrowLeft", false, false, teclas)).toBeNull();
    });

    it("dá nome às teclas comuns e mantém o código das raras", () => {
        expect(rotuloDaTecla("KeyW")).toBe("W");
        expect(rotuloDaTecla("Digit7")).toBe("7");
        expect(rotuloDaTecla("ArrowUp")).toBe("↑");
        expect(rotuloDaTecla("Numpad4")).toBe("Num 4");
        expect(rotuloDaTecla("Semicolon")).toBe("Semicolon");
    });

    it("Tab e as modificadoras sozinhas não podem virar comando", () => {
        for (const codigo of ["Tab", "ShiftLeft", "ControlRight", "AltLeft", "MetaLeft"]) {
            expect(teclaReservada(codigo)).toBe(true);
        }
        expect(teclaReservada("KeyA")).toBe(false);
    });
});
