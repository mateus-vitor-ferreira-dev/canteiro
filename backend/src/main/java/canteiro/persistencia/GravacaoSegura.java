package canteiro.persistencia;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

/**
 * Grava um arquivo de texto sem risco de deixá-lo pela metade: escreve num
 * temporário na mesma pasta e só depois o troca pelo definitivo. Se o jogo
 * fechar no meio, fica o arquivo antigo, inteiro.
 *
 * @author Mateus Vitor Ferreira
 * @version 0.1.0
 */
final class GravacaoSegura {

    private static final String SUFIXO_TEMPORARIO = ".tmp";

    private GravacaoSegura() {
    }

    /**
     * Grava o conteúdo em UTF-8, criando a pasta se preciso.
     *
     * @param arquivo  arquivo definitivo
     * @param conteudo texto a gravar
     * @throws UncheckedIOException se não for possível gravar, como numa pasta sem permissão
     */
    static void escrever(Path arquivo, String conteudo) {
        Path pasta = arquivo.toAbsolutePath().getParent();
        try {
            Files.createDirectories(pasta);
            Path temporario = Files.createTempFile(pasta, arquivo.getFileName().toString(), SUFIXO_TEMPORARIO);
            try {
                Files.writeString(temporario, conteudo, StandardCharsets.UTF_8);
                trocar(temporario, arquivo);
            } finally {
                Files.deleteIfExists(temporario);
            }
        } catch (IOException falha) {
            throw new UncheckedIOException("não foi possível gravar " + arquivo, falha);
        }
    }

    private static void trocar(Path temporario, Path arquivo) throws IOException {
        try {
            Files.move(temporario, arquivo, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
        } catch (AtomicMoveNotSupportedException semTrocaAtomica) {
            Files.move(temporario, arquivo, StandardCopyOption.REPLACE_EXISTING);
        }
    }
}
