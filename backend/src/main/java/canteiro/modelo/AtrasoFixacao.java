package canteiro.modelo;

import canteiro.modelo.constantes.Tempo;

/**
 * O tempo de ajuste antes de fixar uma peça que encostou no apoio (RN16).
 *
 * <p>Quando a gravidade leva a peça até o apoio, ela não é fixada no mesmo
 * ciclo: o jogador tem meio segundo para deslizar ou girar. Cada movimento que
 * dá certo recomeça a contagem, até um número máximo de vezes por peça, para
 * ninguém segurar a peça no ar para sempre. Tudo é contado em ciclos, e a
 * repetição continua dando a mesma partida.</p>
 *
 * @author Mateus Vitor Ferreira
 * @version 0.1.0
 */
public final class AtrasoFixacao {

    /** Quanto a peça espera apoiada antes de ser fixada, em milissegundos. */
    public static final long ESPERA_MS = 500;

    /** Quantas vezes um movimento pode recomeçar a espera na mesma peça. */
    public static final int RECOMECOS_MAXIMOS = 15;

    private static final int CICLOS_DE_ESPERA = Tempo.ciclos(ESPERA_MS);

    private int ciclosApoiada;
    private int recomecos;

    /**
     * Zera tudo para uma peça nova.
     */
    public void novaPeca() {
        ciclosApoiada = 0;
        recomecos = 0;
    }

    /**
     * Conta um ciclo com a peça apoiada.
     *
     * @return {@code true} se a espera acabou e a peça deve ser fixada
     */
    public boolean contarCiclo() {
        return ++ciclosApoiada >= CICLOS_DE_ESPERA;
    }

    /**
     * A peça saiu do apoio (deslizou para um buraco): a espera para até ela encostar de novo.
     */
    public void soltou() {
        ciclosApoiada = 0;
    }

    /**
     * Um movimento deu certo com a peça apoiada: recomeça a espera, se ainda houver recomeços.
     */
    public void movimentou() {
        if (ciclosApoiada > 0 && recomecos < RECOMECOS_MAXIMOS) {
            recomecos++;
            ciclosApoiada = 0;
        }
    }

    /**
     * Ciclos de espera que a peça apoiada tem antes de ser fixada.
     *
     * @return a espera, em ciclos
     */
    public static int ciclosDeEspera() {
        return CICLOS_DE_ESPERA;
    }
}
