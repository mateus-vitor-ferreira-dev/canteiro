package canteiro.modelo;

import canteiro.modelo.materiais.Material;

/**
 * Pontuação, linhas, nível e colapsos da partida.
 *
 * @author Mateus Vitor Ferreira
 * @version 0.1.0
 */
public final class Placar {

    private int pontuacao;
    private int linhas;
    private int nivel;
    private int colapsos;

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
        pontuacao += Pontuacao.pontos(quantidade, nivel, predominante);
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
