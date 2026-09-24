package canteiro.modelo.constantes;

/**
 * Dimensões do tabuleiro (RN01).
 *
 * <p>As linhas são contadas de cima para baixo. As {@link #LINHAS_OCULTAS}
 * primeiras ficam acima da área visível: é onde as peças nascem.</p>
 *
 * @author Mateus Vitor Ferreira
 * @version 0.1.0
 */
public final class Dimensoes {

    /** Colunas do tabuleiro. */
    public static final int COLUNAS = 10;

    /** Linhas que o jogador vê. */
    public static final int LINHAS_VISIVEIS = 20;

    /** Linhas ocultas no topo, onde as peças nascem. */
    public static final int LINHAS_OCULTAS = 2;

    /** Total de linhas da grade: as ocultas mais as visíveis. */
    public static final int LINHAS = LINHAS_OCULTAS + LINHAS_VISIVEIS;

    /** Altura da pilha, em linhas, acima da qual um colapso encerra a partida (RN13). */
    public static final int ALTURA_LIMITE_COLAPSO = 18;

    private Dimensoes() {
    }
}
