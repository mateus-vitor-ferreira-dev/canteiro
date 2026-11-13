package canteiro.modelo;

import java.util.List;

/**
 * Dificuldades oferecidas ao criar uma partida (RF02).
 *
 * <p>Cada dificuldade é um nível inicial: a fácil começa no nível 1, a normal
 * no 3 e a difícil no 5. O intervalo de queda, o limite de desvio e os
 * materiais vêm da {@link Progressao} desse nível. Além disso, cada uma tem
 * dois fatores que valem a partida inteira: um multiplica o limite de desvio
 * e o outro, a carga máxima dos materiais (RN21, RN22).</p>
 *
 * @author Mateus Vitor Ferreira
 * @version 0.1.0
 */
public enum Dificuldade {

    /** Queda lenta, limite de desvio folgado, só os materiais leves e blocos mais resistentes. */
    FACIL("Fácil", 1, 1.0, 1.25),

    /** Queda moderada e o concreto já liberado. */
    NORMAL("Normal", 3, 1.0, 1.0),

    /** Queda rápida, todos os materiais desde o início, limite 20 % mais apertado e blocos 20 % mais frágeis. */
    DIFICIL("Difícil", 5, 0.8, 0.8);

    private final String nomeExibicao;
    private final int nivelInicial;
    private final double fatorLimite;
    private final double fatorResistencia;

    Dificuldade(String nomeExibicao, int nivelInicial, double fatorLimite, double fatorResistencia) {
        this.nomeExibicao = nomeExibicao;
        this.nivelInicial = nivelInicial;
        this.fatorLimite = fatorLimite;
        this.fatorResistencia = fatorResistencia;
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
        return limiteDesvio(nivelInicial);
    }

    /**
     * Devolve o desvio máximo tolerado num nível, já com o fator desta
     * dificuldade.
     *
     * @param nivel nível atual, a partir de 1
     * @return limite de desvio, em colunas
     * @throws IllegalArgumentException se o nível for menor que 1
     */
    public double limiteDesvio(int nivel) {
        return Progressao.limiteDesvio(nivel) * fatorLimite;
    }

    /**
     * Quanto a carga máxima de cada material é multiplicada nesta
     * dificuldade (RN21).
     *
     * @return fator maior que zero; 1 é a resistência normal
     */
    public double fatorResistencia() {
        return fatorResistencia;
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
