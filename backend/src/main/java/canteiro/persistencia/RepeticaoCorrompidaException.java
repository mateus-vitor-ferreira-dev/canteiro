package canteiro.persistencia;

/**
 * O arquivo de uma repetição existe, mas não dá para ler: foi editado à mão,
 * cortado no meio ou é de outro formato.
 *
 * @author Mateus Vitor Ferreira
 * @version 0.1.0
 */
public final class RepeticaoCorrompidaException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    /**
     * Cria a exceção.
     *
     * @param mensagem o que estava errado, para o log
     */
    public RepeticaoCorrompidaException(String mensagem) {
        super(mensagem);
    }
}
