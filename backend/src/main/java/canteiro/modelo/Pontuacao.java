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
 *   <li>o nível multiplica tudo.</li>
 * </ul>
 *
 * @author Mateus Vitor Ferreira
 * @version 0.1.0
 */
public final class Pontuacao {

    /** Mais linhas que dá para eliminar de uma vez: a altura da peça I. */
    public static final int MAXIMO_LINHAS = 4;

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
        if (linhas < 1 || linhas > MAXIMO_LINHAS) {
            throw new IllegalArgumentException("linhas fora do intervalo: " + linhas);
        }
        if (nivel < Progressao.NIVEL_MINIMO) {
            throw new IllegalArgumentException("nível inválido: " + nivel);
        }
        double multiplicador = 1 + predominante.bonusLinha() / PERCENTUAL;
        return (int) Math.round(BASE[linhas] * nivel * multiplicador);
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
