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
const LUZ = "rgba(255, 255, 255, 0.22)";
const SOMBRA = "rgba(10, 14, 22, 0.28)";
const TRINCA_ESCURA = "rgba(14, 16, 22, 0.9)";
const TRINCA_CLARA = "rgba(255, 255, 255, 0.55)";

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
    desenharRelevo(ctx, x, y, lado);
    ctx.lineWidth = 1;
    ctx.strokeStyle = COR_BORDA;
    ctx.strokeRect(x + 0.5, y + 0.5, lado - 1, lado - 1);
}

/**
 * Luz em cima e à esquerda, sombra embaixo e à direita: o bloco ganha volume,
 * como uma peça de verdade, sem esconder a textura.
 */
function desenharRelevo(ctx: CanvasRenderingContext2D, x: number, y: number, lado: number): void {
    const faixa = Math.max(1, lado / 9);
    ctx.fillStyle = LUZ;
    ctx.fillRect(x, y, lado, faixa);
    ctx.fillRect(x, y + faixa, faixa, lado - faixa);
    ctx.fillStyle = SOMBRA;
    ctx.fillRect(x + faixa, y + lado - faixa, lado - faixa, faixa);
    ctx.fillRect(x + lado - faixa, y + faixa, faixa, lado - 2 * faixa);
}

/**
 * As trincas, em frações do lado: cada nível acrescenta uma, entrando por um
 * lado diferente. Fixas, para o bloco não "tremer" a cada quadro.
 */
const TRINCAS: readonly (readonly [number, number][])[] = [
    [
        [0.52, 0],
        [0.44, 0.2],
        [0.56, 0.38],
        [0.46, 0.58],
    ],
    [
        [0, 0.6],
        [0.18, 0.52],
        [0.34, 0.66],
        [0.5, 0.6],
        [0.46, 0.58],
    ],
    [
        [1, 0.3],
        [0.8, 0.42],
        [0.72, 0.68],
        [0.82, 1],
    ],
];

/**
 * Desenha o bloco rachando sob o peso de cima (RN21): uma trinca no nível 1,
 * duas no 2 e três, com uma lasca solta no canto, no 3. A trinca tem um filete
 * claro ao lado do traço escuro, para aparecer no aço e na madeira, e é
 * desenho, não cor: aparece também em escala de cinza (RNF06).
 *
 * @param nivel de 1 (começou a rachar) a 3 (prestes a ceder)
 */
export function desenharRachadura(
    ctx: CanvasRenderingContext2D,
    x: number,
    y: number,
    lado: number,
    nivel: number,
): void {
    const largura = Math.max(1, lado / 14);
    for (const trinca of TRINCAS.slice(0, Math.min(nivel, TRINCAS.length))) {
        tracar(ctx, x, y, lado, trinca, TRINCA_CLARA, largura + 1.5);
        tracar(ctx, x, y, lado, trinca, TRINCA_ESCURA, largura);
    }
    if (nivel >= TRINCAS.length) {
        ctx.fillStyle = TRINCA_ESCURA;
        ctx.beginPath();
        ctx.moveTo(x + lado, y + lado * 0.72);
        ctx.lineTo(x + lado, y + lado);
        ctx.lineTo(x + lado * 0.74, y + lado);
        ctx.closePath();
        ctx.fill();
    }
}

function tracar(
    ctx: CanvasRenderingContext2D,
    x: number,
    y: number,
    lado: number,
    pontos: readonly [number, number][],
    cor: string,
    largura: number,
): void {
    ctx.strokeStyle = cor;
    ctx.lineWidth = largura;
    ctx.lineJoin = "round";
    ctx.beginPath();
    pontos.forEach(([px, py], i) =>
        i === 0
            ? ctx.moveTo(x + lado * px, y + lado * py)
            : ctx.lineTo(x + lado * px, y + lado * py),
    );
    ctx.stroke();
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
