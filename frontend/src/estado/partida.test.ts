import { describe, expect, it, vi } from "vitest";
import type { OuvinteConexao } from "@/api/conexao";
import { estadoDeTeste } from "@/testes-apoio";
import { PartidaAoVivo } from "./partida.svelte";

function partidaFalsa() {
    let ouvinte: OuvinteConexao | undefined;
    const conexao = { enviar: vi.fn(), fechar: vi.fn() };
    const partida = new PartidaAoVivo("abc", (_id, o) => {
        ouvinte = o;
        return conexao;
    });
    return { partida, conexao, ouvinte: ouvinte as OuvinteConexao };
}

describe("PartidaAoVivo", () => {
    it("guarda o último estado e o último evento", () => {
        const { partida, ouvinte } = partidaFalsa();
        expect(partida.estado).toBeNull();

        ouvinte.aoReceber(estadoDeTeste({ estado: "PAUSA" }));
        ouvinte.aoReceber({ tipo: "EVENTO", evento: "COLAPSO", dados: {} });

        expect(partida.pausada).toBe(true);
        expect(partida.encerrada).toBe(false);
        expect(partida.ultimoEvento?.evento).toBe("COLAPSO");
    });

    it("sabe quando está conectada", () => {
        const { partida, ouvinte } = partidaFalsa();
        ouvinte.aoAbrir?.();
        expect(partida.conectado).toBe(true);
        ouvinte.aoFechar?.();
        expect(partida.conectado).toBe(false);
    });

    it("manda comandos e fecha pela conexão", () => {
        const { partida, conexao } = partidaFalsa();
        partida.enviar("GIRAR_HORARIO");
        partida.encerrar();
        expect(conexao.enviar).toHaveBeenCalledWith("GIRAR_HORARIO");
        expect(conexao.fechar).toHaveBeenCalled();
    });

    it("sabe quando está reconectando e quando a partida se perdeu", () => {
        const { partida, ouvinte } = partidaFalsa();
        ouvinte.aoReconectar?.(1, 500);
        expect(partida.reconectando).toBe(true);
        ouvinte.aoAbrir?.();
        expect(partida.reconectando).toBe(false);

        ouvinte.aoReconectar?.(1, 500);
        ouvinte.aoDesistir?.();
        expect(partida.reconectando).toBe(false);
        expect(partida.perdida).toBe(true);
    });
});
