package canteiro.modelo.fisica;

import canteiro.modelo.Celula;
import canteiro.modelo.Queda;
import canteiro.modelo.Tabuleiro;
import canteiro.modelo.constantes.Dimensoes;
import canteiro.modelo.materiais.Material;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * O balanço: a parte da peça que ficou sem nada embaixo (RN20).
 *
 * <p>Cada coluna da peça é olhada pelo bloco mais baixo dela. Se embaixo dele
 * há chão ou um bloco, a coluna está <strong>apoiada</strong>; se há vazio,
 * está <strong>em balanço</strong>. A peça segura o balanço só se o material
 * dela não for mais denso que o mais forte em que ela se apoia: o chão
 * aguenta tudo, aço sobre aço segura, concreto sobre madeira não. Quando não
 * segura, os blocos em balanço se soltam e caem na própria coluna, como no
 * colapso.</p>
 *
 * @author Mateus Vitor Ferreira
 * @version 0.1.0
 */
public final class Balanco {

    private Balanco() {
    }

    /**
     * Solta o balanço da peça recém-fixada, se o apoio não aguentar.
     *
     * @param tabuleiro onde a peça já está fixada
     * @param celulas   as células da peça
     * @param material  o material da peça
     * @return a queda de cada bloco que se soltou, de baixo para cima; vazia se a peça segurou
     */
    public static List<Queda> executar(Tabuleiro tabuleiro, List<Celula> celulas, Material material) {
        Map<Integer, Integer> baseDaColuna = basePorColuna(celulas);
        List<Integer> emBalanco = new ArrayList<>();
        double apoioMaisForte = 0;
        for (Map.Entry<Integer, Integer> base : baseDaColuna.entrySet()) {
            int abaixo = base.getValue() + 1;
            int coluna = base.getKey();
            if (abaixo >= Dimensoes.LINHAS) {
                apoioMaisForte = Double.POSITIVE_INFINITY;
            } else if (tabuleiro.ocupada(abaixo, coluna)) {
                apoioMaisForte = Math.max(apoioMaisForte, tabuleiro.bloco(abaixo, coluna).material().densidade());
            } else {
                emBalanco.add(coluna);
            }
        }
        if (emBalanco.isEmpty() || material.densidade() <= apoioMaisForte) {
            return List.of();
        }
        return soltar(tabuleiro, celulas, emBalanco);
    }

    /** A linha do bloco mais baixo da peça em cada coluna. */
    private static Map<Integer, Integer> basePorColuna(List<Celula> celulas) {
        Map<Integer, Integer> base = new TreeMap<>();
        for (Celula celula : celulas) {
            base.merge(celula.coluna(), celula.linha(), Math::max);
        }
        return base;
    }

    /** Derruba os blocos da peça nas colunas em balanço, começando pelo de baixo. */
    private static List<Queda> soltar(Tabuleiro tabuleiro, List<Celula> celulas, List<Integer> colunas) {
        List<Celula> soltos = new ArrayList<>(celulas.stream().filter(c -> colunas.contains(c.coluna())).toList());
        soltos.sort((a, b) -> Integer.compare(b.linha(), a.linha()));
        List<Queda> quedas = new ArrayList<>();
        for (Celula origem : soltos) {
            Celula destino = Colapso.cair(tabuleiro, origem);
            if (!destino.equals(origem)) {
                quedas.add(new Queda(origem, destino));
            }
        }
        return quedas;
    }
}
