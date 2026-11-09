package canteiro.controle;

import canteiro.modelo.EstadoPartida;
import canteiro.modelo.EventoPartida;
import canteiro.modelo.MotorJogo;
import canteiro.modelo.ObservadorPartida;
import canteiro.modelo.Reprodutor;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * O desfazer do modo treino (RF26): volta a partida para o instante em que a
 * última peça fixada apareceu no topo, com o tabuleiro de antes dela.
 *
 * <p>Não guarda cópias do tabuleiro, do placar nem da fila de peças. Guarda
 * só os marcos, um por peça: quantas jogadas a partida tinha e em que ciclo
 * estava quando a peça nasceu. Desfazer cria um motor novo, com a mesma
 * semente, e o {@link Reprodutor} refaz as jogadas até o marco anterior. Como
 * a partida é reproduzível, tudo volta exatamente como era: tabuleiro, centro
 * de massa, pontos, reserva, meta e as próximas peças.</p>
 *
 * <p>Fica inscrito no motor da partida e é avisado pela thread do laço, a
 * mesma que chama {@link #depoisDoPasso} e {@link #desfazer}.</p>
 *
 * @author Mateus Vitor Ferreira
 * @version 0.1.0
 */
public final class Treino implements ObservadorPartida {

    /** O instante em que uma peça nasceu: quantas jogadas tinham sido aplicadas, e em que ciclo. */
    private record Marco(int jogadas, long ciclo) {
    }

    private final Supplier<MotorJogo> novoMotor;
    private final Deque<Marco> marcos = new ArrayDeque<>();
    private boolean fixou;

    /**
     * Prepara o desfazer de uma partida que ainda não começou.
     *
     * @param novoMotor cria um motor no começo da mesma partida: mesma dificuldade e mesma semente
     */
    public Treino(Supplier<MotorJogo> novoMotor) {
        this.novoMotor = Objects.requireNonNull(novoMotor, "criador de motor");
        marcos.push(new Marco(0, 0));
    }

    @Override
    public void estadoMudou(MotorJogo motor) {
        // os marcos dependem só dos eventos
    }

    @Override
    public void eventoOcorreu(EventoPartida evento) {
        if (evento.tipo() == EventoPartida.Tipo.PECA_FIXADA) {
            fixou = true;
        }
    }

    /**
     * Chamado depois de cada comando aplicado e de cada ciclo. Se uma peça
     * foi fixada nesse passo, a próxima já nasceu: anota o marco dela.
     *
     * @param motor o motor da partida
     */
    public void depoisDoPasso(MotorJogo motor) {
        if (fixou) {
            fixou = false;
            marcos.push(new Marco(motor.jogadas().size(), motor.ciclo()));
        }
    }

    /**
     * Refaz a partida até a última peça fixada voltar ao topo.
     *
     * @param atual    o motor da partida agora
     * @param preparar recebe o motor novo antes de a partida ser refeita, para inscrever quem precisa
     *                 acompanhá-la desde o começo, como o relatório
     * @return o motor novo, ou vazio se não há peça para desfazer, a partida está pausada ou acabou
     */
    public Optional<MotorJogo> desfazer(MotorJogo atual, Consumer<MotorJogo> preparar) {
        // pausada, a partida refeita voltaria andando sem o jogador pedir
        if (atual.estado().encerrada() || atual.estado() == EstadoPartida.PAUSA || marcos.size() < 2) {
            return Optional.empty();
        }
        marcos.pop();
        Marco alvo = marcos.peek();
        MotorJogo motor = novoMotor.get();
        preparar.accept(motor);
        new Reprodutor(atual.jogadas()).avancarAte(motor, alvo.jogadas(), alvo.ciclo());
        fixou = false;
        return Optional.of(motor);
    }
}
