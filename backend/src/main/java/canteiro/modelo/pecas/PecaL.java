package canteiro.modelo.pecas;

import canteiro.modelo.materiais.Material;

/**
 * Peça L: três blocos em linha com um na ponta direita, por cima. É o espelho da J.
 *
 * @author Mateus Vitor Ferreira
 * @version 0.1.0
 */
public final class PecaL extends Peca {

    private static final int[][][] FORMAS = rotacoesDe(new int[][] {
        {0, 0, 1},
        {1, 1, 1},
        {0, 0, 0},
    });

    /**
     * Cria a peça na rotação inicial.
     *
     * @param material material de que a peça é feita
     * @throws NullPointerException se o material for nulo
     */
    public PecaL(Material material) {
        super('L', material);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected int[][][] formas() {
        return FORMAS;
    }
}
