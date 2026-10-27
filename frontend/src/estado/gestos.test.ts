import { describe, expect, it } from "vitest";
import { ReconhecedorGestos } from "./gestos";

const CELULA = 24;

/** Arrasta em passos de 4 px a cada 16 ms, como um dedo de verdade, e junta os comandos. */
function arrastar(gestos: ReconhecedorGestos, dx: number, dy: number, passoMs = 16) {
    const passos = Math.max(1, Math.ceil(Math.max(Math.abs(dx), Math.abs(dy)) / 4));
    const comandos: string[] = [];
    gestos.comecar(100, 100, 0);
    for (let i = 1; i <= passos; i++) {
        comandos.push(
            ...gestos.mover(100 + (dx * i) / passos, 100 + (dy * i) / passos, i * passoMs),
        );
    }
    comandos.push(...gestos.terminar(100 + dx, 100 + dy, passos * passoMs));
    return comandos;
}

describe("ReconhecedorGestos", () => {
    it("toque rápido gira", () => {
        const gestos = new ReconhecedorGestos(CELULA);
        gestos.comecar(50, 50, 0);
        expect(gestos.terminar(53, 52, 120)).toEqual(["GIRAR_HORARIO"]);
    });

    it("dedo parado por muito tempo não gira", () => {
        const gestos = new ReconhecedorGestos(CELULA);
        gestos.comecar(50, 50, 0);
        expect(gestos.terminar(50, 50, 600)).toEqual([]);
    });

    it("arrastar para os lados move uma coluna por célula percorrida", () => {
        expect(arrastar(new ReconhecedorGestos(CELULA), 3 * CELULA + 5, 4)).toEqual([
            "DIREITA",
            "DIREITA",
            "DIREITA",
        ]);
        expect(arrastar(new ReconhecedorGestos(CELULA), -2 * CELULA, 0)).toEqual([
            "ESQUERDA",
            "ESQUERDA",
        ]);
    });

    it("ir e voltar move e desfaz o movimento", () => {
        const gestos = new ReconhecedorGestos(CELULA);
        gestos.comecar(100, 100, 0);
        const ida = gestos.mover(100 + 2 * CELULA, 100, 100);
        const volta = gestos.mover(100, 100, 200);
        expect(ida).toEqual(["DIREITA", "DIREITA"]);
        expect(volta).toEqual(["ESQUERDA", "ESQUERDA"]);
    });

    it("o primeiro movimento decide o eixo: arrasto para o lado não desce a peça", () => {
        const gestos = new ReconhecedorGestos(CELULA);
        gestos.comecar(100, 100, 0);
        gestos.mover(130, 105, 50);
        expect(gestos.mover(130, 100 + 3 * CELULA, 300)).toEqual([]);
    });

    it("arrastar devagar para baixo desce linha a linha, sem queda instantânea", () => {
        const comandos = arrastar(new ReconhecedorGestos(CELULA), 0, 3 * CELULA, 60);
        expect(comandos).toEqual(["DESCER", "DESCER", "DESCER"]);
    });

    it("gesto rápido para baixo faz a queda instantânea", () => {
        const comandos = arrastar(new ReconhecedorGestos(CELULA), 0, 3 * CELULA, 4);
        expect(comandos.at(-1)).toBe("QUEDA_INSTANTANEA");
    });

    it("arrastar para cima guarda na reserva", () => {
        expect(arrastar(new ReconhecedorGestos(CELULA), 0, -3 * CELULA)).toEqual(["RESERVAR"]);
        expect(arrastar(new ReconhecedorGestos(CELULA), 0, -CELULA)).toEqual([]);
    });

    it("gesto cancelado não dá comando", () => {
        const gestos = new ReconhecedorGestos(CELULA);
        gestos.comecar(50, 50, 0);
        gestos.cancelar();
        expect(gestos.mover(150, 50, 50)).toEqual([]);
        expect(gestos.terminar(150, 50, 60)).toEqual([]);
    });
});
