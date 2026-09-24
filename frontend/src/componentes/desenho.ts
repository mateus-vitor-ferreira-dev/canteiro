import type { EstadoDto, Posicao } from "@/api/protocolo";
import { corDoMaterial } from "@/estilos/materiais";

/** Linhas ocultas no topo da grade: o backend manda, mas a tela não mostra (RN01). */
export const LINHAS_OCULTAS = 2;
export const LINHAS_VISIVEIS = 20;
export const COLUNAS = 10;

const COR_FUNDO = "#1b2432";
const COR_GRADE = "#263041";
const COR_FANTASMA = "rgba(255, 255, 255, 0.35)";
const COR_EIXO = "#56d364";
const COR_CENTRO = "#f47067";
const COR_CENTRO_ALERTA = "#ff3b30";
const FOLGA = 1;

/** Desenha o tabuleiro inteiro num contexto 2D. `celula` é o lado de cada quadrado, em pixels. */
export function desenharTabuleiro(
    ctx: CanvasRenderingContext2D,
    estado: EstadoDto | null,
    celula: number,
): void {
    ctx.fillStyle = COR_FUNDO;
    ctx.fillRect(0, 0, COLUNAS * celula, LINHAS_VISIVEIS * celula);
    desenharGrade(ctx, celula);
    if (!estado) {
        return;
    }
    estado.tabuleiro.forEach((linha, l) =>
        linha.forEach((material, c) => {
            if (material) {
                pintar(ctx, [l, c], corDoMaterial(material), celula);
            }
        }),
    );
    for (const posicao of estado.pecaFantasma) {
        contornar(ctx, posicao, celula);
    }
    if (estado.pecaAtual) {
        const cor = corDoMaterial(estado.pecaAtual.material);
        estado.pecaAtual.blocos.forEach((posicao) => pintar(ctx, posicao, cor, celula));
    }
    desenharEquilibrio(ctx, estado, celula);
}

/** Posição na tela de uma célula da grade, ou `null` se ela estiver nas linhas ocultas. */
export function naTela([linha, coluna]: Posicao, celula: number): { x: number; y: number } | null {
    const visivel = linha - LINHAS_OCULTAS;
    return visivel < 0 ? null : { x: coluna * celula, y: visivel * celula };
}

function pintar(
    ctx: CanvasRenderingContext2D,
    posicao: Posicao,
    cor: string,
    celula: number,
): void {
    const ponto = naTela(posicao, celula);
    if (ponto) {
        ctx.fillStyle = cor;
        ctx.fillRect(ponto.x + FOLGA, ponto.y + FOLGA, celula - 2 * FOLGA, celula - 2 * FOLGA);
    }
}

function contornar(ctx: CanvasRenderingContext2D, posicao: Posicao, celula: number): void {
    const ponto = naTela(posicao, celula);
    if (ponto) {
        ctx.strokeStyle = COR_FANTASMA;
        ctx.strokeRect(ponto.x + 2, ponto.y + 2, celula - 4, celula - 4);
    }
}

function desenharGrade(ctx: CanvasRenderingContext2D, celula: number): void {
    ctx.strokeStyle = COR_GRADE;
    ctx.lineWidth = 1;
    for (let c = 1; c < COLUNAS; c++) {
        ctx.beginPath();
        ctx.moveTo(c * celula, 0);
        ctx.lineTo(c * celula, LINHAS_VISIVEIS * celula);
        ctx.stroke();
    }
}

/** O eixo da base (tracejado verde) e o centro de massa (ponto vermelho), para o jogador ver o equilíbrio. */
function desenharEquilibrio(
    ctx: CanvasRenderingContext2D,
    estado: EstadoDto,
    celula: number,
): void {
    const vazio = estado.tabuleiro.every((linha) => linha.every((m) => m === null));
    if (vazio) {
        return;
    }
    const altura = LINHAS_VISIVEIS * celula;
    ctx.strokeStyle = COR_EIXO;
    ctx.setLineDash([6, 6]);
    ctx.beginPath();
    ctx.moveTo(estado.estabilidade.eixo * celula, 0);
    ctx.lineTo(estado.estabilidade.eixo * celula, altura);
    ctx.stroke();
    ctx.setLineDash([]);
    ctx.fillStyle = estado.estabilidade.alerta ? COR_CENTRO_ALERTA : COR_CENTRO;
    ctx.beginPath();
    ctx.arc(
        estado.estabilidade.centroDeMassa * celula,
        altura - celula / 2,
        celula / 4,
        0,
        2 * Math.PI,
    );
    ctx.fill();
}
