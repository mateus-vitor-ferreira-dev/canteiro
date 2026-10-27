package canteiro.modelo.metas;

/**
 * Fechar linha numa obra ousada, com a estrutura perto de cair (RN17).
 *
 * @author Mateus Vitor Ferreira
 * @version 0.1.0
 */
public final class JogadaOusada implements Meta {

    private static final double PERCENTUAL = 100;

    private final double limiar;
    private boolean feita;

    /**
     * Cria a meta.
     *
     * @param limiar índice abaixo do qual a jogada é ousada, de 0 a 1, só para a descrição
     */
    public JogadaOusada(double limiar) {
        this.limiar = limiar;
    }

    @Override
    public String descricao() {
        return "Feche uma linha com a estabilidade abaixo de " + Math.round(limiar * PERCENTUAL) + " %";
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
        feita |= lance.ousada();
    }
}
