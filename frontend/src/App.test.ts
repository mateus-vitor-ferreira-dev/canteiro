import { fireEvent, render, screen } from "@testing-library/svelte";
import { beforeEach, describe, expect, it, vi } from "vitest";
import { listarDificuldades, listarRanking, registrarNoRanking } from "@/api/cliente";
import { Navegacao } from "@/estado/navegacao.svelte";
import { estadoDeTeste } from "@/testes-apoio";
import App from "./App.svelte";

vi.mock("@/api/cliente", async (original) => ({
    ...(await original<typeof import("@/api/cliente")>()),
    listarDificuldades: vi.fn(),
    listarRanking: vi.fn(),
    registrarNoRanking: vi.fn(),
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
        await fireEvent.click(screen.getByRole("button", { name: /Repetições/ }));
        const titulo = screen.getByRole("heading", { name: "Repetições" });
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

    it("salvar no fim da partida leva ao ranking, com o nome destacado", async () => {
        vi.mocked(registrarNoRanking).mockResolvedValue();
        vi.mocked(listarRanking).mockResolvedValue([
            { nome: "Ana", pontuacao: 900, nivel: 2, linhas: 12, colapsos: 0, data: "2026-09-28" },
        ]);
        const navegacao = new Navegacao();
        navegacao.ir({ nome: "fim", id: "abc", placar: estadoDeTeste().placar });
        render(App, { navegacao });

        await fireEvent.input(screen.getByLabelText("Seu nome no ranking"), {
            target: { value: "Ana" },
        });
        await fireEvent.click(screen.getByRole("button", { name: "Salvar no ranking" }));

        await vi.waitFor(() =>
            expect(navegacao.tela).toEqual({ nome: "ranking", destaque: "Ana" }),
        );
        expect(await screen.findByText("Seu nome")).toBeInTheDocument();
    });

    it("sair explica que a aba pode ser fechada", async () => {
        render(App);
        await fireEvent.click(screen.getByRole("button", { name: /Sair/ }));
        expect(screen.getByText(/Pode fechar esta aba/)).toBeInTheDocument();
    });
});
