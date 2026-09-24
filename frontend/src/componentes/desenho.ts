import type { EstadoDto, Posicao, QuedaDto } from "@/api/protocolo";
import { desenharBloco } from "@/estilos/materiais";

/** Linhas ocultas no topo da grade: o backend manda, mas a tela não mostra (RN01). */
export const LINHAS_OCULTAS = 2;
export const LINHAS_VISIVEIS = 20;
export const COLUNAS = 10;

/** Menor e maior lado de um quadrado do tabuleiro, em pixels. */
export const CELULA_MINIMA = 14;
export const CELULA_MAXIMA = 32;

/** Quanto dura a animação do colapso, em milissegundos ("aproximadamente um segundo"). */
export const DURACAO_COLAPSO_MS = 1000;

const COR_FUNDO = "#1b2432";
const COR_GRADE = "#263041";
const COR_FANTASMA = "rgba(255, 255, 255, 0.45)";
const COR_EIXO = "#f2b53a";
const COR_CENTRO = "#ff6b5e";
const COR_CENTRO_BORDA = "#ffffff";
const COR_CLARAO = "244, 112, 103";
const OPACIDADE_CLARAO = 0.45;
const FOLGA = 1;

/**
 * Lado de cada quadrado para o tabuleiro caber na tela: 20 linhas na altura
 * que sobra e 10 colunas na largura disponível, entre 14 e 32 pixels.
 *
 * @param larguraDisponivel largura que o tabuleiro pode ocupar, em pixels
 * @param alturaDisponivel altura que o tabuleiro pode ocupar, em pixels
 */
export function tamanhoDaCelula(larguraDisponivel: number, alturaDisponivel: number): number {
    const cabe = Math.floor(
        Math.min(alturaDisponivel / LINHAS_VISIVEIS, larguraDisponivel / COLUNAS),
    );
    return Math.min(CELULA_MAXIMA, Math.max(CELULA_MINIMA, cabe));
}

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
                pintar(ctx, [l, c], material, celula);
            }
        }),
    );
    for (const posicao of estado.pecaFantasma) {
        contornar(ctx, posicao, celula);
    }
    if (estado.pecaAtual) {
        const material = estado.pecaAtual.material;
        estado.pecaAtual.blocos.forEach((posicao) => pintar(ctx, posicao, material, celula));
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
    material: string,
    celula: number,
): void {
    const ponto = naTela(posicao, celula);
    if (ponto) {
        desenharBloco(ctx, ponto.x + FOLGA, ponto.y + FOLGA, celula - 2 * FOLGA, material);
    }
}

function contornar(ctx: CanvasRenderingContext2D, posicao: Posicao, celula: number): void {
    const ponto = naTela(posicao, celula);
    if (ponto) {
        ctx.lineWidth = 2;
        ctx.strokeStyle = COR_FANTASMA;
        ctx.strokeRect(ponto.x + 3, ponto.y + 3, celula - 6, celula - 6);
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

/** O eixo da base (tracejado âmbar) e o centro de massa (círculo vermelho de borda branca). */
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
    ctx.lineWidth = 2;
    ctx.setLineDash([6, 6]);
    ctx.beginPath();
    ctx.moveTo(estado.estabilidade.eixo * celula, 0);
    ctx.lineTo(estado.estabilidade.eixo * celula, altura);
    ctx.stroke();
    ctx.setLineDash([]);
    // Forma e tamanho, e não só a cor, separam o centro de massa do eixo e mostram o alerta.
    const raio = estado.estabilidade.alerta ? celula / 3 : celula / 4;
    ctx.fillStyle = COR_CENTRO;
    ctx.strokeStyle = COR_CENTRO_BORDA;
    ctx.beginPath();
    ctx.arc(estado.estabilidade.centroDeMassa * celula, altura - celula / 2, raio, 0, 2 * Math.PI);
    ctx.fill();
    ctx.stroke();
}

/**
 * Onde um bloco que está caindo aparece, num instante da animação. A queda
 * acelera como na gravidade: devagar no começo, rápida no fim.
 *
 * @param progresso de 0 (na origem) a 1 (no destino); valores fora disso são limitados
 */
export function posicaoNaQueda(
    queda: QuedaDto,
    progresso: number,
): { linha: number; coluna: number } {
    const p = Math.min(1, Math.max(0, progresso));
    const acelerado = p * p;
    const [linhaOrigem, coluna] = queda.origem;
    const [linhaDestino] = queda.destino;
    return { linha: linhaOrigem + (linhaDestino - linhaOrigem) * acelerado, coluna };
}

/**
 * Desenha o colapso por cima do tabuleiro já desenhado: cada bloco que caiu
 * sai do destino (onde o estado novo já o colocou) e aparece a caminho, da
 * origem ao destino. Um clarão vermelho vai sumindo junto.
 */
export function desenharColapso(
    ctx: CanvasRenderingContext2D,
    estado: EstadoDto,
    quedas: QuedaDto[],
    progresso: number,
    celula: number,
): void {
    for (const queda of quedas) {
        const destino = naTela(queda.destino, celula);
        if (destino) {
            ctx.fillStyle = COR_FUNDO;
            ctx.fillRect(destino.x, destino.y, celula, celula);
        }
    }
    for (const queda of quedas) {
        const material = estado.tabuleiro[queda.destino[0]]?.[queda.destino[1]];
        const { linha, coluna } = posicaoNaQueda(queda, progresso);
        if (material) {
            pintar(ctx, [linha, coluna], material, celula);
        }
    }
    const opacidade = OPACIDADE_CLARAO * (1 - Math.min(1, Math.max(0, progresso)));
    ctx.fillStyle = `rgba(${COR_CLARAO}, ${opacidade})`;
    ctx.fillRect(0, 0, COLUNAS * celula, LINHAS_VISIVEIS * celula);
}
