import { describe, expect, it } from "vitest";
import type { EventoDto, TipoEvento } from "@/api/protocolo";
import { receitaDoEvento } from "./receitas";

function evento(tipo: TipoEvento, dados: EventoDto["dados"] = {}): EventoDto {
    return { tipo: "EVENTO", evento: tipo, dados };
}

describe("receitaDoEvento", () => {
    it("fixação, linhas e colapso têm som (RF28)", () => {
        for (const tipo of ["PECA_FIXADA", "LINHAS_ELIMINADAS", "COLAPSO"] as const) {
            expect(receitaDoEvento(evento(tipo)).length).toBeGreaterThan(0);
        }
    });

    it("os outros eventos são silenciosos", () => {
        for (const tipo of ["NIVEL_SUBIU", "FIM_DE_JOGO", "ERRO"] as const) {
            expect(receitaDoEvento(evento(tipo))).toEqual([]);
        }
    });

    it("cada linha eliminada junto soma uma nota, subindo, até quatro", () => {
        const uma = receitaDoEvento(evento("LINHAS_ELIMINADAS", { linhas: [21] }));
        const quatro = receitaDoEvento(evento("LINHAS_ELIMINADAS", { linhas: [18, 19, 20, 21] }));
        expect(uma).toHaveLength(1);
        expect(quatro).toHaveLength(4);
        const frequencias = quatro.map((n) => n.frequencia);
        expect(frequencias).toEqual([...frequencias].sort((a, b) => a - b));
    });

    it("o colapso é grave e mais longo que a fixação", () => {
        const duracao = (tipo: TipoEvento) =>
            Math.max(...receitaDoEvento(evento(tipo)).map((n) => n.inicio + n.duracao));
        expect(duracao("COLAPSO")).toBeGreaterThan(duracao("PECA_FIXADA"));
    });

    it("toda nota tem volume entre 0 e 1 e duração positiva", () => {
        for (const tipo of ["PECA_FIXADA", "LINHAS_ELIMINADAS", "COLAPSO"] as const) {
            for (const nota of receitaDoEvento(evento(tipo, { linhas: [1, 2, 3, 4] }))) {
                expect(nota.ganho).toBeGreaterThan(0);
                expect(nota.ganho).toBeLessThanOrEqual(1);
                expect(nota.duracao).toBeGreaterThan(0);
            }
        }
    });
});
