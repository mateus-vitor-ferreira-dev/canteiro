package canteiro.modelo;

import canteiro.modelo.constantes.Dimensoes;
import canteiro.modelo.estruturas.GeradorPecas;
import canteiro.modelo.estruturas.Jogada;
import canteiro.modelo.materiais.CatalogoMateriais;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Refazer as jogadas gravadas num motor novo, com a mesma semente, dá a mesma
 * partida (RF23, RF24, RF26).
 *
 * @author Mateus Vitor Ferreira
 * @version 0.1.0
 */
class ReprodutorTest {

    private static final long SEMENTE = 2026;
    private static final int LIMITE_DE_CICLOS = 200_000;
    private static final int UM_COMANDO_A_CADA = 6;
    private static final Comando[] COMANDOS = {Comando.ESQUERDA, Comando.DIREITA, Comando.GIRAR_HORARIO,
        Comando.GIRAR_ANTI_HORARIO, Comando.DESCER, Comando.QUEDA_INSTANTANEA, Comando.RESERVAR};

    @Test
    void reproduzirAPartidaInteiraDaAMesmaPontuacaoEOMesmoTabuleiro() {
        MotorJogo original = jogarAteOFim(Dificuldade.DIFICIL);
        MotorJogo copia = novoMotor(Dificuldade.DIFICIL);
        Reprodutor reprodutor = new Reprodutor(original.jogadas());

        for (int i = 0; i < LIMITE_DE_CICLOS && !copia.estado().encerrada(); i++) {
            reprodutor.passo(copia);
        }

        assertEquals(EstadoPartida.FIM_DE_JOGO, copia.estado());
        assertTrue(reprodutor.aplicouTodas());
        assertEquals(original.placar().pontuacao(), copia.placar().pontuacao());
        assertEquals(original.placar().colapsos(), copia.placar().colapsos());
        assertEquals(original.ciclo(), copia.ciclo());
        assertEquals(ocupadas(original), ocupadas(copia));
    }

    @Test
    void avancarAteUmPontoDaOMesmoEstadoQueAOriginalTinhaAli() {
        Random sorteio = new Random(SEMENTE);
        MotorJogo original = novoMotor(Dificuldade.NORMAL);
        int ciclosAteOPonto = 3_000;
        for (int i = 0; i < ciclosAteOPonto && !original.estado().encerrada(); i++) {
            jogarUmCiclo(original, sorteio);
        }
        List<Jogada> jogadas = original.jogadas();
        MotorJogo copia = novoMotor(Dificuldade.NORMAL);

        new Reprodutor(jogadas).avancarAte(copia, jogadas.size(), original.ciclo());

        assertEquals(original.ciclo(), copia.ciclo());
        assertEquals(original.placar().pontuacao(), copia.placar().pontuacao());
        assertEquals(ocupadas(original), ocupadas(copia));
        assertEquals(original.celulasPecaAtual(), copia.celulasPecaAtual());
        assertEquals(original.proximas(3).toString(), copia.proximas(3).toString());
    }

    @Test
    void pausasDaPartidaOriginalNaoTravamAReproducao() {
        MotorJogo original = novoMotor(Dificuldade.FACIL);
        original.avancarCiclo();
        original.aplicar(Comando.PAUSAR);
        original.aplicar(Comando.ESQUERDA);
        original.avancarCiclo();
        original.aplicar(Comando.RETOMAR);
        original.aplicar(Comando.ESQUERDA);
        original.avancarCiclo();
        MotorJogo copia = novoMotor(Dificuldade.FACIL);

        new Reprodutor(original.jogadas()).avancarAte(copia, original.jogadas().size(), original.ciclo());

        assertEquals(original.celulasPecaAtual(), copia.celulasPecaAtual());
        assertEquals(EstadoPartida.PECA_CAINDO, copia.estado());
    }

    @Test
    void pontoForaDaPartidaERecusado() {
        MotorJogo copia = novoMotor(Dificuldade.FACIL);
        Reprodutor reprodutor = new Reprodutor(List.of(new Jogada(0, Comando.ESQUERDA)));

        assertThrows(IllegalArgumentException.class, () -> reprodutor.avancarAte(copia, 2, 0));
    }

    private static MotorJogo jogarAteOFim(Dificuldade dificuldade) {
        Random sorteio = new Random(SEMENTE);
        MotorJogo motor = novoMotor(dificuldade);
        for (int i = 0; i < LIMITE_DE_CICLOS && !motor.estado().encerrada(); i++) {
            jogarUmCiclo(motor, sorteio);
        }
        assertEquals(EstadoPartida.FIM_DE_JOGO, motor.estado(), "a partida de teste precisa terminar");
        return motor;
    }

    /** Como a sessão: os comandos do ciclo primeiro, depois o motor avança. */
    private static void jogarUmCiclo(MotorJogo motor, Random sorteio) {
        if (sorteio.nextInt(UM_COMANDO_A_CADA) == 0) {
            motor.aplicar(COMANDOS[sorteio.nextInt(COMANDOS.length)]);
        }
        motor.avancarCiclo();
    }

    private static MotorJogo novoMotor(Dificuldade dificuldade) {
        return new MotorJogo(dificuldade,
                new GeradorPecas(SEMENTE, CatalogoMateriais.padrao(), dificuldade.nivelInicial()));
    }

    private static List<String> ocupadas(MotorJogo motor) {
        List<String> ocupadas = new ArrayList<>();
        for (int linha = 0; linha < Dimensoes.LINHAS; linha++) {
            for (int coluna = 0; coluna < Dimensoes.COLUNAS; coluna++) {
                if (motor.tabuleiro().ocupada(linha, coluna)) {
                    ocupadas.add(linha + "," + coluna + "," + motor.tabuleiro().bloco(linha, coluna).material().codigo());
                }
            }
        }
        return ocupadas;
    }
}
