package canteiro.modelo.pecas;

import canteiro.modelo.materiais.Material;

import java.util.function.Function;

/**
 * As sete formas de peça (RN02), cada uma sabendo criar a sua peça.
 *
 * <p>É o que o gerador de peças embaralha no método da sacola: sortear uma
 * forma e depois criar a peça com o material sorteado.</p>
 *
 * @author Mateus Vitor Ferreira
 * @version 0.1.0
 */
public enum Forma {

    /** Quatro em linha. */
    I(PecaI::new),
    /** Quadrado. */
    O(PecaO::new),
    /** T. */
    T(PecaT::new),
    /** Degrau subindo. */
    S(PecaS::new),
    /** Degrau descendo. */
    Z(PecaZ::new),
    /** L invertido. */
    J(PecaJ::new),
    /** L. */
    L(PecaL::new);

    private final Function<Material, Peca> fabrica;

    Forma(Function<Material, Peca> fabrica) {
        this.fabrica = fabrica;
    }

    /**
     * Cria uma peça desta forma, na rotação inicial.
     *
     * @param material material de que a peça é feita
     * @return a peça nova
     * @throws NullPointerException se o material for nulo
     */
    public Peca criar(Material material) {
        return fabrica.apply(material);
    }
}
