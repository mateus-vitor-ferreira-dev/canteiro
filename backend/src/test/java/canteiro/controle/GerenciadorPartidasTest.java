package canteiro.controle;

import canteiro.modelo.Dificuldade;
import canteiro.modelo.estruturas.GeradorPecas;
import canteiro.modelo.materiais.CatalogoMateriais;
import org.junit.jupiter.api.Test;

import canteiro.modelo.ObservadorPartida;

import java.time.Duration;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
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
            d -> new GeradorPecas(1, catalogo, d.nivelInicial()));

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

    @Test
    void recusaPartidaAlemDoLimite() {
        GerenciadorPartidas comTeto = new GerenciadorPartidas(
                d -> new GeradorPecas(1, catalogo, d.nivelInicial()), 2, System::nanoTime);
        comTeto.criar(Dificuldade.FACIL);
        comTeto.criar(Dificuldade.FACIL);

        assertThrows(LimiteDePartidasException.class, () -> comTeto.criar(Dificuldade.FACIL));
        assertEquals(2, comTeto.quantidade());
    }

    @Test
    void descartaSoAsPartidasSemConexaoHaMaisTempoQueATolerancia() {
        AtomicLong agora = new AtomicLong();
        GerenciadorPartidas comRelogio = new GerenciadorPartidas(
                d -> new GeradorPecas(1, catalogo, d.nivelInicial()), GerenciadorPartidas.SEM_LIMITE, agora::get);
        SessaoPartida esquecida = comRelogio.criar(Dificuldade.FACIL);
        SessaoPartida conectada = comRelogio.criar(Dificuldade.FACIL);
        SessaoPartida fechadaAgora = comRelogio.criar(Dificuldade.FACIL);
        conectada.inscrever(motor -> { });
        ObservadorPartida aba = motor -> { };
        fechadaAgora.inscrever(aba);

        agora.addAndGet(Duration.ofMinutes(10).toNanos());
        fechadaAgora.desinscrever(aba);

        assertEquals(1, comRelogio.removerAbandonadas(Duration.ofMinutes(10)));
        assertTrue(comRelogio.buscar(esquecida.id()).isEmpty());
        assertTrue(comRelogio.buscar(conectada.id()).isPresent());
        assertTrue(comRelogio.buscar(fechadaAgora.id()).isPresent(), "a aba acabou de fechar: ainda pode voltar");
    }
}
