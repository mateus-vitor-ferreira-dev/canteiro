package canteiro.controle;

import canteiro.modelo.Dificuldade;
import canteiro.modelo.MotorJogo;
import canteiro.modelo.estruturas.FontePecas;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.security.SecureRandom;
import java.time.Duration;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.function.LongSupplier;

/**
 * As partidas em andamento, numa tabela hash indexada pelo id.
 *
 * <p>O id vem na URL do WebSocket, então a busca precisa ser O(1) e segura
 * entre threads: é um {@link ConcurrentHashMap}.</p>
 *
 * <p>Partida sem ninguém conectado por muito tempo é descartada pela limpeza
 * periódica (RF31), e o número de partidas abertas pode ter um teto, para um
 * servidor público não ser esgotado por partidas esquecidas.</p>
 *
 * @author Mateus Vitor Ferreira
 * @version 0.1.0
 */
public final class GerenciadorPartidas {

    /** Limite que, na prática, não limita: o padrão do jogo local. */
    public static final int SEM_LIMITE = Integer.MAX_VALUE;

    private static final String ALFABETO = "abcdefghijklmnopqrstuvwxyz0123456789";
    private static final int TAMANHO_ID = 8;
    private static final Logger LOG = LoggerFactory.getLogger(GerenciadorPartidas.class);

    private final Map<String, SessaoPartida> sessoes = new ConcurrentHashMap<>();
    private final Function<Dificuldade, FontePecas> fabricaDeFontes;
    private final int limite;
    private final LongSupplier relogio;
    private final SecureRandom sorteio = new SecureRandom();
    private ScheduledExecutorService limpeza;

    /**
     * Cria o gerenciador, sem limite de partidas.
     *
     * @param fabricaDeFontes cria a fonte de peças de cada partida nova
     */
    public GerenciadorPartidas(Function<Dificuldade, FontePecas> fabricaDeFontes) {
        this(fabricaDeFontes, SEM_LIMITE, System::nanoTime);
    }

    /**
     * Cria o gerenciador com um teto de partidas abertas e um relógio próprio.
     *
     * @param fabricaDeFontes cria a fonte de peças de cada partida nova
     * @param limite          máximo de partidas abertas ao mesmo tempo
     * @param relogio         instante atual, em nanossegundos, repassado às sessões
     * @throws IllegalArgumentException se o limite não for positivo
     */
    public GerenciadorPartidas(Function<Dificuldade, FontePecas> fabricaDeFontes, int limite,
                               LongSupplier relogio) {
        if (limite < 1) {
            throw new IllegalArgumentException("o limite de partidas deve ser positivo: " + limite);
        }
        this.fabricaDeFontes = Objects.requireNonNull(fabricaDeFontes, "fábrica de fontes");
        this.limite = limite;
        this.relogio = Objects.requireNonNull(relogio, "relógio");
    }

    /**
     * Cria uma partida, ainda parada: ela começa quando o WebSocket conecta.
     *
     * <p>O teto é conferido antes de criar. Duas criações simultâneas podem
     * passar dele por uma partida, o que basta para proteger o servidor.</p>
     *
     * @param dificuldade dificuldade escolhida
     * @return a sessão criada
     * @throws LimiteDePartidasException se já houver o máximo de partidas abertas
     */
    public SessaoPartida criar(Dificuldade dificuldade) {
        if (sessoes.size() >= limite) {
            throw new LimiteDePartidasException(limite);
        }
        MotorJogo motor = new MotorJogo(dificuldade, fabricaDeFontes.apply(dificuldade));
        String id;
        SessaoPartida sessao;
        do {
            id = novoId();
            sessao = new SessaoPartida(id, motor, relogio);
        } while (sessoes.putIfAbsent(id, sessao) != null);
        return sessao;
    }

    /**
     * Descarta as partidas sem ninguém conectado há pelo menos o tempo dado (RF31).
     *
     * @param tolerancia tempo sem conexão tolerado
     * @return quantas partidas foram descartadas
     */
    public int removerAbandonadas(Duration tolerancia) {
        long toleranciaNanos = tolerancia.toNanos();
        int removidas = 0;
        for (SessaoPartida sessao : sessoes.values()) {
            if (sessao.abandonadaHa(toleranciaNanos)) {
                remover(sessao.id());
                removidas++;
            }
        }
        return removidas;
    }

    /**
     * Passa a descartar as partidas abandonadas a cada intervalo, numa thread
     * própria. Chamar de novo não faz nada.
     *
     * @param tolerancia tempo sem conexão tolerado antes de descartar
     * @param intervalo  de quanto em quanto tempo conferir
     */
    public synchronized void iniciarLimpeza(Duration tolerancia, Duration intervalo) {
        if (limpeza != null) {
            return;
        }
        limpeza = Executors.newSingleThreadScheduledExecutor(tarefa -> {
            Thread thread = new Thread(tarefa, "limpeza-de-partidas");
            thread.setDaemon(true);
            return thread;
        });
        long passo = intervalo.toMillis();
        limpeza.scheduleAtFixedRate(() -> limparComLog(tolerancia), passo, passo, TimeUnit.MILLISECONDS);
    }

    /** Uma falha na limpeza não pode matar o agendamento, que pararia de vez. */
    private void limparComLog(Duration tolerancia) {
        try {
            int removidas = removerAbandonadas(tolerancia);
            if (removidas > 0) {
                LOG.info("{} partida(s) abandonada(s) descartada(s); {} aberta(s)", removidas, quantidade());
            }
        } catch (RuntimeException erro) {
            LOG.error("Erro ao descartar partidas abandonadas", erro);
        }
    }

    /**
     * Busca uma partida pelo id, em O(1).
     *
     * @param id id da partida
     * @return a sessão, se existir
     */
    public Optional<SessaoPartida> buscar(String id) {
        return Optional.ofNullable(sessoes.get(id));
    }

    /**
     * Encerra e esquece uma partida.
     *
     * @param id id da partida
     */
    public void remover(String id) {
        SessaoPartida sessao = sessoes.remove(id);
        if (sessao != null) {
            sessao.encerrar();
        }
    }

    /**
     * Para a limpeza periódica e encerra todas as partidas, ao desligar o servidor.
     */
    public void encerrarTodas() {
        synchronized (this) {
            if (limpeza != null) {
                limpeza.shutdownNow();
            }
        }
        sessoes.keySet().forEach(this::remover);
    }

    /**
     * Quantas partidas estão abertas.
     *
     * @return número de sessões
     */
    public int quantidade() {
        return sessoes.size();
    }

    private String novoId() {
        StringBuilder id = new StringBuilder(TAMANHO_ID);
        for (int i = 0; i < TAMANHO_ID; i++) {
            id.append(ALFABETO.charAt(sorteio.nextInt(ALFABETO.length())));
        }
        return id.toString();
    }
}
