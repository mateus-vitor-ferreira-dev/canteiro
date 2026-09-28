package canteiro.modelo;

import canteiro.modelo.materiais.Madeira;
import canteiro.modelo.pecas.Forma;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Confere o comando de reservar dentro da partida (RF10).
 *
 * @author Mateus Vitor Ferreira
 * @version 0.1.0
 */
class ReservaNoMotorTest {

    private final Madeira madeira = new Madeira();

    private MotorJogo motor(Forma... formas) {
        MotorJogo motor = new MotorJogo(Dificuldade.FACIL, new FonteFixa(madeira, formas));
        motor.avancarCiclo();
        return motor;
    }

    @Test
    void primeiraReservaGuardaAAtualETrazAProximaDaFila() {
        MotorJogo motor = motor(Forma.T, Forma.I, Forma.O);
        assertNull(motor.pecaReservada());
        motor.aplicar(Comando.RESERVAR);
        assertEquals('T', motor.pecaReservada().letra());
        assertEquals('I', motor.pecaAtual().letra());
        assertEquals('O', motor.proximas(1).get(0).letra(), "a fila andou uma peça");
    }

    @Test
    void segundaReservaNaMesmaPecaEIgnorada() {
        MotorJogo motor = motor(Forma.T, Forma.I);
        motor.aplicar(Comando.RESERVAR);
        assertFalse(motor.podeReservar());
        motor.aplicar(Comando.RESERVAR);
        assertEquals('I', motor.pecaAtual().letra());
        assertEquals('T', motor.pecaReservada().letra());
    }

    @Test
    void depoisDeFixarAReservaTrocaPelaGuardada() {
        MotorJogo motor = motor(Forma.T, Forma.I, Forma.O);
        motor.aplicar(Comando.RESERVAR);
        motor.aplicar(Comando.QUEDA_INSTANTANEA);
        assertTrue(motor.podeReservar(), "peça nova liberou a troca");
        assertEquals('O', motor.pecaAtual().letra());
        motor.aplicar(Comando.RESERVAR);
        assertEquals('T', motor.pecaAtual().letra());
        assertEquals('O', motor.pecaReservada().letra());
    }

    @Test
    void pecaQueSaiDaReservaNasceNoTopo() {
        MotorJogo motor = motor(Forma.T, Forma.I, Forma.O);
        motor.aplicar(Comando.RESERVAR);
        motor.aplicar(Comando.QUEDA_INSTANTANEA);
        motor.aplicar(Comando.RESERVAR);
        assertEquals(PecaEmQueda.LINHA_NASCIMENTO, motor.celulasPecaAtual().get(0).linha());
    }

    @Test
    void reservarPausadoNaoFazNada() {
        MotorJogo motor = motor(Forma.T, Forma.I);
        motor.aplicar(Comando.PAUSAR);
        motor.aplicar(Comando.RESERVAR);
        assertNull(motor.pecaReservada());
        assertTrue(motor.podeReservar());
    }
}
