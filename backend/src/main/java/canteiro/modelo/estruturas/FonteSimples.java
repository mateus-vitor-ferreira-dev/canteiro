package canteiro.modelo.estruturas;

import canteiro.modelo.materiais.CatalogoMateriais;
import canteiro.modelo.materiais.Material;
import canteiro.modelo.pecas.Forma;
import canteiro.modelo.pecas.Peca;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.Random;

/**
 * Fonte de peças <strong>provisória</strong>: sorteia forma e material
 * independentemente, sem o método da sacola.
 *
 * <p>Existe só para o jogo rodar de ponta a ponta enquanto o gerador por
 * sacola (issue #12) não fica pronto. Quando ele ficar, esta classe pode ser
 * apagada e a fábrica em {@code Aplicacao} passa a criar o gerador.</p>
 *
 * @author Mateus Vitor Ferreira
 * @version 0.1.0
 */
public final class FonteSimples implements FontePecas {

    private final Random sorteio;
    private final List<Material> materiais = new ArrayList<>();
    private final Deque<Peca> fila = new ArrayDeque<>();

    /**
     * Cria a fonte.
     *
     * @param semente   semente do sorteio, para a partida ser reproduzível
     * @param catalogo  catálogo de onde vêm os materiais
     * @param liberados códigos dos materiais que podem sair
     */
    public FonteSimples(long semente, CatalogoMateriais catalogo, List<String> liberados) {
        this.sorteio = new Random(semente);
        for (String codigo : liberados) {
            materiais.add(catalogo.buscar(codigo));
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Peca proxima() {
        completar(1);
        return fila.poll();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<Peca> espiar(int quantidade) {
        completar(quantidade);
        return fila.stream().limit(quantidade).toList();
    }

    private void completar(int quantidade) {
        while (fila.size() < quantidade) {
            Forma forma = Forma.values()[sorteio.nextInt(Forma.values().length)];
            fila.add(forma.criar(materiais.get(sorteio.nextInt(materiais.size()))));
        }
    }
}
