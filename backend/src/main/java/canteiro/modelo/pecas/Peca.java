package canteiro.modelo.pecas;

import canteiro.modelo.Celula;
import canteiro.modelo.materiais.Material;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Peça que o jogador controla: quatro blocos numa das sete formas, feitos de
 * um material.
 *
 * <p>Forma e material são independentes e ligados por composição: a peça
 * <em>tem</em> um material, não é um. O que é comum às sete formas fica aqui:
 * o material, a rotação atual, girar e calcular a massa. Cada subclasse só
 * informa as matrizes de ocupação das suas rotações, em {@link #formas()}. O
 * motor nunca precisa saber com qual das sete formas está lidando.</p>
 *
 * <p>As rotações seguem o padrão SRS dos jogos de encaixe: a matriz da forma
 * gira dentro do mesmo quadrado, no sentido horário.</p>
 *
 * @author Mateus Vitor Ferreira
 * @version 0.1.0
 */
public abstract class Peca {

    /** Número de blocos de toda peça (RN02). */
    public static final int BLOCOS_POR_PECA = 4;

    private static final int ROTACOES = 4;

    private final char letra;
    private final Material material;
    private int rotacao;

    /**
     * Cria a peça na rotação inicial.
     *
     * @param letra    letra da forma, como {@code 'T'}
     * @param material material de que a peça é feita
     * @throws NullPointerException se o material for nulo
     */
    protected Peca(char letra, Material material) {
        this.letra = letra;
        this.material = Objects.requireNonNull(material, "a peça precisa de um material");
    }

    /**
     * Matrizes de ocupação de cada rotação, na ordem horária. Em cada matriz,
     * {@code 1} é um bloco e {@code 0} é vazio.
     *
     * @return uma matriz por rotação, todas do mesmo tamanho
     */
    protected abstract int[][][] formas();

    /**
     * Calcula as quatro rotações de uma forma, girando a matriz no sentido
     * horário dentro do mesmo quadrado. As subclasses usam isso para declarar
     * só a forma inicial.
     *
     * @param inicial matriz quadrada da rotação inicial
     * @return as quatro rotações, começando pela inicial
     */
    protected static int[][][] rotacoesDe(int[][] inicial) {
        int[][][] rotacoes = new int[ROTACOES][][];
        rotacoes[0] = inicial;
        for (int i = 1; i < ROTACOES; i++) {
            rotacoes[i] = girarMatrizHorario(rotacoes[i - 1]);
        }
        return rotacoes;
    }

    private static int[][] girarMatrizHorario(int[][] matriz) {
        int tamanho = matriz.length;
        int[][] girada = new int[tamanho][tamanho];
        for (int linha = 0; linha < tamanho; linha++) {
            for (int coluna = 0; coluna < tamanho; coluna++) {
                girada[coluna][tamanho - 1 - linha] = matriz[linha][coluna];
            }
        }
        return girada;
    }

    /**
     * Gira a peça um quarto de volta no sentido horário.
     */
    public void girarHorario() {
        rotacao = (rotacao + 1) % formas().length;
    }

    /**
     * Gira a peça um quarto de volta no sentido anti-horário.
     */
    public void girarAntiHorario() {
        rotacao = (rotacao + formas().length - 1) % formas().length;
    }

    /**
     * Posições ocupadas pela peça na rotação atual, relativas ao canto
     * superior esquerdo da matriz da forma.
     *
     * @return as quatro células, de cima para baixo e da esquerda para a direita
     */
    public List<Celula> celulas() {
        return celulasNaRotacao(rotacao);
    }

    /**
     * Posições que a peça ocuparia numa rotação qualquer, sem girá-la. Serve
     * para testar uma rotação antes de aplicá-la.
     *
     * @param indice índice da rotação, de {@code 0} até {@link #quantidadeRotacoes()} menos um
     * @return as quatro células nessa rotação
     * @throws IndexOutOfBoundsException se a rotação não existir
     */
    public List<Celula> celulasNaRotacao(int indice) {
        int[][] matriz = formas()[indice];
        List<Celula> celulas = new ArrayList<>(BLOCOS_POR_PECA);
        for (int linha = 0; linha < matriz.length; linha++) {
            for (int coluna = 0; coluna < matriz[linha].length; coluna++) {
                if (matriz[linha][coluna] != 0) {
                    celulas.add(new Celula(linha, coluna));
                }
            }
        }
        return List.copyOf(celulas);
    }

    /**
     * Massa da peça inteira: a massa de cada bloco vezes o número de blocos (RN07).
     *
     * @return massa, em toneladas
     */
    public double massaTotal() {
        return celulas().size() * material.massaPorBloco();
    }

    /**
     * Devolve a letra da forma.
     *
     * @return uma de {@code I O T S Z J L}
     */
    public char letra() {
        return letra;
    }

    /**
     * Devolve o material de que a peça é feita.
     *
     * @return o material
     */
    public Material material() {
        return material;
    }

    /**
     * Devolve a rotação atual.
     *
     * @return índice da rotação, a partir de {@code 0} na posição inicial
     */
    public int rotacao() {
        return rotacao;
    }

    /**
     * Quantas rotações diferentes a forma tem.
     *
     * @return número de rotações, sempre quatro
     */
    public int quantidadeRotacoes() {
        return formas().length;
    }

    /**
     * Tamanho do quadrado em que a forma gira.
     *
     * @return lado da matriz: 4 para a peça I, 2 para a O e 3 para as demais
     */
    public int tamanho() {
        return formas()[0].length;
    }

    /**
     * Descrição curta, para mensagens e logs.
     *
     * @return letra e material, como {@code "T de Aço"}
     */
    @Override
    public String toString() {
        return letra + " de " + material;
    }
}
