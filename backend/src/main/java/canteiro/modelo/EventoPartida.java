package canteiro.modelo;

import java.util.List;

/**
 * Algo que aconteceu num instante da partida e que o frontend anima ou toca
 * um som (RF28).
 *
 * @param tipo   o que aconteceu
 * @param linhas linhas envolvidas, como as eliminadas; vazia quando não se aplica
 * @param quedas blocos que caíram no colapso, com origem e destino; vazia quando não se aplica
 * @author Mateus Vitor Ferreira
 * @version 0.1.0
 */
public record EventoPartida(Tipo tipo, List<Integer> linhas, List<Queda> quedas) {

    /**
     * Cria o evento com cópias imutáveis das listas.
     *
     * @param tipo   o que aconteceu
     * @param linhas linhas envolvidas
     * @param quedas blocos que caíram
     */
    public EventoPartida {
        linhas = List.copyOf(linhas);
        quedas = List.copyOf(quedas);
    }

    /**
     * Cria um evento com linhas e sem quedas.
     *
     * @param tipo   o que aconteceu
     * @param linhas linhas envolvidas
     */
    public EventoPartida(Tipo tipo, List<Integer> linhas) {
        this(tipo, linhas, List.of());
    }

    /**
     * Cria um evento sem linhas.
     *
     * @param tipo o que aconteceu
     * @return o evento
     */
    public static EventoPartida de(Tipo tipo) {
        return new EventoPartida(tipo, List.of(), List.of());
    }

    /**
     * Tipos de evento.
     */
    public enum Tipo {
        /** Uma peça foi assentada. */
        PECA_FIXADA,
        /** Uma ou mais linhas foram eliminadas. */
        LINHAS_ELIMINADAS,
        /** A estrutura passou do limite e desabou; traz as quedas. */
        COLAPSO,
        /** O jogador subiu de nível. */
        NIVEL_SUBIU,
        /** A partida acabou. */
        FIM_DE_JOGO
    }
}
