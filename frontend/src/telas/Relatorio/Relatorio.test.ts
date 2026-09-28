import { fireEvent, render, screen, within } from "@testing-library/svelte";
import { afterEach, beforeEach, describe, expect, it, vi } from "vitest";
import { ErroApi, buscarRelatorio } from "@/api/cliente";
import type { RelatorioDto } from "@/api/protocolo";
import Relatorio from "./Relatorio.svelte";

vi.mock("@/api/cliente", async (original) => ({
    ...(await original<typeof import("@/api/cliente")>()),
    buscarRelatorio: vi.fn(),
}));

beforeEach(() => {
    // o jsdom não tem Canvas; o gráfico não desenha, mas a tabela e os números aparecem
    vi.spyOn(HTMLCanvasElement.prototype, "getContext").mockReturnValue(null);
});

afterEach(() => {
    vi.clearAllMocks();
});

const RELATORIO: RelatorioDto = {
    indices: [1, 0.8, 0.25, 0.9],
    colapsos: [3],
    materiais: { MADEIRA: 3, ACO: 1 },
};

function montar() {
    const props = { partidaId: "abc", aoVoltar: vi.fn(), aoJogarDeNovo: vi.fn() };
    render(Relatorio, props);
    return props;
}

describe("Relatorio", () => {
    it("busca o relatório da partida e mostra o resumo", async () => {
        vi.mocked(buscarRelatorio).mockResolvedValue(RELATORIO);
        montar();
        expect(buscarRelatorio).toHaveBeenCalledWith("abc");
        expect(await screen.findByText("Peças fixadas")).toBeInTheDocument();
        expect(screen.getByText("74 %")).toBeInTheDocument();
        expect(screen.getByText("na peça 3")).toBeInTheDocument();
    });

    it("tem a tabela com os números do gráfico, marcando o colapso", async () => {
        vi.mocked(buscarRelatorio).mockResolvedValue(RELATORIO);
        montar();
        const tabela = await screen.findByRole("table");
        const linhas = within(tabela).getAllByRole("row");
        expect(linhas).toHaveLength(5);
        expect(linhas[3]).toHaveTextContent("325 %Desabou");
        expect(screen.getByRole("img", { name: /4 peças, com 1 colapsos/ })).toBeInTheDocument();
    });

    it("mostra as peças de cada material, com os quatro sempre presentes", async () => {
        vi.mocked(buscarRelatorio).mockResolvedValue(RELATORIO);
        montar();
        const materiais = await screen.findByText("Peças por material");
        const lista = within(materiais.closest("figure") as HTMLElement).getAllByRole("listitem");
        expect(lista).toHaveLength(4);
        expect(lista[0]).toHaveTextContent("Madeira 3 peças 75 %");
        expect(lista[3]).toHaveTextContent("Aço 1 peça 25 %");
    });

    it("partida sem peças avisa em vez de mostrar gráfico vazio", async () => {
        vi.mocked(buscarRelatorio).mockResolvedValue({ indices: [], colapsos: [], materiais: {} });
        montar();
        expect(await screen.findByText(/Nenhuma peça foi fixada/)).toBeInTheDocument();
        expect(screen.queryByRole("table")).not.toBeInTheDocument();
    });

    it("sem a rota no backend, diz que o relatório ainda não existe e deixa tentar de novo", async () => {
        vi.mocked(buscarRelatorio)
            .mockRejectedValueOnce(new ErroApi("ERRO_DESCONHECIDO", "erro 404", 404))
            .mockResolvedValueOnce(RELATORIO);
        montar();
        expect(await screen.findByRole("alert")).toHaveTextContent(
            "O relatório ainda não está disponível",
        );
        await fireEvent.click(screen.getByRole("button", { name: "Tentar de novo" }));
        expect(await screen.findByText("Peças fixadas")).toBeInTheDocument();
    });

    it("volta ao resultado e começa outra partida", async () => {
        vi.mocked(buscarRelatorio).mockResolvedValue(RELATORIO);
        const { aoVoltar, aoJogarDeNovo } = montar();
        await fireEvent.click(screen.getByRole("button", { name: "Voltar ao resultado" }));
        await fireEvent.click(screen.getByRole("button", { name: "Jogar de novo" }));
        expect(aoVoltar).toHaveBeenCalled();
        expect(aoJogarDeNovo).toHaveBeenCalled();
    });
});
