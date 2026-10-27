import type { EventoDto } from "@/api/protocolo";
import { COLUNAS, LINHAS_OCULTAS } from "./desenho";

/** Quanto dura o clarão das linhas eliminadas, em milissegundos. */
export const DURACAO_CLARAO_LINHAS_MS = 350;

/** Abaixo deste índice a estrutura "range": o centro de massa balança e um som grave toca. */
export const LIMIAR_RANGIDO = 0.25;

const COR_CLARAO_LINHAS = "255, 236, 179";

/**
 * Pinta um clarão claro nas linhas que acabaram de sumir, apagando aos poucos.
 * As linhas vêm na numeração da grade inteira, com as duas ocultas.
 *
 * @param progresso de 0 (aceso) a 1 (apagado)
 */
export function desenharClaraoLinhas(
    ctx: CanvasRenderingContext2D,
    linhas: readonly number[],
    progresso: number,
    celula: number,
): void {
    const opacidade = 0.85 * (1 - Math.min(1, Math.max(0, progresso)));
    ctx.fillStyle = `rgba(${COR_CLARAO_LINHAS}, ${opacidade})`;
    for (const linha of linhas) {
        const visivel = linha - LINHAS_OCULTAS;
        if (visivel >= 0) {
            ctx.fillRect(0, visivel * celula, COLUNAS * celula, celula);
        }
    }
}

/**
 * Quanto o marcador do centro de massa sai do lugar, em pixels, quando a
 * estrutura está para cair. Fora do perigo, zero.
 *
 * @param indice índice de estabilidade, de 0 a 1
 * @param agora relógio da animação, em milissegundos
 */
export function balancoDoCentro(indice: number, agora: number, celula: number): number {
    if (indice >= LIMIAR_RANGIDO) {
        return 0;
    }
    const forca = 1 - indice / LIMIAR_RANGIDO;
    return Math.sin(agora / 45) * celula * 0.12 * forca;
}

/** O que aparece flutuando sobre o tabuleiro depois de um evento. */
export type Destaque = { texto: string; tipo: "ganho" | "perda" | "meta" | "nivel" };

const PONTOS = new Intl.NumberFormat("pt-BR");

/**
 * O texto flutuante de um evento: o ganho das linhas (e se foi ousada), o
 * desconto do colapso, o bônus da meta ou o nível novo.
 *
 * @param nivelAntes nível mostrado quando o evento chega: o backend manda o
 *   evento antes do estado novo, então a subida vai para o nível seguinte
 * @returns o destaque, ou `null` se o evento não mostra nada
 */
export function destaqueDoEvento(evento: EventoDto, nivelAntes?: number): Destaque | null {
    const pontos = evento.dados.pontos ?? 0;
    switch (evento.evento) {
        case "LINHAS_ELIMINADAS": {
            const ganho = `+${PONTOS.format(pontos)}`;
            return { texto: evento.dados.ousada ? `Ousada! ${ganho}` : ganho, tipo: "ganho" };
        }
        case "COLAPSO":
            return pontos < 0 ? { texto: `−${PONTOS.format(-pontos)}`, tipo: "perda" } : null;
        case "META_CUMPRIDA":
            return { texto: `Meta! +${PONTOS.format(pontos)}`, tipo: "meta" };
        case "NIVEL_SUBIU":
            return { texto: nivelAntes ? `Nível ${nivelAntes + 1}` : "Nível novo", tipo: "nivel" };
        default:
            return null;
    }
}
