package canteiro.modelo.estruturas;

import canteiro.modelo.Comando;

import java.util.Objects;

/**
 * Uma jogada: o comando que o jogador deu e o ciclo em que ele chegou.
 *
 * <p>Como o motor é determinístico, as jogadas com os seus ciclos, mais a
 * semente do gerador de peças, bastam para reproduzir a partida inteira.</p>
 *
 * @param ciclo   ciclo do motor em que o comando foi aplicado
 * @param comando comando recebido
 * @author Mateus Vitor Ferreira
 * @version 0.1.0
 */
public record Jogada(long ciclo, Comando comando) {

    /**
     * Valida a jogada.
     *
     * @param ciclo   ciclo do motor em que o comando foi aplicado
     * @param comando comando recebido
     * @throws IllegalArgumentException se o ciclo for negativo
     * @throws NullPointerException     se o comando for nulo
     */
    public Jogada {
        if (ciclo < 0) {
            throw new IllegalArgumentException("ciclo negativo: " + ciclo);
        }
        Objects.requireNonNull(comando, "comando");
    }
}
