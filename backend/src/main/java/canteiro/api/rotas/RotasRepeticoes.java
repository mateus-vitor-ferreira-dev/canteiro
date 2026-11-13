package canteiro.api.rotas;

import canteiro.api.dto.ErroDto;
import canteiro.api.dto.PartidaCriadaDto;
import canteiro.api.dto.RepeticaoDto;
import canteiro.controle.GerenciadorPartidas;
import canteiro.controle.LimiteDePartidasException;
import canteiro.persistencia.ArquivoRepeticoes;
import canteiro.persistencia.Repeticao;
import canteiro.persistencia.RepeticaoCorrompidaException;
import io.javalin.config.RoutesConfig;
import io.javalin.http.Context;
import io.javalin.http.HttpStatus;
import io.javalin.openapi.HttpMethod;
import io.javalin.openapi.OpenApi;
import io.javalin.openapi.OpenApiContent;
import io.javalin.openapi.OpenApiParam;
import io.javalin.openapi.OpenApiResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;
import java.util.Optional;

/**
 * Rotas das partidas gravadas (RF24): a lista e a reprodução.
 *
 * <p>A reprodução é uma sessão como as outras: abre em
 * {@code /ws/partidas/{id}}, mas quem joga são as jogadas gravadas.</p>
 *
 * @author Mateus Vitor Ferreira
 * @version 0.1.0
 */
public final class RotasRepeticoes {

    private static final String CAMINHO = "/api/repeticoes";
    private static final String CAMINHO_REPRODUZIR = CAMINHO + "/{id}/reproduzir";
    private static final Logger LOG = LoggerFactory.getLogger(RotasRepeticoes.class);

    private final ArquivoRepeticoes repeticoes;
    private final GerenciadorPartidas partidas;

    /**
     * Cria as rotas.
     *
     * @param repeticoes onde as partidas gravadas ficam
     * @param partidas   onde a sessão de reprodução é criada
     */
    public RotasRepeticoes(ArquivoRepeticoes repeticoes, GerenciadorPartidas partidas) {
        this.repeticoes = Objects.requireNonNull(repeticoes, "repetições");
        this.partidas = Objects.requireNonNull(partidas, "partidas");
    }

    /**
     * Registra as rotas deste recurso.
     *
     * @param rotas configuração de rotas do servidor
     */
    public void registrar(RoutesConfig rotas) {
        rotas.get(CAMINHO, this::listar);
        rotas.post(CAMINHO_REPRODUZIR, this::reproduzir);
    }

    /**
     * Devolve as partidas gravadas, da mais recente para a mais antiga.
     *
     * @param ctx contexto da requisição
     */
    @OpenApi(
            path = CAMINHO,
            methods = HttpMethod.GET,
            operationId = "listarRepeticoes",
            tags = "Repetições",
            summary = "Lista as partidas gravadas",
            description = "As partidas comuns que terminaram, da mais recente para a mais antiga. Ficam as "
                    + ArquivoRepeticoes.MAXIMO + " últimas.",
            responses = @OpenApiResponse(status = "200", description = "As partidas gravadas",
                    content = @OpenApiContent(from = RepeticaoDto[].class)))
    void listar(Context ctx) {
        ctx.json(repeticoes.listar().stream().map(RepeticaoDto::de).toList());
    }

    /**
     * Cria a reprodução de uma partida gravada e devolve o id da sessão.
     *
     * @param ctx contexto da requisição
     */
    @OpenApi(
            path = CAMINHO_REPRODUZIR,
            methods = HttpMethod.POST,
            operationId = "reproduzirRepeticao",
            tags = "Repetições",
            summary = "Reproduz uma partida gravada",
            description = "Cria uma sessão que refaz as jogadas gravadas. O navegador abre em /ws/partidas/{id}, "
                    + "como uma partida comum; ali só valem os comandos PAUSAR e RETOMAR.",
            pathParams = @OpenApiParam(name = "id", description = "Id da gravação", required = true),
            responses = {
                @OpenApiResponse(status = "201", description = "Reprodução criada",
                        content = @OpenApiContent(from = PartidaCriadaDto.class)),
                @OpenApiResponse(status = "404", description = "A gravação não existe",
                        content = @OpenApiContent(from = ErroDto.class)),
                @OpenApiResponse(status = "422", description = "O arquivo da gravação está estragado",
                        content = @OpenApiContent(from = ErroDto.class)),
                @OpenApiResponse(status = "503", description = "O servidor já tem o máximo de partidas abertas",
                        content = @OpenApiContent(from = ErroDto.class))
            })
    void reproduzir(Context ctx) {
        String id = ctx.pathParam("id");
        Optional<Repeticao> repeticao;
        try {
            repeticao = repeticoes.ler(id);
        } catch (RepeticaoCorrompidaException estragada) {
            LOG.warn("Repetição {} estragada: {}", id, estragada.getMessage());
            ctx.status(HttpStatus.UNPROCESSABLE_CONTENT).json(new ErroDto("REPETICAO_CORROMPIDA",
                    "O arquivo dessa partida gravada está estragado e não dá para reproduzi-la."));
            return;
        }
        if (repeticao.isEmpty()) {
            ctx.status(HttpStatus.NOT_FOUND).json(new ErroDto("REPETICAO_NAO_ENCONTRADA",
                    "Essa partida gravada não existe mais."));
            return;
        }
        try {
            ctx.status(HttpStatus.CREATED).json(new PartidaCriadaDto(partidas.reproduzir(repeticao.get()).id()));
        } catch (LimiteDePartidasException cheio) {
            ctx.status(HttpStatus.SERVICE_UNAVAILABLE).json(new ErroDto("SERVIDOR_CHEIO",
                    "O servidor está com muitas partidas abertas. Tente de novo em alguns minutos."));
        }
    }
}
