package canteiro.api.dto;

import canteiro.persistencia.ArquivoRepeticoes;
import canteiro.persistencia.Repeticao;

/**
 * Uma partida gravada, como aparece em {@code GET /api/repeticoes} (RF24).
 *
 * @param id          nome da gravação, usado em {@code POST /api/repeticoes/{id}/reproduzir}
 * @param data        quando a partida terminou, no horário local, em ISO ({@code "2026-09-28T14:32:05"})
 * @param dificuldade código da dificuldade, como {@code "NORMAL"}
 * @param pontuacao   pontuação final da partida
 * @author Mateus Vitor Ferreira
 * @version 0.1.0
 */
public record RepeticaoDto(String id, String data, String dificuldade, int pontuacao) {

    /**
     * Converte uma repetição lida do arquivo.
     *
     * @param repeticao a repetição
     * @return o DTO correspondente
     */
    public static RepeticaoDto de(Repeticao repeticao) {
        return new RepeticaoDto(repeticao.id(), ArquivoRepeticoes.FORMATO_DATA.format(repeticao.data()),
                repeticao.dificuldade().name(),
                repeticao.pontuacao());
    }
}
