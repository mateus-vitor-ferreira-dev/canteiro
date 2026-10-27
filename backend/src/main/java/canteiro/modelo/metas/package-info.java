/**
 * As metas de cada nível: um objetivo curto, sorteado quando o nível começa,
 * que dá um bônus ao ser cumprido (RN19).
 *
 * <p>Cada meta só observa os {@link canteiro.modelo.metas.Lance lances} da
 * partida, um por peça assentada, e conta o próprio progresso. O
 * {@link canteiro.modelo.metas.QuadroMetas} sorteia a meta do nível e avisa
 * quando ela é cumprida.</p>
 */
package canteiro.modelo.metas;
