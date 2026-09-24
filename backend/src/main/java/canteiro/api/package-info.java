/**
 * Camada de comunicação: servidor HTTP, rotas REST, canal WebSocket e DTOs.
 *
 * <p>É o único pacote, além de {@code canteiro.app}, que pode usar Javalin e
 * Jackson. Recebe as requisições, valida, chama o controle e converte o
 * modelo em DTOs ({@code record}s) antes de enviá-lo como JSON. O modelo
 * nunca é serializado diretamente.</p>
 */
package canteiro.api;
