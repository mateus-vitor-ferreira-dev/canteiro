package canteiro.modelo.estruturas;

import canteiro.modelo.Progressao;
import canteiro.modelo.materiais.CatalogoMateriais;
import canteiro.modelo.materiais.Material;
import canteiro.modelo.pecas.Forma;
import canteiro.modelo.pecas.Peca;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Deque;
import java.util.List;
import java.util.Objects;
import java.util.Random;

/**
 * Gera as peças da partida pelo método da sacola (RN04) e guarda as próximas
 * numa fila (RF05).
 *
 * <p>A sacola tem as sete formas embaralhadas. Cada peça tira uma forma da
 * sacola, e ela só é enchida e embaralhada de novo quando esvazia: nenhuma
 * forma se repete antes de as outras seis saírem. O material de cada peça é
 * sorteado entre os liberados no nível atual (RN03), e a lista muda quando o
 * nível sobe. As peças que já estavam na fila mantêm o material com que foram
 * geradas.</p>
 *
 * <p>Com a mesma semente, a sequência de peças é sempre a mesma.</p>
 *
 * @author Mateus Vitor Ferreira
 * @version 0.1.0
 */
public final class GeradorPecas implements FontePecas {

    private final Random sorteio;
    private final CatalogoMateriais catalogo;
    private final Deque<Forma> sacola = new ArrayDeque<>();
    private final Deque<Peca> fila = new ArrayDeque<>();
    private List<Material> liberados;

    /**
     * Cria o gerador.
     *
     * @param semente      semente do sorteio, para a partida ser reproduzível
     * @param catalogo     catálogo de onde saem os materiais
     * @param nivelInicial nível em que a partida começa, que define os materiais liberados
     * @throws NullPointerException     se o catálogo for nulo
     * @throws IllegalArgumentException se o nível for menor que 1 ou se um
     *                                  material liberado não estiver no catálogo
     */
    public GeradorPecas(long semente, CatalogoMateriais catalogo, int nivelInicial) {
        this.sorteio = new Random(semente);
        this.catalogo = Objects.requireNonNull(catalogo, "catálogo");
        this.liberados = materiaisDoNivel(nivelInicial);
    }

    @Override
    public Peca proxima() {
        completar(1);
        return fila.poll();
    }

    @Override
    public List<Peca> espiar(int quantidade) {
        completar(quantidade);
        return fila.stream().limit(quantidade).toList();
    }

    /**
     * Troca os materiais liberados pelos do novo nível. Só vale para as peças
     * geradas daqui em diante.
     *
     * @param nivel nível atual
     * @throws IllegalArgumentException se o nível for menor que 1
     */
    @Override
    public void nivelMudou(int nivel) {
        liberados = materiaisDoNivel(nivel);
    }

    private List<Material> materiaisDoNivel(int nivel) {
        return Progressao.materiaisLiberados(nivel).stream().map(catalogo::buscar).toList();
    }

    private void completar(int quantidade) {
        while (fila.size() < quantidade) {
            Material material = liberados.get(sorteio.nextInt(liberados.size()));
            fila.add(tirarDaSacola().criar(material));
        }
    }

    private Forma tirarDaSacola() {
        if (sacola.isEmpty()) {
            List<Forma> formas = new ArrayList<>(Arrays.asList(Forma.values()));
            Collections.shuffle(formas, sorteio);
            sacola.addAll(formas);
        }
        return sacola.poll();
    }
}
