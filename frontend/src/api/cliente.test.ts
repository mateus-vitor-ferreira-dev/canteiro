import { afterEach, describe, expect, it, vi } from "vitest";
import {
    ErroApi,
    SEM_CONEXAO,
    criarPartida,
    listarDificuldades,
    listarRepeticoes,
    reproduzirRepeticao,
} from "./cliente";
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

describe("repetições", () => {
    it("lista as gravações e pede a reprodução de uma pelo id", async () => {
        const gravadas = [
            { id: "g 1", data: "2026-09-28T14:32:05", dificuldade: "NORMAL", pontuacao: 900 },
        ];
        responder(gravadas);
        await expect(listarRepeticoes()).resolves.toEqual(gravadas);
        expect(fetch).toHaveBeenLastCalledWith("/api/repeticoes", expect.anything());

        responder({ id: "rep1" }, 201);
        await expect(reproduzirRepeticao("g 1")).resolves.toEqual({ id: "rep1" });
        expect(fetch).toHaveBeenLastCalledWith(
            "/api/repeticoes/g%201/reproduzir",
            expect.objectContaining({ method: "POST" }),
        );
    });
});

describe("criarPartida", () => {
    it("manda a dificuldade e devolve o id", async () => {
        responder({ id: "a1b2c3d4" }, 201);

        await expect(criarPartida("NORMAL")).resolves.toEqual({ id: "a1b2c3d4" });

        const [caminho, opcoes] = vi.mocked(fetch).mock.calls[0] as [string, RequestInit];
        expect(caminho).toBe("/api/partidas");
        expect(opcoes.method).toBe("POST");
        expect(JSON.parse(opcoes.body as string)).toEqual({
            dificuldade: "NORMAL",
            modoTreino: false,
        });
    });

    it("transforma a dificuldade inválida em ErroApi", async () => {
        responder({ erro: "DIFICULDADE_INVALIDA", mensagem: "Escolha uma dificuldade." }, 400);

        await expect(criarPartida("NORMAL")).rejects.toMatchObject({
            codigo: "DIFICULDADE_INVALIDA",
            status: 400,
        });
    });
});
