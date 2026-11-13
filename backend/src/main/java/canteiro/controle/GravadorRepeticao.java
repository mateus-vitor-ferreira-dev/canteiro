package canteiro.controle;

import canteiro.modelo.Dificuldade;
import canteiro.modelo.EstadoPartida;
import canteiro.modelo.MotorJogo;
import canteiro.modelo.ObservadorPartida;
import canteiro.persistencia.ArquivoRepeticoes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.UncheckedIOException;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.function.Supplier;

/**
 * Grava a partida quando ela termina, para ser reproduzida depois (RF24).
 * Grava só a dificuldade, a semente, a pontuação e as jogadas: o resto sai
 * delas.
 *
 * <p>Fica inscrito no motor e grava uma vez só, no primeiro aviso de fim de
 * jogo. Uma falha ao gravar vai para o log e não atrapalha a partida.</p>
 *
 * @author Mateus Vitor Ferreira
 * @version 0.1.0
 */
public final class GravadorRepeticao implements ObservadorPartida {

    private static final Logger LOG = LoggerFactory.getLogger(GravadorRepeticao.class);

    private final ArquivoRepeticoes arquivo;
    private final Dificuldade dificuldade;
    private final long semente;
    private final Supplier<LocalDateTime> agora;
    private boolean gravou;

    /**
     * Prepara a gravação de uma partida.
     *
     * @param arquivo     onde as repetições ficam
     * @param dificuldade dificuldade da partida
     * @param semente     semente do sorteio das peças da partida
     * @param agora       data e hora atuais, no horário local
     */
    public GravadorRepeticao(ArquivoRepeticoes arquivo, Dificuldade dificuldade, long semente,
                             Supplier<LocalDateTime> agora) {
        this.arquivo = Objects.requireNonNull(arquivo, "arquivo de repetições");
        this.dificuldade = Objects.requireNonNull(dificuldade, "dificuldade");
        this.semente = semente;
        this.agora = Objects.requireNonNull(agora, "relógio");
    }

    @Override
    public void estadoMudou(MotorJogo motor) {
        if (gravou || motor.estado() != EstadoPartida.FIM_DE_JOGO) {
            return;
        }
        gravou = true;
        try {
            String id = arquivo.gravar(dificuldade, semente, motor.placar().pontuacao(), motor.jogadas(), agora.get());
            LOG.info("Partida gravada para reprodução: {}", id);
        } catch (UncheckedIOException falha) {
            LOG.warn("Não foi possível gravar a repetição da partida", falha);
        }
    }
}
