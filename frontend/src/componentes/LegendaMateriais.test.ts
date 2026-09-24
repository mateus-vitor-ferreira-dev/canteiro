import { render, screen } from "@testing-library/svelte";
import { beforeEach, describe, expect, it, vi } from "vitest";
import LegendaMateriais from "./LegendaMateriais.svelte";

beforeEach(() => {
    vi.spyOn(HTMLCanvasElement.prototype, "getContext").mockReturnValue(null);
});

describe("LegendaMateriais", () => {
    it("mostra o nome e o peso de cada material, não só a cor", () => {
        render(LegendaMateriais);
        for (const texto of ["Madeira", "Alvenaria", "Concreto", "Aço", "leve", "muito pesado"]) {
            expect(screen.getByText(texto)).toBeInTheDocument();
        }
    });
});
