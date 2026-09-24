package canteiro.api.dto;

/**
 * Mensagem do navegador pelo WebSocket: {@code {"tipo": "COMANDO", "comando": "ESQUERDA"}}.
 *
 * @param tipo    sempre {@code "COMANDO"}
 * @param comando nome de um {@code canteiro.modelo.Comando}
 * @author Mateus Vitor Ferreira
 * @version 0.1.0
 */
public record ComandoDto(String tipo, String comando) {

    /** Valor esperado em {@code tipo}. */
    public static final String TIPO = "COMANDO";
}
