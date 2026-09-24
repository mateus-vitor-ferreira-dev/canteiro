package canteiro.modelo;

import canteiro.modelo.estruturas.FontePecas;
import canteiro.modelo.materiais.Material;
import canteiro.modelo.pecas.Forma;
import canteiro.modelo.pecas.Peca;

import java.util.ArrayList;
import java.util.List;

/**
 * Fonte de peças para os testes: repete uma sequência conhecida de formas,
 * todas do mesmo material.
 */
class FonteFixa implements FontePecas {

    private final Material material;
    private final Forma[] formas;
    private int proxima;

    FonteFixa(Material material, Forma... formas) {
        this.material = material;
        this.formas = formas;
    }

    @Override
    public Peca proxima() {
        Peca peca = formas[proxima % formas.length].criar(material);
        proxima++;
        return peca;
    }

    @Override
    public List<Peca> espiar(int quantidade) {
        List<Peca> pecas = new ArrayList<>();
        for (int i = 0; i < quantidade; i++) {
            pecas.add(formas[(proxima + i) % formas.length].criar(material));
        }
        return pecas;
    }
}
