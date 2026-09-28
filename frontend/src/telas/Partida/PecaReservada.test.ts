import { render, screen } from "@testing-library/svelte";
import { describe, expect, it } from "vitest";
import PecaReservada from "./PecaReservada.svelte";

describe("PecaReservada", () => {
    it("vazia, ensina a tecla de reservar", () => {
        render(PecaReservada, { peca: null, liberada: true });
        expect(screen.getByText("C guarda a peça")).toBeInTheDocument();
    });

    it("mostra a peça guardada e avisa quando a troca está travada", () => {
        render(PecaReservada, {
            peca: {
                forma: "I",
                material: "CONCRETO",
                blocos: [
                    [1, 0],
                    [1, 1],
                    [1, 2],
                    [1, 3],
                ],
            },
            liberada: false,
        });
        expect(screen.getByLabelText("Peça I de Concreto")).toBeInTheDocument();
        expect(screen.getByText("Troca liberada na próxima peça")).toBeInTheDocument();
    });
});
