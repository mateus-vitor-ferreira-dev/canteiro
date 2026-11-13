package canteiro.modelo;

import canteiro.modelo.fisica.Estabilidade;

import java.util.List;

/**
 * O que aconteceria se a peça atual fosse solta agora, na posição da peça
 * fantasma: a estabilidade da estrutura depois da jogada e os blocos que ela
 * quebraria (RF15, RF16, RN20, RN21).
 *
 * @param estabilidade a estabilidade depois de a peça assentar e as linhas completas sumirem
 * @param quebras      os blocos da peça que se soltariam do apoio (RN20)
 * @param esmagados    os blocos que seriam esmagados pelo peso (RN21)
 * @author Mateus Vitor Ferreira
 * @version 0.1.0
 */
public record Previa(Estabilidade estabilidade, List<Celula> quebras, List<Celula> esmagados) {

    /**
     * Copia as listas.
     *
     * @param estabilidade a estabilidade depois da jogada
     * @param quebras      os blocos que se soltariam
     * @param esmagados    os blocos que seriam esmagados
     */
    public Previa {
        quebras = List.copyOf(quebras);
        esmagados = List.copyOf(esmagados);
    }
}
