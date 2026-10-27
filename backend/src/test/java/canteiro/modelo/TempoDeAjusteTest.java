package canteiro.modelo;

import canteiro.modelo.constantes.Dimensoes;
import canteiro.modelo.materiais.Madeira;
import canteiro.modelo.pecas.Forma;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * O tempo de ajuste antes de fixar a peça encostada (RN16), no {@link MotorJogo}
 * e na {@link AtrasoFixacao}.
 *
 * <p>A espera é de 500 ms, ou 30 ciclos. A O nasce nas colunas 4 e 5 e chega ao
 * fundo depois de descer {@code FUNDO - 1} linhas.</p>
 *
 * @author Mateus Vitor Ferreira
 * @version 0.1.0
 */
class TempoDeAjusteTest {

    private static final int FUNDO = Dimensoes.LINHAS - 1;
    private static final int ESPERA = AtrasoFixacao.ciclosDeEspera();

    /** Uma O encostada no fundo, com uma T na fila. */
    private static MotorJogo oNoFundo() {
        MotorJogo motor = new MotorJogo(Dificuldade.FACIL, new FonteFixa(new Madeira(), Forma.O, Forma.T));
        motor.avancarCiclo();
        for (int i = 0; i < FUNDO - 1; i++) {
            motor.aplicar(Comando.DESCER);
        }
        return motor;
    }

    private static void avancar(MotorJogo motor, int ciclos) {
        for (int i = 0; i < ciclos; i++) {
            motor.avancarCiclo();
        }
    }

    @Test
    void esperaMeioSegundo() {
        assertEquals(30, ESPERA);
    }

    @Test
    void pecaEncostadaSoFixaDepoisDaEspera() {
        MotorJogo motor = oNoFundo();
        avancar(motor, ESPERA - 1);
        assertEquals('O', motor.pecaAtual().letra(), "ainda dá para ajustar");
        assertFalse(motor.tabuleiro().ocupada(FUNDO, 4));

        motor.avancarCiclo();
        assertEquals('T', motor.pecaAtual().letra());
        assertTrue(motor.tabuleiro().ocupada(FUNDO, 4));
    }

    @Test
    void moverDuranteAEsperaRecomecaAContagem() {
        MotorJogo motor = oNoFundo();
        avancar(motor, ESPERA - 5);
        motor.aplicar(Comando.ESQUERDA);
        avancar(motor, ESPERA - 1);
        assertEquals('O', motor.pecaAtual().letra());

        motor.avancarCiclo();
        assertTrue(motor.tabuleiro().ocupada(FUNDO, 3), "fixou onde foi deslizada");
    }

    @Test
    void girarTambemDaTempoDeAjuste() {
        MotorJogo motor = new MotorJogo(Dificuldade.FACIL, new FonteFixa(new Madeira(), Forma.T, Forma.O));
        motor.avancarCiclo();
        for (int i = 0; i < FUNDO - 1; i++) {
            motor.aplicar(Comando.DESCER);
        }
        avancar(motor, ESPERA - 5);
        motor.aplicar(Comando.GIRAR_ANTI_HORARIO);
        avancar(motor, ESPERA - 1);
        assertEquals('T', motor.pecaAtual().letra(), "girou no último instante e ainda dá para ajustar");
    }

    @Test
    void osRecomecosTemLimitePorPeca() {
        MotorJogo motor = oNoFundo();
        for (int i = 0; i < AtrasoFixacao.RECOMECOS_MAXIMOS; i++) {
            avancar(motor, 10);
            motor.aplicar(i % 2 == 0 ? Comando.ESQUERDA : Comando.DIREITA);
        }
        avancar(motor, 10);
        motor.aplicar(Comando.ESQUERDA);
        avancar(motor, ESPERA - 11);
        assertEquals('O', motor.pecaAtual().letra());

        motor.avancarCiclo();
        assertEquals('T', motor.pecaAtual().letra(), "o último movimento não recomeçou a espera");
    }

    @Test
    void quedaInstantaneaEDescerSobreOApoioFixamNaHora() {
        MotorJogo motor = oNoFundo();
        motor.aplicar(Comando.DESCER);
        assertEquals('T', motor.pecaAtual().letra());

        motor.aplicar(Comando.QUEDA_INSTANTANEA);
        assertEquals('O', motor.pecaAtual().letra());
    }

    @Test
    void pecaQueSaiDoApoioVoltaAContarDoZero() {
        AtrasoFixacao atraso = new AtrasoFixacao();
        for (int i = 0; i < ESPERA - 1; i++) {
            assertFalse(atraso.contarCiclo());
        }
        atraso.soltou();
        for (int i = 0; i < ESPERA - 1; i++) {
            assertFalse(atraso.contarCiclo());
        }
        assertTrue(atraso.contarCiclo());
    }

    @Test
    void movimentoSemEstarApoiadaNaoGastaRecomeco() {
        AtrasoFixacao atraso = new AtrasoFixacao();
        for (int i = 0; i < AtrasoFixacao.RECOMECOS_MAXIMOS + 5; i++) {
            atraso.movimentou();
        }
        atraso.contarCiclo();
        atraso.movimentou();
        for (int i = 0; i < ESPERA - 1; i++) {
            assertFalse(atraso.contarCiclo(), "ainda tinha recomeços");
        }
    }
}
