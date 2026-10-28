import { describe, expect, it } from "vitest";
import type { EventoDto, QuedaDto, TipoEvento } from "@/api/protocolo";
import { estadoDeTeste } from "@/testes-apoio";
import { Animacoes } from "./animacoes";
import { DURACAO_COLAPSO_MS, DURACAO_QUEBRA_MS } from "./desenho";

function evento(tipo: TipoEvento, dados: EventoDto["dados"] = {}): EventoDto {
    return { tipo: "EVENTO", evento: tipo, dados };
}

/** Contexto 2D que aceita qualquer desenho e não faz nada. */
const ctxMudo = new Proxy({}, { get: () => () => {} }) as unknown as CanvasRenderingContext2D;

describe("Animacoes", () => {
    it("o colapso, a quebra, o esmagamento e as linhas começam a sua animação; os outros eventos, não", () => {
        const animacoes = new Animacoes();
        const queda: QuedaDto = { origem: [10, 0], destino: [21, 0] };
        animacoes.receber(evento("COLAPSO", { quedas: [{ ...queda }] }), 0);
        animacoes.receber(evento("QUEBRA", { quedas: [{ ...queda }] }), 0);
        animacoes.receber(evento("ESMAGAMENTO", { celulas: [[21, 0]] }), 0);
        animacoes.receber(evento("LINHAS_ELIMINADAS", { linhas: [21] }), 0);
        animacoes.receber(evento("PECA_FIXADA"), 0);
        animacoes.receber(evento("QUEBRA"), 0);
        expect(animacoes.emCurso).toBe(4);
    });

    it("cada animação some quando acaba", () => {
        const animacoes = new Animacoes();
        const quedas = [
            { origem: [10, 0] as [number, number], destino: [21, 0] as [number, number] },
        ];
        animacoes.receber(evento("COLAPSO", { quedas }), 0);
        animacoes.receber(evento("QUEBRA", { quedas }), 0);

        animacoes.desenhar(ctxMudo, estadoDeTeste(), DURACAO_QUEBRA_MS + 1, 10);
        expect(animacoes.emCurso).toBe(1);
        animacoes.desenhar(ctxMudo, estadoDeTeste(), DURACAO_COLAPSO_MS + 1, 10);
        expect(animacoes.emCurso).toBe(0);
    });
});
