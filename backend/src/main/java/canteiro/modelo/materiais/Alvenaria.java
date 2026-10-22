package canteiro.modelo.materiais;

/**
 * Alvenaria: peso médio, o material do dia a dia da obra.
 *
 * <p>Densidade de 1,8 t/m³, a de uma parede de tijolos.</p>
 *
 * @author Mateus Vitor Ferreira
 * @version 0.1.0
 */
public final class Alvenaria extends Material {

    /** Código usado no catálogo e no protocolo. */
    public static final String CODIGO = "ALVENARIA";

    private static final double DENSIDADE = 1.8;
    private static final int COR = 0xC8704B;
    private static final int BONUS_LINHA = 20;

    /**
     * Cria o material com os valores padrão do jogo.
     */
    public Alvenaria() {
        this(DENSIDADE, COR);
    }

    /**
     * Cria o material com densidade e cor lidas de arquivo (#24), no lugar das padrão.
     *
     * @param densidade densidade do material, positiva
     * @param corRgb    cor no formato {@code 0xRRGGBB}
     * @throws IllegalArgumentException se a densidade não for positiva ou a cor sair do intervalo RGB
     */
    public Alvenaria(double densidade, int corRgb) {
        super(CODIGO, "Alvenaria", densidade, corRgb);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int bonusLinha() {
        return BONUS_LINHA;
    }
}
