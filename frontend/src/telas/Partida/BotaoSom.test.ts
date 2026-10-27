import { fireEvent, render, screen } from "@testing-library/svelte";
import { describe, expect, it, vi } from "vitest";
import { Som } from "@/estado/som.svelte";
import BotaoSom from "./BotaoSom.svelte";

describe("BotaoSom", () => {
    it("mostra se o som está ligado e alterna ao clicar", async () => {
        const guardado: Record<string, string> = {};
        const som = new Som(vi.fn(), {
            getItem: (c) => guardado[c] ?? null,
            setItem: (c, v) => (guardado[c] = v),
        });
        render(BotaoSom, { som });
        const botao = screen.getByRole("button", { name: "Som" });
        expect(botao).toHaveAttribute("aria-pressed", "true");
        expect(botao).toHaveTextContent("Som ligado");

        await fireEvent.click(botao);
        expect(botao).toHaveAttribute("aria-pressed", "false");
        expect(botao).toHaveTextContent("Som desligado");
    });
});
