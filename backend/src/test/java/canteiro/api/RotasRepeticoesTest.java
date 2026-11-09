package canteiro.api;

import canteiro.controle.GerenciadorPartidas;
import canteiro.modelo.Comando;
import canteiro.modelo.Dificuldade;
import canteiro.modelo.estruturas.Jogada;
import canteiro.persistencia.ArquivoRepeticoes;
import canteiro.persistencia.ArquivosDoJogo;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * As rotas das partidas gravadas, com o servidor de verdade e a pasta temporária (RF24).
 *
 * @author Mateus Vitor Ferreira
 * @version 0.1.0
 */
class RotasRepeticoesTest {

    private static final int PORTA_LIVRE = 0;
    private static final int OK = 200;
    private static final int CRIADO = 201;
    private static final int NAO_ENCONTRADO = 404;
    private static final int ESTRAGADO = 422;
    private static final LocalDateTime FIM = LocalDateTime.of(2026, 9, 28, 14, 32, 5);
    private static final LocalDateTime FIM_NO_MINUTO_CHEIO = LocalDateTime.of(2026, 9, 28, 14, 33, 0);

    private final HttpClient cliente = HttpClient.newHttpClient();
    private final ObjectMapper json = new ObjectMapper();
    @TempDir
    Path pasta;

    private GerenciadorPartidas partidas;
    private ArquivoRepeticoes repeticoes;
    private ServidorWeb servidor;
    private int porta;

    @BeforeEach
    void subirServidor() {
        partidas = ServidoresDeTeste.gerenciador();
        ArquivosDoJogo arquivos = ServidoresDeTeste.arquivos(pasta);
        repeticoes = arquivos.repeticoes();
        servidor = new ServidorWeb(partidas, arquivos);
        porta = servidor.iniciar(PORTA_LIVRE);
    }

    @AfterEach
    void pararServidor() {
        servidor.parar();
    }

    @Test
    void semGravacaoAListaEVazia() throws IOException, InterruptedException {
        HttpResponse<String> resposta = enviar("GET", "/api/repeticoes");

        assertEquals(OK, resposta.statusCode());
        assertEquals(0, json.readTree(resposta.body()).size());
    }

    @Test
    void listaComOsCamposQueATelaUsa() throws IOException, InterruptedException {
        repeticoes.gravar(Dificuldade.NORMAL, 1, 12_340, List.of(new Jogada(1, Comando.ESQUERDA)), FIM);

        JsonNode primeira = json.readTree(enviar("GET", "/api/repeticoes").body()).get(0);

        assertEquals("2026-09-28_14-32-05", primeira.get("id").asText());
        assertEquals("2026-09-28T14:32:05", primeira.get("data").asText());
        assertEquals("NORMAL", primeira.get("dificuldade").asText());
        assertEquals(12_340, primeira.get("pontuacao").asInt());
    }

    @Test
    void aDataTemSempreOsSegundos() throws IOException, InterruptedException {
        repeticoes.gravar(Dificuldade.NORMAL, 1, 0, List.of(), FIM_NO_MINUTO_CHEIO);

        JsonNode primeira = json.readTree(enviar("GET", "/api/repeticoes").body()).get(0);

        assertEquals("2026-09-28T14:33:00", primeira.get("data").asText());
    }

    @Test
    void reproduzirCriaUmaSessaoDeReproducao() throws IOException, InterruptedException {
        String gravada = repeticoes.gravar(Dificuldade.FACIL, 1, 0, List.of(new Jogada(1, Comando.ESQUERDA)), FIM);

        HttpResponse<String> resposta = enviar("POST", "/api/repeticoes/" + gravada + "/reproduzir");

        assertEquals(CRIADO, resposta.statusCode());
        String id = json.readTree(resposta.body()).get("id").asText();
        assertTrue(partidas.buscar(id).orElseThrow().reproducao());
    }

    @Test
    void gravacaoInexistenteDa404() throws IOException, InterruptedException {
        HttpResponse<String> resposta = enviar("POST", "/api/repeticoes/2026-01-01_00-00-00/reproduzir");

        assertEquals(NAO_ENCONTRADO, resposta.statusCode());
        assertEquals("REPETICAO_NAO_ENCONTRADA", json.readTree(resposta.body()).get("erro").asText());
        assertEquals(NAO_ENCONTRADO, enviar("POST", "/api/repeticoes/..%2Franking/reproduzir").statusCode());
    }

    @Test
    void gravacaoEstragadaDaErroComMensagem() throws IOException, InterruptedException {
        Path pastaRepeticoes = Files.createDirectories(pasta.resolve(ArquivoRepeticoes.PASTA));
        Files.writeString(pastaRepeticoes.resolve("2026-01-01_00-00-00.txt"), "lixo");

        HttpResponse<String> resposta = enviar("POST", "/api/repeticoes/2026-01-01_00-00-00/reproduzir");

        assertEquals(ESTRAGADO, resposta.statusCode());
        JsonNode erro = json.readTree(resposta.body());
        assertEquals("REPETICAO_CORROMPIDA", erro.get("erro").asText());
        assertTrue(!erro.get("mensagem").asText().isBlank());
        assertEquals(OK, enviar("GET", "/api/repeticoes").statusCode(), "o servidor continua respondendo");
    }

    @Test
    void asRotasAparecemNaDocumentacao() throws IOException, InterruptedException {
        JsonNode caminhos = json.readTree(enviar("GET", DocumentacaoApi.CAMINHO_ESPECIFICACAO).body()).get("paths");

        assertEquals("listarRepeticoes", caminhos.at("/~1api~1repeticoes/get/operationId").asText());
        assertEquals("reproduzirRepeticao",
                caminhos.at("/~1api~1repeticoes~1{id}~1reproduzir/post/operationId").asText());
    }

    private HttpResponse<String> enviar(String metodo, String caminho) throws IOException, InterruptedException {
        HttpRequest pedido = HttpRequest.newBuilder(URI.create("http://" + ServidorWeb.HOST + ":" + porta + caminho))
                .method(metodo, HttpRequest.BodyPublishers.noBody()).build();
        return cliente.send(pedido, HttpResponse.BodyHandlers.ofString());
    }
}
