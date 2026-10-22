package canteiro.persistencia;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Properties;

/**
 * O arquivo {@code configuracoes.properties}, com as teclas e o volume do
 * jogador (RF27).
 *
 * <p>Formato: uma linha por ação trocada, com os códigos separados por
 * vírgula, e o volume:</p>
 * <pre>
 * tecla.PAUSA=KeyP,Escape
 * volume=60
 * </pre>
 *
 * <p>A leitura nunca lança exceção: arquivo ausente ou ilegível vira o padrão
 * e volume inválido vira {@value ConfiguracoesSalvas#VOLUME_PADRAO}, com aviso
 * no log (RNF11, RNF12). A gravação escreve num arquivo temporário e depois o
 * troca pelo definitivo, para um jogo fechado no meio não deixar o arquivo
 * pela metade.</p>
 *
 * @author Mateus Vitor Ferreira
 * @version 0.1.0
 */
public final class ArquivoConfiguracoes {

    /** Nome do arquivo, dentro da pasta do jogo. */
    public static final String ARQUIVO = "configuracoes.properties";

    private static final String PREFIXO_TECLA = "tecla.";
    private static final String CHAVE_VOLUME = "volume";
    private static final String SEPARADOR = ",";
    private static final String COMENTARIO = "Configurações do CANTEIRO (teclas trocadas e volume)";
    private static final Logger LOG = LoggerFactory.getLogger(ArquivoConfiguracoes.class);

    private final Path pasta;
    private final Path arquivo;

    /**
     * Aponta para o arquivo dentro da pasta dada. Não lê nem cria nada ainda.
     *
     * @param pasta pasta do jogo, como {@code ~/.canteiro}; nos testes, uma pasta temporária
     */
    public ArquivoConfiguracoes(Path pasta) {
        this.pasta = Objects.requireNonNull(pasta, "pasta");
        this.arquivo = pasta.resolve(ARQUIVO);
    }

    /**
     * Lê as configurações salvas.
     *
     * @return o que está no arquivo, ou o padrão se ele faltar ou não puder ser lido
     */
    public synchronized ConfiguracoesSalvas ler() {
        return LeitorPropriedades.lerArquivo(arquivo)
                .map(ArquivoConfiguracoes::converter)
                .orElseGet(ConfiguracoesSalvas::padrao);
    }

    /**
     * Grava as configurações, substituindo as anteriores. Cria a pasta do jogo se preciso.
     *
     * @param configuracoes o que gravar, já validado
     * @throws UncheckedIOException se não for possível gravar, como numa pasta sem permissão
     */
    public synchronized void salvar(ConfiguracoesSalvas configuracoes) {
        Properties propriedades = new Properties();
        configuracoes.teclas().forEach((acao, codigos) ->
                propriedades.setProperty(PREFIXO_TECLA + acao, String.join(SEPARADOR, codigos)));
        propriedades.setProperty(CHAVE_VOLUME, Integer.toString(configuracoes.volume()));
        try {
            Files.createDirectories(pasta);
            Path temporario = Files.createTempFile(pasta, ARQUIVO, ".tmp");
            try {
                try (Writer escritor = Files.newBufferedWriter(temporario, StandardCharsets.UTF_8)) {
                    propriedades.store(escritor, COMENTARIO);
                }
                trocar(temporario);
            } finally {
                Files.deleteIfExists(temporario);
            }
        } catch (IOException falha) {
            throw new UncheckedIOException("não foi possível gravar " + arquivo, falha);
        }
    }

    private void trocar(Path temporario) throws IOException {
        try {
            Files.move(temporario, arquivo, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
        } catch (AtomicMoveNotSupportedException semTrocaAtomica) {
            Files.move(temporario, arquivo, StandardCopyOption.REPLACE_EXISTING);
        }
    }

    private static ConfiguracoesSalvas converter(Properties propriedades) {
        Map<String, List<String>> teclas = new LinkedHashMap<>();
        for (String chave : propriedades.stringPropertyNames()) {
            if (chave.startsWith(PREFIXO_TECLA)) {
                List<String> codigos = Arrays.stream(propriedades.getProperty(chave).split(SEPARADOR))
                        .map(String::strip)
                        .filter(codigo -> !codigo.isEmpty())
                        .toList();
                if (!codigos.isEmpty()) {
                    teclas.put(chave.substring(PREFIXO_TECLA.length()), codigos);
                }
            }
        }
        return new ConfiguracoesSalvas(teclas, volume(propriedades.getProperty(CHAVE_VOLUME)));
    }

    private static int volume(String valor) {
        if (valor == null) {
            return ConfiguracoesSalvas.VOLUME_PADRAO;
        }
        try {
            int volume = Integer.parseInt(valor.strip());
            if (volume >= ConfiguracoesSalvas.VOLUME_MINIMO && volume <= ConfiguracoesSalvas.VOLUME_MAXIMO) {
                return volume;
            }
        } catch (NumberFormatException invalido) {
            // cai no aviso abaixo, como um volume fora da faixa
        }
        LOG.warn("{}={} é inválido; usando {}", CHAVE_VOLUME, valor, ConfiguracoesSalvas.VOLUME_PADRAO);
        return ConfiguracoesSalvas.VOLUME_PADRAO;
    }
}
