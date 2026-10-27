package canteiro.modelo.metas;

/**
 * Eliminar linhas seguidas sem a estabilidade cair abaixo de um limiar. Se
 * cair, ou se a estrutura desabar, a contagem recomeça.
 *
 * @author Mateus Vitor Ferreira
 * @version 0.1.0
 */
public final class ObraFirme implements Meta {

    private static final double PERCENTUAL = 100;

    private final int alvo;
    private final double limiar;
    private int progresso;

    /**
     * Cria a meta.
     *
     * @param alvo   quantas linhas eliminar
     * @param limiar índice mínimo, de 0 a 1
     * @throws IllegalArgumentException se o alvo não for positivo ou o limiar sair de 0 a 1
     */
    public ObraFirme(int alvo, double limiar) {
        if (alvo < 1 || limiar < 0 || limiar > 1) {
            throw new IllegalArgumentException("alvo ou limiar inválido: " + alvo + ", " + limiar);
        }
        this.alvo = alvo;
        this.limiar = limiar;
    }

    @Override
    public String descricao() {
        return "Elimine " + alvo + " linhas sem a estabilidade cair abaixo de "
                + Math.round(limiar * PERCENTUAL) + " %";
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
        if (cumprida()) {
            return;
        }
        if (lance.colapso() || lance.indice() < limiar) {
            progresso = 0;
        } else {
            progresso = Math.min(alvo, progresso + lance.linhas());
        }
    }
}
