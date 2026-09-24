package canteiro.modelo;

/**
 * Comandos que o jogador envia ao motor (seção "O protocolo, em resumo" do README).
 *
 * @author Mateus Vitor Ferreira
 * @version 0.1.0
 */
public enum Comando {

    /** Move a peça uma coluna para a esquerda (RF07). */
    ESQUERDA,
    /** Move a peça uma coluna para a direita (RF07). */
    DIREITA,
    /** Desce a peça uma linha; se ela já estiver apoiada, fixa (RF06). */
    DESCER,
    /** Desce a peça até o primeiro apoio e fixa (RF09). */
    QUEDA_INSTANTANEA,
    /** Gira a peça no sentido horário (RF08). */
    GIRAR_HORARIO,
    /** Gira a peça no sentido anti-horário (RF08). */
    GIRAR_ANTI_HORARIO,
    /** Troca a peça atual pela reservada (RF10). */
    RESERVAR,
    /** Pausa a partida (RF19). */
    PAUSAR,
    /** Retoma a partida pausada (RF19). */
    RETOMAR,
    /** Desfaz a última jogada, só no modo treino (RF26). */
    DESFAZER
}
