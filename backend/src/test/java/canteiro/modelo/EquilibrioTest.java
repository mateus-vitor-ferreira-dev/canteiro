package canteiro.modelo;

import canteiro.modelo.constantes.Dimensoes;
import canteiro.modelo.fisica.AnalisadorEstrutural;
import canteiro.modelo.fisica.Estabilidade;
import canteiro.modelo.materiais.Aco;
import canteiro.modelo.materiais.Alvenaria;
import canteiro.modelo.materiais.Concreto;
import canteiro.modelo.materiais.Madeira;
import canteiro.modelo.materiais.Material;
import canteiro.modelo.pecas.Forma;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
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
                Previa jogada = motor.previa();
                Estabilidade previa = jogada.estabilidade();
                int eventosAntes = registro.eventos.size();
                int nivel = motor.placar().nivel();
                long colapsosAntes = registro.colapsos();
                motor.aplicar(Comando.QUEDA_INSTANTANEA);
                pecas++;
                if (motor.placar().nivel() != nivel) {
                    continue; // o limite mudou no meio da jogada
                }
                boolean desabou = registro.colapsos() > colapsosAntes;
                assertEquals(previa.passouDoLimite(), desabou, "a prévia avisa o colapso, peça " + pecas);
                List<EventoPartida> daJogada = registro.eventos.subList(eventosAntes, registro.eventos.size());
                assertEquals(jogada.quebras(), origens(daJogada, EventoPartida.Tipo.QUEBRA),
                        "a prévia avisa o balanço, peça " + pecas);
                assertEquals(jogada.esmagados(), esmagadosAntesDoColapso(daJogada),
                        "a prévia avisa o esmagamento, peça " + pecas);
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
        Estabilidade previa = motor.previa().estabilidade();
        motor.aplicar(Comando.QUEDA_INSTANTANEA);

        assertEquals(1, motor.placar().linhas(), "a linha de baixo fechou");
        assertEquals(9.0, previa.centroDeMassa(), 1e-9, "sobra só a metade de cima da O, nas colunas 8 e 9");
        assertEquals(9.0, previa.eixo(), 1e-9);
        assertEquals(motor.estabilidade().indice(), previa.indice(), 1e-9);
    }

    @Test
    void fecharLinhaComAEstruturaPertoDeCairEhObraOusada() {
        EventoPartida linhas = fecharFundoComO(new Aco(1000, 0x4E5A6B));
        assertTrue(linhas.ousada(), "a O pesadíssima na ponta tirou a obra do eixo antes de a linha sumir");
        assertEquals(Pontuacao.pontos(1, 1, madeira, true, 0), linhas.pontos(), "a madeira predomina na linha");
    }

    @Test
    void fecharLinhaComAEstruturaFirmeNaoEhOusada() {
        EventoPartida linhas = fecharFundoComO(madeira);
        assertFalse(linhas.ousada());
        assertEquals(Pontuacao.pontos(1, 1, madeira), linhas.pontos());
    }

    @Test
    void sequenciaSobeComPecasSeguidasQueFecharamLinhaEZeraAoErrar() {
        // cinco O fecham as duas linhas de baixo; a quinta inaugura a sequência
        MotorJogo motor = new MotorJogo(Dificuldade.FACIL, FonteLista.fixa().depois(Forma.O, madeira));
        motor.avancarCiclo();
        for (int coluna = 0; coluna < Dimensoes.COLUNAS; coluna += 2) {
            derrubar(motor, coluna - 4);
        }
        assertEquals(1, motor.placar().sequencia());
        derrubar(motor, 0);
        assertEquals(0, motor.placar().sequencia(), "a peça não fechou linha");
    }

    /** Duas I de madeira no fundo, nas colunas 0 a 7, e uma O do material dado fechando a linha. */
    private EventoPartida fecharFundoComO(Material material) {
        MotorJogo motor = new MotorJogo(Dificuldade.FACIL, FonteLista.fixa()
                .depois(Forma.I, madeira).depois(Forma.I, madeira).depois(Forma.O, material));
        motor.avancarCiclo();
        derrubar(motor, -3);
        derrubar(motor, 1);
        Registro registro = new Registro();
        motor.inscrever(registro);
        derrubar(motor, 4);
        assertEquals(0, motor.placar().colapsos(), "sem a linha de baixo, a metade de cima da O está na própria base");
        return registro.eventos.stream().filter(e -> e.tipo() == EventoPartida.Tipo.LINHAS_ELIMINADAS)
                .findFirst().orElseThrow();
    }

    @Test
    void semPecaCaindoNaoHaPrevia() {
        MotorJogo motor = new MotorJogo(Dificuldade.FACIL, FonteLista.fixa().depois(Forma.O, aco));
        assertNull(motor.previa());
        motor.avancarCiclo();
        assertTrue(motor.previa().estabilidade().indice() == 1.0, "uma peça sozinha está sobre a própria base");
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
    private static List<Celula> origens(List<EventoPartida> eventos, EventoPartida.Tipo tipo) {
        return eventos.stream().filter(e -> e.tipo() == tipo)
                .flatMap(e -> e.quedas().stream().map(Queda::origem)).toList();
    }

    private static List<Celula> esmagadosAntesDoColapso(List<EventoPartida> eventos) {
        List<Celula> esmagados = new ArrayList<>();
        for (EventoPartida evento : eventos) {
            if (evento.tipo() == EventoPartida.Tipo.COLAPSO) {
                break;
            }
            if (evento.tipo() == EventoPartida.Tipo.ESMAGAMENTO) {
                esmagados.addAll(evento.celulas());
            }
        }
        return esmagados;
    }

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
