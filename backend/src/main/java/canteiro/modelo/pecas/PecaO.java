package canteiro.modelo.pecas;

import canteiro.modelo.materiais.Material;

/**
 * Peça O: um quadrado 2 × 2. As quatro rotações são iguais.
 *
 * @author Mateus Vitor Ferreira
 * @version 0.1.0
 */
public final class PecaO extends Peca {

    private static final int[][][] FORMAS = rotacoesDe(new int[][] {
        {1, 1},
        {1, 1},
    });

    /**
     * Cria a peça na rotação inicial.
     *
     * @param material material de que a peça é feita
     * @throws NullPointerException se o material for nulo
     */
    public PecaO(Material material) {
        super('O', material);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected int[][][] formas() {
        return FORMAS;
    }
}
