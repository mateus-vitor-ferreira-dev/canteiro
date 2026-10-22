package canteiro.persistencia;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * As configurações do jogador, como ficam no arquivo (RF27): as teclas que
 * ele trocou e o volume dos efeitos.
 *
 * <p>Só as ações trocadas aparecem em {@code teclas}; as outras usam a tecla
 * padrão, que é decidida pelo frontend. Os códigos de tecla são os do
 * navegador ({@code KeyboardEvent.code}), como {@code ArrowLeft} e {@code KeyC}.
 * Este registro não confere se as ações existem nem se há tecla repetida: isso
 * é validado na entrada, pela rota (#71).</p>
 *
 * @param teclas as teclas de cada ação trocada, pelo nome da ação
 * @param volume volume dos efeitos, de {@value #VOLUME_MINIMO} a {@value #VOLUME_MAXIMO}
 * @author Mateus Vitor Ferreira
 * @version 0.1.0
 */
public record ConfiguracoesSalvas(Map<String, List<String>> teclas, int volume) {

    /** Menor volume aceito. */
    public static final int VOLUME_MINIMO = 0;

    /** Maior volume aceito. */
    public static final int VOLUME_MAXIMO = 100;

    /** Volume de quem nunca mexeu nas configurações, o mesmo do frontend. */
    public static final int VOLUME_PADRAO = 60;

    /**
     * Copia as teclas, para ninguém mudar o registro por fora, e confere o volume.
     *
     * @param teclas as teclas de cada ação trocada; nulo vale como nenhuma
     * @param volume volume dos efeitos
     * @throws IllegalArgumentException se o volume estiver fora da faixa
     * @throws NullPointerException     se uma ação, uma lista ou um código de tecla for nulo
     */
    public ConfiguracoesSalvas {
        if (volume < VOLUME_MINIMO || volume > VOLUME_MAXIMO) {
            throw new IllegalArgumentException("volume fora de " + VOLUME_MINIMO + " a " + VOLUME_MAXIMO + ": " + volume);
        }
        Map<String, List<String>> copia = new TreeMap<>();
        if (teclas != null) {
            teclas.forEach((acao, codigos) -> copia.put(acao, List.copyOf(codigos)));
        }
        teclas = Collections.unmodifiableMap(copia);
    }

    /**
     * As configurações de quem nunca salvou nada: nenhuma tecla trocada e o volume padrão.
     *
     * @return as configurações padrão
     */
    public static ConfiguracoesSalvas padrao() {
        return new ConfiguracoesSalvas(Map.of(), VOLUME_PADRAO);
    }
}
