package canteiro.api.dto;

/**
 * Resposta de {@code POST /api/partidas}: o id para conectar no WebSocket.
 *
 * @param id identificador da partida, usado em {@code /ws/partidas/{id}}
 * @author Mateus Vitor Ferreira
 * @version 0.1.0
 */
public record PartidaCriadaDto(String id) {
}
