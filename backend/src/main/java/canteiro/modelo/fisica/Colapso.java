package canteiro.modelo.fisica;

import canteiro.modelo.Bloco;
import canteiro.modelo.Celula;
import canteiro.modelo.Queda;
import canteiro.modelo.Tabuleiro;
import canteiro.modelo.constantes.Dimensoes;

import java.util.ArrayList;
import java.util.List;

/**
 * O desabamento da estrutura quando o desvio passa do limite (RN11).
 *
 * <p>Acha a <strong>linha crítica</strong>, a de maior massa, e desprende os
 * blocos acima dela que não estão consolidados. Cada bloco desprendido cai na
 * própria coluna até encontrar apoio. A queda é <strong>recursiva</strong>:
 * cair uma linha e, se ainda houver espaço, cair de novo a partir dali. Não há
 * tombamento lateral: os blocos continuam nas mesmas colunas.</p>
 *
 * @author Mateus Vitor Ferreira
 * @version 0.1.0
 */
public final class Colapso {

    private Colapso() {
    }

    /**
     * Executa o colapso no tabuleiro.
     *
     * @param tabuleiro tabuleiro a desabar
     * @return cada bloco que caiu, com origem e destino, para a animação
     */
    public static List<Queda> executar(Tabuleiro tabuleiro) {
        int critica = linhaCritica(tabuleiro);
        List<Queda> quedas = new ArrayList<>();
        for (int coluna = 0; coluna < Dimensoes.COLUNAS; coluna++) {
            for (int linha = critica - 1; linha >= 0; linha--) {
                Bloco bloco = tabuleiro.bloco(linha, coluna);
                if (bloco != null && !bloco.consolidado()) {
                    Celula origem = new Celula(linha, coluna);
                    Celula destino = cair(tabuleiro, origem);
                    if (!destino.equals(origem)) {
                        quedas.add(new Queda(origem, destino));
                    }
                }
            }
        }
        return quedas;
    }

    /**
     * A linha de maior massa. No empate, a mais alta, para desprender menos blocos.
     *
     * @param tabuleiro tabuleiro a examinar
     * @return índice da linha crítica; a do fundo se o tabuleiro estiver vazio
     */
    public static int linhaCritica(Tabuleiro tabuleiro) {
        int critica = Dimensoes.LINHAS - 1;
        double maior = -1;
        for (int linha = 0; linha < Dimensoes.LINHAS; linha++) {
            double massa = 0;
            for (int coluna = 0; coluna < Dimensoes.COLUNAS; coluna++) {
                Bloco bloco = tabuleiro.bloco(linha, coluna);
                massa += bloco == null ? 0 : bloco.massa();
            }
            if (massa > maior) {
                maior = massa;
                critica = linha;
            }
        }
        return critica;
    }

    /** Desce o bloco uma linha enquanto houver espaço embaixo. Devolve onde ele parou. */
    private static Celula cair(Tabuleiro tabuleiro, Celula posicao) {
        Celula abaixo = posicao.deslocada(1, 0);
        if (!Tabuleiro.dentro(abaixo) || tabuleiro.ocupada(abaixo.linha(), abaixo.coluna())) {
            return posicao;
        }
        tabuleiro.moverBloco(posicao, abaixo);
        return cair(tabuleiro, abaixo);
    }
}
