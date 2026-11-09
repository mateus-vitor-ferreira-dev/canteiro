package canteiro.api.dto;

import canteiro.controle.RegistroRelatorio;

import java.util.List;
import java.util.Map;

/**
 * Corpo de {@code GET /api/partidas/{id}/relatorio} (RF25).
 *
 * @param indices   o índice de estabilidade, de 0 a 1, logo depois de cada peça fixada, na ordem
 * @param colapsos  o número de cada peça, a partir de 1, cuja fixação derrubou a estrutura
 * @param materiais quantas peças de cada material foram fixadas, pelo código do material
 * @author Mateus Vitor Ferreira
 * @version 0.1.0
 */
public record RelatorioDto(List<Double> indices, List<Integer> colapsos, Map<String, Integer> materiais) {

    /**
     * Converte o registro da partida.
     *
     * @param registro o que foi anotado durante a partida
     * @return o DTO correspondente
     */
    public static RelatorioDto de(RegistroRelatorio registro) {
        return new RelatorioDto(registro.indices(), registro.colapsos(), registro.materiais());
    }
}
