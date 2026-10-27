import type { EstadoDto, Posicao, PreviaDto, QuedaDto } from "@/api/protocolo";
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
const COR_PREVIA = "rgba(255, 255, 255, 0.85)";
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

const JANELA_COMPUTADOR = 1100;
const JANELA_TABLET = 768;

/**
 * O lado da célula para a janela do navegador. Desconta o que fica ao lado do
 * tabuleiro (colunas laterais) ou em cima e embaixo dele (cabeçalho, placar e
 * botões), conforme o arranjo da tela de partida em cada largura.
 *
 * @param largura largura da janela, em pixels
 * @param altura altura da janela, em pixels
 * @param comFaixa se a faixa de treino ou de repetição aparece em cima do tabuleiro
 */
export function celulaParaJanela(largura: number, altura: number, comFaixa = false): number {
    if (largura >= JANELA_COMPUTADOR) {
        return tamanhoDaCelula(largura - 700, altura - 190);
    }
    if (largura >= JANELA_TABLET) {
        return tamanhoDaCelula(largura - 400, altura - 260);
    }
    const faixa = comFaixa ? ALTURA_FAIXA : 0;
    return tamanhoDaCelula(
        largura - LADOS_CELULAR,
        altura - ALTURA_FORA_DO_TABULEIRO_CELULAR - faixa,
    );
}

/**
 * No celular, o que fica ao lado do tabuleiro: o respiro da página, a moldura
 * e a coluna das miniaturas (reserva e próximas).
 */
const LADOS_CELULAR = 96;

/**
 * No celular, o que fica em cima e embaixo do tabuleiro: o cabeçalho
 * compacto, o resumo da partida, as duas linhas de botões e os respiros.
 */
const ALTURA_FORA_DO_TABULEIRO_CELULAR = 270;

/** A faixa que avisa que a partida é de treino ou uma repetição. */
const ALTURA_FAIXA = 34;

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

/**
 * O eixo da base (tracejado âmbar), o centro de massa (círculo vermelho de borda
 * branca) e onde ele vai parar se a peça cair na fantasma (anel vazado; vermelho
 * e tracejado se a jogada derruba a obra).
 */
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
    const previa = estado.estabilidade.previa;
    if (previa) {
        desenharPrevia(ctx, previa, altura - celula / 2, celula);
    }
}

function desenharPrevia(
    ctx: CanvasRenderingContext2D,
    previa: PreviaDto,
    y: number,
    celula: number,
): void {
    ctx.lineWidth = 2;
    ctx.strokeStyle = previa.derruba ? COR_CENTRO : COR_PREVIA;
    ctx.setLineDash(previa.derruba ? [3, 3] : []);
    ctx.beginPath();
    ctx.arc(previa.centroDeMassa * celula, y, celula / 3, 0, 2 * Math.PI);
    ctx.stroke();
    ctx.setLineDash([]);
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
