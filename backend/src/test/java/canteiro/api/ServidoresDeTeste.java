package canteiro.api;

import canteiro.controle.GerenciadorPartidas;
import canteiro.modelo.estruturas.FonteSimples;
import canteiro.modelo.materiais.CatalogoMateriais;

/** Monta o que o servidor precisa nos testes. */
final class ServidoresDeTeste {

    private static final long SEMENTE = 7;

    private ServidoresDeTeste() {
    }

    static GerenciadorPartidas gerenciador() {
        CatalogoMateriais catalogo = CatalogoMateriais.padrao();
        return new GerenciadorPartidas(d -> new FonteSimples(SEMENTE, catalogo, d.materiaisLiberados()));
    }
}
