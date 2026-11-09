package canteiro.persistencia;

import canteiro.modelo.Dificuldade;
import canteiro.modelo.estruturas.Jogada;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

/**
 * Uma partida gravada (RF24): o bastante para refazê-la do começo. Não guarda
 * o tabuleiro, só a dificuldade, a semente das peças e as jogadas, porque a
 * mesma semente com os mesmos comandos nos mesmos ciclos dá a mesma partida.
 * Por isso a reprodução só sai igual com os mesmos materiais: quem mudar a
 * densidade no {@code materiais.properties} depois de gravar vê outra partida.
 *
 * @param id          nome do arquivo, sem o {@code .txt}
 * @param data        quando a partida terminou, no horário local
 * @param dificuldade dificuldade da partida
 * @param semente     semente do sorteio das peças
 * @param pontuacao   pontuação final, para a lista não precisar refazer a partida
 * @param jogadas     as jogadas, na ordem; vazia quando só o cabeçalho foi lido
 * @author Mateus Vitor Ferreira
 * @version 0.1.0
 */
public record Repeticao(String id, LocalDateTime data, Dificuldade dificuldade, long semente, int pontuacao,
                        List<Jogada> jogadas) {

    /**
     * Confere os campos e copia as jogadas.
     *
     * @param id          nome do arquivo, sem o {@code .txt}
     * @param data        quando a partida terminou
     * @param dificuldade dificuldade da partida
     * @param semente     semente do sorteio das peças
     * @param pontuacao   pontuação final
     * @param jogadas     as jogadas, na ordem
     * @throws NullPointerException     se algum campo for nulo
     * @throws IllegalArgumentException se a pontuação for negativa
     */
    public Repeticao {
        Objects.requireNonNull(id, "id");
        Objects.requireNonNull(data, "data");
        Objects.requireNonNull(dificuldade, "dificuldade");
        if (pontuacao < 0) {
            throw new IllegalArgumentException("pontuação negativa: " + pontuacao);
        }
        jogadas = List.copyOf(jogadas);
    }
}
