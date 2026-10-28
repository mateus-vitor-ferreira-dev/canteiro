package canteiro.modelo;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Confere que as dificuldades ficam de fato mais difíceis, na ordem em que
 * são declaradas (RF02).
 *
 * @author Mateus Vitor Ferreira
 * @version 0.1.0
 */
class DificuldadeTest {

    @Test
    void existemTresDificuldades() {
        assertEquals(3, Dificuldade.values().length);
    }

    @Test
    void cadaDificuldadeComecaNumNivelDaProgressao() {
        assertEquals(1, Dificuldade.FACIL.nivelInicial());
        assertEquals(3, Dificuldade.NORMAL.nivelInicial());
        assertEquals(5, Dificuldade.DIFICIL.nivelInicial());
        for (Dificuldade dificuldade : Dificuldade.values()) {
            assertEquals(Progressao.intervaloQuedaMs(dificuldade.nivelInicial()), dificuldade.intervaloQuedaMs());
            assertEquals(dificuldade.limiteDesvio(dificuldade.nivelInicial()), dificuldade.limiteDesvio());
        }
    }

    @Test
    void dificilApertaOLimiteEEnfraqueceOsMateriais() {
        assertEquals(Progressao.limiteDesvio(5) * 0.8, Dificuldade.DIFICIL.limiteDesvio(5), 1e-9);
        assertEquals(Progressao.limiteDesvio(5), Dificuldade.NORMAL.limiteDesvio(5), 1e-9);
        assertTrue(Dificuldade.FACIL.fatorResistencia() > Dificuldade.NORMAL.fatorResistencia());
        assertTrue(Dificuldade.DIFICIL.fatorResistencia() < Dificuldade.NORMAL.fatorResistencia());
    }

    @Test
    void cadaDificuldadeTemQuedaMaisRapidaELimiteMaisApertadoQueAAnterior() {
        Dificuldade[] dificuldades = Dificuldade.values();
        for (int i = 1; i < dificuldades.length; i++) {
            Dificuldade anterior = dificuldades[i - 1];
            Dificuldade atual = dificuldades[i];
            assertTrue(atual.intervaloQuedaMs() < anterior.intervaloQuedaMs(), atual + " deveria cair mais rápido");
            assertTrue(atual.limiteDesvio() < anterior.limiteDesvio(), atual + " deveria tolerar menos desvio");
            assertTrue(atual.materiaisLiberados().containsAll(anterior.materiaisLiberados()),
                    atual + " deveria liberar ao menos os materiais de " + anterior);
        }
    }
}
