package canteiro.modelo.pecas;

import canteiro.modelo.materiais.Material;

/**
 * Peça Z: dois pares em degrau, descendo para a direita. É o espelho da S.
 *
 * @author Mateus Vitor Ferreira
 * @version 0.1.0
 */
public final class PecaZ extends Peca {

    private static final int[][][] FORMAS = rotacoesDe(new int[][] {
        {1, 1, 0},
        {0, 1, 1},
        {0, 0, 0},
    });

    /**
     * Cria a peça na rotação inicial.
     *
     * @param material material de que a peça é feita
     * @throws NullPointerException se o material for nulo
     */
    public PecaZ(Material material) {
        super('Z', material);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected int[][][] formas() {
        return FORMAS;
    }
}
