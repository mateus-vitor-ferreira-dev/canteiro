import { render, screen } from "@testing-library/svelte";
import { describe, expect, it } from "vitest";
import { estadoDeTeste } from "@/testes-apoio";
import Placar from "./Placar.svelte";

const placar = estadoDeTeste().placar;

describe("Placar", () => {
    it("sem sequência, não mostra o multiplicador", () => {
        render(Placar, { placar });
        expect(screen.getByText("1.234")).toBeInTheDocument();
        expect(screen.queryByText("Sequência")).not.toBeInTheDocument();
    });

    it("mostra o multiplicador da sequência, até o dobro", () => {
        const { rerender } = render(Placar, { placar: { ...placar, sequencia: 3 } });
        expect(screen.getByText("×1,3")).toBeInTheDocument();
        rerender({ placar: { ...placar, sequencia: 14 } });
        expect(screen.getByText("×2,0")).toBeInTheDocument();
    });
});
