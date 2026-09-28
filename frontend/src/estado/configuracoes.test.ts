import { describe, expect, it, vi } from "vitest";
import { ErroApi } from "@/api/cliente";
import { Configuracoes, VOLUME_PADRAO, validarConfiguracoes } from "./configuracoes.svelte";

function armazenamento(inicial: Record<string, string> = {}) {
    const dados = { ...inicial };
    return {
        dados,
        getItem: (chave: string) => dados[chave] ?? null,
        setItem: (chave: string, valor: string) => {
            dados[chave] = valor;
        },
    };
}

const SEM_ROTA = new ErroApi("ERRO_DESCONHECIDO", "erro 404", 404);

describe("Configuracoes", () => {
    it("começa com as teclas e o volume padrão", () => {
        const config = new Configuracoes({ ler: vi.fn(), salvar: vi.fn() }, armazenamento());
        expect(config.volume).toBe(VOLUME_PADRAO);
        expect(config.tabela.find((t) => t.comando === "ESQUERDA")?.codigos).toEqual(["ArrowLeft"]);
    });

    it("carrega do servidor e já troca a tabela em uso", async () => {
        const ler = vi.fn().mockResolvedValue({ teclas: { ESQUERDA: ["KeyA"] }, volume: 30 });
        const config = new Configuracoes({ ler, salvar: vi.fn() }, armazenamento());
        await config.carregar();
        expect(config.volume).toBe(30);
        expect(config.tabela.find((t) => t.comando === "ESQUERDA")?.rotulo).toBe("A");
    });

    it("sem servidor, carrega do navegador", async () => {
        const guardado = armazenamento({
            "canteiro.configuracoes": JSON.stringify({ teclas: { SOM: ["KeyN"] }, volume: 80 }),
        });
        const config = new Configuracoes(
            { ler: vi.fn().mockRejectedValue(SEM_ROTA), salvar: vi.fn() },
            guardado,
        );
        await config.carregar();
        expect(config.volume).toBe(80);
        expect(config.teclas).toEqual({ SOM: ["KeyN"] });
    });

    it("arquivo estragado no navegador não impede o jogo: fica o padrão (RNF11)", async () => {
        const guardado = armazenamento({ "canteiro.configuracoes": "{isto não é json" });
        const config = new Configuracoes(
            { ler: vi.fn().mockRejectedValue(SEM_ROTA), salvar: vi.fn() },
            guardado,
        );
        await config.carregar();
        expect(config.volume).toBe(VOLUME_PADRAO);
        expect(config.teclas).toEqual({});
    });

    it("salva no servidor quando ele tem a rota", async () => {
        const salvar = vi.fn().mockResolvedValue(undefined);
        const guardado = armazenamento();
        const config = new Configuracoes({ ler: vi.fn(), salvar }, guardado);
        expect(await config.salvar({ DIREITA: ["KeyD"] }, 40)).toBe("servidor");
        expect(salvar).toHaveBeenCalledWith({ teclas: { DIREITA: ["KeyD"] }, volume: 40 });
        expect(guardado.dados).toEqual({});
        expect(config.volume).toBe(40);
    });

    it("sem a rota no servidor, salva no navegador", async () => {
        const guardado = armazenamento();
        const config = new Configuracoes(
            { ler: vi.fn(), salvar: vi.fn().mockRejectedValue(SEM_ROTA) },
            guardado,
        );
        expect(await config.salvar({ DIREITA: ["KeyD"] }, 40)).toBe("navegador");
        expect(JSON.parse(guardado.dados["canteiro.configuracoes"] ?? "")).toEqual({
            teclas: { DIREITA: ["KeyD"] },
            volume: 40,
        });
    });

    it("recusa do servidor chega a quem salvou, sem mudar nada", async () => {
        const recusa = new ErroApi("CONFIGURACAO_INVALIDA", "Tecla repetida.", 400);
        const config = new Configuracoes(
            { ler: vi.fn(), salvar: vi.fn().mockRejectedValue(recusa) },
            armazenamento(),
        );
        await expect(config.salvar({ DIREITA: ["KeyD"] }, 40)).rejects.toBe(recusa);
        expect(config.volume).toBe(VOLUME_PADRAO);
    });
});

describe("validarConfiguracoes", () => {
    it("descarta ação desconhecida, tecla que não é texto e lista vazia", () => {
        expect(
            validarConfiguracoes({
                teclas: { ESQUERDA: ["KeyA", 7], VOAR: ["KeyV"], DIREITA: [] },
                volume: 50,
            }),
        ).toEqual({ teclas: { ESQUERDA: ["KeyA"] }, volume: 50 });
    });

    it("põe o volume entre 0 e 100, inteiro, e usa o padrão se ele não for número", () => {
        expect(validarConfiguracoes({ teclas: {}, volume: 150 })?.volume).toBe(100);
        expect(validarConfiguracoes({ teclas: {}, volume: -3 })?.volume).toBe(0);
        expect(validarConfiguracoes({ teclas: {}, volume: 42.6 })?.volume).toBe(43);
        expect(validarConfiguracoes({ teclas: {}, volume: "alto" })?.volume).toBe(VOLUME_PADRAO);
    });

    it("o que nem é objeto é recusado", () => {
        expect(validarConfiguracoes(null)).toBeNull();
        expect(validarConfiguracoes("teclas")).toBeNull();
    });
});
