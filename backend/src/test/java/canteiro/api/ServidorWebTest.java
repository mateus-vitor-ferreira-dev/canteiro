package canteiro.api;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.ConnectException;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.NetworkInterface;
import java.net.Socket;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

/**
 * Sobe o servidor de verdade, numa porta livre, e faz requisições HTTP a ele.
 *
 * @author Mateus Vitor Ferreira
 * @version 0.1.0
 */
class ServidorWebTest {

    private static final int PORTA_LIVRE = 0;
    private static final int OK = 200;
    private static final int TEMPO_LIMITE_MS = 1000;

    private final HttpClient cliente = HttpClient.newHttpClient();
    private final ObjectMapper json = new ObjectMapper();
    private ServidorWeb servidor;
    private int porta;

    @BeforeEach
    void subirServidor() {
        servidor = new ServidorWeb();
        porta = servidor.iniciar(PORTA_LIVRE);
    }

    @AfterEach
    void pararServidor() {
        servidor.parar();
    }

    @Test
    void listaAsTresDificuldadesEmJson() throws IOException, InterruptedException {
        HttpResponse<String> resposta = get("/api/dificuldades");

        assertEquals(OK, resposta.statusCode());
        assertTrue(resposta.headers().firstValue("Content-Type").orElse("").startsWith("application/json"));
        JsonNode dificuldades = json.readTree(resposta.body());
        assertEquals(3, dificuldades.size());
        JsonNode normal = dificuldades.get(1);
        assertEquals("NORMAL", normal.get("codigo").asText());
        assertEquals(650, normal.get("intervaloQuedaMs").asLong());
        assertEquals(2.5, normal.get("limiteDesvio").asDouble());
        assertEquals(3, normal.get("materiaisLiberados").size());
    }

    @Test
    void publicaAEspecificacaoOpenApiComAsRotas() throws IOException, InterruptedException {
        HttpResponse<String> resposta = get(DocumentacaoApi.CAMINHO_ESPECIFICACAO);

        assertEquals(OK, resposta.statusCode());
        JsonNode especificacao = json.readTree(resposta.body());
        assertEquals("CANTEIRO — API", especificacao.at("/info/title").asText());
        assertEquals("listarDificuldades",
                especificacao.at("/paths/~1api~1dificuldades/get/operationId").asText());
    }

    @Test
    void mostraATelaDoSwagger() throws IOException, InterruptedException {
        HttpResponse<String> resposta = get(DocumentacaoApi.CAMINHO_SWAGGER);

        assertEquals(OK, resposta.statusCode());
        assertTrue(resposta.body().contains("swagger-ui"), "a página deveria carregar o Swagger UI");
    }

    @Test
    void recusaConexaoPeloEnderecoDeRedeDaMaquina() throws IOException {
        Optional<InetAddress> enderecoDeRede = enderecoDeRede();
        assumeTrue(enderecoDeRede.isPresent(), "a máquina não tem endereço de rede além do local");

        try (Socket socket = new Socket()) {
            InetSocketAddress destino = new InetSocketAddress(enderecoDeRede.get(), porta);
            assertThrows(ConnectException.class, () -> socket.connect(destino, TEMPO_LIMITE_MS),
                    "o servidor deveria escutar só em 127.0.0.1 (RNF13)");
        }
    }

    private static Optional<InetAddress> enderecoDeRede() throws IOException {
        return NetworkInterface.networkInterfaces()
                .flatMap(NetworkInterface::inetAddresses)
                .filter(endereco -> endereco instanceof Inet4Address && !endereco.isLoopbackAddress())
                .findFirst();
    }

    private HttpResponse<String> get(String caminho) throws IOException, InterruptedException {
        URI endereco = URI.create("http://" + ServidorWeb.HOST + ":" + porta + caminho);
        return cliente.send(HttpRequest.newBuilder(endereco).GET().build(), HttpResponse.BodyHandlers.ofString());
    }
}
