package canteiro.modelo.fisica;

import canteiro.modelo.Celula;
import canteiro.modelo.Tabuleiro;
import canteiro.modelo.constantes.Dimensoes;
import canteiro.modelo.materiais.Aco;
import canteiro.modelo.materiais.Concreto;
import canteiro.modelo.materiais.Madeira;
import canteiro.modelo.materiais.Material;
import canteiro.modelo.pecas.PecaI;
import canteiro.modelo.pecas.PecaO;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
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

    @Test
    void torreQueSeInclinaParaForaDoContatoDesaba() {
        // fundo de aço inteiro e, em cima, uma escada de madeira que sai das colunas 0 e 1 e vai para a direita
        Tabuleiro tabuleiro = new Tabuleiro();
        for (int coluna = 0; coluna < Dimensoes.COLUNAS; coluna++) {
            colocar(tabuleiro, FUNDO, coluna, aco);
        }
        int[][] escada = {{1, 0}, {1, 1}, {2, 1}, {2, 2}, {2, 3}, {3, 3}, {3, 4}, {3, 5}, {4, 5}, {4, 6}, {4, 7}};
        for (int[] degrau : escada) {
            colocar(tabuleiro, FUNDO - degrau[0], degrau[1], madeira);
        }

        Estabilidade topo = AnalisadorEstrutural.estabilidadeDoTopo(tabuleiro, 2.0);
        assertEquals(1.0, topo.eixo(), TOLERANCIA, "a escada só encosta no aço nas colunas 0 e 1");
        assertTrue(topo.passouDoLimite());
        Estabilidade estabilidade = analisador.estabilidade(tabuleiro, 2.0);
        assertTrue(estabilidade.passouDoLimite(), "o centro da estrutura inteira está no eixo, mas vale a pior medida");
        assertEquals(topo.desvio(), estabilidade.desvio(), TOLERANCIA);
    }

    @Test
    void pecaNoCantoEmCimaDeUmaLinhaLargaFicaDePe() {
        Tabuleiro tabuleiro = new Tabuleiro();
        for (int coluna = 0; coluna < Dimensoes.COLUNAS; coluna++) {
            colocar(tabuleiro, FUNDO, coluna, new Concreto());
        }
        colocar(tabuleiro, FUNDO - 1, 8, aco);
        colocar(tabuleiro, FUNDO - 1, 9, aco);

        Estabilidade topo = AnalisadorEstrutural.estabilidadeDoTopo(tabuleiro, 1.2);
        assertEquals(0, topo.desvio(), TOLERANCIA, "o aço encosta só nas colunas 8 e 9, e o centro dele está ali");
    }

    @Test
    void semNadaAcimaDaLinhaCriticaNaoHaParteDeCima() {
        Tabuleiro tabuleiro = new Tabuleiro();
        colocar(tabuleiro, FUNDO, 0, madeira);
        assertNull(AnalisadorEstrutural.estabilidadeDoTopo(tabuleiro, 2.0));
    }

    private void colocar(Tabuleiro tabuleiro, int linha, int coluna, Material material) {
        Celula celula = new Celula(linha, coluna);
        tabuleiro.fixar(List.of(celula), material);
        analisador.registrar(celula, material);
    }
}
