import type { EventoDto, PreviaDto } from "@/api/protocolo";

/**
 * O texto que o leitor de tela fala quando algo acontece na partida. Peça
 * fixada não é anunciada: acontece o tempo todo e só atrapalharia.
 *
 * @param evento o evento recebido
 * @param nivel nível atual, para anunciar a subida
 * @returns o aviso, ou `null` se o evento não merece ser falado
 */
export function anuncioDoEvento(evento: EventoDto, nivel?: number): string | null {
    switch (evento.evento) {
        case "LINHAS_ELIMINADAS": {
            const n = evento.dados.linhas?.length ?? 0;
            const linhas = n === 1 ? "1 linha eliminada" : `${n} linhas eliminadas`;
            return evento.dados.ousada ? `${linhas}. Obra ousada!` : linhas;
        }
        case "COLAPSO":
            return "A estrutura desabou!";
        case "QUEBRA":
            return "A parte em balanço se soltou!";
        case "ESMAGAMENTO": {
            const n = evento.dados.celulas?.length ?? 1;
            return n === 1 ? "Um bloco foi esmagado!" : `${n} blocos foram esmagados!`;
        }
        case "META_CUMPRIDA":
            return evento.dados.pontos
                ? `Meta cumprida! Mais ${evento.dados.pontos} pontos`
                : "Meta cumprida!";
        case "NIVEL_SUBIU":
            return nivel ? `Subiu para o nível ${nivel}` : "Subiu de nível";
        case "FIM_DE_JOGO":
            return "Fim de jogo";
        case "ERRO":
            return evento.dados.mensagem ?? "Erro na partida";
        default:
            return null;
    }
}

/**
 * O aviso de que a jogada da peça atual quebraria blocos: a parte em balanço
 * que se soltaria (RN20) e os blocos que seriam esmagados (RN21).
 *
 * @param previa a prévia da jogada
 * @returns o aviso, ou `null` se a jogada não quebra nada
 */
export function avisoDaPrevia(previa: PreviaDto): string | null {
    const partes: string[] = [];
    if (previa.quebras.length) {
        partes.push(`solta ${blocos(previa.quebras.length)}`);
    }
    if (previa.esmagados.length) {
        partes.push(`esmaga ${blocos(previa.esmagados.length)}`);
    }
    return partes.length ? `Esta jogada ${partes.join(" e ")}!` : null;
}

function blocos(n: number): string {
    return n === 1 ? "1 bloco" : `${n} blocos`;
}
