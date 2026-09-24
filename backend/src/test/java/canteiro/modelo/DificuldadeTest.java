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
            assertEquals(Progressao.limiteDesvio(dificuldade.nivelInicial()), dificuldade.limiteDesvio());
        }
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
