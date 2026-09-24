package canteiro.controle;

import canteiro.modelo.Comando;
import canteiro.modelo.MotorJogo;
import canteiro.modelo.ObservadorPartida;
import canteiro.modelo.constantes.Tempo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Uma partida no tempo: o motor, a fila de comandos e o laço de 60 ciclos por
 * segundo, numa thread só dela.
 *
 * <p>Os comandos chegam pela thread do WebSocket e <strong>não mexem no
 * motor na hora</strong>: entram numa fila concorrente e são consumidos no
 * início do ciclo seguinte. Assim, só a thread do laço altera o motor, sem
 * precisar de trava, e a partida fica reproduzível.</p>
 *
 * @author Mateus Vitor Ferreira
 * @version 0.1.0
 */
public final class SessaoPartida {

    private static final Logger LOG = LoggerFactory.getLogger(SessaoPartida.class);
    private static final long ESPERA_ENCERRAR_MS = 1000;
    private static final long MICROS_POR_CICLO = TimeUnit.SECONDS.toMicros(1) / Tempo.CICLOS_POR_SEGUNDO;

    private final String id;
    private final MotorJogo motor;
    private final Queue<Comando> comandos = new ConcurrentLinkedQueue<>();
    private ScheduledExecutorService laco;

    /**
     * Cria a sessão, ainda parada.
     *
     * @param id    identificador da partida
     * @param motor motor da partida
     */
    public SessaoPartida(String id, MotorJogo motor) {
        this.id = Objects.requireNonNull(id, "id");
        this.motor = Objects.requireNonNull(motor, "motor");
    }

    /**
     * Começa o laço, se ainda não começou. Chamar de novo não faz nada.
     */
    public synchronized void iniciar() {
        if (laco != null) {
            return;
        }
        laco = Executors.newSingleThreadScheduledExecutor(tarefa -> {
            Thread thread = new Thread(tarefa, "partida-" + id);
            thread.setDaemon(true);
            return thread;
        });
        laco.scheduleAtFixedRate(this::cicloProtegido, 0, MICROS_POR_CICLO, TimeUnit.MICROSECONDS);
    }

    /**
     * Para o laço de vez e espera o ciclo em andamento terminar. Depois disso,
     * o motor pode ser lido de qualquer thread. A sessão não volta a rodar.
     */
    public synchronized void encerrar() {
        if (laco == null) {
            return;
        }
        laco.shutdownNow();
        try {
            if (!laco.awaitTermination(ESPERA_ENCERRAR_MS, TimeUnit.MILLISECONDS)) {
                LOG.warn("O laço da partida {} não parou a tempo", id);
            }
        } catch (InterruptedException interrompido) {
            Thread.currentThread().interrupt();
        }
    }

    /**
     * Guarda um comando para o próximo ciclo. Pode ser chamado de qualquer thread.
     *
     * @param comando comando do jogador
     */
    public void enfileirar(Comando comando) {
        comandos.add(Objects.requireNonNull(comando, "comando"));
    }

    /**
     * Pausa a partida no próximo ciclo, como quando a conexão cai (RF30).
     */
    public void pausar() {
        enfileirar(Comando.PAUSAR);
    }

    /**
     * Inscreve quem quer ouvir a partida. Os avisos chegam pela thread do laço.
     *
     * @param observador quem vai ouvir
     */
    public void inscrever(ObservadorPartida observador) {
        motor.inscrever(observador);
    }

    /**
     * Tira um observador, como quando a conexão fecha.
     *
     * @param observador quem para de ouvir
     */
    public void desinscrever(ObservadorPartida observador) {
        motor.desinscrever(observador);
    }

    /**
     * Um ciclo: aplica os comandos da fila, na ordem de chegada, e avança o motor.
     */
    void ciclo() {
        Comando comando;
        while ((comando = comandos.poll()) != null) {
            motor.aplicar(comando);
        }
        motor.avancarCiclo();
    }

    /** Um erro num ciclo não pode matar o laço: o agendador para de vez se a tarefa lançar exceção. */
    private void cicloProtegido() {
        try {
            ciclo();
        } catch (RuntimeException erro) {
            LOG.error("Erro no ciclo da partida {}", id, erro);
        }
    }

    /**
     * Devolve o identificador da partida.
     *
     * @return o id
     */
    public String id() {
        return id;
    }

    /**
     * Devolve o motor. Fora da thread do laço, só para leitura em testes.
     *
     * @return o motor da partida
     */
    public MotorJogo motor() {
        return motor;
    }
}
