package canteiro.modelo;

import canteiro.modelo.materiais.Madeira;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Confere a soma de pontos e a subida de nível (RN14).
 *
 * @author Mateus Vitor Ferreira
 * @version 0.1.0
 */
class PlacarTest {

    private final Madeira madeira = new Madeira();

    @Test
    void comecaZeradoNoNivelInicial() {
        Placar placar = new Placar(3);
        assertEquals(0, placar.pontuacao());
        assertEquals(0, placar.linhas());
        assertEquals(3, placar.nivel());
    }

    @Test
    void sobeDeNivelACadaDezLinhas() {
        Placar placar = new Placar(1);
        for (int i = 0; i < 4; i++) {
            assertFalse(placar.registrarLinhas(2, madeira));
        }
        assertTrue(placar.registrarLinhas(2, madeira), "10 linhas");
        assertEquals(2, placar.nivel());
        assertEquals(10, placar.linhas());
    }

    @Test
    void pontosDaEliminacaoQueSobeUsamONivelDeAntes() {
        Placar placar = new Placar(1);
        placar.registrarLinhas(4, madeira);
        placar.registrarLinhas(4, madeira);
        int antes = placar.pontuacao();
        placar.registrarLinhas(4, madeira);
        assertEquals(Pontuacao.pontos(4, 1, madeira), placar.pontuacao() - antes);
        assertEquals(2, placar.nivel());
    }

    @Test
    void acumulaOsPontos() {
        Placar placar = new Placar(2);
        placar.registrarLinhas(1, madeira);
        placar.registrarLinhas(3, madeira);
        assertEquals(Pontuacao.pontos(1, 2, madeira) + Pontuacao.pontos(3, 2, madeira), placar.pontuacao());
    }

    @Test
    void nivelInicialInvalidoEErro() {
        assertThrows(IllegalArgumentException.class, () -> new Placar(0));
    }
}
