package canteiro.modelo.pecas;

import canteiro.modelo.materiais.Material;

/**
 * Peça I: quatro blocos em linha. Gira dentro de um quadrado 4 × 4.
 *
 * @author Mateus Vitor Ferreira
 * @version 0.1.0
 */
public final class PecaI extends Peca {

    private static final int[][][] FORMAS = rotacoesDe(new int[][] {
        {0, 0, 0, 0},
        {1, 1, 1, 1},
        {0, 0, 0, 0},
        {0, 0, 0, 0},
    });

    /**
     * Cria a peça na rotação inicial.
     *
     * @param material material de que a peça é feita
     * @throws NullPointerException se o material for nulo
     */
    public PecaI(Material material) {
        super('I', material);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected int[][][] formas() {
        return FORMAS;
    }
}
