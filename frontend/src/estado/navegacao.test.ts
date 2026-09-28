import { describe, expect, it, vi } from "vitest";
import { Navegacao } from "./navegacao.svelte";

function historicoFalso() {
    return { pushState: vi.fn(), replaceState: vi.fn() };
}

describe("Navegacao", () => {
    it("começa no menu, marcado no histórico", () => {
        const historico = historicoFalso();
        expect(new Navegacao(historico).tela).toEqual({ nome: "menu" });
        expect(historico.replaceState).toHaveBeenCalledWith({ tela: { nome: "menu" } }, "");
    });

    it("ir troca a tela e a põe no histórico", () => {
        const historico = historicoFalso();
        const navegacao = new Navegacao(historico);
        navegacao.ir({ nome: "partida", id: "abc" });
        expect(navegacao.tela).toEqual({ nome: "partida", id: "abc" });
        expect(historico.pushState).toHaveBeenCalledWith(
            { tela: { nome: "partida", id: "abc" } },
            "",
        );
    });

    it("o que vai para o histórico pode ser copiado pelo navegador", () => {
        const historico = historicoFalso();
        const navegacao = new Navegacao(historico);
        navegacao.ir({ nome: "partida", id: "abc" });
        for (const chamada of [
            ...historico.replaceState.mock.calls,
            ...historico.pushState.mock.calls,
        ]) {
            expect(() => structuredClone(chamada[0])).not.toThrow();
        }
    });

    it("voltarAoMenu volta de qualquer tela", () => {
        const navegacao = new Navegacao(historicoFalso());
        navegacao.ir({ nome: "ranking" });
        navegacao.voltarAoMenu();
        expect(navegacao.tela).toEqual({ nome: "menu" });
    });

    it("o voltar do navegador restaura a tela guardada", () => {
        const navegacao = new Navegacao(historicoFalso());
        navegacao.ir({ nome: "ranking" });
        navegacao.restaurar({ tela: { nome: "nova-partida" } });
        expect(navegacao.tela).toEqual({ nome: "nova-partida" });
    });

    it("histórico sem tela volta ao menu", () => {
        const navegacao = new Navegacao(historicoFalso());
        navegacao.ir({ nome: "saida" });
        navegacao.restaurar(null);
        expect(navegacao.tela).toEqual({ nome: "menu" });
    });
});
