package canteiro.modelo;

import canteiro.modelo.constantes.Dimensoes;
import canteiro.modelo.estruturas.Jogada;
import canteiro.modelo.materiais.Madeira;
import canteiro.modelo.pecas.Forma;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Confere que o motor registra as jogadas no histórico (RF23) e que elas
 * bastam para reproduzir a partida.
 *
 * @author Mateus Vitor Ferreira
 * @version 0.1.0
 */
class HistoricoNoMotorTest {

    private final Madeira madeira = new Madeira();

    private MotorJogo motor() {
        MotorJogo motor = new MotorJogo(Dificuldade.FACIL, new FonteFixa(madeira, Forma.T, Forma.I, Forma.O));
        motor.avancarCiclo();
        return motor;
    }

    @Test
    void registraCadaComandoComOCiclo() {
        MotorJogo motor = motor();
        motor.aplicar(Comando.ESQUERDA);
        motor.avancarCiclo();
        motor.avancarCiclo();
        motor.aplicar(Comando.GIRAR_HORARIO);
        assertEquals(List.of(new Jogada(1, Comando.ESQUERDA), new Jogada(3, Comando.GIRAR_HORARIO)),
                motor.jogadas());
    }

    @Test
    void comandoIgnoradoTambemFicaRegistrado() {
        MotorJogo motor = motor();
        motor.aplicar(Comando.RETOMAR);
        assertEquals(List.of(new Jogada(1, Comando.RETOMAR)), motor.jogadas());
    }

    @Test
    void depoisDoFimNadaERegistrado() {
        MotorJogo motor = motor();
        while (!motor.estado().encerrada()) {
            motor.aplicar(Comando.QUEDA_INSTANTANEA);
        }
        int jogadas = motor.jogadas().size();
        motor.aplicar(Comando.ESQUERDA);
        assertEquals(jogadas, motor.jogadas().size());
    }

    @Test
    void repetirAsJogadasNosMesmosCiclosDaAMesmaPartida() {
        MotorJogo original = motor();
        for (int i = 0; i < 40; i++) {
            original.aplicar(i % 3 == 0 ? Comando.ESQUERDA : Comando.DIREITA);
            original.avancarCiclo();
            if (i % 5 == 0) {
                original.aplicar(Comando.QUEDA_INSTANTANEA);
            }
        }

        MotorJogo copia = reproduzir(original.jogadas(), original.ciclo());
        assertEquals(original.celulasPecaAtual(), copia.celulasPecaAtual());
        assertEquals(original.placar().pontuacao(), copia.placar().pontuacao());
        for (int linha = 0; linha < Dimensoes.LINHAS; linha++) {
            for (int coluna = 0; coluna < Dimensoes.COLUNAS; coluna++) {
                assertEquals(original.tabuleiro().ocupada(linha, coluna), copia.tabuleiro().ocupada(linha, coluna));
            }
        }
        assertTrue(original.jogadas().size() > 40);
    }

    private MotorJogo reproduzir(List<Jogada> jogadas, long ateOCiclo) {
        MotorJogo motor = motor();
        int proxima = 0;
        while (motor.ciclo() <= ateOCiclo) {
            while (proxima < jogadas.size() && jogadas.get(proxima).ciclo() == motor.ciclo()) {
                motor.aplicar(jogadas.get(proxima++).comando());
            }
            if (motor.ciclo() == ateOCiclo) {
                break;
            }
            motor.avancarCiclo();
        }
        return motor;
    }
}
