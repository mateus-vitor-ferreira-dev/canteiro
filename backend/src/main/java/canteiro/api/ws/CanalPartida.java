package canteiro.api.ws;

import canteiro.api.dto.ComandoDto;
import canteiro.api.dto.EstadoDto;
import canteiro.api.dto.EventoDto;
import canteiro.controle.GerenciadorPartidas;
import canteiro.controle.SessaoPartida;
import canteiro.modelo.Comando;
import canteiro.modelo.EventoPartida;
import canteiro.modelo.MotorJogo;
import canteiro.modelo.ObservadorPartida;
import io.javalin.config.RoutesConfig;
import io.javalin.websocket.WsCloseStatus;
import io.javalin.websocket.WsConfig;
import io.javalin.websocket.WsContext;
import io.javalin.websocket.WsMessageContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * O canal WebSocket da partida, em {@code /ws/partidas/{id}}.
 *
 * <p>Recebe os comandos do navegador, valida e entrega à sessão. Manda o
 * estado completo a cada mudança e os eventos no instante em que acontecem.
 * Mensagem inválida vira um evento {@code ERRO}, e a partida continua
 * (RNF14). Se a conexão cair, a partida pausa (RF30).</p>
 *
 * @author Mateus Vitor Ferreira
 * @version 0.1.0
 */
public final class CanalPartida {

    /** Caminho do canal, com o id da partida. */
    public static final String CAMINHO = "/ws/partidas/{id}";

    private static final Logger LOG = LoggerFactory.getLogger(CanalPartida.class);

    private final GerenciadorPartidas partidas;
    private final Map<String, Ouvinte> ouvintes = new ConcurrentHashMap<>();

    /**
     * Cria o canal.
     *
     * @param partidas onde as partidas ficam guardadas
     */
    public CanalPartida(GerenciadorPartidas partidas) {
        this.partidas = Objects.requireNonNull(partidas, "partidas");
    }

    /**
     * Registra o canal nas rotas do servidor.
     *
     * @param rotas configuração de rotas do servidor
     */
    public void registrar(RoutesConfig rotas) {
        rotas.ws(CAMINHO, this::configurar);
    }

    private void configurar(WsConfig ws) {
        ws.onConnect(this::aoConectar);
        ws.onMessage(this::aoReceber);
        ws.onClose(ctx -> aoSair(ctx));
        ws.onError(ctx -> aoSair(ctx));
    }

    private void aoConectar(WsContext ctx) {
        Optional<SessaoPartida> sessao = partidas.buscar(ctx.pathParam("id"));
        Ouvinte ouvinte = new Ouvinte(ctx);
        if (sessao.isEmpty()) {
            ouvinte.enviar(EventoDto.erro("Partida não encontrada."));
            ctx.closeSession(WsCloseStatus.POLICY_VIOLATION, "partida não encontrada");
            return;
        }
        ouvintes.put(ctx.sessionId(), ouvinte);
        ouvinte.enviar(EstadoDto.de(sessao.get().motor()));
        sessao.get().inscrever(ouvinte);
        sessao.get().iniciar();
    }

    private void aoReceber(WsMessageContext ctx) {
        Optional<SessaoPartida> sessao = partidas.buscar(ctx.pathParam("id"));
        Comando comando = lerComando(ctx);
        if (comando == null) {
            Ouvinte ouvinte = ouvintes.get(ctx.sessionId());
            if (ouvinte != null) {
                ouvinte.enviar(EventoDto.erro("Mensagem inválida. Esperado: {\"tipo\": \"COMANDO\", \"comando\": \"...\"}."));
            }
        } else {
            sessao.ifPresent(s -> s.enfileirar(comando));
        }
    }

    private static Comando lerComando(WsMessageContext ctx) {
        try {
            ComandoDto dto = ctx.messageAsClass(ComandoDto.class);
            if (dto == null || !ComandoDto.TIPO.equals(dto.tipo()) || dto.comando() == null) {
                return null;
            }
            return Comando.valueOf(dto.comando());
        } catch (Exception invalida) {
            // o Javalin (em Kotlin) lança as exceções do Jackson sem declará-las: pegar só
            // RuntimeException deixaria o JSON malformado escapar (RNF14)
            return null;
        }
    }

    private void aoSair(WsContext ctx) {
        Ouvinte ouvinte = ouvintes.remove(ctx.sessionId());
        partidas.buscar(ctx.pathParam("id")).ifPresent(sessao -> {
            if (ouvinte != null) {
                sessao.desinscrever(ouvinte);
            }
            sessao.pausar();
        });
    }

    /**
     * Uma conexão. Leva os avisos do motor (que chegam pela thread do laço) e
     * os erros (que chegam pela thread do WebSocket) para o navegador. Os
     * envios são sincronizados: duas threads nunca escrevem na mesma conexão
     * ao mesmo tempo.
     */
    private static final class Ouvinte implements ObservadorPartida {
        private final WsContext ctx;

        Ouvinte(WsContext ctx) {
            this.ctx = ctx;
        }

        synchronized void enviar(Object mensagem) {
            try {
                ctx.send(mensagem);
            } catch (RuntimeException falha) {
                LOG.debug("Não foi possível enviar para {}: {}", ctx.sessionId(), falha.getMessage());
            }
        }

        @Override
        public void estadoMudou(MotorJogo motor) {
            enviar(EstadoDto.de(motor));
        }

        @Override
        public void eventoOcorreu(EventoPartida evento) {
            enviar(EventoDto.de(evento));
        }
    }
}
