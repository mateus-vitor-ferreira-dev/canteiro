package canteiro;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Garante a separação de camadas descrita na seção 2.9 da documentação.
 *
 * <p>O modelo e os pacotes que ele usa não podem importar classes gráficas
 * nem as camadas de cima (RNF08). Se esta regra valer, as regras do jogo
 * podem ser testadas sem abrir janela.</p>
 *
 * @author Mateus Vitor Ferreira
 * @version 0.1.0
 */
class ArquiteturaTest {

    private static final Path FONTES = Path.of("src", "main", "java", "canteiro");

    private static final List<String> GRAFICOS = List.of("javax.swing.", "java.awt.");
    private static final List<String> CAMADAS_DE_CIMA =
            List.of("canteiro.app.", "canteiro.visao.", "canteiro.controle.");

    static Stream<Arguments> regras() {
        List<String> semGraficoNemCamadaDeCima = Stream.concat(GRAFICOS.stream(), CAMADAS_DE_CIMA.stream()).toList();
        List<String> modeloIsolado = Stream.concat(semGraficoNemCamadaDeCima.stream(),
                Stream.of("canteiro.persistencia.")).toList();
        return Stream.of(
                Arguments.of("modelo", modeloIsolado),
                Arguments.of("estruturas", modeloIsolado),
                Arguments.of("fisica", modeloIsolado),
                Arguments.of("util", modeloIsolado),
                Arguments.of("persistencia", semGraficoNemCamadaDeCima));
    }

    @ParameterizedTest(name = "canteiro.{0} não importa {1}")
    @MethodSource("regras")
    void pacoteNaoImportaOQueEProibido(String pacote, List<String> proibidos) throws IOException {
        Path raiz = FONTES.resolve(pacote);
        assertTrue(Files.isDirectory(raiz), "pacote não encontrado: " + raiz);

        try (Stream<Path> arquivos = Files.walk(raiz)) {
            List<String> violacoes = arquivos
                    .filter(arquivo -> arquivo.toString().endsWith(".java"))
                    .flatMap(arquivo -> importsProibidos(arquivo, proibidos))
                    .toList();
            assertTrue(violacoes.isEmpty(), "imports proibidos:\n" + String.join("\n", violacoes));
        }
    }

    private static Stream<String> importsProibidos(Path arquivo, List<String> proibidos) {
        try {
            return Files.readAllLines(arquivo).stream()
                    .map(String::strip)
                    .filter(linha -> linha.startsWith("import "))
                    .filter(linha -> proibidos.stream().anyMatch(linha::contains))
                    .map(linha -> arquivo + ": " + linha);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
