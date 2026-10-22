package canteiro.app;

import canteiro.api.ServidorWeb;
import canteiro.controle.GerenciadorPartidas;
import canteiro.persistencia.PastaDoJogo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.util.Map;
import java.util.Objects;

/**
 * Como o servidor sobe, lido das variáveis de ambiente.
 *
 * <p>Sem nenhuma variável definida, vale o jogo local da especificação: só
 * {@value ServidorWeb#HOST}, porta {@value ServidorWeb#PORTA_PADRAO}, navegador
 * aberto sozinho (RF29), partidas sem limite e os arquivos em {@code ~/.canteiro}.
 * A hospedagem pública muda isso
 * pelo ambiente, sem mudar o código:</p>
 *
 * <ul>
 *   <li>{@code CANTEIRO_HOST}: endereço de escuta, como {@code 0.0.0.0} num contêiner;</li>
 *   <li>{@code PORT}: porta, no nome que as hospedagens costumam definir;</li>
 *   <li>{@code CANTEIRO_ABRIR_NAVEGADOR}: {@code false} num servidor sem tela;</li>
 *   <li>{@code CANTEIRO_LIMITE_PARTIDAS}: quantas partidas podem existir ao mesmo tempo;</li>
 *   <li>{@code CANTEIRO_PASTA}: onde ficam os arquivos do jogador, num contêiner sem pasta pessoal.</li>
 * </ul>
 *
 * <p>Valor inválido não impede o jogo de subir: fica registrado no log e vale
 * o padrão (RNF11).</p>
 *
 * @param host           endereço de escuta
 * @param porta          porta de escuta
 * @param abrirNavegador se o jogo deve abrir o navegador ao subir
 * @param limitePartidas máximo de partidas abertas ao mesmo tempo
 * @param pasta          pasta dos arquivos do jogador (materiais, configurações, ranking)
 * @author Mateus Vitor Ferreira
 * @version 0.1.0
 */
public record Configuracao(String host, int porta, boolean abrirNavegador, int limitePartidas, Path pasta) {

    /** Variável com o endereço de escuta. */
    public static final String VAR_HOST = "CANTEIRO_HOST";

    /** Variável com a porta de escuta. */
    public static final String VAR_PORTA = "PORT";

    /** Variável que liga ou desliga a abertura do navegador. */
    public static final String VAR_ABRIR_NAVEGADOR = "CANTEIRO_ABRIR_NAVEGADOR";

    /** Variável com o máximo de partidas abertas ao mesmo tempo. */
    public static final String VAR_LIMITE_PARTIDAS = "CANTEIRO_LIMITE_PARTIDAS";

    /** Variável com a pasta dos arquivos do jogador. */
    public static final String VAR_PASTA = "CANTEIRO_PASTA";

    private static final int MAIOR_PORTA = 65_535;
    private static final Logger LOG = LoggerFactory.getLogger(Configuracao.class);

    /**
     * Valida os campos.
     *
     * @param host           endereço de escuta
     * @param porta          porta de escuta, de 0 a 65535
     * @param abrirNavegador se o jogo deve abrir o navegador ao subir
     * @param limitePartidas máximo de partidas abertas ao mesmo tempo, positivo
     * @param pasta          pasta dos arquivos do jogador
     * @throws NullPointerException     se o host ou a pasta forem nulos
     * @throws IllegalArgumentException se a porta ou o limite estiverem fora da faixa
     */
    public Configuracao {
        Objects.requireNonNull(host, "host");
        Objects.requireNonNull(pasta, "pasta");
        if (porta < 0 || porta > MAIOR_PORTA) {
            throw new IllegalArgumentException("porta fora da faixa: " + porta);
        }
        if (limitePartidas < 1) {
            throw new IllegalArgumentException("o limite de partidas deve ser positivo: " + limitePartidas);
        }
    }

    /**
     * Lê a configuração de um conjunto de variáveis de ambiente.
     *
     * @param ambiente as variáveis, como as de {@link System#getenv()}
     * @return a configuração, com o padrão local no que faltar ou for inválido
     */
    public static Configuracao doAmbiente(Map<String, String> ambiente) {
        Objects.requireNonNull(ambiente, "ambiente");
        String host = texto(ambiente.get(VAR_HOST), ServidorWeb.HOST);
        int porta = inteiro(ambiente, VAR_PORTA, ServidorWeb.PORTA_PADRAO, 0, MAIOR_PORTA);
        boolean abrirNavegador = !"false".equalsIgnoreCase(texto(ambiente.get(VAR_ABRIR_NAVEGADOR), "true"));
        int limite = inteiro(ambiente, VAR_LIMITE_PARTIDAS, GerenciadorPartidas.SEM_LIMITE, 1, Integer.MAX_VALUE);
        return new Configuracao(host, porta, abrirNavegador, limite, pasta(ambiente.get(VAR_PASTA)));
    }

    private static Path pasta(String valor) {
        if (valor == null || valor.isBlank()) {
            return PastaDoJogo.padrao();
        }
        try {
            return Path.of(valor.strip());
        } catch (InvalidPathException invalida) {
            LOG.warn("{}={} é inválido; usando {}", VAR_PASTA, valor, PastaDoJogo.padrao());
            return PastaDoJogo.padrao();
        }
    }

    private static String texto(String valor, String padrao) {
        return valor == null || valor.isBlank() ? padrao : valor.strip();
    }

    private static int inteiro(Map<String, String> ambiente, String variavel, int padrao, int minimo, int maximo) {
        String valor = ambiente.get(variavel);
        if (valor == null || valor.isBlank()) {
            return padrao;
        }
        try {
            int numero = Integer.parseInt(valor.strip());
            if (numero >= minimo && numero <= maximo) {
                return numero;
            }
        } catch (NumberFormatException invalido) {
            // cai no aviso abaixo, como um número fora da faixa
        }
        LOG.warn("{}={} é inválido; usando {}", variavel, valor, padrao);
        return padrao;
    }
}
