package canteiro.modelo;

import canteiro.modelo.materiais.Aco;
import canteiro.modelo.materiais.Alvenaria;
import canteiro.modelo.materiais.Concreto;
import canteiro.modelo.materiais.Madeira;
import canteiro.modelo.materiais.Material;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Confere a fórmula de pontos (RF13).
 *
 * @author Mateus Vitor Ferreira
 * @version 0.1.0
 */
class PontuacaoTest {

    private final Material madeira = new Madeira();
    private final Material aco = new Aco();

    @Test
    void baseCresceMaisQueAsLinhas() {
        assertEquals(110, Pontuacao.pontos(1, 1, madeira));
        assertEquals(330, Pontuacao.pontos(2, 1, madeira));
        assertEquals(550, Pontuacao.pontos(3, 1, madeira));
        assertEquals(880, Pontuacao.pontos(4, 1, madeira));
    }

    @Test
    void quatroLinhasDeUmaVezRendemMaisQueQuatroIsoladas() {
        assertTrue(Pontuacao.pontos(4, 1, madeira) > 4 * Pontuacao.pontos(1, 1, madeira));
    }

    @Test
    void materialPredominanteMultiplicaOsPontos() {
        assertEquals(140, Pontuacao.pontos(1, 1, aco));
        assertTrue(Pontuacao.pontos(2, 1, aco) > Pontuacao.pontos(2, 1, madeira));
    }

    @Test
    void nivelMultiplicaTudo() {
        assertEquals(3 * Pontuacao.pontos(2, 1, aco), Pontuacao.pontos(2, 3, aco));
    }

    @Test
    void predominanteEOMaterialComMaisBlocos() {
        Material alvenaria = new Alvenaria();
        assertEquals(alvenaria, Pontuacao.predominante(List.of(aco, alvenaria, alvenaria, madeira)));
    }

    @Test
    void noEmpateVenceOMaisPesado() {
        Material concreto = new Concreto();
        assertEquals(concreto, Pontuacao.predominante(List.of(madeira, concreto, madeira, concreto)));
    }

    @Test
    void entradasInvalidasSaoErro() {
        assertThrows(IllegalArgumentException.class, () -> Pontuacao.pontos(0, 1, madeira));
        assertThrows(IllegalArgumentException.class, () -> Pontuacao.pontos(5, 1, madeira));
        assertThrows(IllegalArgumentException.class, () -> Pontuacao.pontos(1, 0, madeira));
        assertThrows(IllegalArgumentException.class, () -> Pontuacao.predominante(List.of()));
    }

    @Test
    void obraOusadaVale50PorCentoAMais() {
        assertEquals(165, Pontuacao.pontos(1, 1, madeira, true, 0));
        assertTrue(Pontuacao.ousada(0.24));
        assertTrue(Pontuacao.ousada(0));
        assertTrue(!Pontuacao.ousada(0.25));
    }

    @Test
    void sequenciaSoma10PorCentoPorPecaAte100() {
        assertEquals(1.0, Pontuacao.bonusSequencia(0), 1e-9);
        assertEquals(1.3, Pontuacao.bonusSequencia(3), 1e-9);
        assertEquals(2.0, Pontuacao.bonusSequencia(10), 1e-9);
        assertEquals(2.0, Pontuacao.bonusSequencia(25), 1e-9);
        assertEquals(143, Pontuacao.pontos(1, 1, madeira, false, 3));
    }

    @Test
    void bonusSeMultiplicam() {
        // 800 × nível 2 × aço 1,4 × ousada 1,5 × sequência 2
        assertEquals(6720, Pontuacao.pontos(4, 2, aco, true, 10));
    }

    @Test
    void sequenciaNegativaEhRecusada() {
        assertThrows(IllegalArgumentException.class, () -> Pontuacao.pontos(1, 1, madeira, false, -1));
    }
}
