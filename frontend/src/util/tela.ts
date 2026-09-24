/** O jogador pediu menos movimento ao sistema: nada pisca nem anima (RNF06). */
export function prefereMenosMovimento(): boolean {
    return (
        typeof window.matchMedia === "function" &&
        window.matchMedia("(prefers-reduced-motion: reduce)").matches
    );
}

/**
 * Quantos pixels da tela cabem em um pixel do CSS. Em telas de alta densidade
 * é 2 ou 3; o Canvas precisa disso para não ficar borrado.
 */
export function densidadeDaTela(): number {
    return Math.max(1, Math.min(3, window.devicePixelRatio || 1));
}
