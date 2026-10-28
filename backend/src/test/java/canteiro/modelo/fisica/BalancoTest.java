package canteiro.modelo.fisica;

import canteiro.modelo.Celula;
import canteiro.modelo.Queda;
import canteiro.modelo.Tabuleiro;
import canteiro.modelo.constantes.Dimensoes;
import canteiro.modelo.materiais.Aco;
import canteiro.modelo.materiais.Concreto;
import canteiro.modelo.materiais.Madeira;
import canteiro.modelo.materiais.Material;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * O balanço que o apoio mais leve não aguenta (RN20).
 *
 * @author Mateus Vitor Ferreira
 * @version 0.1.0
 */
class BalancoTest {

    private static final int FUNDO = Dimensoes.LINHAS - 1;
    private final Tabuleiro tabuleiro = new Tabuleiro();
    private final Madeira madeira = new Madeira();
    private final Concreto concreto = new Concreto();
    private final Aco aco = new Aco();

    /** Três blocos deitados na linha de cima do fundo, nas colunas 0 a 2; só a coluna 0 tem apoio. */
    private final List<Celula> deitada = List.of(
            new Celula(FUNDO - 1, 0), new Celula(FUNDO - 1, 1), new Celula(FUNDO - 1, 2));

    @Test
    void concretoApoiadoNaMadeiraPerdeOBalanco() {
        tabuleiro.fixar(List.of(new Celula(FUNDO, 0)), madeira);
        List<Queda> quedas = assentar(deitada, concreto);

        assertEquals(List.of(new Queda(new Celula(FUNDO - 1, 1), new Celula(FUNDO, 1)),
                new Queda(new Celula(FUNDO - 1, 2), new Celula(FUNDO, 2))), quedas);
        assertTrue(tabuleiro.ocupada(FUNDO - 1, 0), "a parte apoiada fica");
        assertFalse(tabuleiro.ocupada(FUNDO - 1, 1));
    }

    @Test
    void mesmoMaterialSeguraOBalanco() {
        tabuleiro.fixar(List.of(new Celula(FUNDO, 0)), concreto);
        assertEquals(List.of(), assentar(deitada, concreto));
    }

    @Test
    void madeiraApoiadaNoConcretoSeguraOBalanco() {
        tabuleiro.fixar(List.of(new Celula(FUNDO, 0)), concreto);
        assertEquals(List.of(), assentar(deitada, madeira));
    }

    @Test
    void bastaUmApoioForte() {
        tabuleiro.fixar(List.of(new Celula(FUNDO, 0)), madeira);
        tabuleiro.fixar(List.of(new Celula(FUNDO, 2)), aco);
        assertEquals(List.of(), assentar(deitada, concreto), "o aço da coluna 2 segura o concreto");
    }

    @Test
    void oChaoAguentaQualquerBalanco() {
        // uma L de aço com o pé no chão e o braço por cima de um vazio
        List<Celula> ele = List.of(new Celula(FUNDO - 1, 0), new Celula(FUNDO - 1, 1),
                new Celula(FUNDO - 1, 2), new Celula(FUNDO, 0));
        assertEquals(List.of(), assentar(ele, aco));
    }

    @Test
    void blocosDaMesmaColunaCaemJuntos() {
        tabuleiro.fixar(List.of(new Celula(FUNDO, 0)), madeira);
        // uma J de concreto: a coluna 1 tem dois blocos, os dois em balanço
        List<Celula> jota = List.of(new Celula(FUNDO - 2, 1), new Celula(FUNDO - 1, 1), new Celula(FUNDO - 1, 0));
        List<Queda> quedas = assentar(jota, concreto);

        assertEquals(2, quedas.size());
        assertTrue(tabuleiro.ocupada(FUNDO, 1));
        assertTrue(tabuleiro.ocupada(FUNDO - 1, 1), "o de cima parou em cima do de baixo");
    }

    private List<Queda> assentar(List<Celula> celulas, Material material) {
        tabuleiro.fixar(celulas, material);
        return Balanco.executar(tabuleiro, celulas, material);
    }
}
