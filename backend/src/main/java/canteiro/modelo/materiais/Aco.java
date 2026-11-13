package canteiro.modelo.materiais;

import canteiro.modelo.Celula;
import canteiro.modelo.Tabuleiro;

import java.util.List;

/**
 * Aço: o material mais pesado e o que mais pontua.
 *
 * <p>Densidade de 7,85 t/m³, a do aço estrutural: mais de treze vezes a da madeira.
 * Uma peça de aço mal colocada desloca o centro de massa sozinha. Liberado a
 * partir do nível 5. Ao ser assentado, consolida os blocos logo abaixo.</p>
 *
 * @author Mateus Vitor Ferreira
 * @version 0.1.0
 */
public final class Aco extends Material {

    /** Código usado no catálogo e no protocolo. */
    public static final String CODIGO = "ACO";

    private static final double DENSIDADE = 7.85;
    private static final int COR = 0x4E5A6B;
    private static final int BONUS_LINHA = 40;

    /**
     * Cria o material com os valores padrão do jogo.
     */
    public Aco() {
        this(DENSIDADE, COR);
    }

    /**
     * Cria o material com densidade e cor lidas de arquivo (#24), no lugar das padrão.
     *
     * @param densidade densidade do material, positiva
     * @param corRgb    cor no formato {@code 0xRRGGBB}
     * @throws IllegalArgumentException se a densidade não for positiva ou a cor sair do intervalo RGB
     */
    public Aco(double densidade, int corRgb) {
        super(CODIGO, "Aço", densidade, corRgb);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int bonusLinha() {
        return BONUS_LINHA;
    }

    /**
     * O aço nunca é esmagado.
     *
     * @return a carga máxima (RN21)
     */
    @Override
    public double cargaMaxima() {
        return Double.POSITIVE_INFINITY;
    }

    /**
     * O aço é pesado: ao ser assentado, consolida os blocos logo abaixo, que
     * passam a resistir ao desprendimento no colapso.
     *
     * @param tabuleiro tabuleiro em que a peça foi fixada
     * @param ocupadas  células em que a peça acabou de ser fixada
     */
    @Override
    public void aoFixar(Tabuleiro tabuleiro, List<Celula> ocupadas) {
        tabuleiro.consolidarAbaixo(ocupadas);
    }
}
