package canteiro.modelo;

import canteiro.modelo.constantes.Dimensoes;
import canteiro.modelo.pecas.Peca;

import java.util.List;
import java.util.Objects;

/**
 * A peça que está caindo, com a posição dela no tabuleiro.
 *
 * <p>Sabe mover, girar com deslocamento corretivo (RF08), achar onde pousaria
 * e se fixar. Toda tentativa consulta o tabuleiro antes: um movimento que
 * colidiria não acontece.</p>
 *
 * @author Mateus Vitor Ferreira
 * @version 0.1.0
 */
public final class PecaEmQueda {

    /** Linha em que a peça nasce: a primeira das linhas ocultas. */
    public static final int LINHA_NASCIMENTO = 0;

    private final Peca peca;
    private final Tabuleiro tabuleiro;
    private int linha;
    private int coluna;

    private PecaEmQueda(Peca peca, Tabuleiro tabuleiro, int linha, int coluna) {
        this.peca = Objects.requireNonNull(peca, "peça");
        this.tabuleiro = Objects.requireNonNull(tabuleiro, "tabuleiro");
        this.linha = linha;
        this.coluna = coluna;
    }

    /**
     * Põe a peça no topo, centralizada nas linhas ocultas.
     *
     * @param peca      peça nova
     * @param tabuleiro tabuleiro da partida
     * @return a peça em queda, que pode já estar colidindo (fim de jogo)
     */
    public static PecaEmQueda nascer(Peca peca, Tabuleiro tabuleiro) {
        return new PecaEmQueda(peca, tabuleiro, LINHA_NASCIMENTO, (Dimensoes.COLUNAS - peca.tamanho()) / 2);
    }

    /**
     * Informa se a peça, onde está, bate em alguma coisa.
     *
     * @return {@code true} se colide
     */
    public boolean colide() {
        return tabuleiro.colide(peca, linha, coluna);
    }

    /**
     * Move a peça, se couber.
     *
     * @param linhas  quantas linhas descer (negativo sobe)
     * @param colunas quantas colunas ir para a direita (negativo vai para a esquerda)
     * @return {@code true} se moveu
     */
    public boolean mover(int linhas, int colunas) {
        if (tabuleiro.colide(peca, linha + linhas, coluna + colunas)) {
            return false;
        }
        linha += linhas;
        coluna += colunas;
        return true;
    }

    /**
     * Gira a peça. Se o giro simples colidir, tenta os deslocamentos corretivos
     * da peça, em ordem, e aplica o primeiro que couber. Se nenhum couber,
     * desfaz o giro (RF08).
     *
     * @param horario {@code true} para o sentido horário
     * @return {@code true} se girou
     */
    public boolean girar(boolean horario) {
        girarPeca(horario);
        if (!colide()) {
            return true;
        }
        for (int[] deslocamento : peca.deslocamentosCorretivos()) {
            if (mover(deslocamento[0], deslocamento[1])) {
                return true;
            }
        }
        girarPeca(!horario);
        return false;
    }

    private void girarPeca(boolean horario) {
        if (horario) {
            peca.girarHorario();
        } else {
            peca.girarAntiHorario();
        }
    }

    /**
     * Desce a peça até o primeiro apoio, sem fixar.
     */
    public void cairAtePouso() {
        linha = linhaDePouso();
    }

    private int linhaDePouso() {
        int pouso = linha;
        while (!tabuleiro.colide(peca, pouso + 1, coluna)) {
            pouso++;
        }
        return pouso;
    }

    /**
     * Fixa a peça onde ela está.
     *
     * @return as células em que ela foi fixada
     * @throws IllegalStateException se ela colidir onde está
     */
    public List<Celula> fixar() {
        return tabuleiro.fixar(peca, linha, coluna);
    }

    /**
     * Posições que a peça ocupa no tabuleiro.
     *
     * @return as quatro células
     */
    public List<Celula> celulas() {
        return posicoes(linha);
    }

    /**
     * Onde a peça pousaria com uma queda instantânea: a "peça fantasma".
     *
     * @return as quatro células do pouso
     */
    public List<Celula> celulasNoPouso() {
        return posicoes(linhaDePouso());
    }

    private List<Celula> posicoes(int linhaDaPeca) {
        return peca.celulas().stream().map(c -> c.deslocada(linhaDaPeca, coluna)).toList();
    }

    /**
     * Devolve a peça.
     *
     * @return a peça, com a rotação atual
     */
    public Peca peca() {
        return peca;
    }
}
