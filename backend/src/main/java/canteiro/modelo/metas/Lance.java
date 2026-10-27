package canteiro.modelo.metas;

/**
 * O resultado de uma peça assentada, que as metas observam.
 *
 * @param linhas       quantas linhas a peça eliminou
 * @param predominante código do material predominante nas linhas eliminadas, ou {@code null} se não houve linha
 * @param ousada       se as linhas foram fechadas numa obra ousada (RN17)
 * @param indice       índice de estabilidade depois de tudo resolvido, de 0 a 1
 * @param colapso      se a estrutura desabou
 * @author Mateus Vitor Ferreira
 * @version 0.1.0
 */
public record Lance(int linhas, String predominante, boolean ousada, double indice, boolean colapso) {
}
