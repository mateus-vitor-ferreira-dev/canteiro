package canteiro.controle;

/**
 * O servidor já tem o máximo de partidas abertas e não cria outra.
 *
 * <p>Só acontece na hospedagem pública, onde o limite protege a máquina: cada
 * partida tem um laço de 60 ciclos por segundo numa thread própria.</p>
 *
 * @author Mateus Vitor Ferreira
 * @version 0.1.0
 */
public final class LimiteDePartidasException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    /**
     * Cria a exceção.
     *
     * @param limite quantas partidas o servidor aceita ao mesmo tempo
     */
    public LimiteDePartidasException(int limite) {
        super("o servidor já tem " + limite + " partidas abertas");
    }
}
