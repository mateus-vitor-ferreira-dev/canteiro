import { fireEvent, render, screen } from "@testing-library/svelte";
import { beforeEach, describe, expect, it, vi } from "vitest";
import Menu from "./Menu.svelte";

beforeEach(() => {
    // o jsdom não tem Canvas; a torre não desenha, mas o menu funciona
    vi.spyOn(HTMLCanvasElement.prototype, "getContext").mockReturnValue(null);
});

describe("Menu", () => {
    it("tem as cinco opções do RF01", () => {
        render(Menu, { aoEscolher: vi.fn() });
        for (const nome of [/Nova partida/, /Ranking/, /Repetições/, /Configurações/, /Sair/]) {
            expect(screen.getByRole("button", { name: nome })).toBeInTheDocument();
        }
    });

    it("cada opção leva à sua tela", async () => {
        const aoEscolher = vi.fn();
        render(Menu, { aoEscolher });
        await fireEvent.click(screen.getByRole("button", { name: /Nova partida/ }));
        await fireEvent.click(screen.getByRole("button", { name: /Ranking/ }));
        await fireEvent.click(screen.getByRole("button", { name: /Sair/ }));
        expect(aoEscolher.mock.calls).toEqual([
            [{ nome: "nova-partida" }],
            [{ nome: "ranking" }],
            [{ nome: "saida" }],
        ]);
    });

    it("abre com o foco em Nova partida, para começar com Enter", () => {
        render(Menu, { aoEscolher: vi.fn() });
        expect(screen.getByRole("button", { name: /Nova partida/ })).toHaveFocus();
    });

    it("avisa quais telas ainda não existem", () => {
        render(Menu, { aoEscolher: vi.fn() });
        expect(screen.getAllByText("Em breve")).toHaveLength(2);
    });

    it("descreve a torre para o leitor de tela", () => {
        render(Menu, { aoEscolher: vi.fn() });
        expect(
            screen.getByRole("img", { name: /centro de massa fica sobre o eixo/ }),
        ).toBeInTheDocument();
    });
});
