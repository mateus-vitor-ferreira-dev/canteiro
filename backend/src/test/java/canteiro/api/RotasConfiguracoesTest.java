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
 * As rotas {@code GET} e {@code PUT /api/configuracoes}, com o servidor de
 * verdade e o arquivo numa pasta temporária (RF27).
 *
 * @author Mateus Vitor Ferreira
 * @version 0.1.0
 */
class RotasConfiguracoesTest {

    private static final String CAMINHO = "/api/configuracoes";
    private static final int PORTA_LIVRE = 0;
    private static final int OK = 200;
    private static final int SEM_CONTEUDO = 204;
    private static final int PEDIDO_INVALIDO = 400;
    private static final int NAO_ENCONTRADO = 404;
    private static final int VOLUME_PADRAO = 60;

    private final HttpClient cliente = HttpClient.newHttpClient();
    private final ObjectMapper json = new ObjectMapper();
    @TempDir
    Path pasta;

    private ServidorWeb servidor;
    private int porta;

    @BeforeEach
    void subirServidor() {
        servidor = new ServidorWeb(ServidoresDeTeste.gerenciador(), ServidoresDeTeste.arquivos(pasta));
        porta = servidor.iniciar(PORTA_LIVRE);
    }

    @AfterEach
    void pararServidor() {
        servidor.parar();
    }

    @Test
    void semNadaSalvoDevolveOPadrao() throws IOException, InterruptedException {
        HttpResponse<String> resposta = get(porta);

        assertEquals(OK, resposta.statusCode());
        JsonNode corpo = json.readTree(resposta.body());
        assertEquals(0, corpo.get("teclas").size());
        assertEquals(VOLUME_PADRAO, corpo.get("volume").asInt());
    }

    @Test
    void salvaEDevolveOQueFoiSalvo() throws IOException, InterruptedException {
        String enviado = "{\"teclas\": {\"ESQUERDA\": [\"KeyA\"], \"PAUSA\": [\"KeyP\", \"Escape\"]}, \"volume\": 35}";

        assertEquals(SEM_CONTEUDO, put(enviado).statusCode());
        JsonNode lido = json.readTree(get(porta).body());
        assertEquals(json.readTree(enviado), lido);
    }

    @Test
    void recusaVolumeForaDaFaixa() throws IOException, InterruptedException {
        assertRecusada(put("{\"teclas\": {}, \"volume\": 150}"));
    }

    @Test
    void recusaAcaoInventada() throws IOException, InterruptedException {
        assertRecusada(put("{\"teclas\": {\"VOAR\": [\"KeyV\"]}, \"volume\": 60}"));
    }

    @Test
    void recusaTeclaQueJaEDeOutraAcao() throws IOException, InterruptedException {
        assertRecusada(put("{\"teclas\": {\"ESQUERDA\": [\"KeyC\"]}, \"volume\": 60}"));
    }

    @Test
    void recusaJsonQuebradoEContinuaRespondendo() throws IOException, InterruptedException {
        assertRecusada(put("{ teclas"));
        assertEquals(OK, get(porta).statusCode());
    }

    @Test
    void pedidoRecusadoNaoMudaOQueEstavaSalvo() throws IOException, InterruptedException {
        put("{\"teclas\": {\"DESCER\": [\"KeyS\"]}, \"volume\": 20}");
        put("{\"teclas\": {}, \"volume\": 150}");

        JsonNode lido = json.readTree(get(porta).body());
        assertEquals("KeyS", lido.at("/teclas/DESCER/0").asText());
        assertEquals(20, lido.get("volume").asInt());
    }

    @Test
    void asDuasRotasEstaoNaDocumentacao() throws IOException, InterruptedException {
        URI endereco = URI.create("http://" + ServidorWeb.HOST + ":" + porta + DocumentacaoApi.CAMINHO_ESPECIFICACAO);
        JsonNode especificacao = json.readTree(cliente.send(HttpRequest.newBuilder(endereco).GET().build(),
                HttpResponse.BodyHandlers.ofString()).body());

        assertEquals("lerConfiguracoes", especificacao.at("/paths/~1api~1configuracoes/get/operationId").asText());
        assertEquals("salvarConfiguracoes",
                especificacao.at("/paths/~1api~1configuracoes/put/operationId").asText());
    }

    @Test
    void servidorCompartilhadoNaoTemConfiguracoes() throws IOException, InterruptedException {
        ServidorWeb compartilhado = new ServidorWeb(ServidoresDeTeste.gerenciador(),
                ServidoresDeTeste.arquivos(pasta), true);
        int portaCompartilhado = compartilhado.iniciar(PORTA_LIVRE);
        try {
            assertEquals(NAO_ENCONTRADO, get(portaCompartilhado).statusCode());
        } finally {
            compartilhado.parar();
        }
    }

    private void assertRecusada(HttpResponse<String> resposta) throws IOException {
        assertEquals(PEDIDO_INVALIDO, resposta.statusCode());
        JsonNode corpo = json.readTree(resposta.body());
        assertEquals("CONFIGURACAO_INVALIDA", corpo.get("erro").asText());
        assertTrue(!corpo.get("mensagem").asText().isBlank(), "a tela mostra a mensagem ao jogador");
    }

    private HttpResponse<String> get(int portaDoServidor) throws IOException, InterruptedException {
        URI endereco = URI.create("http://" + ServidorWeb.HOST + ":" + portaDoServidor + CAMINHO);
        return cliente.send(HttpRequest.newBuilder(endereco).GET().build(), HttpResponse.BodyHandlers.ofString());
    }

    private HttpResponse<String> put(String corpo) throws IOException, InterruptedException {
        URI endereco = URI.create("http://" + ServidorWeb.HOST + ":" + porta + CAMINHO);
        HttpRequest pedido = HttpRequest.newBuilder(endereco)
                .header("Content-Type", "application/json")
                .PUT(HttpRequest.BodyPublishers.ofString(corpo))
                .build();
        return cliente.send(pedido, HttpResponse.BodyHandlers.ofString());
    }
}
