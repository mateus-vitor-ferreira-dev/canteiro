import { fireEvent, render, screen } from "@testing-library/svelte";
import { beforeEach, describe, expect, it, vi } from "vitest";
import { flushSync } from "svelte";
import type { OuvinteConexao } from "@/api/conexao";
import type { EstadoDto } from "@/api/protocolo";
import { estadoDeTeste } from "@/testes-apoio";
import Partida from "./Partida.svelte";

beforeEach(() => {
    // o jsdom não tem Canvas; o tabuleiro não desenha, mas o resto da tela funciona
    vi.spyOn(HTMLCanvasElement.prototype, "getContext").mockReturnValue(null);
});

function montar() {
    let ouvinte: OuvinteConexao | undefined;
    const conexao = { enviar: vi.fn(), fechar: vi.fn() };
    const aoSair = vi.fn();
    const tela = render(Partida, {
        id: "abc",
        aoSair,
        conectar: (_id: string, o: OuvinteConexao) => {
            ouvinte = o;
            return conexao;
        },
    });
    const receber = (estado: EstadoDto) => flushSync(() => ouvinte?.aoReceber(estado));
    const reconectar = () => flushSync(() => ouvinte?.aoReconectar?.(1, 500));
    const desistir = () => flushSync(() => ouvinte?.aoDesistir?.());
    return { conexao, aoSair, tela, receber, reconectar, desistir };
}

describe("Partida", () => {
    it("mostra o placar, a estabilidade e a legenda", () => {
        const { receber } = montar();
        receber(estadoDeTeste());
        expect(screen.getByText("1.234")).toBeInTheDocument();
        expect(screen.getByText("68 %")).toBeInTheDocument();
        expect(screen.getByText("Queda instantânea")).toBeInTheDocument();
        expect(screen.getByLabelText("Peça O de ACO")).toBeInTheDocument();
    });

    it("transforma teclas em comandos, sem repetir a tecla segurada", async () => {
        const { conexao, receber } = montar();
        receber(estadoDeTeste());
        await fireEvent.keyDown(window, { code: "ArrowLeft" });
        await fireEvent.keyDown(window, { code: "ArrowLeft", repeat: true });
        await fireEvent.keyDown(window, { code: "Space" });
        expect(conexao.enviar.mock.calls).toEqual([["ESQUERDA"], ["QUEDA_INSTANTANEA"]]);
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
