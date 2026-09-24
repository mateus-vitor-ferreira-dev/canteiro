package canteiro.modelo;

/**
 * Quem quer saber o que acontece na partida. O motor avisa sem saber quem
 * está ouvindo: é assim que o modelo fala com o WebSocket sem conhecê-lo.
 *
 * @author Mateus Vitor Ferreira
 * @version 0.1.0
 */
public interface ObservadorPartida {

    /**
     * Chamado ao fim de um ciclo em que algo mudou.
     *
     * @param motor o motor, já no estado novo
     */
    void estadoMudou(MotorJogo motor);

    /**
     * Chamado no instante em que um evento acontece. Por padrão, ignora.
     *
     * @param evento o que aconteceu
     */
    default void eventoOcorreu(EventoPartida evento) {
        // quem não anima nem toca som não precisa tratar eventos
    }
}
