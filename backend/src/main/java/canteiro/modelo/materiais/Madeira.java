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
    private static final double CARGA_MAXIMA = 8.0;
    private static final int BONUS_LINHA = 10;

    /**
     * Cria o material com os valores padrão do jogo.
     */
    public Madeira() {
        this(DENSIDADE, COR);
    }

    /**
     * Cria o material com densidade e cor lidas de arquivo (#24), no lugar das padrão.
     *
     * @param densidade densidade do material, positiva
     * @param corRgb    cor no formato {@code 0xRRGGBB}
     * @throws IllegalArgumentException se a densidade não for positiva ou a cor sair do intervalo RGB
     */
    public Madeira(double densidade, int corRgb) {
        super(CODIGO, "Madeira", densidade, corRgb);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int bonusLinha() {
        return BONUS_LINHA;
    }

    /**
     * Madeira aguenta pouco: cinco blocos de alvenaria já a esmagam.
     *
     * @return a carga máxima (RN21)
     */
    @Override
    public double cargaMaxima() {
        return CARGA_MAXIMA;
    }
}
