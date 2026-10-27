package canteiro.modelo.estruturas;

import canteiro.modelo.materiais.Madeira;
import canteiro.modelo.pecas.Forma;
import canteiro.modelo.pecas.Peca;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Confere a reserva de peça: uma troca por peça (RN05).
 *
 * @author Mateus Vitor Ferreira
 * @version 0.1.0
 */
class ReservaTest {

    private final Madeira madeira = new Madeira();
    private final Reserva reserva = new Reserva();

    @Test
    void comecaVaziaELiberada() {
        assertTrue(reserva.guardada().isEmpty());
        assertTrue(reserva.podeTrocar());
    }

    @Test
    void primeiraReservaGuardaEPedeAProximaDaFila() {
        Peca t = Forma.T.criar(madeira);
        assertEquals(Optional.empty(), reserva.trocar(t), "vazio: a próxima vem da fila");
        assertSame(t, reserva.guardada().orElseThrow());
    }

    @Test
    void trocaDevolveAReservadaEGuardaAAtual() {
        Peca t = Forma.T.criar(madeira);
        Peca i = Forma.I.criar(madeira);
        reserva.trocar(t);
        reserva.liberar();
        assertSame(t, reserva.trocar(i).orElseThrow());
        assertSame(i, reserva.guardada().orElseThrow());
    }

    @Test
    void segundaTrocaNaMesmaPecaERecusada() {
        reserva.trocar(Forma.T.criar(madeira));
        assertFalse(reserva.podeTrocar());
        assertThrows(IllegalStateException.class, () -> reserva.trocar(Forma.I.criar(madeira)));
    }

    @Test
    void travaLiberaQuandoUmaPecaNovaSaiDaFila() {
        reserva.trocar(Forma.T.criar(madeira));
        reserva.liberar();
        assertTrue(reserva.podeTrocar());
    }

    @Test
    void pecaGuardadaVoltaParaARotacaoInicial() {
        Peca t = Forma.T.criar(madeira);
        t.girarHorario();
        reserva.trocar(t);
        assertEquals(0, reserva.guardada().orElseThrow().rotacao());
    }

    @Test
    void pecaNulaEErro() {
        assertThrows(NullPointerException.class, () -> reserva.trocar(null));
        assertTrue(reserva.podeTrocar(), "a tentativa com nulo não trava a reserva");
    }
}
