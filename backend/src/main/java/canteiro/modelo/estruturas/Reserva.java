package canteiro.modelo.estruturas;

import canteiro.modelo.pecas.Peca;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Objects;
import java.util.Optional;

/**
 * A reserva de peça (RN05, RF10): guarda uma peça para depois e troca pela
 * que está caindo.
 *
 * <p>É uma pilha de no máximo uma peça. Trocar empilha a peça atual e
 * desempilha a que estava guardada. Depois de uma troca, a reserva fica
 * travada até a próxima peça sair da fila: sem isso, o jogador poderia trocar
 * sem parar e nunca deixar a peça cair.</p>
 *
 * @author Mateus Vitor Ferreira
 * @version 0.1.0
 */
public final class Reserva {

    private final Deque<Peca> pilha = new ArrayDeque<>();
    private boolean travada;

    /**
     * Guarda a peça atual e devolve a que estava guardada. A peça guardada
     * volta para a rotação inicial.
     *
     * @param atual peça que está caindo
     * @return a peça que estava guardada, ou vazio na primeira reserva (aí a
     *         próxima peça vem da fila)
     * @throws NullPointerException  se a peça for nula
     * @throws IllegalStateException se já houve uma troca nesta peça
     */
    public Optional<Peca> trocar(Peca atual) {
        Objects.requireNonNull(atual, "peça");
        if (travada) {
            throw new IllegalStateException("a reserva só pode ser trocada uma vez por peça");
        }
        travada = true;
        Optional<Peca> guardada = Optional.ofNullable(pilha.poll());
        atual.endireitar();
        pilha.push(atual);
        return guardada;
    }

    /**
     * Libera a troca. O motor chama quando uma peça nova sai da fila.
     */
    public void liberar() {
        travada = false;
    }

    /**
     * Informa se a troca está liberada.
     *
     * @return {@code true} se ainda não houve troca nesta peça
     */
    public boolean podeTrocar() {
        return !travada;
    }

    /**
     * Devolve a peça guardada, sem tirá-la da reserva.
     *
     * @return a peça, ou vazio se nada foi guardado ainda
     */
    public Optional<Peca> guardada() {
        return Optional.ofNullable(pilha.peek());
    }
}
