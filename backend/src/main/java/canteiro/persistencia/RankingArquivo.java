package canteiro.persistencia;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/**
 * O ranking persistente, no arquivo {@code ranking.csv} (RF21, RF22).
 *
 * <p>Em memória, é uma tabela hash do nome do jogador para a sua melhor
 * partida: nome repetido não ocupa duas linhas, e só troca se a pontuação nova
 * for maior. A lista ordenada é montada só quando alguém pede o ranking.</p>
 *
 * <p>O arquivo é lido na primeira vez em que o ranking é usado. Arquivo
 * ausente é um ranking vazio; arquivo ilegível também, com aviso no log; linha
 * estragada é pulada, com aviso, e as outras valem (RNF11, RNF12).</p>
 *
 * @author Mateus Vitor Ferreira
 * @version 0.1.0
 */
public final class RankingArquivo {

    /** Nome do arquivo, dentro da pasta do jogo. */
    public static final String ARQUIVO = "ranking.csv";

    /** Quantas partidas o ranking mostra (RF22). */
    public static final int TAMANHO = 10;

    private static final String CABECALHO = String.join(EntradaRanking.SEPARADOR,
            "nome", "pontuacao", "nivel", "linhas", "colapsos", "data");
    private static final int CAMPOS = 6;

    /** Maior pontuação primeiro; no empate, quem chegou antes; depois, o nome. */
    private static final Comparator<EntradaRanking> ORDEM = Comparator
            .comparingInt(EntradaRanking::pontuacao).reversed()
            .thenComparing(EntradaRanking::data)
            .thenComparing(EntradaRanking::nome);

    private static final Logger LOG = LoggerFactory.getLogger(RankingArquivo.class);

    private final Path arquivo;
    private Map<String, EntradaRanking> porNome;

    /**
     * Aponta para o arquivo dentro da pasta dada. Não lê nem cria nada ainda.
     *
     * @param pasta pasta do jogo, como {@code ~/.canteiro}; nos testes, uma pasta temporária
     */
    public RankingArquivo(Path pasta) {
        this.arquivo = Objects.requireNonNull(pasta, "pasta").resolve(ARQUIVO);
    }

    /**
     * As dez melhores partidas, da maior pontuação para a menor (RF22).
     *
     * @return até {@value #TAMANHO} entradas
     */
    public synchronized List<EntradaRanking> dezMelhores() {
        return ordenado().stream().limit(TAMANHO).toList();
    }

    /**
     * Registra uma partida. Se o jogador já está no ranking, só troca se a
     * pontuação nova for maior; senão, a melhor partida dele continua.
     *
     * @param entrada a partida a registrar
     * @return a posição do jogador no ranking completo, a partir de 1
     * @throws java.io.UncheckedIOException se não for possível gravar o arquivo
     */
    public synchronized int registrar(EntradaRanking entrada) {
        Map<String, EntradaRanking> tabela = tabela();
        EntradaRanking anterior = tabela.get(entrada.nome());
        if (anterior == null || entrada.pontuacao() > anterior.pontuacao()) {
            tabela.put(entrada.nome(), entrada);
            try {
                gravar();
            } catch (RuntimeException falha) {
                // a memória não pode ficar diferente do arquivo
                desfazer(tabela, entrada.nome(), anterior);
                throw falha;
            }
        }
        return posicao(entrada.nome());
    }

    private static void desfazer(Map<String, EntradaRanking> tabela, String nome, EntradaRanking anterior) {
        if (anterior == null) {
            tabela.remove(nome);
        } else {
            tabela.put(nome, anterior);
        }
    }

    private int posicao(String nome) {
        List<EntradaRanking> lista = ordenado();
        for (int i = 0; i < lista.size(); i++) {
            if (lista.get(i).nome().equals(nome)) {
                return i + 1;
            }
        }
        throw new IllegalStateException("o jogador acabou de entrar e não está no ranking: " + nome);
    }

    private List<EntradaRanking> ordenado() {
        List<EntradaRanking> lista = new ArrayList<>(tabela().values());
        lista.sort(ORDEM);
        return lista;
    }

    private Map<String, EntradaRanking> tabela() {
        if (porNome == null) {
            porNome = carregar();
        }
        return porNome;
    }

    private Map<String, EntradaRanking> carregar() {
        Map<String, EntradaRanking> tabela = new HashMap<>();
        if (!Files.isRegularFile(arquivo)) {
            return tabela;
        }
        List<String> linhas;
        try {
            linhas = Files.readAllLines(arquivo, StandardCharsets.UTF_8);
        } catch (IOException ilegivel) {
            LOG.warn("Não foi possível ler {} ({}); começando com o ranking vazio", arquivo, ilegivel.toString());
            return tabela;
        }
        for (int i = 0; i < linhas.size(); i++) {
            String linha = linhas.get(i);
            if (linha.isBlank() || linha.equals(CABECALHO)) {
                continue;
            }
            int numero = i + 1;
            interpretar(linha).ifPresentOrElse(
                    entrada -> tabela.merge(entrada.nome(), entrada, RankingArquivo::melhor),
                    () -> LOG.warn("{}, linha {}: \"{}\" está estragada e foi ignorada", arquivo, numero, linha));
        }
        return tabela;
    }

    private static EntradaRanking melhor(EntradaRanking uma, EntradaRanking outra) {
        return outra.pontuacao() > uma.pontuacao() ? outra : uma;
    }

    private static Optional<EntradaRanking> interpretar(String linha) {
        String[] campos = linha.split(EntradaRanking.SEPARADOR, -1);
        if (campos.length != CAMPOS) {
            return Optional.empty();
        }
        try {
            return Optional.of(new EntradaRanking(campos[0].strip(),
                    Integer.parseInt(campos[1].strip()), Integer.parseInt(campos[2].strip()),
                    Integer.parseInt(campos[3].strip()), Integer.parseInt(campos[4].strip()),
                    LocalDate.parse(campos[5].strip())));
        } catch (IllegalArgumentException | DateTimeParseException estragada) {
            // NumberFormatException é uma IllegalArgumentException, como a de nome inválido
            return Optional.empty();
        }
    }

    private void gravar() {
        StringBuilder texto = new StringBuilder(CABECALHO).append('\n');
        for (EntradaRanking e : ordenado()) {
            texto.append(String.join(EntradaRanking.SEPARADOR, e.nome(), Integer.toString(e.pontuacao()),
                    Integer.toString(e.nivel()), Integer.toString(e.linhas()),
                    Integer.toString(e.colapsos()), e.data().toString())).append('\n');
        }
        GravacaoSegura.escrever(arquivo, texto.toString());
    }
}
