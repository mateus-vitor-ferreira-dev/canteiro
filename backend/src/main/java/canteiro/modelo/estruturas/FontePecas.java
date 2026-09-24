package canteiro.modelo.estruturas;

import canteiro.modelo.pecas.Peca;

import java.util.List;

/**
 * De onde o motor tira as peças. O motor não sabe como elas são sorteadas.
 *
 * <p>O gerador pelo método da sacola implementa esta interface; nos testes,
 * uma fonte fixa entrega uma sequência conhecida.</p>
 *
 * @author Mateus Vitor Ferreira
 * @version 0.1.0
 */
public interface FontePecas {

    /**
     * Tira a próxima peça da fila.
     *
     * @return a peça, na rotação inicial
     */
    Peca proxima();

    /**
     * Mostra as próximas peças sem tirá-las da fila (RF05).
     *
     * @param quantidade quantas peças mostrar
     * @return as próximas peças, na ordem em que vão sair
     */
    List<Peca> espiar(int quantidade);

    /**
     * Avisa que o jogador subiu de nível, para a fonte passar a sortear os
     * materiais do nível novo ({@code Progressao.materiaisLiberados(nivel)}).
     * Por padrão, ignora. O nível inicial vem da dificuldade, ao criar a fonte.
     *
     * @param nivel o nível novo
     */
    default void nivelMudou(int nivel) {
        // uma fonte que não muda com o nível não precisa fazer nada
    }
}
