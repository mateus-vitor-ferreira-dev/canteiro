package canteiro.modelo;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Confere a tabela de progressão por nível (RN14).
 *
 * @author Mateus Vitor Ferreira
 * @version 0.1.0
 */
class ProgressaoTest {

    @Test
    void segueATabelaDaCurvaDeDificuldade() {
        long[] intervalos = {800, 800, 650, 650, 500, 500, 380, 380, 280, 280, 200, 200};
        double[] limites = {3.0, 3.0, 2.5, 2.5, 2.0, 2.0, 1.7, 1.7, 1.4, 1.4, 1.2, 1.2};
        for (int nivel = 1; nivel <= intervalos.length; nivel++) {
            assertEquals(intervalos[nivel - 1], Progressao.intervaloQuedaMs(nivel), "nível " + nivel);
            assertEquals(limites[nivel - 1], Progressao.limiteDesvio(nivel), "nível " + nivel);
        }
    }

    @Test
    void nuncaFicaMaisFacilAoSubirDeNivel() {
        for (int nivel = 2; nivel <= 30; nivel++) {
            assertTrue(Progressao.intervaloQuedaMs(nivel) <= Progressao.intervaloQuedaMs(nivel - 1));
            assertTrue(Progressao.limiteDesvio(nivel) <= Progressao.limiteDesvio(nivel - 1));
        }
    }

    @Test
    void doNivelOnzeEmDianteParaDeMudar() {
        assertEquals(Progressao.intervaloQuedaMs(11), Progressao.intervaloQuedaMs(99));
        assertEquals(Progressao.limiteDesvio(11), Progressao.limiteDesvio(99));
    }

    @Test
    void materiaisEntramNosNiveisTresECinco() {
        assertEquals(List.of("MADEIRA", "ALVENARIA"), Progressao.materiaisLiberados(2));
        assertEquals(List.of("MADEIRA", "ALVENARIA", "CONCRETO"), Progressao.materiaisLiberados(3));
        assertEquals(List.of("MADEIRA", "ALVENARIA", "CONCRETO", "ACO"), Progressao.materiaisLiberados(5));
    }

    @Test
    void nivelZeroEErro() {
        assertThrows(IllegalArgumentException.class, () -> Progressao.intervaloQuedaMs(0));
        assertThrows(IllegalArgumentException.class, () -> Progressao.materiaisLiberados(0));
    }
}
