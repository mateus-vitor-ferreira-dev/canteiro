/**
 * Como cada material aparece: cor, nome, peso e **textura**. A textura existe
 * para que a cor nunca seja a única pista (RNF06): em escala de cinza ou com
 * daltonismo, madeira tem veios, alvenaria tem tijolos, concreto é pontilhado
 * e aço tem hachura diagonal.
 */

export type InfoMaterial = {
    codigo: string;
    nome: string;
    /** Como o peso é descrito ao jogador, do mais leve ao mais pesado. */
    peso: string;
    cor: string;
};

/** Os quatro materiais, do mais leve ao mais pesado. As cores são as de `global.css`. */
export const MATERIAIS: readonly InfoMaterial[] = [
    { codigo: "MADEIRA", nome: "Madeira", peso: "leve", cor: "#e6c48f" },
    { codigo: "ALVENARIA", nome: "Alvenaria", peso: "médio", cor: "#c8704b" },
    { codigo: "CONCRETO", nome: "Concreto", peso: "pesado", cor: "#9ea7b1" },
    { codigo: "ACO", nome: "Aço", peso: "muito pesado", cor: "#4e5a6b" },
];

/** Cor usada quando chega um material desconhecido. */
export const COR_DESCONHECIDA = "#ff00ff";

/** Borda clara em todo bloco: separa os blocos entre si e faz o aço aparecer no fundo escuro. */
const COR_BORDA = "rgba(230, 235, 242, 0.85)";
const TRACO_ESCURO = "rgba(60, 38, 16, 0.55)";
const TRACO_CLARO = "rgba(255, 244, 232, 0.8)";
const PONTO_ESCURO = "rgba(27, 36, 50, 0.6)";
const HACHURA_CLARA = "rgba(230, 236, 244, 0.6)";

/** Pontos do concreto, em frações do lado: fixos, para o bloco não "tremer" a cada quadro. */
const PONTOS_CONCRETO: readonly [number, number][] = [
    [0.22, 0.25],
    [0.7, 0.2],
    [0.45, 0.5],
    [0.2, 0.75],
    [0.78, 0.68],
    [0.55, 0.85],
];

/** Informações de um material pelo código. */
export function infoDoMaterial(codigo: string): InfoMaterial | undefined {
    return MATERIAIS.find((m) => m.codigo === codigo);
}

/** Cor de desenho de um material, pelo código. */
export function corDoMaterial(codigo: string): string {
    return infoDoMaterial(codigo)?.cor ?? COR_DESCONHECIDA;
}

/**
 * Desenha um bloco num quadrado de lado `lado`, com a cor, a textura do
 * material e a borda clara. Tudo fica dentro do quadrado.
 */
export function desenharBloco(
    ctx: CanvasRenderingContext2D,
    x: number,
    y: number,
    lado: number,
    codigo: string,
): void {
    ctx.fillStyle = corDoMaterial(codigo);
    ctx.fillRect(x, y, lado, lado);
    ctx.lineWidth = Math.max(1, lado / 16);
    switch (codigo) {
        case "MADEIRA":
            desenharVeios(ctx, x, y, lado);
            break;
        case "ALVENARIA":
            desenharTijolos(ctx, x, y, lado);
            break;
        case "CONCRETO":
            desenharPontos(ctx, x, y, lado);
            break;
        case "ACO":
            desenharHachura(ctx, x, y, lado);
            break;
    }
    ctx.lineWidth = 1;
    ctx.strokeStyle = COR_BORDA;
    ctx.strokeRect(x + 0.5, y + 0.5, lado - 1, lado - 1);
}

function desenharVeios(ctx: CanvasRenderingContext2D, x: number, y: number, lado: number): void {
    ctx.strokeStyle = TRACO_ESCURO;
    for (const altura of [0.3, 0.55, 0.8]) {
        const meio = y + lado * altura;
        ctx.beginPath();
        ctx.moveTo(x + lado * 0.12, meio);
        ctx.quadraticCurveTo(x + lado * 0.5, meio - lado * 0.12, x + lado * 0.88, meio);
        ctx.stroke();
    }
}

function desenharTijolos(ctx: CanvasRenderingContext2D, x: number, y: number, lado: number): void {
    ctx.strokeStyle = TRACO_CLARO;
    ctx.beginPath();
    ctx.moveTo(x, y + lado / 2);
    ctx.lineTo(x + lado, y + lado / 2);
    ctx.moveTo(x + lado / 2, y);
    ctx.lineTo(x + lado / 2, y + lado / 2);
    ctx.moveTo(x + lado * 0.2, y + lado / 2);
    ctx.lineTo(x + lado * 0.2, y + lado);
    ctx.moveTo(x + lado * 0.8, y + lado / 2);
    ctx.lineTo(x + lado * 0.8, y + lado);
    ctx.stroke();
}

function desenharPontos(ctx: CanvasRenderingContext2D, x: number, y: number, lado: number): void {
    ctx.fillStyle = PONTO_ESCURO;
    const raio = Math.max(1, lado / 14);
    for (const [px, py] of PONTOS_CONCRETO) {
        ctx.beginPath();
        ctx.arc(x + lado * px, y + lado * py, raio, 0, 2 * Math.PI);
        ctx.fill();
    }
}

function desenharHachura(ctx: CanvasRenderingContext2D, x: number, y: number, lado: number): void {
    ctx.strokeStyle = HACHURA_CLARA;
    ctx.beginPath();
    for (const inicio of [0.25, 0.5, 0.75, 1]) {
        ctx.moveTo(x, y + lado * inicio);
        ctx.lineTo(x + lado * inicio, y);
    }
    for (const inicio of [0.25, 0.5, 0.75]) {
        ctx.moveTo(x + lado * inicio, y + lado);
        ctx.lineTo(x + lado, y + lado * inicio);
    }
    ctx.stroke();
}
