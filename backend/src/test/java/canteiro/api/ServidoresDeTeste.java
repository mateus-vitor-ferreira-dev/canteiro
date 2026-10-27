package canteiro.api;

import canteiro.controle.GerenciadorPartidas;
import canteiro.modelo.estruturas.GeradorPecas;
import canteiro.modelo.materiais.CatalogoMateriais;
import canteiro.persistencia.ArquivosDoJogo;

import java.nio.file.Path;

/** Monta o que o servidor precisa nos testes. */
final class ServidoresDeTeste {

    private static final long SEMENTE = 7;

    private ServidoresDeTeste() {
    }

    static GerenciadorPartidas gerenciador() {
        CatalogoMateriais catalogo = CatalogoMateriais.padrao();
        return new GerenciadorPartidas(d -> new GeradorPecas(SEMENTE, catalogo, d.nivelInicial()));
    }

    /** Os arquivos do jogador numa pasta temporária, nunca na {@code ~/.canteiro} de verdade. */
    static ArquivosDoJogo arquivos(Path pastaTemporaria) {
        return ArquivosDoJogo.em(pastaTemporaria);
    }
}
