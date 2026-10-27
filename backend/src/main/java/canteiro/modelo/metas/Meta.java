package canteiro.modelo.metas;

/**
 * Um objetivo curto para o nível, com progresso contado de 0 até o alvo (RN19).
 *
 * <p>Cada tipo de meta é uma classe: a descrição e o jeito de contar mudam,
 * a forma de perguntar é a mesma.</p>
 *
 * @author Mateus Vitor Ferreira
 * @version 0.1.0
 */
public interface Meta {

    /**
     * O que o jogador precisa fazer, numa frase para a tela.
     *
     * @return a descrição
     */
    String descricao();

    /**
     * Quanto falta contar para cumprir.
     *
     * @return o alvo, positivo
     */
    int alvo();

    /**
     * Quanto já foi feito, de 0 até o alvo.
     *
     * @return o progresso
     */
    int progresso();

    /**
     * Conta uma peça assentada.
     *
     * @param lance o que a peça fez
     */
    void registrar(Lance lance);

    /**
     * Informa se o progresso chegou ao alvo.
     *
     * @return {@code true} se a meta foi cumprida
     */
    default boolean cumprida() {
        return progresso() >= alvo();
    }
}
