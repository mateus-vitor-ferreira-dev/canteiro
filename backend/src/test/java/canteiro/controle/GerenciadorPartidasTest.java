package canteiro.controle;

import canteiro.modelo.Dificuldade;
import canteiro.modelo.estruturas.FonteSimples;
import canteiro.modelo.materiais.CatalogoMateriais;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * As partidas guardadas pelo id.
 *
 * @author Mateus Vitor Ferreira
 * @version 0.1.0
 */
class GerenciadorPartidasTest {

    private final CatalogoMateriais catalogo = CatalogoMateriais.padrao();
    private final GerenciadorPartidas partidas = new GerenciadorPartidas(
            d -> new FonteSimples(1, catalogo, d.materiaisLiberados()));

    @Test
    void criaPartidasComIdsDiferentes() {
        Set<String> ids = new HashSet<>();
        for (int i = 0; i < 200; i++) {
            ids.add(partidas.criar(Dificuldade.NORMAL).id());
        }
        assertEquals(200, ids.size());
        assertEquals(200, partidas.quantidade());
        assertTrue(ids.stream().allMatch(id -> id.matches("[a-z0-9]{8}")));
    }

    @Test
    void buscaPeloId() {
        SessaoPartida sessao = partidas.criar(Dificuldade.FACIL);
        assertSame(sessao, partidas.buscar(sessao.id()).orElseThrow());
        assertTrue(partidas.buscar("naoexiste").isEmpty());
    }

    @Test
    void removerEsqueceAPartida() {
        SessaoPartida sessao = partidas.criar(Dificuldade.FACIL);
        sessao.iniciar();
        partidas.remover(sessao.id());
        assertTrue(partidas.buscar(sessao.id()).isEmpty());
    }

    @Test
    void encerrarTodasEsvazia() {
        partidas.criar(Dificuldade.FACIL).iniciar();
        partidas.criar(Dificuldade.DIFICIL);
        partidas.encerrarTodas();
        assertEquals(0, partidas.quantidade());
    }
}
