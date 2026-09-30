import { fireEvent, render, screen } from "@testing-library/svelte";
import { beforeEach, describe, expect, it, vi } from "vitest";
import { flushSync } from "svelte";
import type { OuvinteConexao } from "@/api/conexao";
import type { EstadoDto, EventoDto } from "@/api/protocolo";
import { Som } from "@/estado/som.svelte";
import { estadoDeTeste } from "@/testes-apoio";
import Partida from "./Partida.svelte";

beforeEach(() => {
    // o jsdom não tem Canvas; o tabuleiro não desenha, mas o resto da tela funciona
    vi.spyOn(HTMLCanvasElement.prototype, "getContext").mockReturnValue(null);
});

function montar(som?: Som, treino = false, repeticao = false) {
    let ouvinte: OuvinteConexao | undefined;
    const conexao = { enviar: vi.fn(), fechar: vi.fn() };
    const aoSair = vi.fn();
    const aoTerminar = vi.fn();
    const tela = render(Partida, {
        id: "abc",
        treino,
        repeticao,
        aoSair,
        aoTerminar,
        ...(som ? { som } : {}),
        conectar: (_id: string, o: OuvinteConexao) => {
            ouvinte = o;
            return conexao;
        },
    });
    const receber = (estado: EstadoDto) => flushSync(() => ouvinte?.aoReceber(estado));
    const evento = (e: EventoDto) => flushSync(() => ouvinte?.aoReceber(e));
    const reconectar = () => flushSync(() => ouvinte?.aoReconectar?.(1, 500));
    const desistir = () => flushSync(() => ouvinte?.aoDesistir?.());
    return { conexao, aoSair, aoTerminar, tela, receber, evento, reconectar, desistir };
}

describe("Partida", () => {
    it("mostra o placar, a estabilidade e a legenda", () => {
        const { receber } = montar();
        receber(estadoDeTeste());
        expect(screen.getByText("1.234")).toBeInTheDocument();
        expect(screen.getByText("68 %")).toBeInTheDocument();
        expect(screen.getByText("Queda instantânea")).toBeInTheDocument();
        expect(screen.getByLabelText("Peça O de Aço")).toBeInTheDocument();
    });

    it("transforma teclas em comandos, sem repetir a tecla segurada", async () => {
        const { conexao, receber } = montar();
        receber(estadoDeTeste());
        await fireEvent.keyDown(window, { code: "ArrowLeft" });
        await fireEvent.keyDown(window, { code: "ArrowLeft", repeat: true });
        await fireEvent.keyDown(window, { code: "Space" });
        expect(conexao.enviar.mock.calls).toEqual([["ESQUERDA"], ["QUEDA_INSTANTANEA"]]);
    });

    it("fora do modo treino, não há desfazer: U não faz nada e Ctrl+Z só gira", async () => {
        const { conexao, receber } = montar();
        receber(estadoDeTeste());
        expect(screen.queryByText(/Modo treino/)).toBeNull();
        expect(screen.queryByText("Desfazer a última jogada")).toBeNull();

        await fireEvent.keyDown(window, { code: "KeyU" });
        await fireEvent.keyDown(window, { code: "KeyZ", ctrlKey: true });
        expect(conexao.enviar.mock.calls).toEqual([["GIRAR_ANTI_HORARIO"]]);
    });

    it("no modo treino, U e Ctrl+Z desfazem, e a tela avisa que não vale para o ranking", async () => {
        const { conexao, receber } = montar(undefined, true);
        receber(estadoDeTeste());
        expect(screen.getByText(/esta partida não entra no ranking/)).toBeInTheDocument();
        expect(screen.getByText("U ou Ctrl+Z")).toBeInTheDocument();

        await fireEvent.keyDown(window, { code: "KeyU" });
        await fireEvent.keyDown(window, { code: "KeyZ", ctrlKey: true });
        await fireEvent.keyDown(window, { code: "KeyZ" });
        await fireEvent.click(screen.getByRole("button", { name: "Desfazer a última jogada" }));
        expect(conexao.enviar.mock.calls).toEqual([
            ["DESFAZER"],
            ["DESFAZER"],
            ["GIRAR_ANTI_HORARIO"],
            ["DESFAZER"],
        ]);
    });

    it("na repetição, avisa que é uma gravação e só a pausa vira comando", async () => {
        const { conexao, receber } = montar(undefined, false, true);
        receber(estadoDeTeste());
        expect(screen.getByText(/Repetição: dá para assistir e pausar/)).toBeInTheDocument();
        expect(screen.queryByRole("button", { name: "Girar" })).toBeNull();
        expect(screen.queryByText("Queda instantânea")).toBeNull();
        expect(screen.getByText("Pausar ou continuar")).toBeInTheDocument();

        await fireEvent.keyDown(window, { code: "ArrowLeft" });
        await fireEvent.keyDown(window, { code: "Space" });
        expect(conexao.enviar).not.toHaveBeenCalled();

        await fireEvent.keyDown(window, { code: "KeyP" });
        await fireEvent.click(screen.getByRole("button", { name: "Pausar" }));
        expect(conexao.enviar.mock.calls).toEqual([["PAUSAR"], ["PAUSAR"]]);
    });

    it("no fim da repetição, volta para a lista em vez de pedir o resultado", async () => {
        const { aoSair, aoTerminar, receber } = montar(undefined, false, true);
        receber(estadoDeTeste({ estado: "FIM_DE_JOGO" }));
        expect(screen.getByText("Fim da repetição")).toBeInTheDocument();
        expect(screen.queryByRole("button", { name: "Ver resultado" })).toBeNull();

        const voltar = screen.getByRole("button", { name: "Voltar às repetições" });
        expect(voltar).toHaveFocus();
        await fireEvent.click(voltar);
        expect(aoSair).toHaveBeenCalled();
        expect(aoTerminar).not.toHaveBeenCalled();
    });

    it("pausa quando a aba perde o foco e mostra a camada de pausa", async () => {
        const { conexao, receber } = montar();
        receber(estadoDeTeste());
        await fireEvent.blur(window);
        expect(conexao.enviar).toHaveBeenCalledWith("PAUSAR");

        receber(estadoDeTeste({ estado: "PAUSA" }));
        expect(screen.getByText("Pausado")).toBeInTheDocument();
        await fireEvent.keyDown(window, { code: "KeyP" });
        expect(conexao.enviar).toHaveBeenLastCalledWith("RETOMAR");
    });

    it("no fim de jogo mostra a pontuação e deixa jogar de novo", async () => {
        const { conexao, aoSair, receber } = montar();
        receber(estadoDeTeste({ estado: "FIM_DE_JOGO" }));
        expect(screen.getByText("Fim de jogo")).toBeInTheDocument();

        await fireEvent.keyDown(window, { code: "ArrowLeft" });
        expect(conexao.enviar).not.toHaveBeenCalled();

        await fireEvent.click(screen.getByRole("button", { name: "Jogar de novo" }));
        expect(aoSair).toHaveBeenCalled();
    });

    it("no fim de jogo, Ver resultado recebe o foco e entrega o placar final", async () => {
        const { aoTerminar, receber } = montar();
        const estado = estadoDeTeste({ estado: "FIM_DE_JOGO" });
        receber(estado);
        const botao = screen.getByRole("button", { name: "Ver resultado" });
        expect(botao).toHaveFocus();

        await fireEvent.click(botao);
        expect(aoTerminar).toHaveBeenCalledWith(estado.placar);
    });

    it("toca o som de cada evento, e M liga e desliga sem ir ao backend", async () => {
        const tocador = vi.fn();
        const som = new Som(tocador, { getItem: () => null, setItem: vi.fn() });
        const { conexao, receber, evento } = montar(som);
        receber(estadoDeTeste());
        evento({ tipo: "EVENTO", evento: "PECA_FIXADA", dados: {} });
        expect(tocador).toHaveBeenCalledTimes(1);

        await fireEvent.keyDown(window, { code: "KeyM" });
        expect(som.ligado).toBe(false);
        expect(conexao.enviar).not.toHaveBeenCalled();
        evento({ tipo: "EVENTO", evento: "COLAPSO", dados: {} });
        expect(tocador).toHaveBeenCalledTimes(1);
    });

    it("fecha a conexão ao sair da tela", () => {
        const { conexao, tela } = montar();
        tela.unmount();
        expect(conexao.fechar).toHaveBeenCalled();
    });

    it("mostra que está reconectando e, perdida a partida, deixa começar outra", async () => {
        const { aoSair, receber, reconectar, desistir } = montar();
        receber(estadoDeTeste());
        reconectar();
        expect(screen.getByText("Reconectando…")).toBeInTheDocument();

        desistir();
        expect(screen.getByText("Partida perdida")).toBeInTheDocument();
        await fireEvent.click(screen.getByRole("button", { name: "Começar outra" }));
        expect(aoSair).toHaveBeenCalled();
    });
});
