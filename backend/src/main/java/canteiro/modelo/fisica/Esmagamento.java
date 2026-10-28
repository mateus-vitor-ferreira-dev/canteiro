package canteiro.modelo.fisica;

import canteiro.modelo.Bloco;
import canteiro.modelo.Celula;
import canteiro.modelo.Tabuleiro;
import canteiro.modelo.constantes.Dimensoes;

import java.util.ArrayList;
import java.util.List;

/**
 * O esmagamento: o bloco que carrega mais peso do que o material aguenta
 * (RN21).
 *
 * <p>A <strong>carga</strong> de um bloco é a massa dos blocos colados logo
 * acima dele, na mesma coluna, até o primeiro espaço vazio. Passou da carga
 * máxima do material ({@link canteiro.modelo.materiais.Material#cargaMaxima()}),
 * vezes o fator da dificuldade, o bloco é esmagado: estoura, sai da grade e
 * da conta da massa e deixa um <strong>buraco</strong>. Os blocos de cima não
 * descem, ficam presos aos vizinhos da linha, como qualquer bloco sem apoio
 * embaixo. A altura da pilha não muda, a linha do buraco não fecha mais, e o
 * peso de cima deixa de chegar aos blocos de baixo. A conta recomeça até
 * nenhum bloco passar do limite.</p>
 *
 * <p>Antes de ceder, o bloco racha: a {@link #rachaduras(Tabuleiro, double)
 * rachadura} vai de 1 a 3 conforme a carga se aproxima do limite.</p>
 *
 * @author Mateus Vitor Ferreira
 * @version 0.1.0
 */
public final class Esmagamento {

    /** A partir de que fração da carga máxima o bloco começa a rachar, em cada nível de rachadura. */
    private static final double[] RACHA_A_PARTIR_DE = {0.5, 0.7, 0.85};

    private Esmagamento() {
    }

    /**
     * O que um esmagamento fez.
     *
     * @param esmagados onde estava cada bloco esmagado, na ordem; cada um virou um buraco
     */
    public record Resultado(List<Celula> esmagados) {

        /**
         * Copia a lista.
         *
         * @param esmagados onde estava cada bloco esmagado
         */
        public Resultado {
            esmagados = List.copyOf(esmagados);
        }

        /**
         * Informa se algum bloco foi esmagado.
         *
         * @return {@code true} se nada aconteceu
         */
        public boolean vazio() {
            return esmagados.isEmpty();
        }
    }

    /**
     * Um bloco carregado perto do limite.
     *
     * @param celula onde ele está
     * @param nivel  de 1 (começou a rachar) a 3 (prestes a ceder)
     */
    public record Rachadura(Celula celula, int nivel) {
    }

    /**
     * Esmaga, um por vez, os blocos que passaram da carga máxima.
     *
     * @param tabuleiro  a grade
     * @param analisador a conta da massa, que perde cada bloco esmagado
     * @param fator      multiplica a carga máxima de todos os materiais; vem da dificuldade
     * @return os blocos esmagados
     */
    public static Resultado executar(Tabuleiro tabuleiro, AnalisadorEstrutural analisador, double fator) {
        List<Celula> esmagados = new ArrayList<>();
        Celula cedeu = primeiroQueCede(tabuleiro, fator);
        while (cedeu != null) {
            Bloco bloco = tabuleiro.remover(cedeu);
            analisador.remover(cedeu, bloco.material());
            esmagados.add(cedeu);
            cedeu = primeiroQueCede(tabuleiro, fator);
        }
        return new Resultado(esmagados);
    }

    /**
     * Os blocos que já estão rachando, com o nível de cada um.
     *
     * @param tabuleiro a grade
     * @param fator     o mesmo fator de {@link #executar}
     * @return as rachaduras, de cima para baixo e da esquerda para a direita
     */
    public static List<Rachadura> rachaduras(Tabuleiro tabuleiro, double fator) {
        List<Rachadura> rachaduras = new ArrayList<>();
        for (int linha = 0; linha < Dimensoes.LINHAS; linha++) {
            for (int coluna = 0; coluna < Dimensoes.COLUNAS; coluna++) {
                Bloco bloco = tabuleiro.bloco(linha, coluna);
                if (bloco != null) {
                    int nivel = nivel(carga(tabuleiro, linha, coluna) / (bloco.material().cargaMaxima() * fator));
                    if (nivel > 0) {
                        rachaduras.add(new Rachadura(new Celula(linha, coluna), nivel));
                    }
                }
            }
        }
        return rachaduras;
    }

    /**
     * A massa dos blocos colados logo acima da célula, até o primeiro vazio.
     *
     * @param tabuleiro a grade
     * @param linha     linha da célula
     * @param coluna    coluna da célula
     * @return a carga, zero se não há nada em cima
     */
    public static double carga(Tabuleiro tabuleiro, int linha, int coluna) {
        double carga = 0;
        for (int acima = linha - 1; acima >= 0 && tabuleiro.ocupada(acima, coluna); acima--) {
            carga += tabuleiro.bloco(acima, coluna).massa();
        }
        return carga;
    }

    private static int nivel(double fracao) {
        int nivel = 0;
        while (nivel < RACHA_A_PARTIR_DE.length && fracao >= RACHA_A_PARTIR_DE[nivel]) {
            nivel++;
        }
        return nivel;
    }

    /** O bloco mais alto que passou do limite, coluna por coluna; {@code null} se nenhum passou. */
    private static Celula primeiroQueCede(Tabuleiro tabuleiro, double fator) {
        for (int coluna = 0; coluna < Dimensoes.COLUNAS; coluna++) {
            double carga = 0;
            for (int linha = 0; linha < Dimensoes.LINHAS; linha++) {
                Bloco bloco = tabuleiro.bloco(linha, coluna);
                if (bloco == null) {
                    carga = 0;
                } else if (carga > bloco.material().cargaMaxima() * fator) {
                    return new Celula(linha, coluna);
                } else {
                    carga += bloco.massa();
                }
            }
        }
        return null;
    }
}
