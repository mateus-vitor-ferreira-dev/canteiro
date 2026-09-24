package canteiro.modelo;

import canteiro.modelo.constantes.Dimensoes;
import canteiro.modelo.materiais.Material;
import canteiro.modelo.pecas.Peca;

import java.util.ArrayList;
import java.util.List;

/**
 * A grade do jogo: 10 colunas por 20 linhas visíveis, mais 2 linhas ocultas
 * no topo, onde as peças nascem (RN01).
 *
 * <p>A grade é uma matriz de {@link Bloco}: acesso em O(1) por linha e
 * coluna, e {@code null} onde está vazio. Uma peça é posicionada pelo canto
 * superior esquerdo do quadrado em que ela gira.</p>
 *
 * @author Mateus Vitor Ferreira
 * @version 0.1.0
 */
public final class Tabuleiro {

    private final Bloco[][] grade = new Bloco[Dimensoes.LINHAS][Dimensoes.COLUNAS];

    /**
     * Informa se a peça, colocada nessa posição, bate em alguma coisa: nas
     * bordas, no fundo, acima do topo da grade ou num bloco já fixado. As
     * linhas ocultas contam como espaço livre.
     *
     * @param peca   peça, na rotação atual
     * @param linha  linha do canto superior esquerdo da peça
     * @param coluna coluna do canto superior esquerdo da peça
     * @return {@code true} se alguma célula da peça estiver fora da grade ou ocupada
     */
    public boolean colide(Peca peca, int linha, int coluna) {
        for (Celula celula : posicoes(peca, linha, coluna)) {
            if (!dentro(celula) || grade[celula.linha()][celula.coluna()] != null) {
                return true;
            }
        }
        return false;
    }

    /**
     * Fixa a peça na grade: cada célula recebe um bloco do material da peça.
     * Depois, o material reage ao assentamento ({@code aoFixar}), como o aço,
     * que consolida os blocos logo abaixo.
     *
     * @param peca   peça, na rotação atual
     * @param linha  linha do canto superior esquerdo da peça
     * @param coluna coluna do canto superior esquerdo da peça
     * @return as células em que a peça foi fixada
     * @throws IllegalStateException se a peça colidir nessa posição
     */
    public List<Celula> fixar(Peca peca, int linha, int coluna) {
        if (colide(peca, linha, coluna)) {
            throw new IllegalStateException("não dá para fixar " + peca + " em (" + linha + ", " + coluna + ")");
        }
        List<Celula> ocupadas = posicoes(peca, linha, coluna);
        for (Celula celula : ocupadas) {
            grade[celula.linha()][celula.coluna()] = new Bloco(peca.material());
        }
        peca.material().aoFixar(this, ocupadas);
        return ocupadas;
    }

    /**
     * Consolida, em cada coluna das células dadas, o bloco logo abaixo da
     * célula mais baixa. É o efeito do aço (RN11): os blocos consolidados
     * resistem ao desprendimento no colapso.
     *
     * @param celulas células recém-ocupadas por uma peça
     */
    public void consolidarAbaixo(List<Celula> celulas) {
        for (Celula celula : celulas) {
            Celula abaixo = celula.deslocada(1, 0);
            if (!celulas.contains(abaixo) && dentro(abaixo) && grade[abaixo.linha()][abaixo.coluna()] != null) {
                grade[abaixo.linha()][abaixo.coluna()].consolidar();
            }
        }
    }

    /**
     * Informa se uma linha tem as 10 posições ocupadas (RN06).
     *
     * @param linha linha a conferir
     * @return {@code true} se não houver nenhuma posição vazia
     */
    public boolean linhaCompleta(int linha) {
        for (Bloco bloco : grade[linha]) {
            if (bloco == null) {
                return false;
            }
        }
        return true;
    }

    /**
     * Índices das linhas completas, sem eliminá-las.
     *
     * @return as linhas completas, de cima para baixo
     */
    public List<Integer> linhasCompletas() {
        List<Integer> completas = new ArrayList<>();
        for (int linha = 0; linha < Dimensoes.LINHAS; linha++) {
            if (linhaCompleta(linha)) {
                completas.add(linha);
            }
        }
        return completas;
    }

    /**
     * Materiais dos blocos das linhas dadas, para descobrir o predominante.
     *
     * @param linhas linhas a percorrer
     * @return o material de cada bloco dessas linhas
     */
    public List<Material> materiaisDasLinhas(List<Integer> linhas) {
        List<Material> materiais = new ArrayList<>();
        for (int linha : linhas) {
            for (Bloco bloco : grade[linha]) {
                if (bloco != null) {
                    materiais.add(bloco.material());
                }
            }
        }
        return materiais;
    }

    /**
     * Elimina as linhas completas e desce as de cima, que continuam nas mesmas
     * colunas (RN06).
     *
     * @return os índices das linhas eliminadas, de cima para baixo, na
     *         numeração de antes da eliminação; vazia se nenhuma estava completa
     */
    public List<Integer> eliminarLinhasCompletas() {
        List<Integer> eliminadas = new ArrayList<>();
        int destino = Dimensoes.LINHAS - 1;
        for (int origem = Dimensoes.LINHAS - 1; origem >= 0; origem--) {
            if (linhaCompleta(origem)) {
                eliminadas.add(0, origem);
            } else {
                grade[destino--] = grade[origem];
            }
        }
        while (destino >= 0) {
            grade[destino--] = new Bloco[Dimensoes.COLUNAS];
        }
        return eliminadas;
    }

    /**
     * Devolve o bloco de uma posição.
     *
     * @param linha  linha, a partir de zero no topo das linhas ocultas
     * @param coluna coluna, a partir de zero à esquerda
     * @return o bloco, ou {@code null} se a posição estiver vazia
     * @throws IndexOutOfBoundsException se a posição estiver fora da grade
     */
    public Bloco bloco(int linha, int coluna) {
        if (!dentro(new Celula(linha, coluna))) {
            throw new IndexOutOfBoundsException("fora da grade: (" + linha + ", " + coluna + ")");
        }
        return grade[linha][coluna];
    }

    /**
     * Informa se uma posição tem bloco.
     *
     * @param linha  linha
     * @param coluna coluna
     * @return {@code true} se houver um bloco fixado ali
     * @throws IndexOutOfBoundsException se a posição estiver fora da grade
     */
    public boolean ocupada(int linha, int coluna) {
        return bloco(linha, coluna) != null;
    }

    /**
     * Informa se uma célula está dentro da grade.
     *
     * @param celula célula a conferir
     * @return {@code true} se linha e coluna estiverem nos limites
     */
    public static boolean dentro(Celula celula) {
        return celula.linha() >= 0 && celula.linha() < Dimensoes.LINHAS
                && celula.coluna() >= 0 && celula.coluna() < Dimensoes.COLUNAS;
    }

    private static List<Celula> posicoes(Peca peca, int linha, int coluna) {
        List<Celula> posicoes = new ArrayList<>(Peca.BLOCOS_POR_PECA);
        for (Celula celula : peca.celulas()) {
            posicoes.add(celula.deslocada(linha, coluna));
        }
        return posicoes;
    }
}
