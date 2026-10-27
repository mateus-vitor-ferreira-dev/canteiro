package canteiro.api.dto;

/**
 * Corpo de {@code POST /api/partidas}.
 *
 * @param dificuldade código da dificuldade, como {@code "NORMAL"}
 * @param modoTreino  se a partida é de treino, com o desfazer liberado (RF26);
 *                    quando o campo não vem, vale {@code false}
 * @author Mateus Vitor Ferreira
 * @version 0.1.0
 */
public record NovaPartidaDto(String dificuldade, boolean modoTreino) {
}
