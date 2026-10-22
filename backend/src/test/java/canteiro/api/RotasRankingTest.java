package canteiro.api;

import canteiro.controle.GerenciadorPartidas;
import canteiro.controle.SessaoPartida;
import canteiro.modelo.Comando;
import canteiro.modelo.Dificuldade;
import canteiro.modelo.estruturas.FontePecas;
import canteiro.modelo.materiais.Madeira;
import canteiro.modelo.pecas.Peca;
import canteiro.modelo.pecas.PecaO;
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
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * As rotas do ranking, com o servidor de verdade (RF21, RF22).
 *
 * @author Mateus Vitor Ferreira
 * @version 0.1.0
 */
class RotasRankingTest {

    private static final int PORTA_LIVRE = 0;
    private static final int OK = 200;
    private static final int CRIADO = 201;
    private static final int INVALIDO = 400;
    private static final int CONFLITO = 409;
    private static final int EXPIRADA = 410;
    private static final int LIMITE_DE_PECAS = 100;

    /** Só peças O de madeira, no centro: a pilha sobe sem fechar linha nem desabar. */
    private static final FontePecas SO_O = new FontePecas() {
        @Override
        public Peca proxima() {
            return new PecaO(new Madeira());
        }

        @Override
        public List<Peca> espiar(int quantidade) {
            return List.of();
        }
    };

    @TempDir
    Path pasta;

    private final HttpClient cliente = HttpClient.newHttpClient();
    private final ObjectMapper json = new ObjectMapper();
    private GerenciadorPartidas partidas;
    private ServidorWeb servidor;
    private int porta;

    @BeforeEach
    void subirServidor() {
        partidas = new GerenciadorPartidas(d -> SO_O);
        servidor = new ServidorWeb(partidas, ServidoresDeTeste.arquivos(pasta));
        porta = servidor.iniciar(PORTA_LIVRE);
    }

    @AfterEach
    void pararServidor() {
        servidor.parar();
    }

    @Test
    void semPartidasORankingEstaVazio() throws IOException, InterruptedException {
        HttpResponse<String> resposta = get();

        assertEquals(OK, resposta.statusCode());
        assertEquals(0, json.readTree(resposta.body()).size());
    }

    @Test
    void partidaEncerradaEntraComAPontuacaoLidaDoServidor() throws IOException, InterruptedException {
        SessaoPartida sessao = partidaEncerrada();

        HttpResponse<String> registro = post(sessao.id(), "  Ana  ");

        assertEquals(CRIADO, registro.statusCode());
        assertEquals(1, json.readTree(registro.body()).get("posicao").asInt());
        JsonNode primeira = json.readTree(get().body()).get(0);
        assertEquals("Ana", primeira.get("nome").asText(), "os espaços das pontas saem");
        assertEquals(sessao.motor().placar().pontuacao(), primeira.get("pontuacao").asInt());
        assertEquals(sessao.motor().placar().nivel(), primeira.get("nivel").asInt());
        assertEquals(LocalDate.now().toString(), primeira.get("data").asText());
    }

    @Test
    void aMesmaPartidaNaoEntraDuasVezes() throws IOException, InterruptedException {
        SessaoPartida sessao = partidaEncerrada();
        post(sessao.id(), "Ana");

        HttpResponse<String> deNovo = post(sessao.id(), "Bia");

        assertEquals(CONFLITO, deNovo.statusCode());
        assertEquals("PARTIDA_JA_REGISTRADA", erro(deNovo));
    }

    @Test
    void partidaEmAndamentoNaoEntra() throws IOException, InterruptedException {
        SessaoPartida sessao = partidas.criar(Dificuldade.FACIL);
        sessao.motor().avancarCiclo();

        HttpResponse<String> resposta = post(sessao.id(), "Ana");

        assertEquals(CONFLITO, resposta.statusCode());
        assertEquals("PARTIDA_EM_ANDAMENTO", erro(resposta));
    }

    @Test
    void partidaQueNaoExisteMaisDa410() throws IOException, InterruptedException {
        HttpResponse<String> resposta = post("naoexiste", "Ana");

        assertEquals(EXPIRADA, resposta.statusCode());
        assertEquals("PARTIDA_EXPIRADA", erro(resposta));
    }

    @Test
    void nomeInvalidoOuCorpoMalformadoDa400() throws IOException, InterruptedException {
        String id = partidaEncerrada().id();

        assertEquals(INVALIDO, post(id, "   ").statusCode());
        assertEquals(INVALIDO, post(id, "Um nome comprido demais").statusCode());
        assertEquals(INVALIDO, enviar("{ partidaId").statusCode());
        assertEquals("NOME_INVALIDO", erro(post(id, "Ana;Bia")));
        assertTrue(json.readTree(get().body()).isEmpty(), "nada entrou no ranking");
    }

    @Test
    void asRotasAparecemNaDocumentacao() throws IOException, InterruptedException {
        HttpResponse<String> resposta = cliente.send(HttpRequest.newBuilder(endereco(DocumentacaoApi.CAMINHO_ESPECIFICACAO))
                .GET().build(), HttpResponse.BodyHandlers.ofString());

        JsonNode ranking = json.readTree(resposta.body()).at("/paths/~1api~1ranking");
        assertEquals("listarRanking", ranking.at("/get/operationId").asText());
        assertEquals("registrarNoRanking", ranking.at("/post/operationId").asText());
    }

    /** Derruba peças até a pilha chegar ao topo, sem o laço rodando: o teste comanda o motor. */
    private SessaoPartida partidaEncerrada() {
        SessaoPartida sessao = partidas.criar(Dificuldade.FACIL);
        for (int i = 0; i < LIMITE_DE_PECAS && !sessao.motor().estado().encerrada(); i++) {
            sessao.motor().avancarCiclo();
            sessao.motor().aplicar(Comando.QUEDA_INSTANTANEA);
        }
        assertTrue(sessao.motor().estado().encerrada(), "a partida deveria ter acabado");
        return sessao;
    }

    private String erro(HttpResponse<String> resposta) throws IOException {
        return json.readTree(resposta.body()).get("erro").asText();
    }

    private HttpResponse<String> get() throws IOException, InterruptedException {
        return cliente.send(HttpRequest.newBuilder(endereco("/api/ranking")).GET().build(),
                HttpResponse.BodyHandlers.ofString());
    }

    private HttpResponse<String> post(String partidaId, String nome) throws IOException, InterruptedException {
        return enviar(json.writeValueAsString(Map.of("partidaId", partidaId, "nome", nome)));
    }

    private HttpResponse<String> enviar(String corpo) throws IOException, InterruptedException {
        HttpRequest pedido = HttpRequest.newBuilder(endereco("/api/ranking"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(corpo))
                .build();
        return cliente.send(pedido, HttpResponse.BodyHandlers.ofString());
    }

    private URI endereco(String caminho) {
        return URI.create("http://" + ServidorWeb.HOST + ":" + porta + caminho);
    }
}
