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
        super(CODIGO, "Alvenaria", DENSIDADE, COR);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int bonusLinha() {
        return BONUS_LINHA;
    }
}
