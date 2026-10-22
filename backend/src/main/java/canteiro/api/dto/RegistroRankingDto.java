package canteiro.api.dto;

/**
 * Corpo de {@code POST /api/ranking} (RF21). A pontuação não vem aqui: o
 * backend lê da própria partida, para ninguém mandar pontos inventados.
 *
 * @param partidaId id da partida encerrada
 * @param nome      nome do jogador
 * @author Mateus Vitor Ferreira
 * @version 0.1.0
 */
public record RegistroRankingDto(String partidaId, String nome) {
}
