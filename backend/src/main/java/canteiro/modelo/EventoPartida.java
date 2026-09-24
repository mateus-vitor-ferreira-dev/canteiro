package canteiro.modelo;

import java.util.List;

/**
 * Algo que aconteceu num instante da partida e que o frontend anima ou toca
 * um som (RF28).
 *
 * @param tipo   o que aconteceu
 * @param linhas linhas envolvidas, como as eliminadas; vazia quando não se aplica
 * @author Mateus Vitor Ferreira
 * @version 0.1.0
 */
public record EventoPartida(Tipo tipo, List<Integer> linhas) {

    /**
     * Cria o evento com uma cópia imutável das linhas.
     *
     * @param tipo   o que aconteceu
     * @param linhas linhas envolvidas
     */
    public EventoPartida {
        linhas = List.copyOf(linhas);
    }

    /**
     * Cria um evento sem linhas.
     *
     * @param tipo o que aconteceu
     * @return o evento
     */
    public static EventoPartida de(Tipo tipo) {
        return new EventoPartida(tipo, List.of());
    }

    /**
     * Tipos de evento.
     */
    public enum Tipo {
        /** Uma peça foi assentada. */
        PECA_FIXADA,
        /** Uma ou mais linhas foram eliminadas. */
        LINHAS_ELIMINADAS,
        /** O jogador subiu de nível. */
        NIVEL_SUBIU,
        /** A partida acabou. */
        FIM_DE_JOGO
    }
}
