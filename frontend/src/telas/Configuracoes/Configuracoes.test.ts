import { fireEvent, render, screen } from "@testing-library/svelte";
import { beforeEach, describe, expect, it, vi } from "vitest";
import { ErroApi, salvarConfiguracoes } from "@/api/cliente";
import { VOLUME_PADRAO, configuracoes } from "@/estado/configuracoes.svelte";
import Configuracoes from "./Configuracoes.svelte";

vi.mock("@/api/cliente", async (original) => ({
    ...(await original<typeof import("@/api/cliente")>()),
    salvarConfiguracoes: vi.fn(),
}));

beforeEach(() => {
    vi.clearAllMocks();
    localStorage.clear();
    configuracoes.teclas = {};
    configuracoes.volume = VOLUME_PADRAO;
    vi.mocked(salvarConfiguracoes).mockRejectedValue(new ErroApi("X", "erro 404", 404));
});

function montar() {
    const aoVoltar = vi.fn();
    render(Configuracoes, { aoVoltar });
    return { aoVoltar };
}

async function trocar(acao: string, codigo: string) {
    await fireEvent.click(screen.getByRole("button", { name: `Trocar a tecla de ${acao}` }));
    await fireEvent.keyDown(window, { code: codigo });
}

describe("Configuracoes", () => {
    it("troca a tecla de uma ação, e o salvar vale para a partida", async () => {
        montar();
        await trocar("Mover para a esquerda", "KeyA");
        expect(screen.getByText("Mover para a esquerda: agora é A.")).toBeInTheDocument();

        await fireEvent.click(screen.getByRole("button", { name: "Salvar" }));
        expect(
            await screen.findByText("Configurações salvas neste navegador."),
        ).toBeInTheDocument();
        expect(configuracoes.tabela.find((t) => t.comando === "ESQUERDA")?.codigos).toEqual([
            "KeyA",
        ]);
    });

    it("tecla que já é de outra ação é recusada, dizendo de qual", async () => {
        montar();
        await trocar("Mover para a esquerda", "ArrowRight");
        expect(
            screen.getByText('→ já serve para "Mover para a direita". Aperte outra tecla.'),
        ).toBeInTheDocument();
        expect(screen.getByText("Aperte a tecla nova (Esc cancela)")).toBeInTheDocument();
    });

    it("Tab e Shift não viram comando, e Esc cancela a troca", async () => {
        montar();
        await trocar("Descer uma linha", "ShiftLeft");
        expect(screen.getByText(/Tab, Shift, Ctrl e Alt sozinhas não servem/)).toBeInTheDocument();
        await fireEvent.keyDown(window, { code: "Escape" });
        expect(screen.queryByText("Aperte a tecla nova (Esc cancela)")).not.toBeInTheDocument();
        expect(
            screen.getByRole("button", { name: "Trocar a tecla de Descer uma linha" }),
        ).toHaveFocus();
    });

    it("Salvar só fica ativo quando algo mudou", async () => {
        montar();
        const salvar = screen.getByRole("button", { name: "Salvar" });
        expect(salvar).toBeDisabled();
        await fireEvent.input(screen.getByLabelText("Volume dos efeitos"), {
            target: { value: "30" },
        });
        expect(salvar).toBeEnabled();
        expect(screen.getByText("Há mudanças não salvas.")).toBeInTheDocument();
        expect(screen.getByText("30 %")).toBeInTheDocument();
    });

    it("recusa do servidor aparece como erro", async () => {
        vi.mocked(salvarConfiguracoes).mockRejectedValue(
            new ErroApi("CONFIGURACAO_INVALIDA", "Volume fora de 0 a 100.", 400),
        );
        montar();
        await fireEvent.input(screen.getByLabelText("Volume dos efeitos"), {
            target: { value: "30" },
        });
        await fireEvent.click(screen.getByRole("button", { name: "Salvar" }));
        expect(await screen.findByRole("alert")).toHaveTextContent("Volume fora de 0 a 100.");
    });

    it("restaurar volta ao padrão, e volta ao menu", async () => {
        configuracoes.volume = 20;
        configuracoes.teclas = { ESQUERDA: ["KeyA"] };
        const { aoVoltar } = montar();
        await fireEvent.click(screen.getByRole("button", { name: "Restaurar padrão" }));
        expect(screen.getByText(`${VOLUME_PADRAO} %`)).toBeInTheDocument();
        expect(screen.getByText("←")).toBeInTheDocument();
        await fireEvent.click(screen.getByRole("button", { name: "Voltar ao menu" }));
        expect(aoVoltar).toHaveBeenCalled();
    });
});
