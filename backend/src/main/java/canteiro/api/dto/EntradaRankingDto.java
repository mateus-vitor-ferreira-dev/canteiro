package canteiro.api.dto;

import canteiro.persistencia.EntradaRanking;

/**
 * Uma linha do ranking, como vai em {@code GET /api/ranking} (RF22). Espelho
 * de {@code EntradaRankingDto} em {@code frontend/src/api/protocolo.ts}.
 *
 * @param nome      nome do jogador
 * @param pontuacao pontos da partida
 * @param nivel     nível alcançado
 * @param linhas    linhas eliminadas
 * @param colapsos  colapsos sofridos
 * @param data      dia do registro, no formato ISO ({@code 2026-10-22})
 * @author Mateus Vitor Ferreira
 * @version 0.1.0
 */
public record EntradaRankingDto(String nome, int pontuacao, int nivel, int linhas, int colapsos, String data) {

    /**
     * Converte uma entrada do arquivo.
     *
     * @param entrada a entrada do ranking
     * @return o DTO correspondente
     */
    public static EntradaRankingDto de(EntradaRanking entrada) {
        return new EntradaRankingDto(entrada.nome(), entrada.pontuacao(), entrada.nivel(), entrada.linhas(),
                entrada.colapsos(), entrada.data().toString());
    }
}
