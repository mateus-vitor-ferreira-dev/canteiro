import { fireEvent, render, screen } from "@testing-library/svelte";
import { beforeEach, describe, expect, it, vi } from "vitest";
import { listarDificuldades } from "@/api/cliente";
import { Navegacao } from "@/estado/navegacao.svelte";
import App from "./App.svelte";

vi.mock("@/api/cliente", async (original) => ({
    ...(await original<typeof import("@/api/cliente")>()),
    listarDificuldades: vi.fn(),
}));

beforeEach(() => {
    vi.spyOn(HTMLCanvasElement.prototype, "getContext").mockReturnValue(null);
    vi.mocked(listarDificuldades).mockResolvedValue([]);
});

describe("App", () => {
    it("abre no menu", () => {
        render(App);
        expect(
            screen.getByRole("heading", { name: "A obra precisa ficar de pé" }),
        ).toBeInTheDocument();
    });

    it("vai do menu à escolha da dificuldade e volta, sem recarregar", async () => {
        const navegacao = new Navegacao();
        render(App, { navegacao });
        await fireEvent.click(screen.getByRole("button", { name: /Nova partida/ }));
        expect(screen.getByRole("heading", { name: "Escolha a dificuldade" })).toBeInTheDocument();
        expect(navegacao.tela).toEqual({ nome: "nova-partida" });

        await fireEvent.click(screen.getByRole("button", { name: "Voltar ao menu" }));
        expect(navegacao.tela).toEqual({ nome: "menu" });
    });

    it("tela que ainda não existe mostra o aviso e põe o foco no título", async () => {
        render(App);
        await fireEvent.click(screen.getByRole("button", { name: /Ranking/ }));
        const titulo = screen.getByRole("heading", { name: "Ranking" });
        await vi.waitFor(() => expect(titulo).toHaveFocus());
        expect(screen.getByText(/ainda está em construção/)).toBeInTheDocument();
    });

    it("o voltar do navegador volta uma tela", async () => {
        const navegacao = new Navegacao();
        render(App, { navegacao });
        await fireEvent.click(screen.getByRole("button", { name: /Nova partida/ }));
        await fireEvent(
            window,
            new PopStateEvent("popstate", { state: { tela: { nome: "menu" } } }),
        );
        expect(
            screen.getByRole("heading", { name: "A obra precisa ficar de pé" }),
        ).toBeInTheDocument();
    });

    it("sair explica que a aba pode ser fechada", async () => {
        render(App);
        await fireEvent.click(screen.getByRole("button", { name: /Sair/ }));
        expect(screen.getByText(/Pode fechar esta aba/)).toBeInTheDocument();
    });
});
