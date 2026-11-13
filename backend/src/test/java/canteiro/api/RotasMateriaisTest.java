package canteiro.api;

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
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Sobe o servidor e confere o catálogo de materiais em {@code GET /api/materiais} (RF04).
 *
 * @author Mateus Vitor Ferreira
 * @version 0.1.0
 */
class RotasMateriaisTest {

    private static final int OK = 200;
    private static final double DELTA = 1e-9;

    private final HttpClient cliente = HttpClient.newHttpClient();
    private final ObjectMapper json = new ObjectMapper();
    @TempDir
    Path pasta;

    private ServidorWeb servidor;
    private int porta;

    @BeforeEach
    void subirServidor() {
        servidor = new ServidorWeb(ServidoresDeTeste.gerenciador(), ServidoresDeTeste.arquivos(pasta));
        porta = servidor.iniciar(0);
    }

    @AfterEach
    void pararServidor() {
        servidor.parar();
    }

    @Test
    void listaOsQuatroMateriaisDoMaisLeveAoMaisPesado() throws IOException, InterruptedException {
        HttpResponse<String> resposta = get("/api/materiais");

        assertEquals(OK, resposta.statusCode());
        JsonNode materiais = json.readTree(resposta.body());
        assertEquals(4, materiais.size());
        assertEquals("MADEIRA", materiais.get(0).get("codigo").asText());
        assertEquals("ACO", materiais.get(3).get("codigo").asText());
    }

    @Test
    void cadaMaterialTrazNomeDensidadeCorBonusECarga() throws IOException, InterruptedException {
        JsonNode concreto = json.readTree(get("/api/materiais").body()).get(2);

        assertEquals("CONCRETO", concreto.get("codigo").asText());
        assertTrue(!concreto.get("nome").asText().isBlank());
        assertEquals(2.4, concreto.get("densidade").asDouble(), DELTA);
        assertEquals("#9EA7B1", concreto.get("cor").asText());
        assertEquals(30, concreto.get("bonusLinha").asInt());
        assertEquals(40.0, concreto.get("cargaMaxima").asDouble(), DELTA);
    }

    @Test
    void oAcoNaoTemCargaMaxima() throws IOException, InterruptedException {
        JsonNode aco = json.readTree(get("/api/materiais").body()).get(3);

        assertTrue(aco.get("cargaMaxima").isNull());
    }

    @Test
    void aRotaApareceNaDocumentacao() throws IOException, InterruptedException {
        JsonNode especificacao = json.readTree(get(DocumentacaoApi.CAMINHO_ESPECIFICACAO).body());

        assertEquals("listarMateriais", especificacao.at("/paths/~1api~1materiais/get/operationId").asText());
    }

    private HttpResponse<String> get(String caminho) throws IOException, InterruptedException {
        URI endereco = URI.create("http://" + ServidorWeb.HOST + ":" + porta + caminho);
        return cliente.send(HttpRequest.newBuilder(endereco).GET().build(), HttpResponse.BodyHandlers.ofString());
    }
}
