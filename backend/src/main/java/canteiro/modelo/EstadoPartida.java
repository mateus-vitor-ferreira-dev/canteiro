package canteiro.modelo;

/**
 * Estados da partida, que é uma máquina de estados finita (seção "Ciclo de
 * vida da partida" do README).
 *
 * <p>A cada ciclo, o motor olha o estado atual e executa só as transições
 * previstas para ele. {@link #FIXANDO}, {@link #ELIMINANDO_LINHAS} e
 * {@link #COLAPSO} são passagens: o motor passa por eles dentro do mesmo ciclo.</p>
 *
 * @author Mateus Vitor Ferreira
 * @version 0.1.0
 */
public enum EstadoPartida {

    /** Uma peça nova vai entrar no tabuleiro. */
    GERANDO_PECA,
    /** Há uma peça caindo, e o jogador pode movê-la. */
    PECA_CAINDO,
    /** A peça bateu embaixo e está sendo assentada. */
    FIXANDO,
    /** Há linhas completas sendo eliminadas. */
    ELIMINANDO_LINHAS,
    /** A estrutura passou do limite de desvio e está desabando. */
    COLAPSO,
    /** A partida está parada até o jogador retomar. */
    PAUSA,
    /** A partida acabou. */
    FIM_DE_JOGO;

    /**
     * Informa se a partida já acabou.
     *
     * @return {@code true} só em {@link #FIM_DE_JOGO}
     */
    public boolean encerrada() {
        return this == FIM_DE_JOGO;
    }
}
