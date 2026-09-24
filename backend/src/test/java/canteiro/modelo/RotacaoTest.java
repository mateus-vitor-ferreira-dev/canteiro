package canteiro.modelo;

import canteiro.modelo.constantes.Dimensoes;
import canteiro.modelo.materiais.Madeira;
import canteiro.modelo.pecas.Forma;
import canteiro.modelo.pecas.PecaI;
import canteiro.modelo.pecas.PecaO;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Rotação com deslocamento corretivo no {@link MotorJogo} (RF08).
 *
 * @author Mateus Vitor Ferreira
 * @version 0.1.0
 */
class RotacaoTest {

    private static final int FUNDO = Dimensoes.LINHAS - 1;
    private final Madeira madeira = new Madeira();

    private MotorJogo motor(Forma forma) {
        MotorJogo motor = new MotorJogo(Dificuldade.FACIL, new FonteFixa(madeira, forma));
        motor.avancarCiclo();
        return motor;
    }

    private static void repetir(MotorJogo motor, Comando comando, int vezes) {
        for (int i = 0; i < vezes; i++) {
            motor.aplicar(comando);
        }
    }

    @ParameterizedTest(name = "{0}: gira encostada em qualquer parede, nos dois sentidos")
    @EnumSource(Forma.class)
    void todaFormaGiraEncostadaEmQualquerParede(Forma forma) {
        for (Comando parede : List.of(Comando.ESQUERDA, Comando.DIREITA)) {
            for (Comando giro : List.of(Comando.GIRAR_HORARIO, Comando.GIRAR_ANTI_HORARIO)) {
                MotorJogo motor = motor(forma);
                motor.aplicar(Comando.DESCER);
                motor.aplicar(Comando.DESCER);
                for (int volta = 0; volta < 4; volta++) {
                    repetir(motor, parede, Dimensoes.COLUNAS);
                    int antes = motor.pecaAtual().rotacao();
                    motor.aplicar(giro);
                    assertEquals(proxima(antes, giro), motor.pecaAtual().rotacao(),
                            forma + " na parede " + parede + ", giro " + giro + ", volta " + volta);
                }
            }
        }
    }

    @Test
    void iEmPeNaParedeDeitaDeslocandoDuasColunas() {
        MotorJogo motor = motor(Forma.I);
        motor.aplicar(Comando.GIRAR_HORARIO);
        repetir(motor, Comando.ESQUERDA, Dimensoes.COLUNAS);
        motor.aplicar(Comando.GIRAR_HORARIO);

        assertEquals(2, motor.pecaAtual().rotacao());
        assertEquals(List.of(new Celula(2, 0), new Celula(2, 1), new Celula(2, 2), new Celula(2, 3)),
                motor.celulasPecaAtual());
    }

    @Test
    void iDeitadaNoChaoFicaEmPeSubindoDuasLinhas() {
        MotorJogo motor = motor(Forma.I);
        repetir(motor, Comando.DESCER, FUNDO - 1);
        assertEquals(FUNDO, motor.celulasPecaAtual().get(0).linha(), "a I está deitada no chão");

        motor.aplicar(Comando.GIRAR_HORARIO);

        assertEquals(1, motor.pecaAtual().rotacao());
        assertEquals(FUNDO, motor.celulasPecaAtual().get(3).linha(), "em pé, com o pé no chão");
    }

    @Test
    void tNoChaoGiraSubindoUmaLinha() {
        MotorJogo motor = motor(Forma.T);
        repetir(motor, Comando.DESCER, FUNDO - 1);

        motor.aplicar(Comando.GIRAR_HORARIO);

        assertEquals(1, motor.pecaAtual().rotacao());
        assertEquals(FUNDO, motor.celulasPecaAtual().get(3).linha());
    }

    @Test
    void giroERecusadoQuandoNenhumDeslocamentoCabe() {
        MotorJogo motor = motor(Forma.I);
        motor.aplicar(Comando.GIRAR_HORARIO);
        repetir(motor, Comando.ESQUERDA, Dimensoes.COLUNAS);
        List<Celula> antes = motor.celulasPecaAtual();
        encherColunasUmAOito(motor.tabuleiro());

        motor.aplicar(Comando.GIRAR_HORARIO);
        motor.aplicar(Comando.GIRAR_ANTI_HORARIO);

        assertEquals(1, motor.pecaAtual().rotacao(), "a I está presa num poço de uma coluna");
        assertEquals(antes, motor.celulasPecaAtual());
    }

    @Test
    void cadaPecaTemCincoDeslocamentosEAIVaiMaisLonge() {
        int[][] padrao = new PecaO(madeira).deslocamentosCorretivos();
        int[][] daI = new PecaI(madeira).deslocamentosCorretivos();
        assertEquals(5, padrao.length);
        assertEquals(5, daI.length);
        assertEquals(-1, padrao[4][0], "as outras sobem uma linha");
        assertEquals(-2, daI[4][0], "a I sobe duas");
    }

    @Test
    void deslocamentosDevolvidosSaoUmaCopia() {
        PecaI peca = new PecaI(madeira);
        peca.deslocamentosCorretivos()[0][1] = 99;
        assertTrue(peca.deslocamentosCorretivos()[0][1] != 99);
    }

    private static int proxima(int rotacao, Comando giro) {
        return giro == Comando.GIRAR_HORARIO ? (rotacao + 1) % 4 : (rotacao + 3) % 4;
    }

    /**
     * Enche as colunas 1 a 8, de cima a baixo, com peças O. Qualquer giro da I
     * em pé na coluna 0 esbarra nelas, com ou sem deslocamento.
     */
    private void encherColunasUmAOito(Tabuleiro tabuleiro) {
        for (int linha = 0; linha < Dimensoes.LINHAS; linha += 2) {
            for (int coluna = 1; coluna < Dimensoes.COLUNAS - 1; coluna += 2) {
                tabuleiro.fixar(new PecaO(madeira), linha, coluna);
            }
        }
    }
}
