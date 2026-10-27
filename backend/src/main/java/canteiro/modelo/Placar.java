package canteiro.modelo;

import canteiro.modelo.materiais.Material;

/**
 * Pontuação, linhas, nível, colapsos e sequência de linhas da partida.
 *
 * @author Mateus Vitor Ferreira
 * @version 0.1.0
 */
public final class Placar {

    private int pontuacao;
    private int linhas;
    private int nivel;
    private int colapsos;
    private int sequencia;

    /**
     * Começa zerado, no nível inicial.
     *
     * @param nivelInicial nível em que a partida começa
     * @throws IllegalArgumentException se o nível for menor que 1
     */
    public Placar(int nivelInicial) {
        if (nivelInicial < Progressao.NIVEL_MINIMO) {
            throw new IllegalArgumentException("nível inválido: " + nivelInicial);
        }
        this.nivel = nivelInicial;
    }

    /**
     * Soma os pontos de uma eliminação e sobe de nível a cada 10 linhas (RN14).
     * Os pontos usam o nível de antes da subida.
     *
     * @param quantidade   linhas eliminadas de uma vez
     * @param predominante material predominante nessas linhas
     * @return {@code true} se o nível subiu
     */
    public boolean registrarLinhas(int quantidade, Material predominante) {
        return registrarLinhas(quantidade, predominante, false);
    }

    /**
     * Soma os pontos de uma eliminação, com os bônus de ousadia e de sequência,
     * e aumenta a sequência (RN17, RN18). Sobe de nível a cada 10 linhas (RN14);
     * os pontos usam o nível de antes da subida.
     *
     * @param quantidade   linhas eliminadas de uma vez
     * @param predominante material predominante nessas linhas
     * @param ousada       se a estrutura estava abaixo do limiar de ousadia
     * @return {@code true} se o nível subiu
     */
    public boolean registrarLinhas(int quantidade, Material predominante, boolean ousada) {
        pontuacao += Pontuacao.pontos(quantidade, nivel, predominante, ousada, sequencia);
        sequencia++;
        int nivelAntes = nivel;
        int subidas = (linhas + quantidade) / Progressao.LINHAS_POR_NIVEL - linhas / Progressao.LINHAS_POR_NIVEL;
        linhas += quantidade;
        nivel += subidas;
        return nivel > nivelAntes;
    }

    /**
     * Desconta a penalidade de um colapso, sem deixar a pontuação negativa, e
     * soma um no contador (RN12).
     */
    public void registrarColapso() {
        pontuacao = Math.max(0, pontuacao - Pontuacao.penalidadeColapso(nivel));
        colapsos++;
        sequencia = 0;
    }

    /**
     * Uma peça assentou sem fechar linha: a sequência zera (RN18).
     */
    public void quebrarSequencia() {
        sequencia = 0;
    }

    /**
     * Quantas peças seguidas fecharam linha (RN18).
     *
     * @return o tamanho da sequência atual
     */
    public int sequencia() {
        return sequencia;
    }

    /**
     * Devolve quantos colapsos houve na partida.
     *
     * @return contador de colapsos
     */
    public int colapsos() {
        return colapsos;
    }

    /**
     * Devolve a pontuação.
     *
     * @return pontos acumulados
     */
    public int pontuacao() {
        return pontuacao;
    }

    /**
     * Devolve o total de linhas eliminadas.
     *
     * @return quantidade de linhas
     */
    public int linhas() {
        return linhas;
    }

    /**
     * Devolve o nível atual.
     *
     * @return nível, a partir de 1
     */
    public int nivel() {
        return nivel;
    }
}
