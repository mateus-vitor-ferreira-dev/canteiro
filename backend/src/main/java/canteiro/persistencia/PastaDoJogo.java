package canteiro.persistencia;

import java.nio.file.Path;

/**
 * A pasta em que o jogo guarda os arquivos do jogador: {@code ~/.canteiro/}.
 *
 * <p>Ela só é criada quando algo precisa ser gravado. Para ler, a falta da
 * pasta é tratada como a falta do arquivo: vale o padrão (RNF11).</p>
 *
 * @author Mateus Vitor Ferreira
 * @version 0.1.0
 */
public final class PastaDoJogo {

    /** Nome da pasta, dentro da pasta pessoal do usuário. */
    public static final String NOME = ".canteiro";

    private PastaDoJogo() {
    }

    /**
     * A pasta padrão, dentro da pasta pessoal do usuário.
     *
     * @return o caminho de {@code ~/.canteiro}
     */
    public static Path padrao() {
        return Path.of(System.getProperty("user.home"), NOME);
    }
}
