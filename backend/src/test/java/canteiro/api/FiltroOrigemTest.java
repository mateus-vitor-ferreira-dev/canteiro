package canteiro.api;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.WebSocket;
import java.net.http.WebSocketHandshakeException;
import java.nio.file.Path;
import java.util.concurrent.CompletionException;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Confere que só a própria origem do jogo chama a API e abre o WebSocket (RNF13).
 *
 * @author Mateus Vitor Ferreira
 * @version 0.1.0
 */
class FiltroOrigemTest {

    private static final int OK = 200;
    private static final int PROIBIDO = 403;
    private static final long ESPERA_S = 3;

    private final HttpClient cliente = HttpClient.newHttpClient();
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

    @ParameterizedTest
    @CsvSource({
        "http://127.0.0.1:7070, 127.0.0.1:7070, true",
        "https://canteiro.exemplo.com, canteiro.exemplo.com, true",
        "HTTP://LOCALHOST:5173, localhost:5173, true",
        "http://localhost:5173, 127.0.0.1:7070, false",
        "https://outro-site.com, 127.0.0.1:7070, false",
        "http://127.0.0.1:7070.outro-site.com, 127.0.0.1:7070, false",
        "null, 127.0.0.1:7070, false",
        "'http://exemplo com', 127.0.0.1:7070, false",
    })
    void soAceitaAOrigemDoProprioEndereco(String origem, String host, boolean esperado) {
        assertEquals(esperado, FiltroOrigem.permitida(origem, host));
    }

    @Test
    void pedidoSemOrigemPassa() {
        assertTrue(FiltroOrigem.permitida(null, "127.0.0.1:7070"));
    }

    @Test
    void pedidoComOrigemESemHostNaoPassa() {
        assertFalse(FiltroOrigem.permitida("http://127.0.0.1:7070", null));
    }

    @Test
    void apiAceitaAPropriaOrigem() throws IOException, InterruptedException {
        assertEquals(OK, getComOrigem("http://127.0.0.1:" + porta).statusCode());
    }

    @Test
    void apiRecusaOutraOrigem() throws IOException, InterruptedException {
        HttpResponse<String> resposta = getComOrigem("https://outro-site.com");

        assertEquals(PROIBIDO, resposta.statusCode());
        assertTrue(resposta.body().contains("ORIGEM_RECUSADA"));
    }

    @Test
    void webSocketDeOutraOrigemNaoAbre() {
        WebSocket.Builder construtor = cliente.newWebSocketBuilder().header("Origin", "https://outro-site.com");
        URI canal = URI.create("ws://127.0.0.1:" + porta + "/ws/partidas/qualquer");

        CompletionException erro = assertThrows(CompletionException.class,
                () -> construtor.buildAsync(canal, new WebSocket.Listener() { }).join());
        assertInstanceOf(WebSocketHandshakeException.class, erro.getCause());
    }

    @Test
    void webSocketDaPropriaOrigemAbre() throws Exception {
        WebSocket ws = cliente.newWebSocketBuilder()
                .header("Origin", "http://127.0.0.1:" + porta)
                .buildAsync(URI.create("ws://127.0.0.1:" + porta + "/ws/partidas/qualquer"),
                        new WebSocket.Listener() { })
                .get(ESPERA_S, TimeUnit.SECONDS);

        assertNotNull(ws);
        ws.abort();
    }

    private HttpResponse<String> getComOrigem(String origem) throws IOException, InterruptedException {
        URI endereco = URI.create("http://127.0.0.1:" + porta + "/api/dificuldades");
        HttpRequest pedido = HttpRequest.newBuilder(endereco).header("Origin", origem).GET().build();
        return cliente.send(pedido, HttpResponse.BodyHandlers.ofString());
    }
}
