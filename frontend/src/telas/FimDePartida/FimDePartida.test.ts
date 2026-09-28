import { fireEvent, render, screen } from "@testing-library/svelte";
import { afterEach, describe, expect, it, vi } from "vitest";
import { ErroApi, registrarNoRanking } from "@/api/cliente";
import { estadoDeTeste } from "@/testes-apoio";
import FimDePartida from "./FimDePartida.svelte";

vi.mock("@/api/cliente", async (original) => ({
    ...(await original<typeof import("@/api/cliente")>()),
    registrarNoRanking: vi.fn(),
}));

afterEach(() => {
    vi.clearAllMocks();
});

function montar() {
    const props = {
        partidaId: "abc",
        placar: { ...estadoDeTeste().placar, pontuacao: 12340, tempoSegundos: 185 },
        aoSalvo: vi.fn(),
        aoJogarDeNovo: vi.fn(),
        aoVoltar: vi.fn(),
    };
    render(FimDePartida, props);
    return props;
}

async function salvarComo(nome: string) {
    await fireEvent.input(screen.getByLabelText("Seu nome no ranking"), {
        target: { value: nome },
    });
    await fireEvent.click(screen.getByRole("button", { name: "Salvar no ranking" }));
}

describe("FimDePartida", () => {
    it("mostra a pontuação, o nível, as linhas, os colapsos e o tempo", () => {
        montar();
        expect(screen.getByText("12.340")).toBeInTheDocument();
        expect(screen.getByText("Colapsos").nextElementSibling).toHaveTextContent("1");
        expect(screen.getByText("3 min 05 s")).toBeInTheDocument();
    });

    it("salva o nome sem espaços nas pontas e avisa quem chamou", async () => {
        vi.mocked(registrarNoRanking).mockResolvedValue();
        const { aoSalvo } = montar();
        await salvarComo("  Ana  ");
        expect(registrarNoRanking).toHaveBeenCalledWith("abc", "Ana");
        await vi.waitFor(() => expect(aoSalvo).toHaveBeenCalledWith("Ana"));
    });

    it("nome vazio não vai para o backend e explica o que falta", async () => {
        montar();
        await salvarComo("   ");
        expect(registrarNoRanking).not.toHaveBeenCalled();
        expect(screen.getByRole("alert")).toHaveTextContent("Digite um nome para salvar.");
        expect(screen.getByLabelText("Seu nome no ranking")).toHaveFocus();
        expect(screen.getByLabelText("Seu nome no ranking")).toHaveAttribute(
            "aria-invalid",
            "true",
        );
    });

    it("o erro some quando o jogador volta a digitar", async () => {
        montar();
        await salvarComo("");
        expect(screen.getByRole("alert")).toBeInTheDocument();
        await fireEvent.input(screen.getByLabelText("Seu nome no ranking"), {
            target: { value: "A" },
        });
        expect(screen.queryByRole("alert")).not.toBeInTheDocument();
    });

    it("erro do backend aparece como mensagem, e a tela continua", async () => {
        vi.mocked(registrarNoRanking).mockRejectedValue(
            new ErroApi("NOME_INVALIDO", "O nome pode ter até 20 caracteres.", 400),
        );
        const { aoSalvo } = montar();
        await salvarComo("Ana");
        expect(await screen.findByRole("alert")).toHaveTextContent("até 20 caracteres");
        expect(aoSalvo).not.toHaveBeenCalled();
        expect(screen.getByRole("button", { name: "Salvar no ranking" })).toBeEnabled();
    });

    it("sem a rota do ranking no backend, diz que ele ainda não existe", async () => {
        vi.mocked(registrarNoRanking).mockRejectedValue(
            new ErroApi("ERRO_DESCONHECIDO", "O jogo respondeu com erro 404.", 404),
        );
        montar();
        await salvarComo("Ana");
        expect(await screen.findByRole("alert")).toHaveTextContent(
            "O ranking ainda não está disponível",
        );
    });

    it("jogar de novo e voltar ao menu", async () => {
        const { aoJogarDeNovo, aoVoltar } = montar();
        await fireEvent.click(screen.getByRole("button", { name: "Jogar de novo" }));
        await fireEvent.click(screen.getByRole("button", { name: "Voltar ao menu" }));
        expect(aoJogarDeNovo).toHaveBeenCalled();
        expect(aoVoltar).toHaveBeenCalled();
    });
});
