package canteiro.api.dto;

/**
 * Corpo de {@code POST /api/partidas}.
 *
 * @param dificuldade código da dificuldade, como {@code "NORMAL"}
 * @author Mateus Vitor Ferreira
 * @version 0.1.0
 */
public record NovaPartidaDto(String dificuldade) {
}
