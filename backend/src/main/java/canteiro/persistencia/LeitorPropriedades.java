package canteiro.persistencia;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;
import java.util.Properties;

/**
 * Leitura defensiva de arquivos {@code .properties}, em UTF-8.
 *
 * <p>Um arquivo do jogador que falte, esteja vazio ou não possa ser lido
 * nunca derruba o jogo: o problema vai para o log e vale o padrão (RNF11).
 * Conteúdo estranho dentro de um arquivo legível, como uma letra no lugar de
 * um número, é tratado por quem interpreta os valores (RNF12).</p>
 *
 * @author Mateus Vitor Ferreira
 * @version 0.1.0
 */
public final class LeitorPropriedades {

    private static final Logger LOG = LoggerFactory.getLogger(LeitorPropriedades.class);

    private LeitorPropriedades() {
    }

    /**
     * Lê um arquivo do jogador.
     *
     * @param arquivo caminho do arquivo
     * @return as propriedades, ou vazio se o arquivo faltar, estiver vazio ou não puder ser lido
     */
    public static Optional<Properties> lerArquivo(Path arquivo) {
        if (!Files.isRegularFile(arquivo)) {
            LOG.info("{} não existe; usando os valores padrão", arquivo);
            return Optional.empty();
        }
        try (Reader leitor = Files.newBufferedReader(arquivo, StandardCharsets.UTF_8)) {
            Properties propriedades = new Properties();
            propriedades.load(leitor);
            if (propriedades.isEmpty()) {
                LOG.warn("{} está vazio; usando os valores padrão", arquivo);
                return Optional.empty();
            }
            return Optional.of(propriedades);
        } catch (IOException | IllegalArgumentException ilegivel) {
            // IllegalArgumentException: escape de unicode malformado; IOException: inclui UTF-8 inválido
            LOG.warn("Não foi possível ler {} ({}); usando os valores padrão", arquivo, ilegivel.toString());
            return Optional.empty();
        }
    }

    /**
     * Lê um arquivo do jogador por cima dos valores padrão que vêm dentro do JAR.
     * Chave que o arquivo não tiver, ou o arquivo inteiro se não puder ser lido,
     * cai no padrão.
     *
     * @param arquivo       caminho do arquivo do jogador
     * @param recursoPadrao caminho do arquivo padrão no classpath, como {@code /dados/materiais.properties}
     * @return as propriedades do jogador, com o padrão por baixo
     * @throws IllegalStateException se o arquivo padrão não estiver no JAR, o que é defeito de empacotamento
     */
    public static Properties comPadrao(Path arquivo, String recursoPadrao) {
        Properties resultado = new Properties(lerRecurso(recursoPadrao));
        lerArquivo(arquivo).ifPresent(resultado::putAll);
        return resultado;
    }

    private static Properties lerRecurso(String recurso) {
        try (InputStream entrada = LeitorPropriedades.class.getResourceAsStream(recurso)) {
            if (entrada == null) {
                throw new IllegalStateException("arquivo padrão ausente do JAR: " + recurso);
            }
            Properties propriedades = new Properties();
            propriedades.load(new InputStreamReader(entrada, StandardCharsets.UTF_8));
            return propriedades;
        } catch (IOException defeito) {
            throw new UncheckedIOException("não foi possível ler o arquivo padrão " + recurso, defeito);
        }
    }
}
