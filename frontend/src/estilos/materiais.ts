/** Cor de cada material no Canvas: as mesmas dos tokens `--cor-*` de `global.css`. */
export const CORES_MATERIAIS: Readonly<Record<string, string>> = {
    MADEIRA: "#e6c48f",
    ALVENARIA: "#c8704b",
    CONCRETO: "#9ea7b1",
    ACO: "#4e5a6b",
};

/** Cor usada quando chega um material desconhecido. */
export const COR_DESCONHECIDA = "#ff00ff";

/** Cor de desenho de um material, pelo código. */
export function corDoMaterial(codigo: string): string {
    return CORES_MATERIAIS[codigo] ?? COR_DESCONHECIDA;
}
