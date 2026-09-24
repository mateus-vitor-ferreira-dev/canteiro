package canteiro.api;

/**
 * Corpo de toda resposta de erro da API.
 *
 * @param erro     código do erro, em maiúsculas, para o frontend decidir o que fazer
 * @param mensagem texto que pode ser mostrado ao jogador
 * @author Mateus Vitor Ferreira
 * @version 0.1.0
 */
public record ErroDto(String erro, String mensagem) {
}
