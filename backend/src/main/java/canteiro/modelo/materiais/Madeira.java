package canteiro.modelo.materiais;

/**
 * Madeira: o material mais leve, fácil de equilibrar e o que menos pontua.
 *
 * <p>Densidade de 0,6 t/m³, a de uma madeira de construção comum.</p>
 *
 * @author Mateus Vitor Ferreira
 * @version 0.1.0
 */
public final class Madeira extends Material {

    /** Código usado no catálogo e no protocolo. */
    public static final String CODIGO = "MADEIRA";

    private static final double DENSIDADE = 0.6;
    private static final int COR = 0xE6C48F;
    private static final int BONUS_LINHA = 10;

    /**
     * Cria o material com os valores padrão do jogo.
     */
    public Madeira() {
        super(CODIGO, "Madeira", DENSIDADE, COR);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int bonusLinha() {
        return BONUS_LINHA;
    }
}
