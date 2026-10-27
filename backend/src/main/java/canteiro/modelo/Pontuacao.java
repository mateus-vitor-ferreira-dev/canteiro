package canteiro.modelo;

import canteiro.modelo.materiais.Material;

import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;

/**
 * Quantos pontos vale uma eliminação de linhas (RF13).
 *
 * <p>A fórmula é {@code base × nível × (1 + bônus do material / 100)}:</p>
 * <ul>
 *   <li>a base cresce mais que as linhas: 1 linha vale 100, 2 valem 300, 3
 *   valem 500 e 4 valem 800 (RN06). Quatro de uma vez valem o dobro de quatro
 *   separadas;</li>
 *   <li>o material predominante nas linhas eliminadas multiplica os pontos
 *   pelo seu bônus: a madeira dá 10 % a mais, o aço 40 % (RN03);</li>
 *   <li>o nível multiplica tudo;</li>
 *   <li>a <strong>obra ousada</strong>, a eliminação feita com o índice de
 *   estabilidade abaixo de 25 %, vale 50 % a mais (RN17);</li>
 *   <li>a <strong>sequência</strong>, cada peça seguida que fecha linha, soma
 *   10 % por peça, até 100 % (RN18).</li>
 * </ul>
 *
 * @author Mateus Vitor Ferreira
 * @version 0.1.0
 */
public final class Pontuacao {

    /** Mais linhas que dá para eliminar de uma vez: a altura da peça I. */
    public static final int MAXIMO_LINHAS = 4;

    /** Abaixo deste índice de estabilidade, fechar linha é uma obra ousada (RN17). */
    public static final double LIMIAR_OUSADIA = 0.25;

    /** Quanto a obra ousada soma ao multiplicador (RN17). */
    public static final double BONUS_OUSADIA = 0.5;

    /** Quanto cada peça da sequência soma ao multiplicador (RN18). */
    public static final double BONUS_POR_SEQUENCIA = 0.1;

    /** Sequência a partir da qual o bônus para de crescer: 10 peças, 100 % (RN18). */
    public static final int SEQUENCIA_MAXIMA = 10;

    private static final int[] BASE = {0, 100, 300, 500, 800};
    private static final double PERCENTUAL = 100.0;

    private Pontuacao() {
    }

    /**
     * Pontos de uma eliminação.
     *
     * @param linhas       quantas linhas foram eliminadas de uma vez, de 1 a 4
     * @param nivel        nível atual, a partir de 1
     * @param predominante material com mais blocos nas linhas eliminadas
     * @return pontos ganhos
     * @throws IllegalArgumentException se as linhas ou o nível estiverem fora do intervalo
     */
    public static int pontos(int linhas, int nivel, Material predominante) {
        return pontos(linhas, nivel, predominante, false, 0);
    }

    /**
     * Pontos de uma eliminação, com os bônus de risco.
     *
     * @param linhas       quantas linhas foram eliminadas de uma vez, de 1 a 4
     * @param nivel        nível atual, a partir de 1
     * @param predominante material com mais blocos nas linhas eliminadas
     * @param ousada       se a estrutura estava abaixo do limiar de ousadia ao fechar as linhas (RN17)
     * @param sequencia    quantas peças seguidas, antes desta, fecharam linha (RN18)
     * @return pontos ganhos
     * @throws IllegalArgumentException se as linhas, o nível ou a sequência estiverem fora do intervalo
     */
    public static int pontos(int linhas, int nivel, Material predominante, boolean ousada, int sequencia) {
        if (linhas < 1 || linhas > MAXIMO_LINHAS) {
            throw new IllegalArgumentException("linhas fora do intervalo: " + linhas);
        }
        if (nivel < Progressao.NIVEL_MINIMO) {
            throw new IllegalArgumentException("nível inválido: " + nivel);
        }
        if (sequencia < 0) {
            throw new IllegalArgumentException("sequência negativa: " + sequencia);
        }
        double multiplicador = (1 + predominante.bonusLinha() / PERCENTUAL)
                * (ousada ? 1 + BONUS_OUSADIA : 1)
                * bonusSequencia(sequencia);
        return (int) Math.round(BASE[linhas] * nivel * multiplicador);
    }

    /**
     * O multiplicador da sequência: 1 sem sequência, 2 com dez peças ou mais (RN18).
     *
     * @param sequencia quantas peças seguidas fecharam linha
     * @return o multiplicador, de 1 a 2
     */
    public static double bonusSequencia(int sequencia) {
        return 1 + BONUS_POR_SEQUENCIA * Math.min(sequencia, SEQUENCIA_MAXIMA);
    }

    /**
     * Informa se fechar linha com este índice de estabilidade é uma obra ousada (RN17).
     *
     * @param indice índice de estabilidade logo depois de a peça assentar, antes de as linhas sumirem
     * @return {@code true} abaixo do limiar de ousadia
     */
    public static boolean ousada(double indice) {
        return indice < LIMIAR_OUSADIA;
    }

    /**
     * Quanto um colapso desconta: o equivalente a duas linhas eliminadas no
     * nível atual, sem bônus de material (RN12).
     *
     * @param nivel nível atual, a partir de 1
     * @return pontos a descontar
     */
    public static int penalidadeColapso(int nivel) {
        return BASE[2] * nivel;
    }

    /**
     * O material com mais blocos. No empate, vence o que tem o maior bônus,
     * que é também o mais pesado.
     *
     * @param materiais materiais de cada bloco das linhas eliminadas
     * @return o material predominante
     * @throws IllegalArgumentException se a coleção estiver vazia
     */
    public static Material predominante(Collection<Material> materiais) {
        Map<Material, Integer> contagem = new HashMap<>();
        for (Material material : materiais) {
            contagem.merge(material, 1, Integer::sum);
        }
        return contagem.entrySet().stream()
                .max(Comparator.<Map.Entry<Material, Integer>>comparingInt(Map.Entry::getValue)
                        .thenComparingInt(e -> e.getKey().bonusLinha()))
                .map(Map.Entry::getKey)
                .orElseThrow(() -> new IllegalArgumentException("nenhum bloco para comparar"));
    }
}
