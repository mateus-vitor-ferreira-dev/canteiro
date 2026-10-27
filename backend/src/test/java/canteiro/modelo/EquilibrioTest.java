package canteiro.modelo;

import canteiro.modelo.constantes.Dimensoes;
import canteiro.modelo.fisica.AnalisadorEstrutural;
import canteiro.modelo.fisica.Estabilidade;
import canteiro.modelo.materiais.Aco;
import canteiro.modelo.materiais.Alvenaria;
import canteiro.modelo.materiais.Concreto;
import canteiro.modelo.materiais.Madeira;
import canteiro.modelo.pecas.Forma;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * A física do equilíbrio rodando dentro do {@link MotorJogo} (RN07 a RN13).
 *
 * @author Mateus Vitor Ferreira
 * @version 0.1.0
 */
class EquilibrioTest {

    private final Madeira madeira = new Madeira();
    private final Aco aco = new Aco();

    @Test
    void incrementalCoincideComAVarreduraApos500Pecas() {
        Random sorteio = new Random(42);
        int pecas = 0;
        int partidas = 0;
        while (pecas < 500) {
            MotorJogo motor = new MotorJogo(Dificuldade.FACIL,
                    FonteLista.sorteada(partidas++, List.of(madeira, new Alvenaria(), new Concreto(), aco)));
            motor.avancarCiclo();
            while (!motor.estado().encerrada() && pecas < 500) {
                jogarAoAcaso(motor, sorteio);
                pecas++;
                if (!motor.analisador().vazia()) {
                    assertEquals(AnalisadorEstrutural.centroPorVarredura(motor.tabuleiro()),
                            motor.analisador().centroDeMassa(), 0.001, "depois da peça " + pecas);
                }
            }
        }
        assertTrue(partidas >= 1);
    }

    @Test
    void previaConfereComOQueAcontecePorQuinhentasPecas() {
        Random sorteio = new Random(7);
        int pecas = 0;
        int partidas = 0;
        while (pecas < 500) {
            MotorJogo motor = new MotorJogo(Dificuldade.DIFICIL,
                    FonteLista.sorteada(partidas++, List.of(madeira, new Alvenaria(), new Concreto(), aco)));
            motor.avancarCiclo();
            Registro registro = new Registro();
            motor.inscrever(registro);
            while (!motor.estado().encerrada() && pecas < 500) {
                mexerAoAcaso(motor, sorteio);
                Estabilidade previa = motor.previa();
                int nivel = motor.placar().nivel();
                long colapsosAntes = registro.colapsos();
                motor.aplicar(Comando.QUEDA_INSTANTANEA);
                pecas++;
                if (motor.placar().nivel() != nivel) {
                    continue; // o limite mudou no meio da jogada
                }
                boolean desabou = registro.colapsos() > colapsosAntes;
                assertEquals(previa.passouDoLimite(), desabou, "a prévia avisa o colapso, peça " + pecas);
                if (!desabou) {
                    assertEquals(motor.estabilidade().centroDeMassa(), previa.centroDeMassa(), 1e-6);
                    assertEquals(motor.estabilidade().eixo(), previa.eixo(), 1e-9);
                    assertEquals(motor.estabilidade().indice(), previa.indice(), 1e-6);
                }
            }
        }
    }

    @Test
    void previaDescontaAsLinhasQueAPecaCompleta() {
        // duas I de madeira deixam o fundo com as colunas 8 e 9 vazias; a O de aço fecha a linha
        MotorJogo motor = new MotorJogo(Dificuldade.FACIL, FonteLista.fixa()
                .depois(Forma.I, madeira).depois(Forma.I, madeira).depois(Forma.O, aco));
        motor.avancarCiclo();
        derrubar(motor, -3);
        derrubar(motor, 1);
        for (int i = 0; i < 4; i++) {
            motor.aplicar(Comando.DIREITA);
        }
        Estabilidade previa = motor.previa();
        motor.aplicar(Comando.QUEDA_INSTANTANEA);

        assertEquals(1, motor.placar().linhas(), "a linha de baixo fechou");
        assertEquals(9.0, previa.centroDeMassa(), 1e-9, "sobra só a metade de cima da O, nas colunas 8 e 9");
        assertEquals(9.0, previa.eixo(), 1e-9);
        assertEquals(motor.estabilidade().indice(), previa.indice(), 1e-9);
    }

    @Test
    void semPecaCaindoNaoHaPrevia() {
        MotorJogo motor = new MotorJogo(Dificuldade.FACIL, FonteLista.fixa().depois(Forma.O, aco));
        assertNull(motor.previa());
        motor.avancarCiclo();
        assertTrue(motor.previa().indice() == 1.0, "uma peça sozinha está sobre a própria base");
    }

    @Test
    void acoNumaSoLateralDerrubaEmAteDozePecas() {
        MotorJogo motor = baseDeMadeiraEAcoNaEsquerda();
        Registro registro = new Registro();
        motor.inscrever(registro);
        int pecas = 0;
        while (registro.colapsos() == 0 && pecas < 12) {
            derrubar(motor, -4);
            pecas++;
        }
        assertEquals(1, registro.colapsos(), "caiu com " + pecas + " peças de aço");
        assertEquals(1, motor.placar().colapsos());
    }

    @Test
    void colapsoDescontaPontosSemFicarNegativo() {
        MotorJogo motor = baseDeMadeiraEAcoNaEsquerda();
        derrubar(motor, -4);
        assertEquals(1, motor.placar().colapsos());
        assertEquals(0, motor.placar().pontuacao());
    }

    @Test
    void colapsoComAPilhaAltaDemaisAcabaAPartida() {
        MotorJogo motor = baseDeMadeiraEAcoNaEsquerda();
        Registro registro = new Registro();
        motor.inscrever(registro);
        for (int i = 0; i < Dimensoes.LINHAS && !motor.estado().encerrada(); i++) {
            derrubar(motor, -4);
        }
        assertEquals(EstadoPartida.FIM_DE_JOGO, motor.estado());
        assertTrue(motor.tabuleiro().alturaPilha() > Dimensoes.ALTURA_LIMITE_COLAPSO);
        EventoPartida.Tipo ultimo = registro.eventos.get(registro.eventos.size() - 1).tipo();
        EventoPartida.Tipo penultimo = registro.eventos.get(registro.eventos.size() - 2).tipo();
        assertEquals(EventoPartida.Tipo.FIM_DE_JOGO, ultimo);
        assertEquals(EventoPartida.Tipo.COLAPSO, penultimo);
    }

    @Test
    void pecaSozinhaNoCantoEstaEquilibrada() {
        MotorJogo motor = new MotorJogo(Dificuldade.DIFICIL, FonteLista.fixa().depois(Forma.O, aco));
        motor.avancarCiclo();
        derrubar(motor, -4);
        assertEquals(0.0, motor.estabilidade().desvio(), 1e-9);
        assertEquals(0, motor.placar().colapsos());
    }

    /** Duas I de madeira no chão (colunas 0 a 3 e 5 a 8) e depois só O de aço. */
    private MotorJogo baseDeMadeiraEAcoNaEsquerda() {
        MotorJogo motor = new MotorJogo(Dificuldade.DIFICIL, FonteLista.fixa()
                .depois(Forma.I, madeira).depois(Forma.I, madeira).depois(Forma.O, aco));
        motor.avancarCiclo();
        derrubar(motor, -3);
        derrubar(motor, 2);
        assertEquals(0, motor.placar().colapsos(), "a base de madeira está equilibrada");
        return motor;
    }

    private static void derrubar(MotorJogo motor, int deslocamento) {
        for (int i = 0; i < Math.abs(deslocamento); i++) {
            motor.aplicar(deslocamento < 0 ? Comando.ESQUERDA : Comando.DIREITA);
        }
        motor.aplicar(Comando.QUEDA_INSTANTANEA);
    }

    private static void mexerAoAcaso(MotorJogo motor, Random sorteio) {
        Comando[] movimentos = {Comando.ESQUERDA, Comando.DIREITA, Comando.GIRAR_HORARIO, Comando.GIRAR_ANTI_HORARIO};
        for (int i = sorteio.nextInt(8); i > 0; i--) {
            motor.aplicar(movimentos[sorteio.nextInt(movimentos.length)]);
        }
    }

    private static void jogarAoAcaso(MotorJogo motor, Random sorteio) {
        Comando[] movimentos = {Comando.ESQUERDA, Comando.DIREITA, Comando.GIRAR_HORARIO, Comando.GIRAR_ANTI_HORARIO};
        for (int i = sorteio.nextInt(8); i > 0; i--) {
            motor.aplicar(movimentos[sorteio.nextInt(movimentos.length)]);
        }
        motor.aplicar(Comando.QUEDA_INSTANTANEA);
    }

    /** Observador que só anota os eventos. */
    private static final class Registro implements ObservadorPartida {
        private final List<EventoPartida> eventos = new ArrayList<>();

        @Override
        public void estadoMudou(MotorJogo motor) {
            // só os eventos interessam aqui
        }

        @Override
        public void eventoOcorreu(EventoPartida evento) {
            eventos.add(evento);
        }

        long colapsos() {
            return eventos.stream().filter(e -> e.tipo() == EventoPartida.Tipo.COLAPSO).count();
        }
    }
}
