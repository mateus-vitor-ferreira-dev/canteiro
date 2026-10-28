import type { EstadoDto, EventoDto, Posicao, QuedaDto } from "@/api/protocolo";
import { DURACAO_COLAPSO_MS, DURACAO_QUEBRA_MS, desenharColapso, desenharQuedas } from "./desenho";
import {
    DURACAO_CLARAO_LINHAS_MS,
    DURACAO_ESTOURO_MS,
    desenharClaraoLinhas,
    desenharEstouro,
} from "./efeitos";

type Animacao = { inicio: number; duracao: number } & (
    | { tipo: "linhas"; linhas: number[] }
    | { tipo: "colapso"; quedas: QuedaDto[] }
    | { tipo: "quebra"; quedas: QuedaDto[] }
    | { tipo: "estouro"; celulas: Posicao[] }
);

/**
 * As animações em curso sobre o tabuleiro: o clarão das linhas eliminadas, a
 * queda do colapso (RF17), o balanço que se solta (RN20) e o estouro dos
 * blocos esmagados (RN21). Cada evento começa a sua; várias rodam juntas, e
 * cada uma some sozinha quando acaba.
 */
export class Animacoes {
    private ativas: Animacao[] = [];

    /**
     * Começa a animação do evento, se ele tiver uma.
     *
     * @param agora relógio da animação, em milissegundos
     */
    receber(evento: EventoDto, agora: number): void {
        const { linhas, quedas, celulas } = evento.dados;
        if (evento.evento === "LINHAS_ELIMINADAS" && linhas?.length) {
            this.ativas.push({
                tipo: "linhas",
                linhas,
                inicio: agora,
                duracao: DURACAO_CLARAO_LINHAS_MS,
            });
        } else if (evento.evento === "COLAPSO" && quedas?.length) {
            this.ativas.push({
                tipo: "colapso",
                quedas,
                inicio: agora,
                duracao: DURACAO_COLAPSO_MS,
            });
        } else if (evento.evento === "QUEBRA" && quedas?.length) {
            this.ativas.push({ tipo: "quebra", quedas, inicio: agora, duracao: DURACAO_QUEBRA_MS });
        } else if (evento.evento === "ESMAGAMENTO" && celulas?.length) {
            this.ativas.push({
                tipo: "estouro",
                celulas,
                inicio: agora,
                duracao: DURACAO_ESTOURO_MS,
            });
        }
    }

    /** Quantas animações ainda estão rodando. */
    get emCurso(): number {
        return this.ativas.length;
    }

    /**
     * Desenha o quadro atual de cada animação por cima do tabuleiro já
     * desenhado e descarta as que terminaram.
     *
     * @param agora relógio da animação, em milissegundos
     */
    desenhar(
        ctx: CanvasRenderingContext2D,
        estado: EstadoDto | null,
        agora: number,
        celula: number,
    ): void {
        this.ativas = this.ativas.filter((animacao) => agora - animacao.inicio < animacao.duracao);
        for (const animacao of this.ativas) {
            const progresso = (agora - animacao.inicio) / animacao.duracao;
            if (animacao.tipo === "linhas") {
                desenharClaraoLinhas(ctx, animacao.linhas, progresso, celula);
            } else if (animacao.tipo === "estouro") {
                desenharEstouro(ctx, animacao.celulas, progresso, celula);
            } else if (estado && animacao.tipo === "colapso") {
                desenharColapso(ctx, estado, animacao.quedas, progresso, celula);
            } else if (estado) {
                desenharQuedas(ctx, estado, animacao.quedas, progresso, celula);
            }
        }
    }
}
