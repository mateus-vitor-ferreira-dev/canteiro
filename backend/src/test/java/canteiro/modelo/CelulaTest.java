package canteiro.modelo;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Confere o deslocamento e a igualdade de {@link Celula}.
 *
 * @author Mateus Vitor Ferreira
 * @version 0.1.0
 */
class CelulaTest {

    @Test
    void deslocaSemAlterarAOriginal() {
        Celula origem = new Celula(2, 3);
        assertEquals(new Celula(3, 1), origem.deslocada(1, -2));
        assertEquals(new Celula(2, 3), origem);
    }

    @Test
    void celulasComAMesmaPosicaoSaoIguais() {
        assertEquals(new Celula(0, 0), new Celula(0, 0));
        assertEquals(new Celula(0, 0).hashCode(), new Celula(0, 0).hashCode());
    }
}
