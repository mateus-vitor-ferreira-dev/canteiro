package canteiro.modelo.metas;

/**
 * Eliminar linhas com um material predominando.
 *
 * @author Mateus Vitor Ferreira
 * @version 0.1.0
 */
public final class LinhasDeMaterial implements Meta {

    private final String codigo;
    private final String nome;
    private final int alvo;
    private int progresso;

    /**
     * Cria a meta.
     *
     * @param codigo código do material, como {@code "ACO"}
     * @param nome   nome do material na frase, como {@code "aço"}
     * @param alvo   quantas linhas eliminar
     * @throws IllegalArgumentException se o alvo não for positivo
     */
    public LinhasDeMaterial(String codigo, String nome, int alvo) {
        if (alvo < 1) {
            throw new IllegalArgumentException("alvo deve ser positivo: " + alvo);
        }
        this.codigo = codigo;
        this.nome = nome;
        this.alvo = alvo;
    }

    @Override
    public String descricao() {
        return "Elimine " + alvo + " linhas com " + nome + " predominando";
    }

    @Override
    public int alvo() {
        return alvo;
    }

    @Override
    public int progresso() {
        return progresso;
    }

    @Override
    public void registrar(Lance lance) {
        if (codigo.equals(lance.predominante())) {
            progresso = Math.min(alvo, progresso + lance.linhas());
        }
    }
}
