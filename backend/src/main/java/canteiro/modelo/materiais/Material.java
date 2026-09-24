package canteiro.modelo.materiais;

import canteiro.modelo.Celula;
import canteiro.modelo.Tabuleiro;

import java.util.List;
import java.util.Objects;

/**
 * Material de construção de uma peça: define quanto cada bloco pesa, a cor
 * com que ele é desenhado e quanto vale uma linha feita com ele.
 *
 * <p>Forma e material são independentes: qualquer peça pode ser fabricada em
 * qualquer material. As subclasses só informam os próprios valores e o bônus
 * por linha; o que é comum a todos, como o cálculo da massa, fica aqui.</p>
 *
 * <p>A cor é um número RGB ({@code 0xRRGGBB}), e não uma classe gráfica, para
 * que o modelo não dependa de nenhuma biblioteca de interface (RNF08).</p>
 *
 * @author Mateus Vitor Ferreira
 * @version 0.1.0
 */
public abstract class Material {

    /** Volume de um bloco, em unidades cúbicas. Todos os blocos têm o mesmo tamanho (RN07). */
    public static final double VOLUME_BLOCO = 1.0;

    private static final int COR_MAXIMA = 0xFFFFFF;

    private final String codigo;
    private final String nome;
    private final double densidade;
    private final int corRgb;

    /**
     * Cria um material com valores já validados.
     *
     * @param codigo    identificador em maiúsculas, como {@code "ACO"}
     * @param nome      nome mostrado ao jogador, como {@code "Aço"}
     * @param densidade densidade, em toneladas por unidade cúbica; precisa ser positiva
     * @param corRgb    cor no formato {@code 0xRRGGBB}
     * @throws IllegalArgumentException se o código ou o nome estiverem vazios, a
     *                                  densidade não for positiva ou a cor estiver fora do intervalo RGB
     */
    protected Material(String codigo, String nome, double densidade, int corRgb) {
        if (codigo == null || codigo.isBlank() || nome == null || nome.isBlank()) {
            throw new IllegalArgumentException("código e nome do material são obrigatórios");
        }
        if (!(densidade > 0)) {
            throw new IllegalArgumentException("a densidade precisa ser positiva: " + densidade);
        }
        if (corRgb < 0 || corRgb > COR_MAXIMA) {
            throw new IllegalArgumentException("cor fora do intervalo RGB: " + corRgb);
        }
        this.codigo = codigo;
        this.nome = nome;
        this.densidade = densidade;
        this.corRgb = corRgb;
    }

    /**
     * Pontos extras de cada linha eliminada que tenha este material como
     * predominante. Materiais mais pesados, e portanto mais arriscados, valem mais.
     *
     * @return bônus por linha, em pontos
     */
    public abstract int bonusLinha();

    /**
     * Reação do material no instante em que a peça é assentada. Por padrão,
     * nada acontece; o material que tiver um efeito sobrescreve este método,
     * como o aço, que consolida os blocos logo abaixo. O tabuleiro chama isto
     * sem saber qual é o material.
     *
     * @param tabuleiro tabuleiro em que a peça foi fixada
     * @param ocupadas  células em que a peça acabou de ser fixada
     */
    public void aoFixar(Tabuleiro tabuleiro, List<Celula> ocupadas) {
        // a maioria dos materiais não tem efeito ao ser assentada
    }

    /**
     * Massa de um bloco deste material: a densidade vezes o volume do bloco (RN07).
     *
     * @return massa de um bloco, em toneladas
     */
    public double massaPorBloco() {
        return densidade * VOLUME_BLOCO;
    }

    /**
     * Devolve o identificador do material.
     *
     * @return código em maiúsculas, como {@code "MADEIRA"}
     */
    public String codigo() {
        return codigo;
    }

    /**
     * Devolve o nome mostrado ao jogador.
     *
     * @return nome com acentuação, como {@code "Aço"}
     */
    public String nome() {
        return nome;
    }

    /**
     * Devolve a densidade do material.
     *
     * @return densidade, em toneladas por unidade cúbica
     */
    public double densidade() {
        return densidade;
    }

    /**
     * Devolve a cor com que os blocos deste material são desenhados.
     *
     * @return cor no formato {@code 0xRRGGBB}
     */
    public int corRgb() {
        return corRgb;
    }

    /**
     * Dois materiais são iguais quando têm o mesmo código.
     *
     * @param outro objeto a comparar
     * @return {@code true} se for um material com o mesmo código
     */
    @Override
    public boolean equals(Object outro) {
        return outro instanceof Material material && codigo.equals(material.codigo);
    }

    /**
     * Código de espalhamento coerente com {@link #equals(Object)}.
     *
     * @return hash do código
     */
    @Override
    public int hashCode() {
        return Objects.hash(codigo);
    }

    /**
     * Devolve o nome do material, para mensagens e logs.
     *
     * @return o nome mostrado ao jogador
     */
    @Override
    public String toString() {
        return nome;
    }
}
