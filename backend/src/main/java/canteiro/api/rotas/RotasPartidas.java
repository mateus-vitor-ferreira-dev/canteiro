package canteiro.api.rotas;

import canteiro.api.dto.ErroDto;
import canteiro.api.dto.NovaPartidaDto;
import canteiro.api.dto.PartidaCriadaDto;
import canteiro.api.dto.RelatorioDto;
import canteiro.controle.GerenciadorPartidas;
import canteiro.controle.LimiteDePartidasException;
import canteiro.modelo.Dificuldade;
import io.javalin.config.RoutesConfig;
import io.javalin.http.Context;
import io.javalin.http.HttpStatus;
import io.javalin.openapi.HttpMethod;
import io.javalin.openapi.OpenApi;
import io.javalin.openapi.OpenApiContent;
import io.javalin.openapi.OpenApiParam;
import io.javalin.openapi.OpenApiRequestBody;
import io.javalin.openapi.OpenApiResponse;

import java.util.Objects;

/**
 * Rotas de criação de partida (RF02) e do relatório de fim de partida (RF25).
 *
 * @author Mateus Vitor Ferreira
 * @version 0.1.0
 */
public final class RotasPartidas {

    private static final String CAMINHO = "/api/partidas";
    private static final String CAMINHO_RELATORIO = CAMINHO + "/{id}/relatorio";

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
        rotas.get(CAMINHO_RELATORIO, this::relatorio);
    }

    /**
     * Devolve o relatório da partida: a estabilidade a cada peça, os colapsos
     * e as peças por material. Funciona também com a partida em andamento.
     *
     * @param ctx contexto da requisição
     */
    @OpenApi(
            path = CAMINHO_RELATORIO,
            methods = HttpMethod.GET,
            operationId = "relatorioDaPartida",
            tags = "Partida",
            summary = "Relatório da partida",
            description = "O índice de estabilidade logo depois de cada peça fixada, o número das peças que "
                    + "derrubaram a estrutura (a partir de 1) e quantas peças de cada material entraram.",
            pathParams = @OpenApiParam(name = "id", description = "Id da partida", required = true),
            responses = {
                @OpenApiResponse(status = "200", description = "O relatório",
                        content = @OpenApiContent(from = RelatorioDto.class)),
                @OpenApiResponse(status = "404", description = "A partida não existe ou não está mais no servidor",
                        content = @OpenApiContent(from = ErroDto.class))
            })
    void relatorio(Context ctx) {
        partidas.buscar(ctx.pathParam("id")).ifPresentOrElse(
                sessao -> ctx.json(RelatorioDto.de(sessao.relatorio())),
                () -> ctx.status(HttpStatus.NOT_FOUND).json(new ErroDto("PARTIDA_NAO_ENCONTRADA",
                        "Essa partida não está mais no servidor. Ela expira 10 minutos depois de fechada.")));
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
            description = "Cria a partida na dificuldade escolhida, de treino ou não, e devolve o id. A partida "
                    + "começa quando o navegador conecta em /ws/partidas/{id}. Na de treino, o comando DESFAZER "
                    + "volta a última peça fixada para o topo, e ela não entra no ranking.",
            requestBody = @OpenApiRequestBody(content = @OpenApiContent(from = NovaPartidaDto.class), required = true),
            responses = {
                @OpenApiResponse(status = "201", description = "Partida criada",
                        content = @OpenApiContent(from = PartidaCriadaDto.class)),
                @OpenApiResponse(status = "400", description = "Dificuldade ausente ou desconhecida",
                        content = @OpenApiContent(from = ErroDto.class)),
                @OpenApiResponse(status = "503", description = "O servidor já tem o máximo de partidas abertas",
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
        try {
            ctx.status(HttpStatus.CREATED).json(new PartidaCriadaDto(partidas.criar(dificuldade, corpo.modoTreino()).id()));
        } catch (LimiteDePartidasException cheio) {
            ctx.status(HttpStatus.SERVICE_UNAVAILABLE).json(new ErroDto("SERVIDOR_CHEIO",
                    "O servidor está com muitas partidas abertas. Tente de novo em alguns minutos."));
        }
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
