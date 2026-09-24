package canteiro.api;

import canteiro.modelo.Dificuldade;

import java.util.Arrays;
import java.util.List;

/**
 * Dificuldade como é enviada ao frontend pela rota {@code GET /api/dificuldades}.
 *
 * @param codigo             identificador usado ao criar a partida, como {@code "NORMAL"}
 * @param nome               nome mostrado ao jogador
 * @param intervaloQuedaMs   intervalo inicial de queda, em milissegundos
 * @param limiteDesvio       limite inicial de desvio, em colunas
 * @param materiaisLiberados códigos dos materiais liberados no início
 * @author Mateus Vitor Ferreira
 * @version 0.1.0
 */
public record DificuldadeDto(String codigo, String nome, long intervaloQuedaMs,
                             double limiteDesvio, List<String> materiaisLiberados) {

    /**
     * Converte uma dificuldade do modelo no formato do protocolo.
     *
     * @param dificuldade dificuldade a converter
     * @return a dificuldade no formato enviado ao frontend
     */
    public static DificuldadeDto de(Dificuldade dificuldade) {
        return new DificuldadeDto(dificuldade.name(), dificuldade.nomeExibicao(),
                dificuldade.intervaloQuedaMs(), dificuldade.limiteDesvio(),
                dificuldade.materiaisLiberados());
    }

    /**
     * Converte todas as dificuldades, da mais fácil para a mais difícil.
     *
     * @return lista com uma entrada por dificuldade
     */
    public static List<DificuldadeDto> todas() {
        return Arrays.stream(Dificuldade.values()).map(DificuldadeDto::de).toList();
    }
}
