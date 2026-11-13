package canteiro.controle;

import canteiro.modelo.Comando;
import canteiro.modelo.Dificuldade;
import canteiro.modelo.EventoPartida;
import canteiro.modelo.MotorJogo;
import canteiro.modelo.ObservadorPartida;
import canteiro.modelo.estruturas.GeradorPecas;
import canteiro.modelo.materiais.CatalogoMateriais;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Joga partidas de verdade com o registro inscrito e confere o que ele anotou (RF25).
 *
 * @author Mateus Vitor Ferreira
 * @version 0.1.0
 */
class RegistroRelatorioTest {

    private static final long SEMENTE = 42;
    private static final int MAIS_MOVIMENTOS_POR_PECA = 8;
    private static final int LIMITE_DE_PECAS = 500;
    private static final Comando[] MOVIMENTOS = {
        Comando.ESQUERDA, Comando.DIREITA, Comando.GIRAR_HORARIO, Comando.GIRAR_ANTI_HORARIO};

    @Test
    void anotaUmIndicePorPecaEContaOsMateriais() {
        MotorJogo motor = novoMotor();
        RegistroRelatorio registro = new RegistroRelatorio(motor);
        motor.inscrever(registro);
        int[] fixadas = new int[1];
        motor.inscrever(contador(EventoPartida.Tipo.PECA_FIXADA, fixadas));

        jogarAoAcaso(motor);

        assertTrue(fixadas[0] > 0);
        assertEquals(fixadas[0], registro.indices().size());
        assertEquals(fixadas[0], registro.materiais().values().stream().mapToInt(Integer::intValue).sum());
        assertTrue(registro.indices().stream().allMatch(indice -> indice >= 0 && indice <= 1));
    }

    @Test
    void marcaOsColapsosPeloNumeroDaPeca() {
        MotorJogo motor = novoMotor();
        RegistroRelatorio registro = new RegistroRelatorio(motor);
        motor.inscrever(registro);

        jogarAoAcaso(motor);

        List<Integer> colapsos = registro.colapsos();
        assertFalse(colapsos.isEmpty(), "jogar ao acaso no difícil tem que derrubar a obra");
        assertEquals(motor.placar().colapsos(), colapsos.size());
        assertTrue(colapsos.stream().allMatch(peca -> peca >= 1 && peca <= registro.indices().size()));
    }

    @Test
    void semPecaFixadaORelatorioEstaVazio() {
        RegistroRelatorio registro = new RegistroRelatorio(novoMotor());

        assertTrue(registro.indices().isEmpty());
        assertTrue(registro.colapsos().isEmpty());
        assertTrue(registro.materiais().isEmpty());
    }

    private static MotorJogo novoMotor() {
        return new MotorJogo(Dificuldade.DIFICIL,
                new GeradorPecas(SEMENTE, CatalogoMateriais.padrao(), Dificuldade.DIFICIL.nivelInicial()));
    }

    /** Joga até o fim, com movimentos sorteados por uma semente fixa: a obra desaba algumas vezes. */
    private static void jogarAoAcaso(MotorJogo motor) {
        Random sorteio = new Random(SEMENTE);
        motor.avancarCiclo();
        for (int peca = 0; peca < LIMITE_DE_PECAS && !motor.estado().encerrada(); peca++) {
            for (int i = sorteio.nextInt(MAIS_MOVIMENTOS_POR_PECA); i > 0; i--) {
                motor.aplicar(MOVIMENTOS[sorteio.nextInt(MOVIMENTOS.length)]);
            }
            motor.aplicar(Comando.QUEDA_INSTANTANEA);
        }
    }

    private static ObservadorPartida contador(EventoPartida.Tipo tipo, int[] contagem) {
        return new ObservadorPartida() {
            @Override
            public void estadoMudou(MotorJogo motor) {
                // só os eventos interessam
            }

            @Override
            public void eventoOcorreu(EventoPartida evento) {
                if (evento.tipo() == tipo) {
                    contagem[0]++;
                }
            }
        };
    }
}
