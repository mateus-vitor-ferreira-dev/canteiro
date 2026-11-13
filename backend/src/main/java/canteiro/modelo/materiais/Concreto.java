package canteiro.modelo.materiais;

/**
 * Concreto: pesado, pontua bem mas exige cuidado com o equilíbrio.
 *
 * <p>Densidade de 2,4 t/m³, a do concreto armado. Liberado a partir do nível 3.</p>
 *
 * @author Mateus Vitor Ferreira
 * @version 0.1.0
 */
public final class Concreto extends Material {

    /** Código usado no catálogo e no protocolo. */
    public static final String CODIGO = "CONCRETO";

    private static final double DENSIDADE = 2.4;
    private static final int COR = 0x9EA7B1;
    private static final double CARGA_MAXIMA = 40.0;
    private static final int BONUS_LINHA = 30;

    /**
     * Cria o material com os valores padrão do jogo.
     */
    public Concreto() {
        this(DENSIDADE, COR);
    }

    /**
     * Cria o material com densidade e cor lidas de arquivo (#24), no lugar das padrão.
     *
     * @param densidade densidade do material, positiva
     * @param corRgb    cor no formato {@code 0xRRGGBB}
     * @throws IllegalArgumentException se a densidade não for positiva ou a cor sair do intervalo RGB
     */
    public Concreto(double densidade, int corRgb) {
        super(CODIGO, "Concreto", densidade, corRgb);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int bonusLinha() {
        return BONUS_LINHA;
    }

    /**
     * O concreto só cede sob seis blocos de aço ou mais.
     *
     * @return a carga máxima (RN21)
     */
    @Override
    public double cargaMaxima() {
        return CARGA_MAXIMA;
    }
}
