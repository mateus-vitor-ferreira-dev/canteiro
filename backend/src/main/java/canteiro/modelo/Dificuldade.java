package canteiro.modelo;

import java.util.List;

/**
 * Dificuldades oferecidas ao criar uma partida (RF02).
 *
 * <p>Cada dificuldade é um nível inicial: a fácil começa no nível 1, a normal
 * no 3 e a difícil no 5. O intervalo de queda, o limite de desvio e os
 * materiais vêm da {@link Progressao} desse nível.</p>
 *
 * @author Mateus Vitor Ferreira
 * @version 0.1.0
 */
public enum Dificuldade {

    /** Queda lenta, limite de desvio folgado e só os materiais leves. */
    FACIL("Fácil", 1),

    /** Queda moderada e o concreto já liberado. */
    NORMAL("Normal", 3),

    /** Queda rápida, limite apertado e todos os materiais desde o início. */
    DIFICIL("Difícil", 5);

    private final String nomeExibicao;
    private final int nivelInicial;

    Dificuldade(String nomeExibicao, int nivelInicial) {
        this.nomeExibicao = nomeExibicao;
        this.nivelInicial = nivelInicial;
    }

    /**
     * Devolve o nome mostrado ao jogador.
     *
     * @return nome da dificuldade, com acentuação
     */
    public String nomeExibicao() {
        return nomeExibicao;
    }

    /**
     * Nível em que a partida começa.
     *
     * @return nível inicial, a partir de 1
     */
    public int nivelInicial() {
        return nivelInicial;
    }

    /**
     * Devolve o intervalo inicial entre duas descidas da peça em queda.
     *
     * @return intervalo de queda, em milissegundos
     */
    public long intervaloQuedaMs() {
        return Progressao.intervaloQuedaMs(nivelInicial);
    }

    /**
     * Devolve o desvio máximo tolerado no início da partida (RN11).
     *
     * @return limite de desvio inicial, em colunas
     */
    public double limiteDesvio() {
        return Progressao.limiteDesvio(nivelInicial);
    }

    /**
     * Devolve os códigos dos materiais liberados no início da partida (RN03).
     *
     * @return lista imutável de códigos de material
     */
    public List<String> materiaisLiberados() {
        return Progressao.materiaisLiberados(nivelInicial);
    }
}
