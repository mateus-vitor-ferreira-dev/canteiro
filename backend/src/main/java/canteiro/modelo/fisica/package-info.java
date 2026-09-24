/**
 * Análise estrutural: acumuladores de massa, centro de massa, índice de
 * estabilidade e regra de colapso.
 *
 * <p>O centro de massa é recalculado de forma incremental, em tempo constante
 * por bloco alterado (RNF02). Faz parte do modelo: não depende de servidor,
 * de JSON nem de classes gráficas (RNF08).</p>
 */
package canteiro.modelo.fisica;
