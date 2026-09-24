package canteiro.modelo;

import java.util.List;

/**
 * Dificuldades oferecidas ao criar uma partida (RF02).
 *
 * <p>Cada dificuldade define as condições iniciais da partida: o intervalo de
 * queda, o limite de desvio do centro de massa e os materiais liberados. Os
 * valores partem da tabela de progressão da especificação (seção 4.4): a
 * dificuldade fácil começa como o nível 1, a normal como o nível 3 e a difícil
 * como o nível 5. São valores iniciais, a ajustar nos testes com jogadores.</p>
 *
 * @author Mateus Vitor Ferreira
 * @version 0.1.0
 */
public enum Dificuldade {

    /** Queda lenta, limite de desvio folgado e só os materiais leves. */
    FACIL("Fácil", 800, 3.0, List.of("MADEIRA", "ALVENARIA")),

    /** Queda moderada e o concreto já liberado. */
    NORMAL("Normal", 650, 2.5, List.of("MADEIRA", "ALVENARIA", "CONCRETO")),

    /** Queda rápida, limite apertado e todos os materiais desde o início. */
    DIFICIL("Difícil", 500, 2.0, List.of("MADEIRA", "ALVENARIA", "CONCRETO", "ACO"));

    private final String nomeExibicao;
    private final long intervaloQuedaMs;
    private final double limiteDesvio;
    private final List<String> materiaisLiberados;

    Dificuldade(String nomeExibicao, long intervaloQuedaMs, double limiteDesvio,
                List<String> materiaisLiberados) {
        this.nomeExibicao = nomeExibicao;
        this.intervaloQuedaMs = intervaloQuedaMs;
        this.limiteDesvio = limiteDesvio;
        this.materiaisLiberados = materiaisLiberados;
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
     * Devolve o intervalo inicial entre duas descidas da peça em queda.
     *
     * @return intervalo de queda, em milissegundos
     */
    public long intervaloQuedaMs() {
        return intervaloQuedaMs;
    }

    /**
     * Devolve o desvio máximo tolerado entre o centro de massa e o eixo da base
     * antes do colapso (RN11).
     *
     * @return limite de desvio inicial, em colunas
     */
    public double limiteDesvio() {
        return limiteDesvio;
    }

    /**
     * Devolve os códigos dos materiais que podem ser sorteados no início da
     * partida (RN03).
     *
     * @return lista imutável de códigos de material
     */
    public List<String> materiaisLiberados() {
        return materiaisLiberados;
    }
}
