package canteiro.modelo.fisica;

import canteiro.modelo.Celula;
import canteiro.modelo.Tabuleiro;
import canteiro.modelo.constantes.Dimensoes;
import canteiro.modelo.materiais.Aco;
import canteiro.modelo.materiais.Madeira;
import canteiro.modelo.pecas.PecaI;
import canteiro.modelo.pecas.PecaO;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Centro de massa, base de apoio e índice de estabilidade (RN07 a RN10).
 *
 * @author Mateus Vitor Ferreira
 * @version 0.1.0
 */
class AnalisadorEstruturalTest {

    private static final double TOLERANCIA = 1e-9;
    private static final int FUNDO = Dimensoes.LINHAS - 1;

    private final AnalisadorEstrutural analisador = new AnalisadorEstrutural();
    private final Madeira madeira = new Madeira();
    private final Aco aco = new Aco();

    @Test
    void estruturaSimetricaTemCentroNoMeio() {
        analisador.registrar(new Celula(FUNDO, 0), madeira);
        analisador.registrar(new Celula(FUNDO, 9), madeira);
        assertEquals(5.0, analisador.centroDeMassa(), TOLERANCIA);
    }

    @Test
    void simetricaNaFormaMasNaoNaMassaPendeParaOLadoPesado() {
        analisador.registrar(new Celula(FUNDO, 0), aco);
        analisador.registrar(new Celula(FUNDO, 9), madeira);
        double esperado = (7.85 * 0.5 + 0.6 * 9.5) / (7.85 + 0.6);
        assertEquals(esperado, analisador.centroDeMassa(), TOLERANCIA);
        assertTrue(analisador.centroDeMassa() < 5.0);
    }

    @Test
    void colunaUnicaTemCentroNoMeioDaColuna() {
        for (int linha = FUNDO; linha > FUNDO - 5; linha--) {
            analisador.registrar(new Celula(linha, 3), aco);
        }
        assertEquals(3.5, analisador.centroDeMassa(), TOLERANCIA);
        assertEquals(5 * 7.85, analisador.massaTotal(), TOLERANCIA);
    }

    @Test
    void removerDesfazORegistro() {
        analisador.registrar(new Celula(FUNDO, 0), aco);
        analisador.registrar(new Celula(FUNDO, 8), madeira);
        analisador.remover(new Celula(FUNDO, 0), aco);
        assertEquals(8.5, analisador.centroDeMassa(), TOLERANCIA);
        analisador.remover(new Celula(FUNDO, 8), madeira);
        assertTrue(analisador.vazia());
    }

    @Test
    void estruturaVaziaNaoTemCentroMasEstaEstavel() {
        assertThrows(IllegalStateException.class, analisador::centroDeMassa);
        Estabilidade estabilidade = analisador.estabilidade(new Tabuleiro(), 2.0);
        assertEquals(1.0, estabilidade.indice());
        assertFalse(estabilidade.passouDoLimite());
    }

    @Test
    void baseDeApoioEALinhaMaisBaixaOcupada() {
        Tabuleiro tabuleiro = new Tabuleiro();
        assertEquals(5.0, AnalisadorEstrutural.eixoDaBase(tabuleiro), "vazio: meio do tabuleiro");
        tabuleiro.fixar(new PecaO(madeira), FUNDO - 1, 0);
        assertEquals(1.0, AnalisadorEstrutural.eixoDaBase(tabuleiro), "a O no canto é a própria base");
        tabuleiro.fixar(new PecaI(madeira), FUNDO - 1, 5);
        assertEquals(4.5, AnalisadorEstrutural.eixoDaBase(tabuleiro), "da coluna 0 até a 8");
    }

    @Test
    void indiceCaiLinearmenteAteOLimite() {
        Tabuleiro tabuleiro = new Tabuleiro();
        tabuleiro.fixar(new PecaI(madeira), FUNDO - 1, 0);
        tabuleiro.fixar(new PecaI(madeira), FUNDO - 1, 4);
        registrarLinha(FUNDO, 0, 8);
        analisador.registrar(new Celula(FUNDO - 1, 0), aco);

        Estabilidade estabilidade = analisador.estabilidade(tabuleiro, 2.0);
        double centro = analisador.centroDeMassa();
        assertEquals(4.0, estabilidade.eixo(), TOLERANCIA);
        assertEquals(Math.abs(centro - 4.0), estabilidade.desvio(), TOLERANCIA);
        assertEquals(Math.max(0, 1 - estabilidade.desvio() / 2.0), estabilidade.indice(), TOLERANCIA);
        assertEquals(estabilidade.indice() < Estabilidade.LIMIAR_ALERTA, estabilidade.alerta());
    }

    @Test
    void passaDoLimiteQuandoODesvioEMaior() {
        assertTrue(new Estabilidade(0, 2.1, 2.0, 1.0, 3.1, true).passouDoLimite());
        assertFalse(new Estabilidade(0, 2.0, 2.0, 1.0, 3.0, true).passouDoLimite());
    }

    private void registrarLinha(int linha, int deColuna, int ateColuna) {
        for (int coluna = deColuna; coluna < ateColuna; coluna++) {
            analisador.registrar(new Celula(linha, coluna), madeira);
        }
    }
}
