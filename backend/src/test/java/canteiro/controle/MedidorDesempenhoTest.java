package canteiro.controle;

import canteiro.controle.MedidorDesempenho.Medicao;
import org.junit.jupiter.api.Test;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * O medidor do laço, com um relógio falso que o teste avança à mão.
 *
 * @author Mateus Vitor Ferreira
 * @version 0.1.0
 */
class MedidorDesempenhoTest {

    private static final long UM_MS = TimeUnit.MILLISECONDS.toNanos(1);
    private static final long DEZ_SEGUNDOS = TimeUnit.SECONDS.toNanos(10);
    private static final int CICLOS_EM_DEZ_SEGUNDOS = 600;
    private static final double TOLERANCIA = 1e-9;

    private final AtomicLong relogio = new AtomicLong(5_000);
    private final MedidorDesempenho medidor = new MedidorDesempenho(relogio::get);

    @Test
    void antesDeDezSegundosNaoFechaAJanela() {
        medidor.registrar(UM_MS);
        relogio.addAndGet(DEZ_SEGUNDOS - 1);

        assertTrue(medidor.fecharJanelaSeVenceu().isEmpty());
    }

    @Test
    void seiscentosCiclosEmDezSegundosSaoSessentaPorSegundo() {
        for (int i = 0; i < CICLOS_EM_DEZ_SEGUNDOS; i++) {
            medidor.registrar(UM_MS);
        }
        relogio.addAndGet(DEZ_SEGUNDOS);

        Medicao medicao = medidor.fecharJanelaSeVenceu().orElseThrow();

        assertEquals(60.0, medicao.ciclosPorSegundo(), TOLERANCIA);
    }

    @Test
    void calculaAMediaEOMaximoDasDuracoes() {
        medidor.registrar(UM_MS);
        medidor.registrar(3 * UM_MS);
        medidor.registrar(2 * UM_MS);
        relogio.addAndGet(DEZ_SEGUNDOS);

        Medicao medicao = medidor.fecharJanelaSeVenceu().orElseThrow();

        assertEquals(2.0, medicao.mediaMs(), TOLERANCIA);
        assertEquals(3.0, medicao.maximoMs(), TOLERANCIA);
        assertTrue(medicao.memoriaMb() > 0, "a JVM do teste usa alguma memória");
    }

    @Test
    void depoisDeFecharRecomecaDoZero() {
        medidor.registrar(3 * UM_MS);
        relogio.addAndGet(DEZ_SEGUNDOS);
        medidor.fecharJanelaSeVenceu();
        assertTrue(medidor.fecharJanelaSeVenceu().isEmpty(), "a janela nova acabou de abrir");

        medidor.registrar(UM_MS);
        relogio.addAndGet(DEZ_SEGUNDOS);
        Medicao medicao = medidor.fecharJanelaSeVenceu().orElseThrow();

        assertEquals(0.1, medicao.ciclosPorSegundo(), TOLERANCIA);
        assertEquals(1.0, medicao.maximoMs(), TOLERANCIA);
    }

    @Test
    void janelaSemCicloNenhumNaoDividePorZero() {
        relogio.addAndGet(DEZ_SEGUNDOS);

        Medicao medicao = medidor.fecharJanelaSeVenceu().orElseThrow();

        assertEquals(0.0, medicao.ciclosPorSegundo(), TOLERANCIA);
        assertEquals(0.0, medicao.mediaMs(), TOLERANCIA);
    }
}
