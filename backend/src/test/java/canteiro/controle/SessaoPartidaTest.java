package canteiro.controle;

import canteiro.modelo.Celula;
import canteiro.modelo.Comando;
import canteiro.modelo.Dificuldade;
import canteiro.modelo.EstadoPartida;
import canteiro.modelo.MotorJogo;
import canteiro.modelo.estruturas.FontePecas;
import canteiro.modelo.materiais.Madeira;
import canteiro.modelo.pecas.Peca;
import canteiro.modelo.pecas.PecaT;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * A sessão: fila de comandos e laço de 60 ciclos por segundo.
 *
 * @author Mateus Vitor Ferreira
 * @version 0.1.0
 */
class SessaoPartidaTest {

    /** Sempre a mesma peça T de madeira. */
    private static final FontePecas SO_T = new FontePecas() {
        @Override
        public Peca proxima() {
            return new PecaT(new Madeira());
        }

        @Override
        public List<Peca> espiar(int quantidade) {
            return List.of();
        }
    };

    private final SessaoPartida sessao = new SessaoPartida("teste", new MotorJogo(Dificuldade.FACIL, SO_T));

    @Test
    void comandosEntramNoCicloSeguinteNaOrdemDeChegada() {
        sessao.ciclo();
        sessao.enfileirar(Comando.ESQUERDA);
        sessao.enfileirar(Comando.ESQUERDA);
        sessao.enfileirar(Comando.DIREITA);
        assertEquals(new Celula(0, 4), sessao.motor().celulasPecaAtual().get(0), "ainda não aplicou");

        sessao.ciclo();

        assertEquals(new Celula(0, 3), sessao.motor().celulasPecaAtual().get(0));
    }

    @Test
    void aceitaComandosDeVariasThreadsAoMesmoTempo() throws InterruptedException {
        sessao.ciclo();
        List<Thread> threads = new ArrayList<>();
        for (int t = 0; t < 4; t++) {
            Thread thread = new Thread(() -> {
                for (int i = 0; i < 250; i++) {
                    sessao.enfileirar(Comando.ESQUERDA);
                }
            });
            threads.add(thread);
            thread.start();
        }
        for (Thread thread : threads) {
            thread.join();
        }
        sessao.ciclo();
        assertEquals(new Celula(0, 1), sessao.motor().celulasPecaAtual().get(0), "encostou na parede");
    }

    @Test
    void pausarPausaNoProximoCiclo() {
        sessao.ciclo();
        sessao.pausar();
        sessao.ciclo();
        assertEquals(EstadoPartida.PAUSA, sessao.motor().estado());
    }

    @Test
    void lacoRodaPertoDeSessentaCiclosPorSegundo() throws InterruptedException {
        sessao.iniciar();
        sessao.iniciar();
        Thread.sleep(500);
        sessao.encerrar();
        long ciclos = sessao.motor().ciclo();
        assertTrue(ciclos >= 20 && ciclos <= 40, "em meio segundo rodou " + ciclos + " ciclos");
    }

    @Test
    void erroNumCicloNaoMataOLaco() throws InterruptedException {
        AtomicInteger tentativas = new AtomicInteger();
        FontePecas quebrada = new FontePecas() {
            @Override
            public Peca proxima() {
                tentativas.incrementAndGet();
                throw new IllegalStateException("fonte quebrada de propósito");
            }

            @Override
            public List<Peca> espiar(int quantidade) {
                return List.of();
            }
        };
        SessaoPartida comErro = new SessaoPartida("erro", new MotorJogo(Dificuldade.FACIL, quebrada));
        comErro.iniciar();
        Thread.sleep(200);
        comErro.encerrar();
        assertTrue(tentativas.get() > 3, "o laço continuou tentando: " + tentativas.get());
    }
}
