package canteiro.api;

import canteiro.api.dto.EstadoDto;
import canteiro.api.dto.EventoDto;
import canteiro.modelo.Celula;
import canteiro.modelo.Dificuldade;
import canteiro.modelo.EventoPartida;
import canteiro.modelo.MotorJogo;
import canteiro.modelo.Queda;
import canteiro.modelo.estruturas.FonteSimples;
import canteiro.modelo.materiais.CatalogoMateriais;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Confere o JSON do protocolo contra exemplos fixos. Se um nome de campo
 * mudar aqui, precisa mudar em {@code frontend/src/api/protocolo.ts} no mesmo PR.
 *
 * @author Mateus Vitor Ferreira
 * @version 0.1.0
 */
class ProtocoloJsonTest {

    private static final int LIMITE_BYTES = 4096;
    private final ObjectMapper json = new ObjectMapper();

    @Test
    void eventoDeLinhasTemSoAsLinhas() throws Exception {
        String gerado = json.writeValueAsString(EventoDto.de(
                new EventoPartida(EventoPartida.Tipo.LINHAS_ELIMINADAS, List.of(20, 21))));
        assertEquals("{\"tipo\":\"EVENTO\",\"evento\":\"LINHAS_ELIMINADAS\",\"dados\":{\"linhas\":[20,21]}}", gerado);
    }

    @Test
    void eventoDeColapsoTrazAsQuedas() throws Exception {
        Queda queda = new Queda(new Celula(15, 5), new Celula(21, 5));
        String gerado = json.writeValueAsString(EventoDto.de(
                new EventoPartida(EventoPartida.Tipo.COLAPSO, List.of(), List.of(queda))));
        assertEquals("{\"tipo\":\"EVENTO\",\"evento\":\"COLAPSO\",\"dados\":"
                + "{\"quedas\":[{\"origem\":[15,5],\"destino\":[21,5]}]}}", gerado);
    }

    @Test
    void eventoDeErroTrazAMensagem() throws Exception {
        assertEquals("{\"tipo\":\"EVENTO\",\"evento\":\"ERRO\",\"dados\":{\"mensagem\":\"x\"}}",
                json.writeValueAsString(EventoDto.erro("x")));
    }

    @Test
    void estadoTemOsCamposDoProtocolo() throws Exception {
        JsonNode estado = json.readTree(json.writeValueAsString(EstadoDto.de(motorComUmaPeca())));

        assertEquals(List.of("tipo", "ciclo", "estado", "tabuleiro", "pecaAtual", "pecaFantasma", "proximas",
                "placar", "estabilidade"), campos(estado));
        assertEquals(List.of("forma", "material", "blocos"), campos(estado.get("pecaAtual")));
        assertEquals(List.of("pontuacao", "linhas", "nivel", "colapsos", "tempoSegundos"), campos(estado.get("placar")));
        assertEquals(List.of("indice", "desvio", "limite", "centroDeMassa", "eixo", "alerta"),
                campos(estado.get("estabilidade")));
        assertEquals(22, estado.get("tabuleiro").size());
        assertEquals(10, estado.get("tabuleiro").get(0).size());
        assertEquals(3, estado.get("proximas").size());
        assertEquals(4, estado.at("/proximas/0/blocos").size(), "as próximas vêm com a forma, para a tela desenhar");
        assertEquals(4, estado.get("pecaFantasma").size());
    }

    @Test
    void mensagemDeEstadoCabeEmQuatroKilobytes() throws Exception {
        MotorJogo motor = motorComUmaPeca();
        for (int i = 0; i < 6; i++) {
            motor.aplicar(canteiro.modelo.Comando.QUEDA_INSTANTANEA);
        }
        byte[] bytes = json.writeValueAsString(EstadoDto.de(motor)).getBytes(StandardCharsets.UTF_8);
        assertTrue(bytes.length <= LIMITE_BYTES, "a mensagem tem " + bytes.length + " bytes");
    }

    private static MotorJogo motorComUmaPeca() {
        MotorJogo motor = new MotorJogo(Dificuldade.DIFICIL,
                new FonteSimples(3, CatalogoMateriais.padrao(), Dificuldade.DIFICIL.materiaisLiberados()));
        motor.avancarCiclo();
        return motor;
    }

    private static List<String> campos(JsonNode no) {
        List<String> nomes = new ArrayList<>();
        no.fieldNames().forEachRemaining(nomes::add);
        return nomes;
    }
}
