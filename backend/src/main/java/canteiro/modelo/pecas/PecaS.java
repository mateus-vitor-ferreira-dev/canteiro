package canteiro.modelo.pecas;

import canteiro.modelo.materiais.Material;

/**
 * Peça S: dois pares em degrau, subindo para a direita.
 *
 * @author Mateus Vitor Ferreira
 * @version 0.1.0
 */
public final class PecaS extends Peca {

    private static final int[][][] FORMAS = rotacoesDe(new int[][] {
        {0, 1, 1},
        {1, 1, 0},
        {0, 0, 0},
    });

    /**
     * Cria a peça na rotação inicial.
     *
     * @param material material de que a peça é feita
     * @throws NullPointerException se o material for nulo
     */
    public PecaS(Material material) {
        super('S', material);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected int[][][] formas() {
        return FORMAS;
    }
}
