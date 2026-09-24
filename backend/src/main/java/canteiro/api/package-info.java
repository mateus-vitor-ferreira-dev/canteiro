/**
 * Camada de comunicação: tudo o que sabe que existe HTTP.
 *
 * <p>É o único pacote, além de {@code canteiro.app}, que pode usar Javalin e
 * Jackson. Aqui fica o {@code ServidorWeb}; as rotas REST ficam em
 * {@code rotas}, o canal WebSocket em {@code ws} e os DTOs em {@code dto}.</p>
 */
package canteiro.api;
