package canteiro.modelo.pecas;

import canteiro.modelo.Celula;
import canteiro.modelo.materiais.Aco;
import canteiro.modelo.materiais.Madeira;
import canteiro.modelo.materiais.Material;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Confere as sete formas em todas as rotações.
 *
 * @author Mateus Vitor Ferreira
 * @version 0.1.0
 */
class PecaTest {

    private static final double TOLERANCIA = 1e-9;
    private final Material aco = new Aco();

    @ParameterizedTest(name = "{0}: 4 blocos conexos em cada rotação (RN02)")
    @EnumSource(Forma.class)
    void todaRotacaoTemQuatroBlocosConexos(Forma forma) {
        Peca peca = forma.criar(aco);
        for (int r = 0; r < peca.quantidadeRotacoes(); r++) {
            List<Celula> celulas = peca.celulasNaRotacao(r);
            assertEquals(Peca.BLOCOS_POR_PECA, celulas.size(), forma + " rotação " + r);
            assertTrue(conexas(celulas), forma + " rotação " + r + " tem blocos soltos");
        }
    }

    @ParameterizedTest(name = "{0}: tem 4 rotações dentro do quadrado")
    @EnumSource(Forma.class)
    void temQuatroRotacoesDentroDoQuadrado(Forma forma) {
        Peca peca = forma.criar(aco);
        assertEquals(4, peca.quantidadeRotacoes());
        for (int r = 0; r < peca.quantidadeRotacoes(); r++) {
            for (Celula c : peca.celulasNaRotacao(r)) {
                assertTrue(c.linha() >= 0 && c.linha() < peca.tamanho(), forma + " saiu do quadrado");
                assertTrue(c.coluna() >= 0 && c.coluna() < peca.tamanho(), forma + " saiu do quadrado");
            }
        }
    }

    @ParameterizedTest(name = "{0}: girar 4 vezes volta ao início")
    @EnumSource(Forma.class)
    void girarQuatroVezesVoltaAoInicio(Forma forma) {
        Peca peca = forma.criar(aco);
        List<Celula> inicial = peca.celulas();
        for (int i = 0; i < 4; i++) {
            peca.girarHorario();
        }
        assertEquals(0, peca.rotacao());
        assertEquals(inicial, peca.celulas());
    }

    @ParameterizedTest(name = "{0}: anti-horário desfaz o horário")
    @EnumSource(Forma.class)
    void antiHorarioDesfazOHorario(Forma forma) {
        Peca peca = forma.criar(aco);
        for (int r = 0; r < 4; r++) {
            List<Celula> antes = peca.celulas();
            peca.girarHorario();
            peca.girarAntiHorario();
            assertEquals(antes, peca.celulas(), forma + " rotação " + r);
            peca.girarHorario();
        }
    }

    @Test
    void antiHorarioAPartirDoInicioVaiParaAUltimaRotacao() {
        Peca peca = new PecaT(aco);
        peca.girarAntiHorario();
        assertEquals(3, peca.rotacao());
    }

    @ParameterizedTest(name = "{0}: a forma muda ao girar, menos a O")
    @EnumSource(value = Forma.class, names = "O", mode = EnumSource.Mode.EXCLUDE)
    void girarMudaAsCelulas(Forma forma) {
        Peca peca = forma.criar(aco);
        List<Celula> antes = peca.celulas();
        peca.girarHorario();
        assertNotEquals(antes, peca.celulas(), forma.toString());
    }

    @Test
    void aOEIgualEmTodasAsRotacoes() {
        Peca o = new PecaO(aco);
        for (int r = 1; r < 4; r++) {
            assertEquals(o.celulasNaRotacao(0), o.celulasNaRotacao(r));
        }
    }

    @Test
    void asSeteFormasSaoDiferentesEntreSiEmQualquerRotacao() {
        Set<Set<List<Celula>>> vistas = new HashSet<>();
        for (Forma forma : Forma.values()) {
            Peca peca = forma.criar(aco);
            Set<List<Celula>> rotacoes = new HashSet<>();
            for (int r = 0; r < 4; r++) {
                rotacoes.add(normalizada(peca.celulasNaRotacao(r)));
            }
            for (Set<List<Celula>> outra : vistas) {
                Set<List<Celula>> comum = new HashSet<>(outra);
                comum.retainAll(rotacoes);
                assertTrue(comum.isEmpty(), forma + " é uma rotação de outra forma");
            }
            vistas.add(rotacoes);
        }
        assertEquals(Forma.values().length, vistas.size());
    }

    @Test
    void aTSegueOPadraoSrs() {
        Peca t = new PecaT(aco);
        assertEquals(List.of(new Celula(0, 1), new Celula(1, 0), new Celula(1, 1), new Celula(1, 2)), t.celulas());
        t.girarHorario();
        assertEquals(List.of(new Celula(0, 1), new Celula(1, 1), new Celula(1, 2), new Celula(2, 1)), t.celulas());
    }

    @Test
    void tamanhoDoQuadradoDependeDaForma() {
        assertEquals(4, new PecaI(aco).tamanho());
        assertEquals(2, new PecaO(aco).tamanho());
        assertEquals(3, new PecaT(aco).tamanho());
    }

    @ParameterizedTest(name = "{0}: criada pela forma certa, com a letra certa")
    @EnumSource(Forma.class)
    void formaCriaAPecaDaLetraCerta(Forma forma) {
        Peca peca = forma.criar(aco);
        assertEquals(forma.name().charAt(0), peca.letra());
        assertEquals("Peca" + forma.name(), peca.getClass().getSimpleName());
    }

    @Test
    void pecaTemUmMaterialSemSerUm() {
        Peca peca = new PecaL(aco);
        assertSame(aco, peca.material());
        assertFalse(Material.class.isAssignableFrom(Peca.class));
    }

    @Test
    void massaDaPecaEQuatroBlocosDoMaterial() {
        assertEquals(4 * aco.massaPorBloco(), new PecaI(aco).massaTotal(), TOLERANCIA);
        Material madeira = new Madeira();
        assertEquals(4 * madeira.massaPorBloco(), new PecaS(madeira).massaTotal(), TOLERANCIA);
    }

    @Test
    void semMaterialEErro() {
        assertThrows(NullPointerException.class, () -> new PecaT(null));
    }

    @Test
    void celulasNaoPodemSerAlteradasPorFora() {
        Peca peca = new PecaJ(aco);
        assertThrows(UnsupportedOperationException.class, () -> peca.celulas().clear());
    }

    @Test
    void descricaoMostraLetraEMaterial() {
        assertEquals("T de Aço", new PecaT(aco).toString());
    }

    private static boolean conexas(List<Celula> celulas) {
        Set<Celula> restantes = new HashSet<>(celulas);
        Deque<Celula> aVisitar = new ArrayDeque<>();
        aVisitar.push(celulas.get(0));
        restantes.remove(celulas.get(0));
        while (!aVisitar.isEmpty()) {
            Celula atual = aVisitar.pop();
            for (Celula vizinha : List.of(atual.deslocada(1, 0), atual.deslocada(-1, 0),
                    atual.deslocada(0, 1), atual.deslocada(0, -1))) {
                if (restantes.remove(vizinha)) {
                    aVisitar.push(vizinha);
                }
            }
        }
        return restantes.isEmpty();
    }

    /** Leva a forma para o canto superior esquerdo, para comparar formas sem depender da posição. */
    private static List<Celula> normalizada(List<Celula> celulas) {
        int linhaMinima = celulas.stream().mapToInt(Celula::linha).min().orElse(0);
        int colunaMinima = celulas.stream().mapToInt(Celula::coluna).min().orElse(0);
        return celulas.stream()
                .map(c -> c.deslocada(-linhaMinima, -colunaMinima))
                .sorted((a, b) -> a.linha() != b.linha() ? a.linha() - b.linha() : a.coluna() - b.coluna())
                .collect(Collectors.toList());
    }
}
