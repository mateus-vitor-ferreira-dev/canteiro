package canteiro.modelo;

import canteiro.modelo.estruturas.FontePecas;
import canteiro.modelo.materiais.Material;
import canteiro.modelo.pecas.Forma;
import canteiro.modelo.pecas.Peca;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Fonte de peças para os testes: uma lista fixa de peças (a última se repete)
 * ou um sorteio com semente conhecida.
 */
final class FonteLista implements FontePecas {

    private final List<Forma> formas = new ArrayList<>();
    private final List<Material> materiais = new ArrayList<>();
    private final Random sorteio;
    private final List<Material> paraSortear;
    private int proxima;

    private FonteLista(Random sorteio, List<Material> paraSortear) {
        this.sorteio = sorteio;
        this.paraSortear = paraSortear;
    }

    static FonteLista fixa() {
        return new FonteLista(null, List.of());
    }

    static FonteLista sorteada(long semente, List<Material> materiais) {
        return new FonteLista(new Random(semente), materiais);
    }

    FonteLista depois(Forma forma, Material material) {
        formas.add(forma);
        materiais.add(material);
        return this;
    }

    @Override
    public Peca proxima() {
        if (sorteio != null) {
            Forma forma = Forma.values()[sorteio.nextInt(Forma.values().length)];
            return forma.criar(paraSortear.get(sorteio.nextInt(paraSortear.size())));
        }
        int i = Math.min(proxima++, formas.size() - 1);
        return formas.get(i).criar(materiais.get(i));
    }

    @Override
    public List<Peca> espiar(int quantidade) {
        return List.of();
    }
}
