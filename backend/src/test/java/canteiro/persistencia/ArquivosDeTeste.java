package canteiro.persistencia;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;

/** Copia os arquivos feitos para quebrar, de {@code src/test/resources/arquivos/}, para uma pasta temporária. */
final class ArquivosDeTeste {

    private ArquivosDeTeste() {
    }

    /**
     * Copia um arquivo de teste para a pasta, com o nome que o jogo procura.
     *
     * @param nomeDoTeste nome do arquivo em {@code arquivos/}
     * @param pasta       pasta temporária do teste
     * @param nomeNoJogo  nome com que o jogo procura o arquivo, como {@code materiais.properties}
     */
    static void copiar(String nomeDoTeste, Path pasta, String nomeNoJogo) {
        try (InputStream entrada = ArquivosDeTeste.class.getResourceAsStream("/arquivos/" + nomeDoTeste)) {
            if (entrada == null) {
                throw new IllegalArgumentException("arquivo de teste não encontrado: " + nomeDoTeste);
            }
            Files.copy(entrada, pasta.resolve(nomeNoJogo));
        } catch (IOException falha) {
            throw new UncheckedIOException(falha);
        }
    }
}
