package canteiro.modelo;

import canteiro.modelo.constantes.Dimensoes;
import canteiro.modelo.materiais.Aco;
import canteiro.modelo.materiais.Madeira;
import canteiro.modelo.materiais.Material;
import canteiro.modelo.pecas.Peca;
import canteiro.modelo.pecas.PecaI;
import canteiro.modelo.pecas.PecaO;
import canteiro.modelo.pecas.PecaT;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Confere colisão, fixação e o efeito do aço no {@link Tabuleiro}.
 *
 * <p>A peça T na rotação inicial ocupa (0,1), (1,0), (1,1) e (1,2) do seu
 * quadrado 3 × 3; a I deitada ocupa a linha 1 do quadrado 4 × 4.</p>
 *
 * @author Mateus Vitor Ferreira
 * @version 0.1.0
 */
class TabuleiroTest {

    private static final int FUNDO = Dimensoes.LINHAS - 1;
    private static final int ULTIMA_COLUNA = Dimensoes.COLUNAS - 1;

    private final Tabuleiro tabuleiro = new Tabuleiro();
    private final Material madeira = new Madeira();
    private final Material aco = new Aco();

    @Test
    void comecaVazioComAsDimensoesDaRn01() {
        assertEquals(22, Dimensoes.LINHAS);
        assertEquals(10, Dimensoes.COLUNAS);
        for (int linha = 0; linha < Dimensoes.LINHAS; linha++) {
            for (int coluna = 0; coluna < Dimensoes.COLUNAS; coluna++) {
                assertFalse(tabuleiro.ocupada(linha, coluna));
            }
        }
    }

    @Test
    void pecaEmEspacoLivreNaoColide() {
        assertFalse(tabuleiro.colide(new PecaT(madeira), 10, 4));
    }

    @Test
    void pecaEncostadaNaBordaEsquerdaNaoAtravessa() {
        Peca t = new PecaT(madeira);
        assertFalse(tabuleiro.colide(t, 10, 0));
        assertTrue(tabuleiro.colide(t, 10, -1));
    }

    @Test
    void pecaEncostadaNaBordaDireitaNaoAtravessa() {
        Peca t = new PecaT(madeira);
        assertFalse(tabuleiro.colide(t, 10, ULTIMA_COLUNA - 2));
        assertTrue(tabuleiro.colide(t, 10, ULTIMA_COLUNA - 1));
        Peca i = new PecaI(madeira);
        assertFalse(tabuleiro.colide(i, 10, ULTIMA_COLUNA - 3));
        assertTrue(tabuleiro.colide(i, 10, ULTIMA_COLUNA - 2));
    }

    @Test
    void pecaEncostadaNoFundoNaoAtravessa() {
        Peca t = new PecaT(madeira);
        assertFalse(tabuleiro.colide(t, FUNDO - 1, 4));
        assertTrue(tabuleiro.colide(t, FUNDO, 4));
    }

    @Test
    void parteVaziaDaMatrizPodeFicarForaDaGrade() {
        Peca iEmPe = new PecaI(madeira);
        iEmPe.girarHorario();
        assertFalse(tabuleiro.colide(iEmPe, 10, -2), "a I em pé ocupa só a coluna 2 do quadrado");
        assertTrue(tabuleiro.colide(iEmPe, 10, -3));
    }

    @Test
    void linhasOcultasContamComoEspacoLivre() {
        assertFalse(tabuleiro.colide(new PecaT(madeira), 0, 4), "a T nasce nas duas linhas ocultas");
        assertFalse(tabuleiro.colide(new PecaI(madeira), -1, 3), "só a linha vazia da I fica acima do topo");
    }

    @Test
    void blocoAcimaDoTopoDaGradeColide() {
        assertTrue(tabuleiro.colide(new PecaT(madeira), -1, 4));
    }

    @Test
    void pecaSobreUmBlocoFixadoColide() {
        tabuleiro.fixar(new PecaO(madeira), FUNDO - 1, 0);
        Peca t = new PecaT(madeira);
        assertTrue(tabuleiro.colide(t, FUNDO - 2, 0));
        assertFalse(tabuleiro.colide(t, FUNDO - 3, 0));
    }

    @Test
    void fixarGravaOsBlocosComOMaterialDaPeca() {
        List<Celula> ocupadas = tabuleiro.fixar(new PecaT(aco), FUNDO - 1, 4);

        assertEquals(List.of(new Celula(FUNDO - 1, 5), new Celula(FUNDO, 4), new Celula(FUNDO, 5),
                new Celula(FUNDO, 6)), ocupadas);
        for (Celula celula : ocupadas) {
            assertSame(aco, tabuleiro.bloco(celula.linha(), celula.coluna()).material());
        }
        assertNull(tabuleiro.bloco(FUNDO - 1, 4));
    }

    @Test
    void naoFixaOndeColideENaoMexeNaGrade() {
        assertThrows(IllegalStateException.class, () -> tabuleiro.fixar(new PecaT(madeira), FUNDO, 4));
        assertFalse(tabuleiro.ocupada(FUNDO, 4));
    }

    @Test
    void acoConsolidaOsBlocosLogoAbaixo() {
        tabuleiro.fixar(new PecaO(madeira), FUNDO - 1, 0);
        tabuleiro.fixar(new PecaI(aco), FUNDO - 3, 0);

        assertTrue(tabuleiro.bloco(FUNDO - 1, 0).consolidado());
        assertTrue(tabuleiro.bloco(FUNDO - 1, 1).consolidado());
        assertFalse(tabuleiro.bloco(FUNDO, 0).consolidado(), "só o bloco imediatamente abaixo");
    }

    @Test
    void acoNaoConsolidaOsProprioBlocos() {
        tabuleiro.fixar(new PecaO(madeira), FUNDO - 1, 0);
        Peca iEmPe = new PecaI(aco);
        iEmPe.girarHorario();
        tabuleiro.fixar(iEmPe, FUNDO - 5, -2);

        assertTrue(tabuleiro.bloco(FUNDO - 1, 0).consolidado());
        for (int linha = FUNDO - 5; linha <= FUNDO - 2; linha++) {
            assertFalse(tabuleiro.bloco(linha, 0).consolidado(), "linha " + linha);
        }
    }

    @Test
    void madeiraNaoConsolidaNada() {
        tabuleiro.fixar(new PecaO(madeira), FUNDO - 1, 0);
        tabuleiro.fixar(new PecaI(madeira), FUNDO - 3, 0);
        assertFalse(tabuleiro.bloco(FUNDO - 1, 0).consolidado());
    }

    @Test
    void posicaoForaDaGradeEErro() {
        assertThrows(IndexOutOfBoundsException.class, () -> tabuleiro.bloco(-1, 0));
        assertThrows(IndexOutOfBoundsException.class, () -> tabuleiro.bloco(0, Dimensoes.COLUNAS));
        assertFalse(Tabuleiro.dentro(new Celula(Dimensoes.LINHAS, 0)));
        assertTrue(Tabuleiro.dentro(new Celula(0, 0)));
    }
}
