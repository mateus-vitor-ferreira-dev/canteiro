import { fireEvent, render, screen } from "@testing-library/svelte";
import { afterEach, describe, expect, it, vi } from "vitest";
import { ErroApi, listarRepeticoes, reproduzirRepeticao } from "@/api/cliente";
import type { RepeticaoDto } from "@/api/protocolo";
import Repeticoes from "./Repeticoes.svelte";

vi.mock("@/api/cliente", async (original) => ({
    ...(await original<typeof import("@/api/cliente")>()),
    listarRepeticoes: vi.fn(),
    reproduzirRepeticao: vi.fn(),
}));

afterEach(() => {
    vi.clearAllMocks();
});

const GRAVADAS: RepeticaoDto[] = [
    {
        id: "2026-09-28_14-32-05",
        data: "2026-09-28T14:32:05",
        dificuldade: "NORMAL",
        pontuacao: 12340,
    },
    {
        id: "2026-09-27_09-10-00",
        data: "2026-09-27T09:10:00",
        dificuldade: "DIFICIL",
        pontuacao: 900,
    },
];

function montar() {
    const props = { aoReproduzir: vi.fn(), aoJogar: vi.fn(), aoVoltar: vi.fn() };
    render(Repeticoes, props);
    return props;
}

describe("Repeticoes", () => {
    it("lista as partidas gravadas com data, dificuldade e pontuação", async () => {
        vi.mocked(listarRepeticoes).mockResolvedValue(GRAVADAS);
        montar();
        const linhas = await screen.findAllByRole("listitem");
        expect(linhas).toHaveLength(2);
        expect(linhas[0]).toHaveTextContent("28/09/2026 às 14:32");
        expect(linhas[0]).toHaveTextContent("Dificuldade Normal");
        expect(linhas[0]).toHaveTextContent("12.340 pontos");
        expect(linhas[1]).toHaveTextContent("Dificuldade Difícil");
    });

    it("escolher uma partida cria a reprodução e entrega o id dela", async () => {
        vi.mocked(listarRepeticoes).mockResolvedValue(GRAVADAS);
        vi.mocked(reproduzirRepeticao).mockResolvedValue({ id: "rep1" });
        const { aoReproduzir } = montar();

        await fireEvent.click(await screen.findByRole("button", { name: /28\/09\/2026/ }));

        expect(reproduzirRepeticao).toHaveBeenCalledWith("2026-09-28_14-32-05");
        await vi.waitFor(() => expect(aoReproduzir).toHaveBeenCalledWith("rep1"));
    });

    it("se a reprodução não abre, avisa e deixa tentar outra", async () => {
        vi.mocked(listarRepeticoes).mockResolvedValue(GRAVADAS);
        vi.mocked(reproduzirRepeticao).mockRejectedValue(
            new ErroApi("REPETICAO_INVALIDA", "O arquivo desta repetição está corrompido.", 422),
        );
        const { aoReproduzir } = montar();

        await fireEvent.click(await screen.findByRole("button", { name: /28\/09\/2026/ }));

        expect(await screen.findByRole("alert")).toHaveTextContent("está corrompido");
        expect(aoReproduzir).not.toHaveBeenCalled();
        expect(screen.getByRole("button", { name: /27\/09\/2026/ })).toBeEnabled();
    });

    it("sem nenhuma gravação, convida a jogar", async () => {
        vi.mocked(listarRepeticoes).mockResolvedValue([]);
        const { aoJogar } = montar();
        expect(await screen.findByText("Nenhuma partida gravada ainda.")).toBeInTheDocument();
        await fireEvent.click(screen.getByRole("button", { name: "Jogar agora" }));
        expect(aoJogar).toHaveBeenCalled();
    });

    it("enquanto o backend não tem a rota, diz que ainda não está disponível", async () => {
        vi.mocked(listarRepeticoes).mockRejectedValue(
            new ErroApi("NAO_ENCONTRADO", "Not found", 404),
        );
        montar();
        expect(await screen.findByRole("alert")).toHaveTextContent(
            "A lista de repetições ainda não está disponível nesta versão do jogo.",
        );
    });

    it("erro da API mostra a mensagem e deixa tentar de novo", async () => {
        vi.mocked(listarRepeticoes).mockRejectedValueOnce(
            new ErroApi("SEM_CONEXAO", "Não foi possível falar com o jogo.", 0),
        );
        vi.mocked(listarRepeticoes).mockResolvedValue(GRAVADAS);
        const { aoVoltar } = montar();
        expect(await screen.findByRole("alert")).toHaveTextContent("Não foi possível falar");

        await fireEvent.click(screen.getByRole("button", { name: "Tentar de novo" }));
        expect(await screen.findAllByRole("listitem")).toHaveLength(2);

        await fireEvent.click(screen.getByRole("button", { name: "Voltar ao menu" }));
        expect(aoVoltar).toHaveBeenCalled();
    });
});
