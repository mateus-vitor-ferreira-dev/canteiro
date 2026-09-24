package canteiro.modelo;

import canteiro.modelo.materiais.Concreto;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Confere a massa e a consolidação de {@link Bloco}.
 *
 * @author Mateus Vitor Ferreira
 * @version 0.1.0
 */
class BlocoTest {

    @Test
    void massaEADoMaterial() {
        Concreto concreto = new Concreto();
        assertEquals(concreto.massaPorBloco(), new Bloco(concreto).massa());
    }

    @Test
    void comecaSoltoEConsolidaUmaVezSo() {
        Bloco bloco = new Bloco(new Concreto());
        assertFalse(bloco.consolidado());
        bloco.consolidar();
        bloco.consolidar();
        assertTrue(bloco.consolidado());
    }

    @Test
    void semMaterialEErro() {
        assertThrows(NullPointerException.class, () -> new Bloco(null));
    }
}
