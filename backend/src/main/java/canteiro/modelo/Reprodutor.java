package canteiro.modelo;

import canteiro.modelo.estruturas.Jogada;

import java.util.List;
import java.util.Objects;

/**
 * Refaz uma partida a partir das jogadas gravadas (RF23, RF24, RF26).
 *
 * <p>A partida é reproduzível: um motor novo, com a mesma dificuldade e uma
 * fonte de peças com a mesma semente, que recebe os mesmos comandos nos
 * mesmos ciclos, chega exatamente ao mesmo tabuleiro e à mesma pontuação. Uma
 * jogada do ciclo {@code c} é aplicada quando o motor está no ciclo
 * {@code c}, antes de ele avançar, como a sessão faz ao vivo.</p>
 *
 * <p>Serve ao replay, que anda um ciclo por vez ({@link #passo}), e ao
 * desfazer do modo treino, que refaz tudo de uma vez até um ponto
 * ({@link #avancarAte}).</p>
 *
 * @author Mateus Vitor Ferreira
 * @version 0.1.0
 */
public final class Reprodutor {

    private final List<Jogada> jogadas;
    private int aplicadas;

    /**
     * Prepara a reprodução, ainda sem aplicar nada.
     *
     * @param jogadas as jogadas da partida, na ordem em que aconteceram
     */
    public Reprodutor(List<Jogada> jogadas) {
        this.jogadas = List.copyOf(Objects.requireNonNull(jogadas, "jogadas"));
    }

    /**
     * Um ciclo da reprodução: aplica as jogadas gravadas no ciclo atual do
     * motor e o faz avançar.
     *
     * @param motor o motor que refaz a partida
     */
    public void passo(MotorJogo motor) {
        aplicarDoCiclo(motor, jogadas.size());
        motor.avancarCiclo();
    }

    /**
     * Refaz a partida de uma vez até o ponto em que a original tinha aplicado
     * {@code quantidade} jogadas e estava no ciclo dado.
     *
     * @param motor      o motor que refaz a partida, ainda no começo
     * @param quantidade quantas jogadas aplicar
     * @param ciclo      em que ciclo parar
     * @throws IllegalArgumentException se o ponto não está na partida
     * @throws IllegalStateException    se a partida refeita não chegar ao ponto, o que indica jogadas
     *                                  de outra partida ou peças diferentes das originais
     */
    public void avancarAte(MotorJogo motor, int quantidade, long ciclo) {
        if (quantidade < 0 || quantidade > jogadas.size()) {
            throw new IllegalArgumentException("a partida tem " + jogadas.size() + " jogadas, não " + quantidade);
        }
        while (aplicadas < quantidade || motor.ciclo() < ciclo) {
            if (aplicarDoCiclo(motor, quantidade) == 0) {
                avancarSemTravar(motor);
            }
        }
        if (aplicadas != quantidade || motor.ciclo() != ciclo) {
            throw new IllegalStateException("a partida refeita passou do ponto: ciclo " + motor.ciclo()
                    + " com " + aplicadas + " jogadas, esperado ciclo " + ciclo + " com " + quantidade);
        }
    }

    /**
     * Informa se todas as jogadas já foram aplicadas.
     *
     * @return {@code true} quando não sobra jogada para aplicar
     */
    public boolean aplicouTodas() {
        return aplicadas == jogadas.size();
    }

    /** Aplica, na ordem, as jogadas do ciclo atual do motor, sem passar do limite. */
    private int aplicarDoCiclo(MotorJogo motor, int limite) {
        int antes = aplicadas;
        while (aplicadas < limite && jogadas.get(aplicadas).ciclo() == motor.ciclo()) {
            motor.aplicar(jogadas.get(aplicadas++).comando());
        }
        return aplicadas - antes;
    }

    /** Pausada ou encerrada, a partida não anda: sem jogada para destravar, ela nunca chegaria ao ponto. */
    private static void avancarSemTravar(MotorJogo motor) {
        long antes = motor.ciclo();
        motor.avancarCiclo();
        if (motor.ciclo() == antes) {
            throw new IllegalStateException("a partida refeita parou no ciclo " + antes + " (" + motor.estado()
                    + ") e não chega ao ponto pedido");
        }
    }
}
