import { render, screen, waitFor } from "@testing-library/svelte";
import { describe, expect, it } from "vitest";
import Destaques from "./Destaques.svelte";

describe("Destaques", () => {
    it("mostra o ganho das linhas e some depois da animação", async () => {
        const { rerender } = render(Destaques, { evento: null });
        await rerender({
            evento: { tipo: "EVENTO", evento: "LINHAS_ELIMINADAS", dados: { pontos: 330 } },
        });
        expect(screen.getByText("+330")).toBeInTheDocument();
        await waitFor(() => expect(screen.queryByText("+330")).not.toBeInTheDocument(), {
            timeout: 2000,
        });
    });
});
