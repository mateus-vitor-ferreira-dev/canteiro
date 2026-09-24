import { afterEach, describe, expect, it, vi } from "vitest";
import { ErroApi, SEM_CONEXAO, criarPartida, listarDificuldades } from "./cliente";
import type { DificuldadeDto } from "./protocolo";

const NORMAL: DificuldadeDto = {
    codigo: "NORMAL",
    nome: "Normal",
    intervaloQuedaMs: 650,
    limiteDesvio: 2.5,
    materiaisLiberados: ["MADEIRA", "ALVENARIA", "CONCRETO"],
};

function responder(corpo: unknown, status = 200): void {
    vi.stubGlobal(
        "fetch",
        vi.fn().mockResolvedValue(new Response(JSON.stringify(corpo), { status })),
    );
}

afterEach(() => {
    vi.unstubAllGlobals();
});

describe("listarDificuldades", () => {
    it("devolve a lista que o backend mandou", async () => {
        responder([NORMAL]);

        await expect(listarDificuldades()).resolves.toEqual([NORMAL]);
        expect(fetch).toHaveBeenCalledWith("/api/dificuldades", expect.anything());
    });

    it("transforma a resposta de erro da API em ErroApi", async () => {
        responder({ erro: "ERRO_INTERNO", mensagem: "Ocorreu um erro inesperado no jogo." }, 500);

        await expect(listarDificuldades()).rejects.toMatchObject({
            codigo: "ERRO_INTERNO",
            message: "Ocorreu um erro inesperado no jogo.",
            status: 500,
        });
    });

    it("avisa quando o backend não está rodando", async () => {
        vi.stubGlobal("fetch", vi.fn().mockRejectedValue(new TypeError("Failed to fetch")));

        const erro = await listarDificuldades().catch((e: unknown) => e);

        expect(erro).toBeInstanceOf(ErroApi);
        expect(erro).toMatchObject({ codigo: SEM_CONEXAO, status: 0 });
    });
});

describe("criarPartida", () => {
    it("manda a dificuldade e devolve o id", async () => {
        responder({ id: "a1b2c3d4" }, 201);

        await expect(criarPartida("NORMAL")).resolves.toEqual({ id: "a1b2c3d4" });

        const [caminho, opcoes] = vi.mocked(fetch).mock.calls[0] as [string, RequestInit];
        expect(caminho).toBe("/api/partidas");
        expect(opcoes.method).toBe("POST");
        expect(JSON.parse(opcoes.body as string)).toEqual({ dificuldade: "NORMAL" });
    });

    it("transforma a dificuldade inválida em ErroApi", async () => {
        responder({ erro: "DIFICULDADE_INVALIDA", mensagem: "Escolha uma dificuldade." }, 400);

        await expect(criarPartida("NORMAL")).rejects.toMatchObject({
            codigo: "DIFICULDADE_INVALIDA",
            status: 400,
        });
    });
});
