package canteiro.persistencia;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Leitura defensiva: nada que esteja num arquivo do jogador lança exceção (RNF11).
 *
 * @author Mateus Vitor Ferreira
 * @version 0.1.0
 */
class LeitorPropriedadesTest {

    @TempDir
    Path pasta;

    @Test
    void arquivoAusenteDaVazio() {
        assertTrue(LeitorPropriedades.lerArquivo(pasta.resolve("nao-existe.properties")).isEmpty());
    }

    @Test
    void pastaNoLugarDoArquivoDaVazio() throws IOException {
        Path pastaComNomeDeArquivo = Files.createDirectory(pasta.resolve("materiais.properties"));

        assertTrue(LeitorPropriedades.lerArquivo(pastaComNomeDeArquivo).isEmpty());
    }

    @Test
    void acentoEmUtf8EhLidoCerto() throws IOException {
        Path arquivo = Files.writeString(pasta.resolve("a.properties"), "nome=Construção\n");

        assertEquals("Construção", LeitorPropriedades.lerArquivo(arquivo).orElseThrow().getProperty("nome"));
    }

    @Test
    void chaveDoJogadorPassaNaFrenteDoPadrao() throws IOException {
        Path arquivo = Files.writeString(pasta.resolve(CarregadorMateriais.ARQUIVO), "ACO.densidade=9\n");

        Properties lidas = LeitorPropriedades.comPadrao(arquivo, CarregadorMateriais.RECURSO_PADRAO);

        assertEquals("9", lidas.getProperty("ACO.densidade"));
        assertEquals("0.6", lidas.getProperty("MADEIRA.densidade"));
    }

    @Test
    void recursoPadraoAusenteEhDefeito() {
        assertThrows(IllegalStateException.class,
                () -> LeitorPropriedades.comPadrao(pasta.resolve("x"), "/dados/nao-existe.properties"));
    }
}
