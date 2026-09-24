/**
 * Camada de modelo: toda a regra do jogo.
 *
 * <p>Motor, tabuleiro, dificuldades e estados da partida ficam aqui; peças,
 * materiais, estruturas de dados, física e constantes ficam nos subpacotes.
 * Nada neste pacote nem nos subpacotes depende de servidor, de JSON, de
 * classes gráficas ou das camadas de cima (RNF08), para que uma partida
 * inteira possa ser testada sem subir o Javalin. As mudanças de estado são
 * avisadas apenas por observadores.</p>
 */
package canteiro.modelo;
