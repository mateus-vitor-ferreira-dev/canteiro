package canteiro.modelo.pecas;

import canteiro.modelo.materiais.Material;

/**
 * Peça J: três blocos em linha com um na ponta esquerda, por cima.
 *
 * @author Mateus Vitor Ferreira
 * @version 0.1.0
 */
public final class PecaJ extends Peca {

    private static final int[][][] FORMAS = rotacoesDe(new int[][] {
        {1, 0, 0},
        {1, 1, 1},
        {0, 0, 0},
    });

    /**
     * Cria a peça na rotação inicial.
     *
     * @param material material de que a peça é feita
     * @throws NullPointerException se o material for nulo
     */
    public PecaJ(Material material) {
        super('J', material);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected int[][][] formas() {
        return FORMAS;
    }
}
