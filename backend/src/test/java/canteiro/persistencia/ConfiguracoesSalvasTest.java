package canteiro.persistencia;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * O registro das configurações: cópia defensiva e volume na faixa.
 *
 * @author Mateus Vitor Ferreira
 * @version 0.1.0
 */
class ConfiguracoesSalvasTest {

    @Test
    void recusaVolumeForaDaFaixa() {
        assertThrows(IllegalArgumentException.class, () -> new ConfiguracoesSalvas(Map.of(), -1));
        assertThrows(IllegalArgumentException.class,
                () -> new ConfiguracoesSalvas(Map.of(), ConfiguracoesSalvas.VOLUME_MAXIMO + 1));
    }

    @Test
    void teclasNulasValemComoNenhuma() {
        assertTrue(new ConfiguracoesSalvas(null, ConfiguracoesSalvas.VOLUME_PADRAO).teclas().isEmpty());
    }

    @Test
    void mudarOMapaOriginalNaoMudaORegistro() {
        List<String> codigos = new ArrayList<>(List.of("KeyA"));
        Map<String, List<String>> teclas = new HashMap<>(Map.of("ESQUERDA", codigos));
        ConfiguracoesSalvas salvas = new ConfiguracoesSalvas(teclas, ConfiguracoesSalvas.VOLUME_PADRAO);

        codigos.add("KeyQ");
        teclas.put("DIREITA", List.of("KeyD"));

        assertEquals(Map.of("ESQUERDA", List.of("KeyA")), salvas.teclas());
        assertThrows(UnsupportedOperationException.class, () -> salvas.teclas().clear());
    }
}
