package canteiro.modelo.metas;

import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Cada tipo de meta e o sorteio do quadro (RN19).
 *
 * @author Mateus Vitor Ferreira
 * @version 0.1.0
 */
class MetasTest {

    private static Lance linhas(int quantidade, String material) {
        return new Lance(quantidade, material, false, 1, false);
    }

    private static Lance semLinha(double indice) {
        return new Lance(0, null, false, indice, false);
    }

    @Test
    void linhasDeMaterialSoContaOMaterialPedido() {
        Meta meta = new LinhasDeMaterial("ACO", "aço", 2);
        assertEquals("Elimine 2 linhas com aço predominando", meta.descricao());
        meta.registrar(linhas(3, "MADEIRA"));
        assertEquals(0, meta.progresso());
        meta.registrar(linhas(1, "ACO"));
        assertFalse(meta.cumprida());
        meta.registrar(linhas(4, "ACO"));
        assertEquals(2, meta.progresso(), "o progresso para no alvo");
        assertTrue(meta.cumprida());
    }

    @Test
    void eliminacaoGrandePedeTudoDeUmaVez() {
        Meta meta = new EliminacaoGrande(3);
        meta.registrar(linhas(2, "MADEIRA"));
        meta.registrar(linhas(2, "MADEIRA"));
        assertFalse(meta.cumprida(), "duas de duas não valem");
        meta.registrar(linhas(3, "MADEIRA"));
        assertTrue(meta.cumprida());
        assertThrows(IllegalArgumentException.class, () -> new EliminacaoGrande(5));
    }

    @Test
    void obraFirmeRecomecaQuandoAEstabilidadeCai() {
        Meta meta = new ObraFirme(4, 0.5);
        assertEquals("Elimine 4 linhas sem a estabilidade cair abaixo de 50 %", meta.descricao());
        meta.registrar(linhas(3, "MADEIRA"));
        meta.registrar(semLinha(0.4));
        assertEquals(0, meta.progresso());
        meta.registrar(linhas(2, "MADEIRA"));
        meta.registrar(new Lance(0, null, false, 0.9, true));
        assertEquals(0, meta.progresso(), "o colapso também recomeça");
        meta.registrar(linhas(4, "MADEIRA"));
        assertTrue(meta.cumprida());
        meta.registrar(semLinha(0));
        assertTrue(meta.cumprida(), "cumprida, não volta atrás");
    }

    @Test
    void jogadaOusadaPedeUmaLinhaOusada() {
        Meta meta = new JogadaOusada(0.25);
        assertEquals("Feche uma linha com a estabilidade abaixo de 25 %", meta.descricao());
        meta.registrar(linhas(1, "ACO"));
        assertFalse(meta.cumprida());
        meta.registrar(new Lance(1, "ACO", true, 0.1, false));
        assertTrue(meta.cumprida());
    }

    @Test
    void quadroAvisaUmaVezSoQuandoAMetaECumprida() {
        QuadroMetas quadro = metaOusada();
        assertFalse(quadro.registrar(linhas(1, "MADEIRA")));
        assertTrue(quadro.registrar(new Lance(1, "MADEIRA", true, 0.1, false)));
        assertFalse(quadro.registrar(new Lance(1, "MADEIRA", true, 0.1, false)), "o bônus sai uma vez só");
    }

    @Test
    void mesmaSementeSorteiaAsMesmasMetas() {
        for (int nivel = 1; nivel <= 20; nivel++) {
            QuadroMetas a = new QuadroMetas(99, 0.25);
            QuadroMetas b = new QuadroMetas(99, 0.25);
            a.comecarNivel(nivel, "ACO");
            b.comecarNivel(nivel, "ACO");
            assertEquals(a.atual().descricao(), b.atual().descricao());
        }
    }

    @Test
    void osQuatroTiposAparecem() {
        Set<Class<?>> tipos = new HashSet<>();
        QuadroMetas quadro = new QuadroMetas(1, 0.25);
        for (int nivel = 1; nivel <= 60; nivel++) {
            quadro.comecarNivel(nivel, "CONCRETO");
            assertNotNull(quadro.atual());
            tipos.add(quadro.atual().getClass());
        }
        assertEquals(4, tipos.size());
    }

    @Test
    void bonusCresceComONivel() {
        assertEquals(500, QuadroMetas.bonus(1));
        assertEquals(3500, QuadroMetas.bonus(7));
    }

    /** Um quadro cuja meta é a jogada ousada, achado procurando o nível. */
    private static QuadroMetas metaOusada() {
        for (int nivel = 1; nivel < 100; nivel++) {
            QuadroMetas quadro = new QuadroMetas(5, 0.25);
            quadro.comecarNivel(nivel, "ACO");
            if (quadro.atual() instanceof JogadaOusada) {
                return quadro;
            }
        }
        throw new AssertionError("nenhum nível sorteou a jogada ousada");
    }
}
