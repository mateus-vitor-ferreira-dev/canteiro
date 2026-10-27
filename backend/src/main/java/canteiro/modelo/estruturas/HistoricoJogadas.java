package canteiro.modelo.estruturas;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Deque;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * Todas as jogadas da partida, numa pilha (RF23).
 *
 * <p>O topo é a jogada mais recente: é ela que o desfazer do modo treino
 * (RF26) tira. O replay (RF24) percorre a pilha de baixo para cima, na ordem
 * em que as jogadas aconteceram.</p>
 *
 * @author Mateus Vitor Ferreira
 * @version 0.1.0
 */
public final class HistoricoJogadas {

    private final Deque<Jogada> pilha = new ArrayDeque<>();

    /**
     * Empilha uma jogada.
     *
     * @param jogada jogada feita
     * @throws NullPointerException se a jogada for nula
     */
    public void registrar(Jogada jogada) {
        pilha.push(Objects.requireNonNull(jogada, "jogada"));
    }

    /**
     * Tira a jogada mais recente.
     *
     * @return a jogada, ou vazio se o histórico estiver vazio
     */
    public Optional<Jogada> desfazerUltima() {
        return Optional.ofNullable(pilha.poll());
    }

    /**
     * Devolve a jogada mais recente, sem tirá-la.
     *
     * @return a jogada, ou vazio se o histórico estiver vazio
     */
    public Optional<Jogada> ultima() {
        return Optional.ofNullable(pilha.peek());
    }

    /**
     * Devolve as jogadas na ordem em que aconteceram, da primeira à última.
     *
     * @return cópia imutável das jogadas
     */
    public List<Jogada> emOrdem() {
        List<Jogada> jogadas = new ArrayList<>(pilha);
        Collections.reverse(jogadas);
        return Collections.unmodifiableList(jogadas);
    }

    /**
     * Quantas jogadas há no histórico.
     *
     * @return o tamanho da pilha
     */
    public int tamanho() {
        return pilha.size();
    }

    /**
     * Informa se não há nenhuma jogada.
     *
     * @return {@code true} se o histórico estiver vazio
     */
    public boolean vazio() {
        return pilha.isEmpty();
    }
}
