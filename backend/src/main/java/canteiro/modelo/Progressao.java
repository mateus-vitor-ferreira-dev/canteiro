package canteiro.modelo;

import java.util.List;

/**
 * Como a partida fica mais difícil a cada nível (tabela "A curva de
 * dificuldade" do README): a peça cai mais rápido, o limite de desvio aperta e
 * novos materiais entram.
 *
 * <p>São os valores iniciais, a ajustar nos testes com jogadores. Do nível 11
 * em diante, os valores param de mudar.</p>
 *
 * @author Mateus Vitor Ferreira
 * @version 0.1.0
 */
public final class Progressao {

    /** Linhas eliminadas para subir um nível (RN14). */
    public static final int LINHAS_POR_NIVEL = 10;

    /** Primeiro nível do jogo. */
    public static final int NIVEL_MINIMO = 1;

    private static final long[] INTERVALO_QUEDA_MS = {800, 650, 500, 380, 280, 200};
    private static final double[] LIMITE_DESVIO = {3.0, 2.5, 2.0, 1.7, 1.4, 1.2};
    private static final int NIVEIS_POR_FAIXA = 2;

    private static final List<String> LEVES = List.of("MADEIRA", "ALVENARIA");
    private static final List<String> SEM_ACO = List.of("MADEIRA", "ALVENARIA", "CONCRETO");
    private static final List<String> TODOS = List.of("MADEIRA", "ALVENARIA", "CONCRETO", "ACO");
    private static final int NIVEL_CONCRETO = 3;
    private static final int NIVEL_ACO = 5;

    private Progressao() {
    }

    /**
     * Intervalo entre duas descidas da peça no nível dado.
     *
     * @param nivel nível, a partir de 1
     * @return intervalo, em milissegundos
     * @throws IllegalArgumentException se o nível for menor que 1
     */
    public static long intervaloQuedaMs(int nivel) {
        return INTERVALO_QUEDA_MS[faixa(nivel)];
    }

    /**
     * Desvio máximo tolerado antes do colapso no nível dado (RN11).
     *
     * @param nivel nível, a partir de 1
     * @return limite, em colunas
     * @throws IllegalArgumentException se o nível for menor que 1
     */
    public static double limiteDesvio(int nivel) {
        return LIMITE_DESVIO[faixa(nivel)];
    }

    /**
     * Códigos dos materiais que podem ser sorteados no nível dado (RN03).
     *
     * @param nivel nível, a partir de 1
     * @return lista imutável de códigos, do mais leve para o mais pesado
     * @throws IllegalArgumentException se o nível for menor que 1
     */
    public static List<String> materiaisLiberados(int nivel) {
        faixa(nivel);
        if (nivel >= NIVEL_ACO) {
            return TODOS;
        }
        return nivel >= NIVEL_CONCRETO ? SEM_ACO : LEVES;
    }

    private static int faixa(int nivel) {
        if (nivel < NIVEL_MINIMO) {
            throw new IllegalArgumentException("nível inválido: " + nivel);
        }
        return Math.min((nivel - NIVEL_MINIMO) / NIVEIS_POR_FAIXA, INTERVALO_QUEDA_MS.length - 1);
    }
}
