package canteiro.modelo.fisica;

import canteiro.modelo.Bloco;
import canteiro.modelo.Celula;
import canteiro.modelo.Tabuleiro;
import canteiro.modelo.constantes.Dimensoes;
import canteiro.modelo.materiais.Material;

/**
 * Análise estrutural da pilha: centro de massa, base de apoio e índice de
 * estabilidade (RN07 a RN10).
 *
 * <p>O centro de massa é calculado de forma <strong>incremental</strong>, em
 * tempo constante por bloco alterado (RNF02): em vez de percorrer a grade a
 * cada ciclo, o analisador mantém dois acumuladores, a massa total e a soma
 * dos momentos (massa × posição do centro da coluna). Fixar um bloco soma nos
 * dois; eliminar, subtrai. Um bloco que só cai na mesma coluna não muda nada.</p>
 *
 * <p>A <strong>base de apoio</strong> é a faixa ocupada pela linha mais baixa
 * da estrutura, e o eixo é o meio dela: uma peça sozinha num canto está
 * apoiada sobre si mesma; uma torre inclinada para fora da base cai.</p>
 *
 * @author Mateus Vitor Ferreira
 * @version 0.1.0
 */
public final class AnalisadorEstrutural {

    /** Centro de uma coluna, a partir da borda esquerda dela. */
    private static final double MEIO_DA_COLUNA = 0.5;

    /**
     * Abaixo desta massa, a estrutura conta como vazia. Somar e subtrair
     * números com vírgula deixa sobras minúsculas, que dariam um centro de
     * massa absurdo quando a estrutura esvazia.
     */
    private static final double MASSA_DESPREZIVEL = 1e-9;

    private double massaTotal;
    private double momento;

    /**
     * Soma um bloco aos acumuladores, em O(1).
     *
     * @param celula   onde o bloco foi fixado
     * @param material material do bloco
     */
    public void registrar(Celula celula, Material material) {
        double massa = material.massaPorBloco();
        massaTotal += massa;
        momento += massa * (celula.coluna() + MEIO_DA_COLUNA);
    }

    /**
     * Tira um bloco dos acumuladores, em O(1).
     *
     * @param celula   onde o bloco estava
     * @param material material do bloco
     */
    public void remover(Celula celula, Material material) {
        double massa = material.massaPorBloco();
        massaTotal -= massa;
        momento -= massa * (celula.coluna() + MEIO_DA_COLUNA);
        if (vazia()) {
            massaTotal = 0;
            momento = 0;
        }
    }

    /**
     * Informa se não há massa na estrutura.
     *
     * @return {@code true} se a estrutura estiver vazia
     */
    public boolean vazia() {
        return massaTotal < MASSA_DESPREZIVEL;
    }

    /**
     * Massa de toda a estrutura (RN07).
     *
     * @return massa total, em toneladas
     */
    public double massaTotal() {
        return massaTotal;
    }

    /**
     * Posição horizontal do centro de massa: a média das colunas dos blocos,
     * ponderada pela massa (RN08).
     *
     * @return centro de massa, em colunas a partir da borda esquerda
     * @throws IllegalStateException se a estrutura estiver vazia
     */
    public double centroDeMassa() {
        if (vazia()) {
            throw new IllegalStateException("estrutura vazia não tem centro de massa");
        }
        return momento / massaTotal;
    }

    /**
     * Estabilidade atual da estrutura.
     *
     * @param tabuleiro tabuleiro, para achar a base de apoio
     * @param limite    desvio máximo tolerado no nível
     * @return índice, desvio e demais valores; estrutura vazia é totalmente estável
     */
    public Estabilidade estabilidade(Tabuleiro tabuleiro, double limite) {
        if (vazia()) {
            double meio = Dimensoes.COLUNAS / 2.0;
            return new Estabilidade(1, 0, limite, meio, meio, false);
        }
        double centro = centroDeMassa();
        double eixo = eixoDaBase(tabuleiro);
        double desvio = Math.abs(centro - eixo);
        double indice = Math.max(0, 1 - desvio / limite);
        return new Estabilidade(indice, desvio, limite, centro, eixo, indice < Estabilidade.LIMIAR_ALERTA);
    }

    /**
     * Meio da base de apoio: a faixa entre o primeiro e o último bloco da
     * linha mais baixa ocupada (RN09).
     *
     * @param tabuleiro tabuleiro a examinar
     * @return eixo, em colunas a partir da borda esquerda; o meio do tabuleiro se estiver vazio
     */
    public static double eixoDaBase(Tabuleiro tabuleiro) {
        for (int linha = Dimensoes.LINHAS - 1; linha >= 0; linha--) {
            int primeira = -1;
            int ultima = -1;
            for (int coluna = 0; coluna < Dimensoes.COLUNAS; coluna++) {
                if (tabuleiro.ocupada(linha, coluna)) {
                    primeira = primeira < 0 ? coluna : primeira;
                    ultima = coluna;
                }
            }
            if (primeira >= 0) {
                return (primeira + ultima + 1) / 2.0;
            }
        }
        return Dimensoes.COLUNAS / 2.0;
    }

    /**
     * Centro de massa calculado do jeito ingênuo, percorrendo a grade inteira.
     * Serve para conferir o cálculo incremental nos testes.
     *
     * @param tabuleiro tabuleiro a percorrer
     * @return centro de massa, em colunas
     * @throws IllegalStateException se o tabuleiro estiver vazio
     */
    public static double centroPorVarredura(Tabuleiro tabuleiro) {
        double massa = 0;
        double soma = 0;
        for (int linha = 0; linha < Dimensoes.LINHAS; linha++) {
            for (int coluna = 0; coluna < Dimensoes.COLUNAS; coluna++) {
                Bloco bloco = tabuleiro.bloco(linha, coluna);
                if (bloco != null) {
                    massa += bloco.massa();
                    soma += bloco.massa() * (coluna + MEIO_DA_COLUNA);
                }
            }
        }
        if (massa <= 0) {
            throw new IllegalStateException("estrutura vazia não tem centro de massa");
        }
        return soma / massa;
    }
}
