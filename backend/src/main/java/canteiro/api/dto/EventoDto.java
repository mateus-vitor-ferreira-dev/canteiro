package canteiro.api.dto;

import canteiro.modelo.EventoPartida;
import canteiro.modelo.Queda;
import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.List;

/**
 * Algo que aconteceu, enviado no instante em que acontece, para o frontend
 * animar e tocar som: {@code {"tipo": "EVENTO", "evento": "COLAPSO", "dados": {...}}}.
 *
 * @param tipo   sempre {@code "EVENTO"}
 * @param evento nome do evento, como {@code "LINHAS_ELIMINADAS"} ou {@code "ERRO"}
 * @param dados  detalhes; só os campos que se aplicam vão no JSON
 * @author Mateus Vitor Ferreira
 * @version 0.1.0
 */
public record EventoDto(String tipo, String evento, DadosDto dados) {

    /** Valor de {@code tipo}. */
    public static final String TIPO = "EVENTO";

    /** Evento de mensagem inválida (RNF14). */
    public static final String ERRO = "ERRO";

    /**
     * Converte um evento do motor.
     *
     * @param evento evento da partida
     * @return o evento no formato do protocolo
     */
    public static EventoDto de(EventoPartida evento) {
        List<QuedaDto> quedas = evento.quedas().stream().map(QuedaDto::de).toList();
        Integer pontos = evento.pontos() == 0 ? null : evento.pontos();
        Boolean ousada = evento.ousada() ? Boolean.TRUE : null;
        return new EventoDto(TIPO, evento.tipo().name(), new DadosDto(evento.linhas(), quedas, pontos, ousada,
                EstadoDto.pares(evento.celulas()), null));
    }

    /**
     * Evento de erro, para mensagem inválida recebida.
     *
     * @param mensagem explicação do que estava errado
     * @return o evento de erro
     */
    public static EventoDto erro(String mensagem) {
        return new EventoDto(TIPO, ERRO, new DadosDto(List.of(), List.of(), null, null, List.of(), mensagem));
    }

    /**
     * Detalhes do evento.
     *
     * @param linhas   linhas eliminadas
     * @param quedas   blocos que caíram: no colapso ou no balanço (RN20)
     * @param pontos   quanto a pontuação mudou: ganho da eliminação ou desconto (negativo) do colapso
     * @param ousada   {@code true} se as linhas foram fechadas numa obra ousada (RN17)
     * @param celulas  células envolvidas, como pares [linha, coluna]: as dos blocos esmagados (RN21)
     * @param mensagem explicação de um erro
     */
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    public record DadosDto(List<Integer> linhas, List<QuedaDto> quedas, Integer pontos, Boolean ousada,
                           List<List<Integer>> celulas, String mensagem) {
    }

    /**
     * Um bloco que caiu: no colapso ou no balanço.
     *
     * @param origem  [linha, coluna] antes de cair
     * @param destino [linha, coluna] onde parou
     */
    public record QuedaDto(List<Integer> origem, List<Integer> destino) {

        static QuedaDto de(Queda queda) {
            return new QuedaDto(List.of(queda.origem().linha(), queda.origem().coluna()),
                    List.of(queda.destino().linha(), queda.destino().coluna()));
        }
    }
}
