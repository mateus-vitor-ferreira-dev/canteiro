package canteiro.controle;

import canteiro.modelo.Comando;
import canteiro.modelo.Dificuldade;
import canteiro.modelo.EventoPartida;
import canteiro.modelo.MotorJogo;
import canteiro.modelo.ObservadorPartida;
import canteiro.modelo.constantes.Dimensoes;
import canteiro.modelo.estruturas.GeradorPecas;
import canteiro.modelo.materiais.CatalogoMateriais;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertSame;

/**
 * O desfazer do modo treino, pela sessão, sem o laço rodando: o teste chama
 * os ciclos (RF26).
 *
 * @author Mateus Vitor Ferreira
 * @version 0.1.0
 */
class TreinoTest {

    private final CatalogoMateriais catalogo = CatalogoMateriais.padrao();
    private final GerenciadorPartidas partidas = new GerenciadorPartidas(
            (dificuldade, semente) -> new GeradorPecas(semente, catalogo, dificuldade.nivelInicial()));

    @Test
    void desfazerVoltaOTabuleiroAoQueEraAntesDaUltimaPeca() {
        SessaoPartida sessao = partidas.criar(Dificuldade.NORMAL, true);
        sessao.ciclo();
        derrubar(sessao, Comando.ESQUERDA, 3);
        derrubar(sessao, Comando.DIREITA, 3);
        Foto antes = Foto.de(sessao.motor());
        derrubar(sessao, Comando.DIREITA, 1);
        assertNotEquals(antes.ocupadas(), Foto.de(sessao.motor()).ocupadas(), "a terceira peça mudou o tabuleiro");

        desfazer(sessao);

        assertEquals(antes, Foto.de(sessao.motor()));
    }

    @Test
    void desfazerDuasVezesVoltaDuasPecas() {
        SessaoPartida sessao = partidas.criar(Dificuldade.FACIL, true);
        sessao.ciclo();
        Foto vazio = Foto.de(sessao.motor());
        derrubar(sessao, Comando.ESQUERDA, 2);
        Foto umaPeca = Foto.de(sessao.motor());
        derrubar(sessao, Comando.DIREITA, 2);

        desfazer(sessao);
        assertEquals(umaPeca, Foto.de(sessao.motor()));
        desfazer(sessao);
        assertEquals(vazio.ocupadas(), Foto.de(sessao.motor()).ocupadas());
        assertEquals(vazio.atual(), Foto.de(sessao.motor()).atual());
    }

    @Test
    void semPecaFixadaDesfazerNaoFazNada() {
        SessaoPartida sessao = partidas.criar(Dificuldade.FACIL, true);
        sessao.ciclo();
        MotorJogo motor = sessao.motor();

        desfazer(sessao);

        assertSame(motor, sessao.motor());
    }

    @Test
    void pausadaNaoDesfaz() {
        SessaoPartida sessao = partidas.criar(Dificuldade.FACIL, true);
        sessao.ciclo();
        derrubar(sessao, Comando.ESQUERDA, 2);
        sessao.pausar();
        sessao.ciclo();
        MotorJogo motor = sessao.motor();

        desfazer(sessao);

        assertSame(motor, sessao.motor());
    }

    @Test
    void foraDoTreinoDesfazerEIgnorado() {
        SessaoPartida sessao = partidas.criar(Dificuldade.FACIL);
        sessao.ciclo();
        derrubar(sessao, Comando.ESQUERDA, 2);
        Foto depois = Foto.de(sessao.motor());
        MotorJogo motor = sessao.motor();

        desfazer(sessao);

        assertSame(motor, sessao.motor());
        assertEquals(depois.ocupadas(), Foto.de(sessao.motor()).ocupadas());
    }

    @Test
    void ouvinteContinuaRecebendoDoMotorNovo() {
        SessaoPartida sessao = partidas.criar(Dificuldade.FACIL, true);
        Ouvinte ouvinte = new Ouvinte();
        sessao.inscrever(ouvinte);
        sessao.ciclo();
        derrubar(sessao, Comando.ESQUERDA, 2);
        MotorJogo antigo = sessao.motor();

        desfazer(sessao);
        assertNotSame(antigo, sessao.motor());
        assertSame(sessao.motor(), ouvinte.ultimoMotor, "o ouvinte recebeu o estado do motor novo");
        int fixadas = ouvinte.fixadas;
        derrubar(sessao, Comando.DIREITA, 2);

        assertEquals(fixadas + 1, ouvinte.fixadas, "os eventos do motor novo chegam ao ouvinte");
    }

    @Test
    void oRelatorioMostraAPartidaQueFicou() {
        SessaoPartida sessao = partidas.criar(Dificuldade.FACIL, true);
        sessao.ciclo();
        derrubar(sessao, Comando.ESQUERDA, 2);
        derrubar(sessao, Comando.DIREITA, 2);
        derrubar(sessao, Comando.DESCER, 0);
        assertEquals(3, sessao.relatorio().indices().size());

        desfazer(sessao);

        assertEquals(2, sessao.relatorio().indices().size());
        assertEquals(2, sessao.relatorio().materiais().values().stream().mapToInt(Integer::intValue).sum());
    }

    @Test
    void desfazerDepoisDeEsperarAGravidadeFixarTambemVolta() {
        SessaoPartida sessao = partidas.criar(Dificuldade.FACIL, true);
        sessao.ciclo();
        derrubar(sessao, Comando.ESQUERDA, 2);
        Foto antes = Foto.de(sessao.motor());
        int pecas = sessao.relatorio().indices().size();
        for (int i = 0; i < Dimensoes.LINHAS * 100 && sessao.relatorio().indices().size() == pecas; i++) {
            sessao.ciclo();
        }
        assertEquals(pecas + 1, sessao.relatorio().indices().size(), "a gravidade fixou a peça");

        desfazer(sessao);

        assertEquals(antes, Foto.de(sessao.motor()));
    }

    /** Move a peça e a derruba, num ciclo só, como o jogador faria rapidinho. */
    private static void derrubar(SessaoPartida sessao, Comando movimento, int vezes) {
        for (int i = 0; i < vezes; i++) {
            sessao.enfileirar(movimento);
        }
        sessao.enfileirar(Comando.QUEDA_INSTANTANEA);
        sessao.ciclo();
        sessao.ciclo();
    }

    private static void desfazer(SessaoPartida sessao) {
        sessao.enfileirar(Comando.DESFAZER);
        sessao.ciclo();
    }

    /** O que o jogador vê: blocos fixados, peça em queda, as próximas, a reserva e o placar. */
    private record Foto(List<String> ocupadas, String atual, String proximas, String reservada, int pontuacao,
                        int linhas, int colapsos) {
        static Foto de(MotorJogo motor) {
            List<String> ocupadas = new ArrayList<>();
            for (int linha = 0; linha < Dimensoes.LINHAS; linha++) {
                for (int coluna = 0; coluna < Dimensoes.COLUNAS; coluna++) {
                    if (motor.tabuleiro().ocupada(linha, coluna)) {
                        ocupadas.add(linha + "," + coluna + "," + motor.tabuleiro().bloco(linha, coluna).material());
                    }
                }
            }
            return new Foto(ocupadas, String.valueOf(motor.pecaAtual()), motor.proximas(3).toString(),
                    String.valueOf(motor.pecaReservada()), motor.placar().pontuacao(), motor.placar().linhas(),
                    motor.placar().colapsos());
        }
    }

    private static final class Ouvinte implements ObservadorPartida {
        private MotorJogo ultimoMotor;
        private int fixadas;

        @Override
        public void estadoMudou(MotorJogo motor) {
            ultimoMotor = motor;
        }

        @Override
        public void eventoOcorreu(EventoPartida evento) {
            if (evento.tipo() == EventoPartida.Tipo.PECA_FIXADA) {
                fixadas++;
            }
        }
    }

}
