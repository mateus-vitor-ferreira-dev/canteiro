package canteiro.modelo.metas;

/**
 * Eliminar várias linhas de uma vez só.
 *
 * @author Mateus Vitor Ferreira
 * @version 0.1.0
 */
public final class EliminacaoGrande implements Meta {

    private final int linhas;
    private boolean feita;

    /**
     * Cria a meta.
     *
     * @param linhas quantas linhas, no mínimo, numa eliminação só
     * @throws IllegalArgumentException se não estiver entre 2 e 4
     */
    public EliminacaoGrande(int linhas) {
        if (linhas < 2 || linhas > 4) {
            throw new IllegalArgumentException("linhas fora do intervalo: " + linhas);
        }
        this.linhas = linhas;
    }

    @Override
    public String descricao() {
        return "Elimine " + linhas + " linhas ou mais de uma vez";
    }

    @Override
    public int alvo() {
        return 1;
    }

    @Override
    public int progresso() {
        return feita ? 1 : 0;
    }

    @Override
    public void registrar(Lance lance) {
        feita |= lance.linhas() >= linhas;
    }
}
