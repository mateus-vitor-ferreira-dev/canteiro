package canteiro.modelo;

import canteiro.modelo.constantes.Dimensoes;
import canteiro.modelo.materiais.Madeira;
import canteiro.modelo.materiais.Material;
import canteiro.modelo.pecas.Forma;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Roda partidas no {@link MotorJogo}, sem servidor e sem navegador.
 *
 * <p>Na dificuldade fácil a peça cai uma linha a cada 48 ciclos (800 ms).
 * A T nasce na coluna 3 e ocupa (0,4), (1,3), (1,4) e (1,5).</p>
 *
 * @author Mateus Vitor Ferreira
 * @version 0.1.0
 */
class MotorJogoTest {

    private static final int FUNDO = Dimensoes.LINHAS - 1;
    private final Material madeira = new Madeira();

    private MotorJogo motor(Forma... formas) {
        MotorJogo motor = new MotorJogo(Dificuldade.FACIL, new FonteFixa(madeira, formas));
        motor.avancarCiclo();
        return motor;
    }

    private static void repetir(MotorJogo motor, Comando comando, int vezes) {
        for (int i = 0; i < vezes; i++) {
            motor.aplicar(comando);
        }
    }

    private static List<Celula> t(int linha, int coluna) {
        return List.of(new Celula(linha, coluna + 1), new Celula(linha + 1, coluna),
                new Celula(linha + 1, coluna + 1), new Celula(linha + 1, coluna + 2));
    }

    @Test
    void primeiroCicloGeraAPecaCentralizadaNasLinhasOcultas() {
        MotorJogo motor = motor(Forma.T);
        assertEquals(EstadoPartida.PECA_CAINDO, motor.estado());
        assertEquals(t(0, 3), motor.celulasPecaAtual());
    }

    @Test
    void comandosAntesDaPrimeiraPecaSaoIgnorados() {
        MotorJogo motor = new MotorJogo(Dificuldade.FACIL, new FonteFixa(madeira, Forma.T));
        motor.aplicar(Comando.ESQUERDA);
        assertEquals(EstadoPartida.GERANDO_PECA, motor.estado());
        assertNull(motor.pecaAtual());
    }

    @Test
    void pecaDesceUmaLinhaACadaIntervalo() {
        MotorJogo motor = motor(Forma.T);
        assertEquals(48, motor.ciclosPorQueda());
        for (int i = 0; i < 47; i++) {
            motor.avancarCiclo();
        }
        assertEquals(t(0, 3), motor.celulasPecaAtual());
        motor.avancarCiclo();
        assertEquals(t(1, 3), motor.celulasPecaAtual());
    }

    @Test
    void moveParaOsLadosERecusaAtravessarAParede() {
        MotorJogo motor = motor(Forma.T);
        repetir(motor, Comando.ESQUERDA, 3);
        assertEquals(t(0, 0), motor.celulasPecaAtual());
        motor.aplicar(Comando.ESQUERDA);
        assertEquals(t(0, 0), motor.celulasPecaAtual(), "não atravessa a parede esquerda");
        repetir(motor, Comando.DIREITA, 10);
        assertEquals(t(0, 7), motor.celulasPecaAtual(), "para na parede direita");
    }

    @Test
    void giraNosDoisSentidos() {
        MotorJogo motor = motor(Forma.T);
        motor.aplicar(Comando.GIRAR_HORARIO);
        assertEquals(1, motor.pecaAtual().rotacao());
        motor.aplicar(Comando.GIRAR_ANTI_HORARIO);
        motor.aplicar(Comando.GIRAR_ANTI_HORARIO);
        assertEquals(3, motor.pecaAtual().rotacao());
    }

    @Test
    void quedaInstantaneaPousaNoFundoEGeraAProxima() {
        MotorJogo motor = motor(Forma.T, Forma.O);
        List<Celula> fantasma = motor.celulasFantasma();
        motor.aplicar(Comando.QUEDA_INSTANTANEA);

        assertEquals(t(FUNDO - 1, 3), fantasma);
        for (Celula celula : fantasma) {
            assertTrue(motor.tabuleiro().ocupada(celula.linha(), celula.coluna()));
        }
        assertEquals('O', motor.pecaAtual().letra());
        assertEquals(EstadoPartida.PECA_CAINDO, motor.estado());
    }

    @Test
    void descerComAPecaApoiadaFixa() {
        MotorJogo motor = motor(Forma.O, Forma.T);
        repetir(motor, Comando.DESCER, FUNDO - 1);
        assertEquals('O', motor.pecaAtual().letra());
        motor.aplicar(Comando.DESCER);
        assertEquals('T', motor.pecaAtual().letra());
        assertTrue(motor.tabuleiro().ocupada(FUNDO, 4));
    }

    @Test
    void linhasCompletasSomem() {
        MotorJogo motor = motor(Forma.O);
        Registro registro = new Registro();
        motor.inscrever(registro);
        for (int coluna = 0; coluna < Dimensoes.COLUNAS; coluna += 2) {
            posicionarEDerrubar(motor, coluna - 4);
        }
        assertEquals(2, motor.linhasEliminadas());
        assertTrue(registro.eventos.contains(
                new EventoPartida(EventoPartida.Tipo.LINHAS_ELIMINADAS, List.of(FUNDO - 1, FUNDO))));
        for (int coluna = 0; coluna < Dimensoes.COLUNAS; coluna++) {
            assertFalse(motor.tabuleiro().ocupada(FUNDO, coluna));
        }
    }

    @Test
    void eliminacaoSomaPontosNoPlacar() {
        MotorJogo motor = motor(Forma.O);
        for (int coluna = 0; coluna < Dimensoes.COLUNAS; coluna += 2) {
            posicionarEDerrubar(motor, coluna - 4);
        }
        assertEquals(Pontuacao.pontos(2, 1, madeira), motor.placar().pontuacao());
        assertEquals(2, motor.placar().linhas());
    }

    @Test
    void subirDeNivelAceleraAQueda() {
        MotorJogo motor = new MotorJogo(Dificuldade.NORMAL, new FonteFixa(madeira, Forma.O));
        motor.avancarCiclo();
        Registro registro = new Registro();
        motor.inscrever(registro);
        assertEquals(39, motor.ciclosPorQueda(), "650 ms no nível 3");
        for (int volta = 0; volta < 10; volta++) {
            for (int coluna = 0; coluna < Dimensoes.COLUNAS; coluna += 2) {
                posicionarEDerrubar(motor, coluna - 4);
            }
        }
        assertEquals(20, motor.placar().linhas());
        assertEquals(5, motor.placar().nivel());
        assertEquals(30, motor.ciclosPorQueda(), "500 ms no nível 5");
        assertTrue(registro.eventos.contains(EventoPartida.de(EventoPartida.Tipo.NIVEL_SUBIU)));
    }

    @Test
    void linhasDeCimaDescemDepoisDaEliminacao() {
        MotorJogo motor = motor(Forma.I, Forma.I, Forma.O);
        posicionarEDerrubar(motor, -3);
        posicionarEDerrubar(motor, 1);
        posicionarEDerrubar(motor, 4);

        assertEquals(1, motor.linhasEliminadas());
        assertTrue(motor.tabuleiro().ocupada(FUNDO, 8), "a metade de cima da O desceu");
        assertTrue(motor.tabuleiro().ocupada(FUNDO, 9));
        assertFalse(motor.tabuleiro().ocupada(FUNDO - 1, 8));
        assertFalse(motor.tabuleiro().ocupada(FUNDO, 0));
    }

    @Test
    void partidaAcabaQuandoAPecaNasceColidindo() {
        MotorJogo motor = motor(Forma.O);
        Registro registro = new Registro();
        motor.inscrever(registro);
        for (int i = 0; i < Dimensoes.LINHAS && !motor.estado().encerrada(); i++) {
            motor.aplicar(Comando.QUEDA_INSTANTANEA);
        }
        assertEquals(EstadoPartida.FIM_DE_JOGO, motor.estado());
        assertTrue(registro.eventos.contains(EventoPartida.de(EventoPartida.Tipo.FIM_DE_JOGO)));

        long ciclo = motor.ciclo();
        motor.avancarCiclo();
        motor.aplicar(Comando.ESQUERDA);
        assertEquals(ciclo, motor.ciclo(), "partida encerrada não anda");
    }

    @Test
    void pausaCongelaAPartidaAteRetomar() {
        MotorJogo motor = motor(Forma.T);
        motor.aplicar(Comando.PAUSAR);
        assertEquals(EstadoPartida.PAUSA, motor.estado());
        for (int i = 0; i < 200; i++) {
            motor.avancarCiclo();
        }
        motor.aplicar(Comando.ESQUERDA);
        assertEquals(t(0, 3), motor.celulasPecaAtual());

        motor.aplicar(Comando.RETOMAR);
        assertEquals(EstadoPartida.PECA_CAINDO, motor.estado());
        motor.aplicar(Comando.ESQUERDA);
        assertEquals(t(0, 2), motor.celulasPecaAtual());
    }

    @Test
    void observadorEAvisadoDasMudancas() {
        MotorJogo motor = motor(Forma.T);
        Registro registro = new Registro();
        motor.inscrever(registro);
        motor.aplicar(Comando.ESQUERDA);
        motor.aplicar(Comando.QUEDA_INSTANTANEA);
        assertEquals(2, registro.mudancas);
        assertEquals(List.of(EventoPartida.de(EventoPartida.Tipo.PECA_FIXADA)), registro.eventos);
    }

    @Test
    void mostraAsProximasPecas() {
        MotorJogo motor = motor(Forma.T, Forma.I, Forma.O);
        assertEquals(List.of('I', 'O', 'T'), motor.proximas(3).stream().map(p -> p.letra()).toList());
    }

    @Test
    void partidaInteiraRodaDoComecoAoFimEEReproduzivel() {
        MotorJogo primeira = jogarAteOFim();
        MotorJogo segunda = jogarAteOFim();

        assertEquals(EstadoPartida.FIM_DE_JOGO, primeira.estado());
        assertEquals(primeira.ciclo(), segunda.ciclo());
        assertEquals(ocupadas(primeira), ocupadas(segunda), "mesmos comandos, mesma partida");
    }

    private MotorJogo jogarAteOFim() {
        MotorJogo motor = new MotorJogo(Dificuldade.NORMAL, new FonteFixa(madeira, Forma.values()));
        // gira e derruba sempre no centro: a pilha cresce sem fechar linha, e a partida acaba
        Comando[] roteiro = {Comando.GIRAR_HORARIO, Comando.GIRAR_ANTI_HORARIO, Comando.GIRAR_HORARIO,
            Comando.DESCER, Comando.QUEDA_INSTANTANEA};
        for (int passo = 0; passo < 100_000 && !motor.estado().encerrada(); passo++) {
            motor.avancarCiclo();
            if (passo % 7 == 0) {
                motor.aplicar(roteiro[(passo / 7) % roteiro.length]);
            }
        }
        return motor;
    }

    private static List<Celula> ocupadas(MotorJogo motor) {
        List<Celula> ocupadas = new ArrayList<>();
        for (int linha = 0; linha < Dimensoes.LINHAS; linha++) {
            for (int coluna = 0; coluna < Dimensoes.COLUNAS; coluna++) {
                if (motor.tabuleiro().ocupada(linha, coluna)) {
                    ocupadas.add(new Celula(linha, coluna));
                }
            }
        }
        return ocupadas;
    }

    /** Move a peça atual pelo deslocamento de colunas dado e derruba. */
    private static void posicionarEDerrubar(MotorJogo motor, int deslocamento) {
        repetir(motor, deslocamento < 0 ? Comando.ESQUERDA : Comando.DIREITA, Math.abs(deslocamento));
        motor.aplicar(Comando.QUEDA_INSTANTANEA);
    }

    /** Observador que só anota o que ouviu. */
    private static final class Registro implements ObservadorPartida {
        private int mudancas;
        private final List<EventoPartida> eventos = new ArrayList<>();

        @Override
        public void estadoMudou(MotorJogo motor) {
            mudancas++;
        }

        @Override
        public void eventoOcorreu(EventoPartida evento) {
            eventos.add(evento);
        }
    }
}
