package canteiro.persistencia;

import java.time.LocalDate;
import java.util.Objects;
import java.util.Optional;

/**
 * Uma linha do ranking: a melhor partida de um jogador (RF21, RF22).
 *
 * @param nome      nome do jogador, como ele digitou, sem espaços nas pontas
 * @param pontuacao pontos da partida
 * @param nivel     nível alcançado
 * @param linhas    linhas eliminadas
 * @param colapsos  colapsos sofridos
 * @param data      dia em que a partida foi registrada
 * @author Mateus Vitor Ferreira
 * @version 0.1.0
 */
public record EntradaRanking(String nome, int pontuacao, int nivel, int linhas, int colapsos, LocalDate data) {

    /** Maior nome aceito, em caracteres. O frontend usa o mesmo limite. */
    public static final int NOME_MAXIMO = 20;

    /** O separador dos campos no arquivo: não pode aparecer no nome. */
    static final String SEPARADOR = ";";

    /**
     * Confere os campos.
     *
     * @param nome      nome do jogador
     * @param pontuacao pontos da partida, zero ou mais
     * @param nivel     nível alcançado, um ou mais
     * @param linhas    linhas eliminadas, zero ou mais
     * @param colapsos  colapsos sofridos, zero ou mais
     * @param data      dia do registro
     * @throws IllegalArgumentException se o nome não for válido ({@link #problemaNoNome(String)}) ou um número for negativo
     * @throws NullPointerException     se a data for nula
     */
    public EntradaRanking {
        problemaNoNome(nome).ifPresent(problema -> {
            throw new IllegalArgumentException(problema);
        });
        if (pontuacao < 0 || nivel < 1 || linhas < 0 || colapsos < 0) {
            throw new IllegalArgumentException("números do placar inválidos para " + nome);
        }
        Objects.requireNonNull(data, "data");
    }

    /**
     * Diz o que há de errado com um nome, para a rota explicar ao jogador antes
     * de tentar gravar. O nome deve vir sem espaços nas pontas.
     *
     * @param nome nome a conferir
     * @return a mensagem para o jogador, ou vazio se o nome serve
     */
    public static Optional<String> problemaNoNome(String nome) {
        if (nome == null || nome.isBlank()) {
            return Optional.of("Digite um nome para salvar.");
        }
        if (nome.length() > NOME_MAXIMO) {
            return Optional.of("O nome deve ter no máximo " + NOME_MAXIMO + " caracteres.");
        }
        if (nome.contains(SEPARADOR) || nome.chars().anyMatch(Character::isISOControl)) {
            return Optional.of("O nome não pode ter ponto e vírgula nem quebra de linha.");
        }
        return Optional.empty();
    }
}
