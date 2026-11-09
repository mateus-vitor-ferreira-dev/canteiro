package canteiro.api.rotas;

import canteiro.api.dto.ConfiguracoesDto;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Confere as regras das configurações sem subir servidor (RF27).
 *
 * @author Mateus Vitor Ferreira
 * @version 0.1.0
 */
class ValidacaoConfiguracoesTest {

    private static final int VOLUME = 60;

    @Test
    void semTeclaTrocadaEValido() {
        assertEquals(Optional.empty(), ValidacaoConfiguracoes.problema(new ConfiguracoesDto(Map.of(), VOLUME)));
    }

    @Test
    void teclasNulasValemComoNenhuma() {
        assertEquals(Optional.empty(), ValidacaoConfiguracoes.problema(new ConfiguracoesDto(null, VOLUME)));
    }

    @Test
    void trocaQueNaoRepeteTeclaEValida() {
        ConfiguracoesDto dto = new ConfiguracoesDto(Map.of(
                "ESQUERDA", List.of("KeyA"), "PAUSA", List.of("KeyP", "Escape")), VOLUME);

        assertEquals(Optional.empty(), ValidacaoConfiguracoes.problema(dto));
    }

    @Test
    void trocarDuasAcoesEntreSiEValido() {
        ConfiguracoesDto dto = new ConfiguracoesDto(Map.of(
                "ESQUERDA", List.of("KeyC"), "RESERVAR", List.of("ArrowLeft")), VOLUME);

        assertEquals(Optional.empty(), ValidacaoConfiguracoes.problema(dto));
    }

    @Test
    void recusaVolumeForaDaFaixa() {
        assertTrue(ValidacaoConfiguracoes.problema(new ConfiguracoesDto(Map.of(), 150)).isPresent());
        assertTrue(ValidacaoConfiguracoes.problema(new ConfiguracoesDto(Map.of(), -1)).isPresent());
    }

    @Test
    void recusaAcaoInventada() {
        Optional<String> problema = ValidacaoConfiguracoes.problema(
                new ConfiguracoesDto(Map.of("VOAR", List.of("KeyV")), VOLUME));

        assertTrue(problema.orElseThrow().contains("VOAR"));
    }

    @Test
    void recusaAcaoSemTecla() {
        assertTrue(ValidacaoConfiguracoes.problema(
                new ConfiguracoesDto(Map.of("DESCER", List.of()), VOLUME)).isPresent());
        Map<String, List<String>> nula = new HashMap<>();
        nula.put("DESCER", null);
        assertTrue(ValidacaoConfiguracoes.problema(new ConfiguracoesDto(nula, VOLUME)).isPresent());
    }

    @Test
    void recusaTeclaQueJaEDeOutraAcaoPadrao() {
        Optional<String> problema = ValidacaoConfiguracoes.problema(
                new ConfiguracoesDto(Map.of("ESQUERDA", List.of("KeyC")), VOLUME));

        assertTrue(problema.orElseThrow().contains("RESERVAR"));
    }

    @Test
    void recusaAMesmaTeclaEmDuasAcoesTrocadas() {
        ConfiguracoesDto dto = new ConfiguracoesDto(Map.of(
                "ESQUERDA", List.of("KeyA"), "DIREITA", List.of("KeyA")), VOLUME);

        assertTrue(ValidacaoConfiguracoes.problema(dto).isPresent());
    }

    @Test
    void recusaCorpoMalformado() {
        assertTrue(ValidacaoConfiguracoes.problema(null).isPresent());
    }
}
