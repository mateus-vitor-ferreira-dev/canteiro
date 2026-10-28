import { describe, expect, it } from "vitest";
import type { EventoDto, PreviaDto, TipoEvento } from "@/api/protocolo";
import { anuncioDoEvento, avisoDaPrevia } from "./anuncios";

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

    it("anuncia a obra ousada", () => {
        expect(
            anuncioDoEvento(
                evento("LINHAS_ELIMINADAS", { linhas: [21], pontos: 165, ousada: true }),
            ),
        ).toBe("1 linha eliminada. Obra ousada!");
    });

    it("anuncia a meta cumprida, com o bônus", () => {
        expect(anuncioDoEvento(evento("META_CUMPRIDA", { pontos: 1000 }))).toBe(
            "Meta cumprida! Mais 1000 pontos",
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

describe("balanço e esmagamento", () => {
    it("anuncia o balanço que se soltou e os blocos esmagados", () => {
        expect(anuncioDoEvento(evento("QUEBRA"))).toBe("A parte em balanço se soltou!");
        expect(anuncioDoEvento(evento("ESMAGAMENTO", { celulas: [[21, 0]] }))).toBe(
            "Um bloco foi esmagado!",
        );
        expect(
            anuncioDoEvento(
                evento("ESMAGAMENTO", {
                    celulas: [
                        [21, 0],
                        [20, 3],
                    ],
                }),
            ),
        ).toBe("2 blocos foram esmagados!");
    });

    it("avisa o que a jogada quebraria, ou nada", () => {
        const previa: PreviaDto = {
            indice: 1,
            desvio: 0,
            centroDeMassa: 5,
            eixo: 5,
            derruba: false,
            quebras: [],
            esmagados: [],
        };
        expect(avisoDaPrevia(previa)).toBeNull();
        expect(
            avisoDaPrevia({
                ...previa,
                quebras: [
                    [20, 1],
                    [20, 2],
                ],
            }),
        ).toBe("Esta jogada solta 2 blocos!");
        expect(avisoDaPrevia({ ...previa, quebras: [[20, 1]], esmagados: [[21, 0]] })).toBe(
            "Esta jogada solta 1 bloco e esmaga 1 bloco!",
        );
    });
});
