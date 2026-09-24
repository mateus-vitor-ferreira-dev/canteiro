/** Funções puras de apresentação: sem Svelte e sem rede, para serem fáceis de testar. */

const DECIMAL = new Intl.NumberFormat("pt-BR", {
    minimumFractionDigits: 1,
    maximumFractionDigits: 1,
});

const SEGUNDOS = new Intl.NumberFormat("pt-BR", {
    minimumFractionDigits: 1,
    maximumFractionDigits: 2,
});

const MS_POR_SEGUNDO = 1000;

/** Formata um desvio em colunas, como `"2,5 colunas"` ou `"1,7 coluna"`. */
export function formatarColunas(valor: number): string {
    return `${DECIMAL.format(valor)} ${valor >= 2 ? "colunas" : "coluna"}`;
}

/** Formata um intervalo em milissegundos como segundos, como `"0,8 s"` ou `"0,65 s"`. */
export function formatarSegundos(milissegundos: number): string {
    return `${SEGUNDOS.format(milissegundos / MS_POR_SEGUNDO)} s`;
}

/**
 * Converte a cor de um material, que o backend guarda como número RGB, em cor CSS.
 *
 * @param rgb cor no formato `0xRRGGBB`
 * @returns cor no formato `#rrggbb`
 */
export function corCss(rgb: number): string {
    return `#${(rgb & 0xffffff).toString(16).padStart(6, "0")}`;
}
