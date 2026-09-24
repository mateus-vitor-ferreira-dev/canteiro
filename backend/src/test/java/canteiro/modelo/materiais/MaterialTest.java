package canteiro.modelo.materiais;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Confere os valores dos quatro materiais e as validações da classe {@link Material}.
 *
 * @author Mateus Vitor Ferreira
 * @version 0.1.0
 */
class MaterialTest {

    private static final double TOLERANCIA = 1e-9;
    private static final int COR_MAXIMA = 0xFFFFFF;

    private final List<Material> doMaisLeveAoMaisPesado =
            List.of(new Madeira(), new Alvenaria(), new Concreto(), new Aco());

    @Test
    void massaDeUmBlocoEADensidadeVezesOVolume() {
        for (Material material : doMaisLeveAoMaisPesado) {
            assertEquals(material.densidade() * Material.VOLUME_BLOCO, material.massaPorBloco(), TOLERANCIA,
                    material + " (RN07)");
        }
    }

    @Test
    void cadaMaterialEMaisPesadoEValeMaisQueOAnterior() {
        for (int i = 1; i < doMaisLeveAoMaisPesado.size(); i++) {
            Material anterior = doMaisLeveAoMaisPesado.get(i - 1);
            Material atual = doMaisLeveAoMaisPesado.get(i);
            assertTrue(atual.densidade() > anterior.densidade(), atual + " deveria ser mais denso que " + anterior);
            assertTrue(atual.bonusLinha() > anterior.bonusLinha(), atual + " deveria valer mais que " + anterior);
        }
    }

    @Test
    void madeiraEAcoTemOsBonusDaEspecificacao() {
        assertEquals(10, new Madeira().bonusLinha());
        assertEquals(40, new Aco().bonusLinha());
    }

    @Test
    void coresSaoRgbValidasEDiferentesEntreSi() {
        long distintas = doMaisLeveAoMaisPesado.stream().mapToInt(Material::corRgb).distinct().count();
        assertEquals(doMaisLeveAoMaisPesado.size(), distintas);
        for (Material material : doMaisLeveAoMaisPesado) {
            assertTrue(material.corRgb() >= 0 && material.corRgb() <= COR_MAXIMA, material.toString());
        }
    }

    @Test
    void nomeApareceProntoParaOJogador() {
        assertEquals("Aço", new Aco().nome());
        assertEquals("Aço", new Aco().toString());
        assertEquals("ACO", new Aco().codigo());
    }

    @Test
    void doisMateriaisComOMesmoCodigoSaoIguais() {
        assertEquals(new Aco(), new Aco());
        assertEquals(new Aco().hashCode(), new Aco().hashCode());
        assertNotEquals(new Aco(), new Madeira());
    }

    @Test
    void recusaDensidadeQueNaoSejaPositiva() {
        assertThrows(IllegalArgumentException.class, () -> materialDeTeste("X", 0, 0));
        assertThrows(IllegalArgumentException.class, () -> materialDeTeste("X", -1, 0));
        assertThrows(IllegalArgumentException.class, () -> materialDeTeste("X", Double.NaN, 0));
    }

    @Test
    void recusaCorForaDoIntervaloRgb() {
        assertThrows(IllegalArgumentException.class, () -> materialDeTeste("X", 1, -1));
        assertThrows(IllegalArgumentException.class, () -> materialDeTeste("X", 1, COR_MAXIMA + 1));
    }

    @Test
    void recusaCodigoVazio() {
        assertThrows(IllegalArgumentException.class, () -> materialDeTeste(" ", 1, 0));
    }

    private static Material materialDeTeste(String codigo, double densidade, int cor) {
        return new Material(codigo, "Teste", densidade, cor) {
            @Override
            public int bonusLinha() {
                return 0;
            }
        };
    }
}
