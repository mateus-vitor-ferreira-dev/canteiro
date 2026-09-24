package canteiro.modelo;

/**
 * Um bloco que caiu no colapso: de onde saiu e onde parou. O frontend usa
 * isso para animar o desabamento.
 *
 * @param origem  posição antes de cair
 * @param destino posição em que parou
 * @author Mateus Vitor Ferreira
 * @version 0.1.0
 */
public record Queda(Celula origem, Celula destino) {
}
