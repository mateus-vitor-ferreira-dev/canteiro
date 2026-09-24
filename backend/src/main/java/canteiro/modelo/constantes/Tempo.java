package canteiro.modelo.constantes;

/**
 * Ritmo da simulação.
 *
 * <p>O motor avança em ciclos, não no relógio: a mesma sequência de comandos
 * sempre produz a mesma partida, o que é condição para o replay.</p>
 *
 * @author Mateus Vitor Ferreira
 * @version 0.1.0
 */
public final class Tempo {

    /** Ciclos de simulação por segundo (RNF01). */
    public static final int CICLOS_POR_SEGUNDO = 60;

    /** Milissegundos em um segundo. */
    public static final int MS_POR_SEGUNDO = 1000;

    private Tempo() {
    }

    /**
     * Converte um intervalo em milissegundos no número de ciclos equivalente,
     * arredondado, e no mínimo um.
     *
     * @param milissegundos intervalo
     * @return quantidade de ciclos
     */
    public static int ciclos(long milissegundos) {
        return (int) Math.max(1, Math.round(milissegundos * (double) CICLOS_POR_SEGUNDO / MS_POR_SEGUNDO));
    }
}
