package canteiro.modelo;

import canteiro.modelo.constantes.Dimensoes;
import canteiro.modelo.materiais.Madeira;
import canteiro.modelo.pecas.PecaO;
import canteiro.modelo.pecas.PecaT;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * A peça em queda e a posição dela.
 *
 * @author Mateus Vitor Ferreira
 * @version 0.1.0
 */
class PecaEmQuedaTest {

    private static final int FUNDO = Dimensoes.LINHAS - 1;
    private final Tabuleiro tabuleiro = new Tabuleiro();
    private final Madeira madeira = new Madeira();

    @Test
    void nasceCentralizadaNoTopo() {
        PecaEmQueda o = PecaEmQueda.nascer(new PecaO(madeira), tabuleiro);
        assertEquals(List.of(new Celula(0, 4), new Celula(0, 5), new Celula(1, 4), new Celula(1, 5)), o.celulas());
        assertFalse(o.colide());
    }

    @Test
    void naoMoveParaDentroDaParede() {
        PecaEmQueda t = PecaEmQueda.nascer(new PecaT(madeira), tabuleiro);
        for (int i = 0; i < 3; i++) {
            assertTrue(t.mover(0, -1));
        }
        assertFalse(t.mover(0, -1));
    }

    @Test
    void pousoEFixacao() {
        PecaEmQueda t = PecaEmQueda.nascer(new PecaT(madeira), tabuleiro);
        List<Celula> fantasma = t.celulasNoPouso();
        t.cairAtePouso();
        assertEquals(fantasma, t.celulas());
        assertEquals(FUNDO, t.celulas().get(3).linha());
        t.fixar();
        assertTrue(tabuleiro.ocupada(FUNDO, 4));
    }

    @Test
    void giroComEspacoDevolveVerdadeiro() {
        tabuleiro.fixar(new PecaO(madeira), 0, 0);
        tabuleiro.fixar(new PecaO(madeira), 0, 8);
        PecaEmQueda t = PecaEmQueda.nascer(new PecaT(madeira), tabuleiro);
        assertTrue(t.girar(true));
    }
}
