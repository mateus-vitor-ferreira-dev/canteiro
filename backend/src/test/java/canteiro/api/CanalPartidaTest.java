package canteiro.api;

import canteiro.controle.GerenciadorPartidas;
import canteiro.modelo.EstadoPartida;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.WebSocket;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.function.Predicate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

/**
 * Cria partidas pela rota REST e joga pelo WebSocket, com servidor de verdade.
 *
 * @author Mateus Vitor Ferreira
 * @version 0.1.0
 */
class CanalPartidaTest {

    private static final int PORTA_LIVRE = 0;
    private static final long ESPERA_MS = 3000;

    private final HttpClient cliente = HttpClient.newHttpClient();
    private final ObjectMapper json = new ObjectMapper();
    private GerenciadorPartidas partidas;
    private ServidorWeb servidor;
    private int porta;

    @BeforeEach
    void subirServidor() {
        partidas = ServidoresDeTeste.gerenciador();
        servidor = new ServidorWeb(partidas);
        porta = servidor.iniciar(PORTA_LIVRE);
    }

    @AfterEach
    void pararServidor() {
        servidor.parar();
    }

    @Test
    void criaPartidaEDevolveOId() throws Exception {
        HttpResponse<String> resposta = post("{\"dificuldade\": \"NORMAL\"}");
        assertEquals(201, resposta.statusCode());
        String id = json.readTree(resposta.body()).get("id").asText();
        assertTrue(partidas.buscar(id).isPresent());
    }

    @Test
    void dificuldadeInvalidaEErro400() throws Exception {
        for (String corpo : new String[] {"{\"dificuldade\": \"IMPOSSIVEL\"}", "{}", "isso não é json"}) {
            HttpResponse<String> resposta = post(corpo);
            assertEquals(400, resposta.statusCode(), corpo);
            assertEquals("DIFICULDADE_INVALIDA", json.readTree(resposta.body()).get("erro").asText());
        }
    }

    @Test
    void recebeOEstadoEAplicaComandos() throws Exception {
        Conexao conexao = conectar(criarPartida());
        JsonNode caindo = conexao.esperar(m -> "PECA_CAINDO".equals(m.path("estado").asText()));
        int coluna = caindo.at("/pecaAtual/blocos/0/1").asInt();

        conexao.enviar("{\"tipo\": \"COMANDO\", \"comando\": \"ESQUERDA\"}");

        conexao.esperar(m -> m.at("/pecaAtual/blocos/0/1").asInt(-1) == coluna - 1);
        conexao.fechar();
    }

    @Test
    void mensagemInvalidaViraErroEAPartidaContinua() throws Exception {
        Conexao conexao = conectar(criarPartida());
        conexao.esperar(m -> "PECA_CAINDO".equals(m.path("estado").asText()));

        conexao.enviar("isso não é json");
        JsonNode erro = conexao.esperar(m -> "ERRO".equals(m.path("evento").asText()));
        assertTrue(erro.at("/dados/mensagem").asText().contains("Mensagem inválida"));

        conexao.enviar("{\"tipo\": \"COMANDO\", \"comando\": \"VOAR\"}");
        conexao.esperar(m -> "ERRO".equals(m.path("evento").asText()));

        conexao.enviar("{\"tipo\": \"COMANDO\", \"comando\": \"PAUSAR\"}");
        conexao.esperar(m -> "PAUSA".equals(m.path("estado").asText()));
        conexao.fechar();
    }

    @Test
    void fecharAConexaoPausaAPartida() throws Exception {
        String id = criarPartida();
        Conexao conexao = conectar(id);
        conexao.esperar(m -> "PECA_CAINDO".equals(m.path("estado").asText()));

        conexao.fechar();

        long limite = System.currentTimeMillis() + ESPERA_MS;
        while (partidas.buscar(id).orElseThrow().motor().estado() != EstadoPartida.PAUSA) {
            if (System.currentTimeMillis() > limite) {
                fail("a partida não pausou depois que a conexão fechou");
            }
            Thread.sleep(20);
        }
    }

    @Test
    void partidaInexistenteERecusada() throws Exception {
        Conexao conexao = conectar("naoexiste");
        JsonNode erro = conexao.esperar(m -> "ERRO".equals(m.path("evento").asText()));
        assertTrue(erro.at("/dados/mensagem").asText().contains("não encontrada"));
        assertTrue(conexao.fechada.get(ESPERA_MS, TimeUnit.MILLISECONDS) > 1000, "fechou com código de erro");
    }

    private String criarPartida() throws Exception {
        return json.readTree(post("{\"dificuldade\": \"FACIL\"}").body()).get("id").asText();
    }

    private HttpResponse<String> post(String corpo) throws IOException, InterruptedException {
        HttpRequest pedido = HttpRequest.newBuilder(URI.create("http://127.0.0.1:" + porta + "/api/partidas"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(corpo)).build();
        return cliente.send(pedido, HttpResponse.BodyHandlers.ofString());
    }

    private Conexao conectar(String id) throws Exception {
        Conexao conexao = new Conexao();
        conexao.ws = cliente.newWebSocketBuilder()
                .buildAsync(URI.create("ws://127.0.0.1:" + porta + "/ws/partidas/" + id), conexao)
                .get(ESPERA_MS, TimeUnit.MILLISECONDS);
        return conexao;
    }

    /** Cliente WebSocket que junta as mensagens recebidas numa fila. */
    private final class Conexao implements WebSocket.Listener {
        private final BlockingQueue<JsonNode> recebidas = new LinkedBlockingQueue<>();
        private final CompletableFuture<Integer> fechada = new CompletableFuture<>();
        private final StringBuilder parcial = new StringBuilder();
        private WebSocket ws;

        @Override
        public CompletionStage<?> onText(WebSocket webSocket, CharSequence dados, boolean ultima) {
            parcial.append(dados);
            if (ultima) {
                try {
                    recebidas.add(json.readTree(parcial.toString()));
                } catch (IOException e) {
                    fail("o servidor mandou um JSON inválido: " + parcial);
                }
                parcial.setLength(0);
            }
            webSocket.request(1);
            return null;
        }

        @Override
        public CompletionStage<?> onClose(WebSocket webSocket, int codigo, String motivo) {
            fechada.complete(codigo);
            return null;
        }

        JsonNode esperar(Predicate<JsonNode> condicao) throws InterruptedException {
            long limite = System.currentTimeMillis() + ESPERA_MS;
            while (System.currentTimeMillis() < limite) {
                JsonNode mensagem = recebidas.poll(limite - System.currentTimeMillis(), TimeUnit.MILLISECONDS);
                if (mensagem != null && condicao.test(mensagem)) {
                    return mensagem;
                }
            }
            return fail("a mensagem esperada não chegou");
        }

        void enviar(String texto) throws Exception {
            ws.sendText(texto, true).get(ESPERA_MS, TimeUnit.MILLISECONDS);
        }

        void fechar() throws Exception {
            ws.sendClose(WebSocket.NORMAL_CLOSURE, "fim").get(ESPERA_MS, TimeUnit.MILLISECONDS);
        }
    }
}
