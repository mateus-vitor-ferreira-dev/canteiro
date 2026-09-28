import { fireEvent, render, screen, within } from "@testing-library/svelte";
import { afterEach, describe, expect, it, vi } from "vitest";
import { ErroApi, listarRanking } from "@/api/cliente";
import type { EntradaRankingDto } from "@/api/protocolo";
import Ranking from "./Ranking.svelte";

vi.mock("@/api/cliente", async (original) => ({
    ...(await original<typeof import("@/api/cliente")>()),
    listarRanking: vi.fn(),
}));

afterEach(() => {
    vi.clearAllMocks();
});

function entrada(nome: string, pontuacao: number): EntradaRankingDto {
    return { nome, pontuacao, nivel: 3, linhas: 1, colapsos: 2, data: "2026-09-28" };
}

function montar(destaque?: string) {
    const props = { destaque, aoJogar: vi.fn(), aoVoltar: vi.fn() };
    render(Ranking, props);
    return props;
}

describe("Ranking", () => {
    it("mostra as pontuações na ordem, com posição, detalhes e pontos", async () => {
        vi.mocked(listarRanking).mockResolvedValue([entrada("Ana", 12340), entrada("Bia", 900)]);
        montar();
        const linhas = await screen.findAllByRole("listitem");
        expect(linhas).toHaveLength(2);
        expect(within(linhas[0] as HTMLElement).getByText("Ana")).toBeInTheDocument();
        expect(linhas[0]).toHaveTextContent("12.340 pontos");
        expect(linhas[0]).toHaveTextContent("nível 3, 1 linha, 2 colapsos, 28/09/2026");
        expect(linhas[1]).toHaveTextContent("Bia");
    });

    it("destaca o nome que acabou de ser registrado", async () => {
        vi.mocked(listarRanking).mockResolvedValue([entrada("Ana", 12340), entrada("Bia", 900)]);
        montar("Bia");
        const linhas = await screen.findAllByRole("listitem");
        expect(linhas[1]).toHaveAttribute("aria-current", "true");
        expect(within(linhas[1] as HTMLElement).getByText("Seu nome")).toBeInTheDocument();
        expect(linhas[0]).not.toHaveAttribute("aria-current");
    });

    it("ranking vazio convida a jogar", async () => {
        vi.mocked(listarRanking).mockResolvedValue([]);
        const { aoJogar } = montar();
        await fireEvent.click(await screen.findByRole("button", { name: "Jogar agora" }));
        expect(aoJogar).toHaveBeenCalled();
    });

    it("erro aparece como mensagem e dá para tentar de novo", async () => {
        vi.mocked(listarRanking)
            .mockRejectedValueOnce(new ErroApi("ERRO_DESCONHECIDO", "erro 404", 404))
            .mockResolvedValueOnce([entrada("Ana", 10)]);
        montar();
        expect(await screen.findByRole("alert")).toHaveTextContent(
            "O ranking ainda não está disponível",
        );
        await fireEvent.click(screen.getByRole("button", { name: "Tentar de novo" }));
        expect(await screen.findByText("Ana")).toBeInTheDocument();
    });

    it("volta ao menu", async () => {
        vi.mocked(listarRanking).mockResolvedValue([]);
        const { aoVoltar } = montar();
        await fireEvent.click(screen.getByRole("button", { name: "Voltar ao menu" }));
        expect(aoVoltar).toHaveBeenCalled();
    });
});
