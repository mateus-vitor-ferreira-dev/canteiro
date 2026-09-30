package canteiro.api.rotas;

import canteiro.api.dto.ErroDto;
import canteiro.api.dto.NovaPartidaDto;
import canteiro.api.dto.PartidaCriadaDto;
import canteiro.controle.GerenciadorPartidas;
import canteiro.modelo.Dificuldade;
import io.javalin.config.RoutesConfig;
import io.javalin.http.Context;
import io.javalin.http.HttpStatus;
import io.javalin.openapi.HttpMethod;
import io.javalin.openapi.OpenApi;
import io.javalin.openapi.OpenApiContent;
import io.javalin.openapi.OpenApiRequestBody;
import io.javalin.openapi.OpenApiResponse;

import java.util.Objects;

/**
 * Rotas de criação de partida (RF02).
 *
 * @author Mateus Vitor Ferreira
 * @version 0.1.0
 */
public final class RotasPartidas {

    private static final String CAMINHO = "/api/partidas";

    private final GerenciadorPartidas partidas;

    /**
     * Cria as rotas.
     *
     * @param partidas onde as partidas ficam guardadas
     */
    public RotasPartidas(GerenciadorPartidas partidas) {
        this.partidas = Objects.requireNonNull(partidas, "partidas");
    }

    /**
     * Registra as rotas deste recurso.
     *
     * @param rotas configuração de rotas do servidor
     */
    public void registrar(RoutesConfig rotas) {
        rotas.post(CAMINHO, this::criar);
    }

    /**
     * Cria uma partida na dificuldade pedida. Ela começa quando o WebSocket conecta.
     *
     * @param ctx contexto da requisição
     */
    @OpenApi(
            path = CAMINHO,
            methods = HttpMethod.POST,
            operationId = "criarPartida",
            tags = "Partida",
            summary = "Cria uma partida",
            description = "Cria a partida na dificuldade escolhida e devolve o id. A partida começa quando o "
                    + "navegador conecta em /ws/partidas/{id}.",
            requestBody = @OpenApiRequestBody(content = @OpenApiContent(from = NovaPartidaDto.class), required = true),
            responses = {
                @OpenApiResponse(status = "201", description = "Partida criada",
                        content = @OpenApiContent(from = PartidaCriadaDto.class)),
                @OpenApiResponse(status = "400", description = "Dificuldade ausente ou desconhecida",
                        content = @OpenApiContent(from = ErroDto.class)),
                @OpenApiResponse(status = "501", description = "Modo treino pedido, mas ainda não disponível",
                        content = @OpenApiContent(from = ErroDto.class))
            })
    void criar(Context ctx) {
        NovaPartidaDto corpo = lerCorpo(ctx);
        Dificuldade dificuldade = lerDificuldade(corpo);
        if (dificuldade == null) {
            ctx.status(HttpStatus.BAD_REQUEST).json(new ErroDto("DIFICULDADE_INVALIDA",
                    "Escolha uma dificuldade: FACIL, NORMAL ou DIFICIL."));
            return;
        }
        if (corpo.modoTreino()) {
            // o motor ainda não desfaz jogadas (#37): melhor recusar do que criar um treino que não é treino
            ctx.status(HttpStatus.NOT_IMPLEMENTED).json(new ErroDto("MODO_TREINO_INDISPONIVEL",
                    "O modo treino ainda não está disponível nesta versão do jogo."));
            return;
        }
        ctx.status(HttpStatus.CREATED).json(new PartidaCriadaDto(partidas.criar(dificuldade).id()));
    }

    private static NovaPartidaDto lerCorpo(Context ctx) {
        try {
            return ctx.bodyAsClass(NovaPartidaDto.class);
        } catch (Exception invalida) {
            // o Javalin (em Kotlin) lança as exceções do Jackson sem declará-las: pegar só
            // RuntimeException deixaria o JSON malformado escapar (RNF14)
            return null;
        }
    }

    private static Dificuldade lerDificuldade(NovaPartidaDto corpo) {
        if (corpo == null || corpo.dificuldade() == null) {
            return null;
        }
        try {
            return Dificuldade.valueOf(corpo.dificuldade());
        } catch (IllegalArgumentException desconhecida) {
            return null;
        }
    }
}
