package canteiro.modelo.estruturas;

import canteiro.modelo.Progressao;
import canteiro.modelo.materiais.CatalogoMateriais;
import canteiro.modelo.pecas.Forma;
import canteiro.modelo.pecas.Peca;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Confere o método da sacola (RN04), a fila das próximas (RF05) e o sorteio
 * dos materiais liberados (RN03).
 *
 * @author Mateus Vitor Ferreira
 * @version 0.1.0
 */
class GeradorPecasTest {

    private static final int PECAS = 200;
    private static final int FORMAS = Forma.values().length;

    private final CatalogoMateriais catalogo = CatalogoMateriais.padrao();

    @Test
    void cadaGrupoDeSeteTemTodasAsFormas() {
        GeradorPecas gerador = new GeradorPecas(42, catalogo, 1);
        List<Character> letras = sequencia(gerador, PECAS - PECAS % FORMAS);
        for (int inicio = 0; inicio < letras.size(); inicio += FORMAS) {
            Set<Character> sacola = new HashSet<>(letras.subList(inicio, inicio + FORMAS));
            assertEquals(FORMAS, sacola.size(), "sacola que começa na peça " + inicio);
        }
    }

    @Test
    void nenhumaFormaVoltaAntesDeAsOutrasSeisSairem() {
        List<Character> letras = sequencia(new GeradorPecas(7, catalogo, 1), PECAS);
        for (int i = 0; i < letras.size(); i++) {
            int sacola = i / FORMAS;
            for (int j = i + 1; j < letras.size() && j / FORMAS == sacola; j++) {
                assertNotEquals(letras.get(i), letras.get(j), "peças " + i + " e " + j);
            }
        }
    }

    @Test
    void espiarNaoTiraDaFila() {
        GeradorPecas gerador = new GeradorPecas(3, catalogo, 1);
        List<Peca> proximas = gerador.espiar(3);
        assertEquals(3, proximas.size());
        assertEquals(proximas, gerador.espiar(3), "espiar de novo devolve as mesmas");
        for (Peca esperada : proximas) {
            assertSame(esperada, gerador.proxima());
        }
    }

    @Test
    void mesmaSementeDaAMesmaSequencia() {
        assertEquals(descricao(new GeradorPecas(99, catalogo, 5)), descricao(new GeradorPecas(99, catalogo, 5)));
    }

    @Test
    void sementesDiferentesDaoSequenciasDiferentes() {
        assertNotEquals(descricao(new GeradorPecas(1, catalogo, 5)), descricao(new GeradorPecas(2, catalogo, 5)));
    }

    @Test
    void soSaemMateriaisLiberadosNoNivel() {
        GeradorPecas gerador = new GeradorPecas(5, catalogo, 1);
        List<String> leves = Progressao.materiaisLiberados(1);
        for (int i = 0; i < PECAS; i++) {
            assertTrue(leves.contains(gerador.proxima().material().codigo()));
        }
    }

    @Test
    void subirDeNivelLiberaMateriaisNovos() {
        GeradorPecas gerador = new GeradorPecas(5, catalogo, 1);
        gerador.nivelMudou(5);
        Set<String> sorteados = new HashSet<>();
        for (int i = 0; i < PECAS; i++) {
            sorteados.add(gerador.proxima().material().codigo());
        }
        assertEquals(new HashSet<>(Progressao.materiaisLiberados(5)), sorteados);
    }

    @Test
    void pecasJaNaFilaMantemOMaterial() {
        GeradorPecas gerador = new GeradorPecas(5, catalogo, 1);
        List<Peca> naFila = gerador.espiar(3);
        gerador.nivelMudou(5);
        for (Peca esperada : naFila) {
            assertSame(esperada, gerador.proxima());
        }
    }

    @Test
    void nivelInvalidoEErro() {
        assertThrows(IllegalArgumentException.class, () -> new GeradorPecas(1, catalogo, 0));
    }

    private static List<Character> sequencia(GeradorPecas gerador, int quantidade) {
        List<Character> letras = new ArrayList<>();
        for (int i = 0; i < quantidade; i++) {
            letras.add(gerador.proxima().letra());
        }
        return letras;
    }

    private static List<String> descricao(GeradorPecas gerador) {
        List<String> pecas = new ArrayList<>();
        for (int i = 0; i < PECAS; i++) {
            Peca peca = gerador.proxima();
            pecas.add(peca.letra() + peca.material().codigo());
        }
        return pecas;
    }
}
