package canteiro.api.dto;

import canteiro.persistencia.ConfiguracoesSalvas;

import java.util.List;
import java.util.Map;

/**
 * Corpo de {@code GET} e {@code PUT /api/configuracoes} (RF27): as teclas que
 * o jogador trocou e o volume dos efeitos.
 *
 * <p>Só as ações trocadas aparecem em {@code teclas}; as outras ficam com a
 * tecla padrão. Os códigos são os do navegador ({@code KeyboardEvent.code}).</p>
 *
 * @param teclas os códigos de tecla de cada ação trocada, pelo nome da ação
 * @param volume volume dos efeitos, de 0 a 100
 * @author Mateus Vitor Ferreira
 * @version 0.1.0
 */
public record ConfiguracoesDto(Map<String, List<String>> teclas, int volume) {

    /**
     * Converte o que está no arquivo.
     *
     * @param salvas as configurações lidas do arquivo
     * @return o DTO correspondente
     */
    public static ConfiguracoesDto de(ConfiguracoesSalvas salvas) {
        return new ConfiguracoesDto(salvas.teclas(), salvas.volume());
    }

    /**
     * Converte para o formato do arquivo. Chame só depois de validar.
     *
     * @return as configurações a gravar
     * @throws IllegalArgumentException se o volume estiver fora da faixa
     */
    public ConfiguracoesSalvas paraSalvar() {
        return new ConfiguracoesSalvas(teclas, volume);
    }
}
