import { fireEvent, render, screen } from "@testing-library/svelte";
import { afterEach, describe, expect, it, vi } from "vitest";
import { ErroApi, criarPartida, listarDificuldades } from "@/api/cliente";
import NovaPartida from "./NovaPartida.svelte";

vi.mock("@/api/cliente", async (original) => ({
    ...(await original<typeof import("@/api/cliente")>()),
    listarDificuldades: vi.fn(),
    criarPartida: vi.fn(),
}));

afterEach(() => {
    vi.clearAllMocks();
});

describe("NovaPartida", () => {
    it("mostra cada dificuldade com queda, limite e materiais", async () => {
        vi.mocked(listarDificuldades).mockResolvedValue([
            {
                codigo: "FACIL",
                nome: "Fácil",
                intervaloQuedaMs: 800,
                limiteDesvio: 3,
                materiaisLiberados: ["MADEIRA", "ALVENARIA"],
            },
            {
                codigo: "DIFICIL",
                nome: "Difícil",
                intervaloQuedaMs: 500,
                limiteDesvio: 2,
                materiaisLiberados: ["MADEIRA", "ALVENARIA", "CONCRETO", "ACO"],
            },
        ]);

        render(NovaPartida, { aoCriar: vi.fn() });

        expect(await screen.findByRole("heading", { name: "Fácil" })).toBeInTheDocument();
        expect(screen.getByText("uma linha a cada 0,8 s")).toBeInTheDocument();
        expect(screen.getByText("3,0 colunas")).toBeInTheDocument();
        expect(screen.getByText("madeira, alvenaria, concreto, aço")).toBeInTheDocument();
    });

    it("mostra a mensagem de erro quando o backend não responde", async () => {
        vi.mocked(listarDificuldades).mockRejectedValue(
            new ErroApi(
                "SEM_CONEXAO",
                "Não foi possível falar com o jogo. O backend está rodando?",
                0,
            ),
        );

        render(NovaPartida, { aoCriar: vi.fn() });

        expect(await screen.findByRole("alert")).toHaveTextContent("O backend está rodando?");
    });

    it("clicar numa dificuldade cria a partida e entrega o id", async () => {
        vi.mocked(listarDificuldades).mockResolvedValue([
            {
                codigo: "NORMAL",
                nome: "Normal",
                intervaloQuedaMs: 650,
                limiteDesvio: 2.5,
                materiaisLiberados: ["MADEIRA"],
            },
        ]);
        vi.mocked(criarPartida).mockResolvedValue({ id: "xyz" });
        const aoCriar = vi.fn();
        render(NovaPartida, { aoCriar });

        await fireEvent.click(await screen.findByRole("button", { name: /Normal/ }));

        expect(criarPartida).toHaveBeenCalledWith("NORMAL");
        await vi.waitFor(() => expect(aoCriar).toHaveBeenCalledWith("xyz"));
    });
});
