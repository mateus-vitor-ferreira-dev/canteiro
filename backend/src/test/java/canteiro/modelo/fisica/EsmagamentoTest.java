package canteiro.modelo.fisica;

import canteiro.modelo.Celula;
import canteiro.modelo.Tabuleiro;
import canteiro.modelo.constantes.Dimensoes;
import canteiro.modelo.materiais.Aco;
import canteiro.modelo.materiais.Alvenaria;
import canteiro.modelo.materiais.Madeira;
import canteiro.modelo.materiais.Material;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Carga máxima, esmagamento e rachaduras (RN21).
 *
 * @author Mateus Vitor Ferreira
 * @version 0.1.0
 */
class EsmagamentoTest {

    private static final int FUNDO = Dimensoes.LINHAS - 1;
    private final Tabuleiro tabuleiro = new Tabuleiro();
    private final AnalisadorEstrutural analisador = new AnalisadorEstrutural();
    private final Madeira madeira = new Madeira();
    private final Alvenaria alvenaria = new Alvenaria();

    @Test
    void cargaEhAMassaColadaEmCimaAteOPrimeiroVazio() {
        empilhar(0, madeira, alvenaria, alvenaria);
        empilharEm(FUNDO - 4, 0, alvenaria);
        assertEquals(2 * alvenaria.densidade(), Esmagamento.carga(tabuleiro, FUNDO, 0), 1e-9,
                "o bloco solto acima do vazio não pesa aqui");
    }

    @Test
    void madeiraSobCincoAlvenariasViraBuraco() {
        empilhar(0, madeira, alvenaria, alvenaria, alvenaria, alvenaria, alvenaria);
        double massaAntes = analisador.massaTotal();

        Esmagamento.Resultado resultado = Esmagamento.executar(tabuleiro, analisador, 1);

        assertEquals(List.of(new Celula(FUNDO, 0)), resultado.esmagados());
        assertFalse(tabuleiro.ocupada(FUNDO, 0), "ficou um buraco");
        assertTrue(tabuleiro.ocupada(FUNDO - 1, 0), "os blocos de cima não descem");
        assertEquals(massaAntes - madeira.densidade(), analisador.massaTotal(), 1e-9);
    }

    @Test
    void madeiraSobQuatroAlvenariasRachaMasAguenta() {
        empilhar(0, madeira, alvenaria, alvenaria, alvenaria, alvenaria);

        assertTrue(Esmagamento.executar(tabuleiro, analisador, 1).vazio());
        assertEquals(List.of(new Esmagamento.Rachadura(new Celula(FUNDO, 0), 3)),
                Esmagamento.rachaduras(tabuleiro, 1), "7,2 de 8: prestes a ceder");
    }

    @Test
    void fatorDaDificuldadeMudaOLimite() {
        empilhar(0, madeira, alvenaria, alvenaria, alvenaria, alvenaria, alvenaria);
        assertTrue(Esmagamento.executar(tabuleiro, analisador, 1.25).vazio(), "no fácil a madeira aguenta 10");
    }

    @Test
    void oBuracoAliviaQuemEstaEmbaixo() {
        // duas madeiras e seis alvenarias: a madeira de cima cede primeiro e a de baixo fica sem peso
        empilhar(0, madeira, madeira, alvenaria, alvenaria, alvenaria, alvenaria, alvenaria, alvenaria);

        Esmagamento.Resultado resultado = Esmagamento.executar(tabuleiro, analisador, 1);

        assertEquals(List.of(new Celula(FUNDO - 1, 0)), resultado.esmagados());
        assertTrue(tabuleiro.ocupada(FUNDO, 0));
        assertEquals(0, Esmagamento.carga(tabuleiro, FUNDO, 0));
    }

    @Test
    void acoNuncaEhEsmagado() {
        Aco aco = new Aco();
        Material[] torre = new Material[Dimensoes.LINHAS];
        Arrays.fill(torre, aco);
        empilhar(0, torre);
        assertTrue(Esmagamento.executar(tabuleiro, analisador, 0.8).vazio());
        assertEquals(List.of(), Esmagamento.rachaduras(tabuleiro, 0.8));
    }

    @Test
    void rachaduraCresceComACarga() {
        empilhar(0, madeira, alvenaria, alvenaria, alvenaria); // 5,4 de 8
        empilhar(1, madeira, alvenaria, alvenaria);            // 3,6 de 8: ainda inteira
        assertEquals(List.of(new Esmagamento.Rachadura(new Celula(FUNDO, 0), 1)),
                Esmagamento.rachaduras(tabuleiro, 1));
    }

    /** Empilha os blocos na coluna, a partir do fundo, de baixo para cima. */
    private void empilhar(int coluna, Material... deBaixoParaCima) {
        for (int i = 0; i < deBaixoParaCima.length; i++) {
            empilharEm(FUNDO - i, coluna, deBaixoParaCima[i]);
        }
    }

    private void empilharEm(int linha, int coluna, Material material) {
        Celula celula = new Celula(linha, coluna);
        tabuleiro.fixar(List.of(celula), material);
        analisador.registrar(celula, material);
    }
}
