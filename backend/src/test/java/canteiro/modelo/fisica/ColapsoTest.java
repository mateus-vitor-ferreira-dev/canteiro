package canteiro.modelo.fisica;

import canteiro.modelo.Celula;
import canteiro.modelo.Queda;
import canteiro.modelo.Tabuleiro;
import canteiro.modelo.constantes.Dimensoes;
import canteiro.modelo.materiais.Aco;
import canteiro.modelo.materiais.Madeira;
import canteiro.modelo.pecas.PecaI;
import canteiro.modelo.pecas.PecaO;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Linha crítica e queda recursiva dos blocos desprendidos (RN11).
 *
 * @author Mateus Vitor Ferreira
 * @version 0.1.0
 */
class ColapsoTest {

    private static final int FUNDO = Dimensoes.LINHAS - 1;
    private final Tabuleiro tabuleiro = new Tabuleiro();
    private final Madeira madeira = new Madeira();
    private final Aco aco = new Aco();

    @Test
    void linhaCriticaEADeMaiorMassa() {
        tabuleiro.fixar(new PecaI(madeira), FUNDO - 1, 0);
        tabuleiro.fixar(new PecaO(aco), FUNDO - 2, 0);
        assertEquals(FUNDO - 2, Colapso.linhaCritica(tabuleiro), "as duas linhas da O pesam igual: vence a de cima");
    }

    @Test
    void blocosAcimaDaLinhaCriticaCaemAteEncontrarApoio() {
        tabuleiro.fixar(new PecaI(aco), FUNDO - 1, 0);
        PecaI emPe = new PecaI(madeira);
        emPe.girarHorario();
        tabuleiro.fixar(emPe, FUNDO - 8, 3);
        assertTrue(tabuleiro.ocupada(FUNDO - 5, 5), "a I em pé está solta no ar, na coluna 5");

        List<Queda> quedas = Colapso.executar(tabuleiro);

        assertEquals(4, quedas.size());
        assertEquals(new Queda(new Celula(FUNDO - 5, 5), new Celula(FUNDO, 5)), quedas.get(0));
        for (int linha = FUNDO - 3; linha <= FUNDO; linha++) {
            assertTrue(tabuleiro.ocupada(linha, 5), "linha " + linha);
        }
        assertFalse(tabuleiro.ocupada(FUNDO - 4, 5));
    }

    @Test
    void blocoConsolidadoNaoCai() {
        tabuleiro.fixar(new PecaI(aco), FUNDO - 1, 0);
        tabuleiro.fixar(new PecaO(madeira), FUNDO - 5, 5);
        tabuleiro.bloco(FUNDO - 4, 5).consolidar();

        Colapso.executar(tabuleiro);

        assertTrue(tabuleiro.ocupada(FUNDO - 4, 5), "o consolidado resiste");
        assertTrue(tabuleiro.ocupada(FUNDO, 6), "o vizinho solto caiu até o fundo");
    }

    @Test
    void semBuracoNadaCai() {
        tabuleiro.fixar(new PecaO(madeira), FUNDO - 1, 0);
        tabuleiro.fixar(new PecaO(madeira), FUNDO - 3, 0);
        assertEquals(List.of(), Colapso.executar(tabuleiro));
    }
}
