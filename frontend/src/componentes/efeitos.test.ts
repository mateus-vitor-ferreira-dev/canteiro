import { describe, expect, it } from "vitest";
import type { EventoDto, TipoEvento } from "@/api/protocolo";
import { balancoDoCentro, desenharClaraoLinhas, destaqueDoEvento } from "./efeitos";

function evento(tipo: TipoEvento, dados: EventoDto["dados"] = {}): EventoDto {
    return { tipo: "EVENTO", evento: tipo, dados };
}

describe("desenharClaraoLinhas", () => {
    it("acende as linhas visíveis e apaga com o tempo", () => {
        const faixas: string[] = [];
        const falso = {
            fillStyle: "",
            fillRect(x: number, y: number, l: number, a: number) {
                faixas.push(`${falso.fillStyle}@${x},${y},${l},${a}`);
            },
        };
        const ctx = falso as unknown as CanvasRenderingContext2D;

        desenharClaraoLinhas(ctx, [1, 21], 0, 10);
        desenharClaraoLinhas(ctx, [21], 1, 10);

        expect(faixas).toEqual([
            "rgba(255, 236, 179, 0.85)@0,190,100,10",
            "rgba(255, 236, 179, 0)@0,190,100,10",
        ]);
    });
});

describe("balancoDoCentro", () => {
    it("parado com a estrutura firme; balança mais quanto mais perto de cair", () => {
        expect(balancoDoCentro(0.5, 1234, 24)).toBe(0);
        const perto = Math.abs(balancoDoCentro(0.2, 70, 24));
        const quase = Math.abs(balancoDoCentro(0.01, 70, 24));
        expect(perto).toBeGreaterThan(0);
        expect(quase).toBeGreaterThan(perto);
        expect(quase).toBeLessThanOrEqual(24 * 0.12);
    });
});

describe("destaqueDoEvento", () => {
    it("mostra o ganho das linhas e avisa a jogada ousada", () => {
        expect(destaqueDoEvento(evento("LINHAS_ELIMINADAS", { pontos: 1320 }))).toEqual({
            texto: "+1.320",
            tipo: "ganho",
        });
        expect(
            destaqueDoEvento(evento("LINHAS_ELIMINADAS", { pontos: 165, ousada: true }))?.texto,
        ).toBe("Ousada! +165");
    });

    it("mostra o desconto do colapso, o bônus da meta e o nível novo", () => {
        expect(destaqueDoEvento(evento("COLAPSO", { pontos: -600 }))).toEqual({
            texto: "−600",
            tipo: "perda",
        });
        expect(destaqueDoEvento(evento("COLAPSO"))).toBeNull();
        expect(destaqueDoEvento(evento("META_CUMPRIDA", { pontos: 1500 }))?.texto).toBe(
            "Meta! +1.500",
        );
        expect(destaqueDoEvento(evento("NIVEL_SUBIU"), 3)?.texto).toBe("Nível 4");
        expect(destaqueDoEvento(evento("PECA_FIXADA"))).toBeNull();
    });
});
