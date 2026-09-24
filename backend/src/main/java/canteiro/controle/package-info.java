/**
 * Camada de controle: sessões de partida, fila de comandos e laço de atualização.
 *
 * <p>Os comandos chegam pela thread do WebSocket, entram numa fila concorrente
 * e são consumidos pelo motor no início do ciclo seguinte, o que mantém a
 * partida determinística e reproduzível. Este pacote não sabe o que é HTTP
 * nem JSON: quem converte o estado para o frontend é {@code canteiro.api}.</p>
 */
package canteiro.controle;
