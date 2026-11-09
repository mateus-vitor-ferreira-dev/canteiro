package canteiro.persistencia;

import canteiro.modelo.Comando;
import canteiro.modelo.Dificuldade;
import canteiro.modelo.estruturas.Jogada;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.regex.Pattern;

/**
 * As partidas gravadas, uma por arquivo, na pasta {@code repeticoes/} do jogo (RF24).
 *
 * <p>Formato: a primeira linha identifica o arquivo, a segunda traz a
 * dificuldade, a semente, a pontuação e quando a partida terminou, e depois
 * vem uma jogada por linha, com o ciclo e o comando:</p>
 * <pre>
 * CANTEIRO-REPETICAO 1
 * NORMAL;-4962768465676381896;12340;2026-09-28T14:32:05
 * 12;ESQUERDA
 * 30;QUEDA_INSTANTANEA
 * </pre>
 *
 * <p>O arquivo se chama pela data e hora, como {@code 2026-09-28_14-32-05.txt}.
 * O id que vem da URL só é aceito nesse formato, para nunca apontar para fora
 * da pasta. Ficam as {@value #MAXIMO} mais recentes; a mais antiga é apagada
 * quando entra uma nova. Arquivo estragado não derruba o jogo: some da lista,
 * com aviso no log, e pedir para reproduzi-lo dá
 * {@link RepeticaoCorrompidaException} (RNF11, RNF12).</p>
 *
 * @author Mateus Vitor Ferreira
 * @version 0.1.0
 */
public final class ArquivoRepeticoes {

    /** Nome da pasta, dentro da pasta do jogo. */
    public static final String PASTA = "repeticoes";

    /** Quantas repetições ficam guardadas, no máximo. */
    public static final int MAXIMO = 50;

    private static final String EXTENSAO = ".txt";
    private static final String ASSINATURA = "CANTEIRO-REPETICAO 1";
    private static final String SEPARADOR = ";";
    private static final int CAMPOS_CABECALHO = 4;
    private static final int CAMPOS_JOGADA = 2;
    private static final DateTimeFormatter FORMATO_ID = DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss");

    /** Data e hora em ISO, sempre com os segundos, mesmo quando são zero. */
    public static final DateTimeFormatter FORMATO_DATA = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");
    private static final Pattern ID_VALIDO = Pattern.compile("\\d{4}-\\d{2}-\\d{2}_\\d{2}-\\d{2}-\\d{2}(-\\d{1,4})?");
    private static final Comparator<Repeticao> MAIS_RECENTE_PRIMEIRO = Comparator
            .comparing(Repeticao::data).thenComparing(Repeticao::id).reversed();
    private static final Logger LOG = LoggerFactory.getLogger(ArquivoRepeticoes.class);

    private final Path pasta;

    /**
     * Aponta para a pasta das repetições dentro da pasta do jogo. Não lê nem cria nada ainda.
     *
     * @param pastaDoJogo pasta do jogo, como {@code ~/.canteiro}; nos testes, uma pasta temporária
     */
    public ArquivoRepeticoes(Path pastaDoJogo) {
        this.pasta = Objects.requireNonNull(pastaDoJogo, "pasta").resolve(PASTA);
    }

    /**
     * Grava uma partida encerrada e apaga as mais antigas além do máximo.
     *
     * @param dificuldade dificuldade da partida
     * @param semente     semente do sorteio das peças
     * @param pontuacao   pontuação final
     * @param jogadas     as jogadas, na ordem
     * @param fim         quando a partida terminou
     * @return o id da repetição gravada
     * @throws java.io.UncheckedIOException se não for possível gravar
     */
    public synchronized String gravar(Dificuldade dificuldade, long semente, int pontuacao, List<Jogada> jogadas,
                                      LocalDateTime fim) {
        LocalDateTime data = fim.truncatedTo(ChronoUnit.SECONDS);
        String id = idLivre(data);
        StringBuilder texto = new StringBuilder(ASSINATURA).append('\n')
                .append(String.join(SEPARADOR, dificuldade.name(), Long.toString(semente),
                        Integer.toString(pontuacao), FORMATO_DATA.format(data))).append('\n');
        for (Jogada jogada : jogadas) {
            texto.append(jogada.ciclo()).append(SEPARADOR).append(jogada.comando().name()).append('\n');
        }
        GravacaoSegura.escrever(arquivo(id), texto.toString());
        apagarAlemDoMaximo();
        return id;
    }

    /**
     * As repetições gravadas, da mais recente para a mais antiga, só com o
     * cabeçalho: as jogadas vêm vazias.
     *
     * @return as repetições que puderam ser lidas
     */
    public synchronized List<Repeticao> listar() {
        List<Repeticao> lista = new ArrayList<>();
        for (Path arquivo : arquivos()) {
            String id = idDe(arquivo);
            try {
                lista.add(lerCabecalho(id));
            } catch (RepeticaoCorrompidaException | IOException estragada) {
                LOG.warn("A repetição {} está estragada e ficou fora da lista: {}", arquivo, estragada.getMessage());
            }
        }
        lista.sort(MAIS_RECENTE_PRIMEIRO);
        return lista;
    }

    /**
     * Lê uma repetição inteira, com as jogadas.
     *
     * @param id o nome do arquivo, sem o {@code .txt}
     * @return a repetição, ou vazio se não existe ou o id não tem o formato de uma
     * @throws RepeticaoCorrompidaException se o arquivo existe, mas não pode ser lido
     */
    public synchronized Optional<Repeticao> ler(String id) {
        if (id == null || !ID_VALIDO.matcher(id).matches() || !Files.isRegularFile(arquivo(id))) {
            return Optional.empty();
        }
        try (BufferedReader leitor = Files.newBufferedReader(arquivo(id), StandardCharsets.UTF_8)) {
            Repeticao cabecalho = cabecalho(id, leitor);
            List<Jogada> jogadas = new ArrayList<>();
            String linha;
            int numero = 2;
            while ((linha = leitor.readLine()) != null) {
                numero++;
                if (!linha.isBlank()) {
                    jogadas.add(jogada(linha, numero));
                }
            }
            return Optional.of(new Repeticao(id, cabecalho.data(), cabecalho.dificuldade(), cabecalho.semente(),
                    cabecalho.pontuacao(), jogadas));
        } catch (IOException ilegivel) {
            throw new RepeticaoCorrompidaException("não foi possível ler " + id + ": " + ilegivel.getMessage());
        }
    }

    private Repeticao lerCabecalho(String id) throws IOException {
        try (BufferedReader leitor = Files.newBufferedReader(arquivo(id), StandardCharsets.UTF_8)) {
            return cabecalho(id, leitor);
        }
    }

    private static Repeticao cabecalho(String id, BufferedReader leitor) throws IOException {
        if (!ASSINATURA.equals(leitor.readLine())) {
            throw new RepeticaoCorrompidaException("a primeira linha não é " + ASSINATURA);
        }
        String linha = leitor.readLine();
        String[] campos = linha == null ? new String[0] : linha.split(SEPARADOR, -1);
        if (campos.length != CAMPOS_CABECALHO) {
            throw new RepeticaoCorrompidaException("cabeçalho malformado: " + linha);
        }
        try {
            return new Repeticao(id, LocalDateTime.parse(campos[3].strip()), Dificuldade.valueOf(campos[0].strip()),
                    Long.parseLong(campos[1].strip()), Integer.parseInt(campos[2].strip()), List.of());
        } catch (IllegalArgumentException | DateTimeParseException invalido) {
            // NumberFormatException é uma IllegalArgumentException, como a de dificuldade desconhecida
            throw new RepeticaoCorrompidaException("cabeçalho inválido: " + linha);
        }
    }

    private static Jogada jogada(String linha, int numero) {
        String[] campos = linha.split(SEPARADOR, -1);
        try {
            if (campos.length == CAMPOS_JOGADA) {
                return new Jogada(Long.parseLong(campos[0].strip()), Comando.valueOf(campos[1].strip()));
            }
        } catch (IllegalArgumentException invalida) {
            // cai na exceção abaixo, como uma linha com campos a mais
        }
        throw new RepeticaoCorrompidaException("linha " + numero + " inválida: " + linha);
    }

    /** Duas partidas que terminam no mesmo segundo, como na versão online, não podem se sobrescrever. */
    private String idLivre(LocalDateTime data) {
        String base = FORMATO_ID.format(data);
        String id = base;
        for (int extra = 2; Files.exists(arquivo(id)); extra++) {
            id = base + "-" + extra;
        }
        return id;
    }

    private void apagarAlemDoMaximo() {
        List<Repeticao> lista = listar();
        for (Repeticao velha : lista.subList(Math.min(MAXIMO, lista.size()), lista.size())) {
            try {
                Files.deleteIfExists(arquivo(velha.id()));
            } catch (IOException falha) {
                LOG.warn("Não foi possível apagar a repetição antiga {}: {}", velha.id(), falha.getMessage());
            }
        }
    }

    private List<Path> arquivos() {
        List<Path> arquivos = new ArrayList<>();
        if (!Files.isDirectory(pasta)) {
            return arquivos;
        }
        try (DirectoryStream<Path> itens = Files.newDirectoryStream(pasta, "*" + EXTENSAO)) {
            for (Path item : itens) {
                if (ID_VALIDO.matcher(idDe(item)).matches()) {
                    arquivos.add(item);
                }
            }
        } catch (IOException ilegivel) {
            LOG.warn("Não foi possível listar {}: {}", pasta, ilegivel.getMessage());
        }
        return arquivos;
    }

    private static String idDe(Path arquivo) {
        String nome = arquivo.getFileName().toString();
        return nome.substring(0, nome.length() - EXTENSAO.length());
    }

    private Path arquivo(String id) {
        return pasta.resolve(id + EXTENSAO);
    }
}
