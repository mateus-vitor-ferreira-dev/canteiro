package canteiro.controle;

import canteiro.modelo.EventoPartida;
import canteiro.modelo.MotorJogo;
import canteiro.modelo.ObservadorPartida;
import canteiro.modelo.pecas.Peca;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.TreeMap;

/**
 * Anota, peça a peça, o que o relatório de fim de partida mostra (RF25): o
 * índice de estabilidade logo depois de cada peça fixada, as peças cuja
 * fixação derrubou a estrutura e quantas peças de cada material entraram.
 *
 * <p>Coleta tudo de fora, como observador, sem mexer no motor. Os avisos
 * chegam pela thread do laço e a leitura vem da thread HTTP, por isso os
 * métodos são sincronizados.</p>
 *
 * @author Mateus Vitor Ferreira
 * @version 0.1.0
 */
public final class RegistroRelatorio implements ObservadorPartida {

    private MotorJogo motor;
    private final List<Double> indices = new ArrayList<>();
    private final List<Integer> colapsos = new ArrayList<>();
    private final List<String> materiaisPorPeca = new ArrayList<>();

    /**
     * Cria o registro. Ele só começa a anotar depois de inscrito no motor.
     *
     * @param motor o motor da partida, lido a cada peça fixada
     */
    public RegistroRelatorio(MotorJogo motor) {
        this.motor = Objects.requireNonNull(motor, "motor");
    }

    @Override
    public void estadoMudou(MotorJogo motorDaPartida) {
        // o relatório só depende dos eventos
    }

    @Override
    public synchronized void eventoOcorreu(EventoPartida evento) {
        switch (evento.tipo()) {
            case PECA_FIXADA -> anotarPeca();
            case COLAPSO -> colapsos.add(indices.size());
            default -> {
                // os outros eventos não entram no relatório
            }
        }
    }

    /** No aviso da fixação, a peça que assentou ainda é a atual do motor. */
    private void anotarPeca() {
        indices.add(motor.estabilidade().indice());
        Peca peca = motor.pecaAtual();
        materiaisPorPeca.add(peca == null ? null : peca.material().codigo());
    }

    /**
     * Esquece tudo o que foi anotado e passa a ler outro motor, como quando o
     * modo treino desfaz a jogada e a partida é refeita desde o começo (RF26):
     * o relatório mostra a partida que ficou.
     *
     * @param novo o motor da partida refeita, ainda no começo
     */
    public synchronized void recomecar(MotorJogo novo) {
        motor = Objects.requireNonNull(novo, "motor");
        indices.clear();
        colapsos.clear();
        materiaisPorPeca.clear();
    }

    /**
     * O índice de estabilidade, de 0 a 1, logo depois de cada peça fixada, na ordem.
     *
     * @return cópia da lista
     */
    public synchronized List<Double> indices() {
        return List.copyOf(indices);
    }

    /**
     * O número de cada peça, a partir de 1, cuja fixação derrubou a estrutura.
     *
     * @return cópia da lista, em ordem crescente
     */
    public synchronized List<Integer> colapsos() {
        return List.copyOf(colapsos);
    }

    /**
     * Quantas peças de cada material foram fixadas, pelo código do material.
     *
     * @return cópia da contagem, em ordem alfabética do código
     */
    public synchronized Map<String, Integer> materiais() {
        Map<String, Integer> contagem = new TreeMap<>();
        for (String codigo : materiaisPorPeca) {
            if (codigo != null) {
                contagem.merge(codigo, 1, Integer::sum);
            }
        }
        return contagem;
    }
}
