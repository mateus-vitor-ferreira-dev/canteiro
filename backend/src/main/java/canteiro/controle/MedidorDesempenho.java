package canteiro.controle;

import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.function.LongSupplier;

/**
 * Mede o laço de uma partida em janelas de dez segundos: quantos ciclos
 * rodaram por segundo, quanto durou um ciclo em média e no pior caso, e
 * quanta memória a JVM está usando.
 *
 * <p>Não tem thread própria nem relógio próprio: quem chama informa a duração
 * de cada ciclo, e o relógio vem pelo construtor, o que permite testar com um
 * relógio falso. É usado só pela thread do laço.</p>
 *
 * @author Mateus Vitor Ferreira
 * @version 0.1.0
 */
public final class MedidorDesempenho {

    private static final long JANELA_NANOS = TimeUnit.SECONDS.toNanos(10);
    private static final double NANOS_POR_SEGUNDO = TimeUnit.SECONDS.toNanos(1);
    private static final double NANOS_POR_MS = TimeUnit.MILLISECONDS.toNanos(1);
    private static final long BYTES_POR_MB = 1024L * 1024L;

    private final LongSupplier relogioNanos;
    private long inicioJanela;
    private long ciclos;
    private long somaNanos;
    private long maximoNanos;

    /**
     * O resultado de uma janela de medição.
     *
     * @param ciclosPorSegundo ciclos que rodaram por segundo na janela
     * @param mediaMs          duração média de um ciclo, em milissegundos
     * @param maximoMs         duração do ciclo mais lento, em milissegundos
     * @param memoriaMb        memória em uso na JVM ao fechar a janela, em megabytes
     */
    public record Medicao(double ciclosPorSegundo, double mediaMs, double maximoMs, long memoriaMb) {
    }

    /**
     * Cria o medidor e abre a primeira janela no instante atual do relógio.
     *
     * @param relogioNanos relógio em nanossegundos, como {@code System::nanoTime}
     */
    public MedidorDesempenho(LongSupplier relogioNanos) {
        this.relogioNanos = Objects.requireNonNull(relogioNanos, "relogioNanos");
        this.inicioJanela = relogioNanos.getAsLong();
    }

    /**
     * Anota um ciclo que acabou de rodar.
     *
     * @param duracaoNanos quanto o ciclo durou, em nanossegundos
     */
    public void registrar(long duracaoNanos) {
        ciclos++;
        somaNanos += duracaoNanos;
        maximoNanos = Math.max(maximoNanos, duracaoNanos);
    }

    /**
     * Fecha a janela se ela já dura dez segundos: devolve a medição e recomeça
     * a contagem do zero.
     *
     * @return a medição da janela que fechou, ou vazio se ela ainda não venceu
     */
    public Optional<Medicao> fecharJanelaSeVenceu() {
        long agora = relogioNanos.getAsLong();
        long decorrido = agora - inicioJanela;
        if (decorrido < JANELA_NANOS) {
            return Optional.empty();
        }
        double media = ciclos == 0 ? 0 : somaNanos / (double) ciclos / NANOS_POR_MS;
        Medicao medicao = new Medicao(
                ciclos * NANOS_POR_SEGUNDO / decorrido,
                media,
                maximoNanos / NANOS_POR_MS,
                memoriaEmUsoMb());
        inicioJanela = agora;
        ciclos = 0;
        somaNanos = 0;
        maximoNanos = 0;
        return Optional.of(medicao);
    }

    private static long memoriaEmUsoMb() {
        Runtime jvm = Runtime.getRuntime();
        return (jvm.totalMemory() - jvm.freeMemory()) / BYTES_POR_MB;
    }
}
