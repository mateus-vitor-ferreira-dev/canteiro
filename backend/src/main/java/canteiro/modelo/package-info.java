/**
 * Camada de modelo: motor do jogo, tabuleiro e máquina de estados da partida.
 *
 * <p>Concentra todas as regras do jogo e não depende de servidor, de JSON nem
 * de classes gráficas (RNF08), para que uma partida inteira possa ser
 * testada sem subir o Javalin. Avisa as mudanças de estado apenas por
 * observadores.</p>
 */
package canteiro.modelo;
