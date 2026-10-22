package canteiro.api.dto;

/**
 * Resposta de {@code POST /api/ranking}: em que lugar o jogador ficou.
 *
 * @param posicao posição no ranking completo, a partir de 1
 * @author Mateus Vitor Ferreira
 * @version 0.1.0
 */
public record PosicaoRankingDto(int posicao) {
}
