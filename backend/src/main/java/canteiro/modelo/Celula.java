package canteiro.modelo;

/**
 * Uma posição na grade: linha e coluna, contadas a partir de zero.
 *
 * <p>A linha cresce de cima para baixo e a coluna da esquerda para a direita,
 * como numa matriz.</p>
 *
 * @param linha  linha, a partir de zero no topo
 * @param coluna coluna, a partir de zero à esquerda
 * @author Mateus Vitor Ferreira
 * @version 0.1.0
 */
public record Celula(int linha, int coluna) {

    /**
     * Devolve esta posição deslocada.
     *
     * @param linhas  quantas linhas descer (negativo sobe)
     * @param colunas quantas colunas ir para a direita (negativo vai para a esquerda)
     * @return uma nova célula com o deslocamento aplicado
     */
    public Celula deslocada(int linhas, int colunas) {
        return new Celula(linha + linhas, coluna + colunas);
    }
}
