import { describe, expect, it } from "vitest";
import type { EventoDto, TipoEvento } from "@/api/protocolo";
import { anuncioDoEvento } from "./anuncios";

function evento(tipo: TipoEvento, dados: EventoDto["dados"] = {}): EventoDto {
    return { tipo: "EVENTO", evento: tipo, dados };
}

describe("anuncioDoEvento", () => {
    it("conta as linhas eliminadas, no singular e no plural", () => {
        expect(anuncioDoEvento(evento("LINHAS_ELIMINADAS", { linhas: [20] }))).toBe(
            "1 linha eliminada",
        );
        expect(anuncioDoEvento(evento("LINHAS_ELIMINADAS", { linhas: [19, 20, 21] }))).toBe(
            "3 linhas eliminadas",
        );
    });

    it("avisa o colapso, a subida de nível e o fim de jogo", () => {
        expect(anuncioDoEvento(evento("COLAPSO"))).toBe("A estrutura desabou!");
        expect(anuncioDoEvento(evento("NIVEL_SUBIU"), 4)).toBe("Subiu para o nível 4");
        expect(anuncioDoEvento(evento("FIM_DE_JOGO"))).toBe("Fim de jogo");
    });

    it("repete a mensagem de erro do servidor", () => {
        expect(anuncioDoEvento(evento("ERRO", { mensagem: "comando inválido" }))).toBe(
            "comando inválido",
        );
    });

    it("não anuncia peça fixada", () => {
        expect(anuncioDoEvento(evento("PECA_FIXADA"))).toBeNull();
    });
});
