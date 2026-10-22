package canteiro.api.rotas;

import canteiro.api.dto.EntradaRankingDto;
import canteiro.api.dto.ErroDto;
import canteiro.api.dto.PosicaoRankingDto;
import canteiro.api.dto.RegistroRankingDto;
import canteiro.controle.GerenciadorPartidas;
import canteiro.controle.SessaoPartida;
import canteiro.modelo.Placar;
import canteiro.persistencia.EntradaRanking;
import canteiro.persistencia.RankingArquivo;
import io.javalin.config.RoutesConfig;
import io.javalin.http.Context;
import io.javalin.http.HttpStatus;
import io.javalin.openapi.HttpMethod;
import io.javalin.openapi.OpenApi;
import io.javalin.openapi.OpenApiContent;
import io.javalin.openapi.OpenApiRequestBody;
import io.javalin.openapi.OpenApiResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.UncheckedIOException;
import java.time.LocalDate;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Rotas do ranking (RF21, RF22).
 *
 * <p>A pontuação nunca vem do navegador: o {@code POST} manda só o id da
 * partida e o nome, e a pontuação é lida da própria partida, que precisa ter
 * terminado. Cada partida entra uma vez só.</p>
 *
 * @author Mateus Vitor Ferreira
 * @version 0.1.0
 */
public final class RotasRanking {

    private static final String CAMINHO = "/api/ranking";
    private static final Logger LOG = LoggerFactory.getLogger(RotasRanking.class);

    private final GerenciadorPartidas partidas;
    private final RankingArquivo ranking;
    private final Set<String> registradas = ConcurrentHashMap.newKeySet();

    /**
     * Cria as rotas.
     *
     * @param partidas onde estão as partidas, para ler a pontuação de verdade
     * @param ranking  o arquivo do ranking
     */
    public RotasRanking(GerenciadorPartidas partidas, RankingArquivo ranking) {
        this.partidas = Objects.requireNonNull(partidas, "partidas");
        this.ranking = Objects.requireNonNull(ranking, "ranking");
    }

    /**
     * Registra as rotas deste recurso.
     *
     * @param rotas configuração de rotas do servidor
     */
    public void registrar(RoutesConfig rotas) {
        rotas.get(CAMINHO, this::listar);
        rotas.post(CAMINHO, this::registrarPartida);
    }

    /**
     * Devolve as dez melhores partidas, da maior pontuação para a menor.
     *
     * @param ctx contexto da requisição
     */
    @OpenApi(
            path = CAMINHO,
            methods = HttpMethod.GET,
            operationId = "listarRanking",
            tags = "Ranking",
            summary = "Lista o ranking",
            description = "As dez melhores pontuações, uma por jogador, da maior para a menor.",
            responses = @OpenApiResponse(status = "200", description = "O ranking",
                    content = @OpenApiContent(from = EntradaRankingDto[].class)))
    void listar(Context ctx) {
        ctx.json(ranking.dezMelhores().stream().map(EntradaRankingDto::de).toList());
    }

    /**
     * Registra uma partida encerrada no ranking, com o nome do jogador.
     *
     * @param ctx contexto da requisição
     */
    @OpenApi(
            path = CAMINHO,
            methods = HttpMethod.POST,
            operationId = "registrarNoRanking",
            tags = "Ranking",
            summary = "Registra uma partida no ranking",
            description = "A pontuação é lida da partida no servidor. Só entra partida encerrada, uma vez só. "
                    + "Se o jogador já está no ranking, fica a maior pontuação dele.",
            requestBody = @OpenApiRequestBody(content = @OpenApiContent(from = RegistroRankingDto.class),
                    required = true),
            responses = {
                @OpenApiResponse(status = "201", description = "Registrada; devolve a posição do jogador",
                        content = @OpenApiContent(from = PosicaoRankingDto.class)),
                @OpenApiResponse(status = "400", description = "Corpo malformado ou nome inválido",
                        content = @OpenApiContent(from = ErroDto.class)),
                @OpenApiResponse(status = "409", description = "Partida em andamento ou já registrada",
                        content = @OpenApiContent(from = ErroDto.class)),
                @OpenApiResponse(status = "410", description = "A partida não está mais no servidor",
                        content = @OpenApiContent(from = ErroDto.class)),
                @OpenApiResponse(status = "500", description = "Não foi possível gravar o arquivo",
                        content = @OpenApiContent(from = ErroDto.class))
            })
    void registrarPartida(Context ctx) {
        RegistroRankingDto corpo = lerCorpo(ctx);
        String nome = corpo == null || corpo.nome() == null ? "" : corpo.nome().strip();
        Optional<String> problema = corpo == null || corpo.partidaId() == null
                ? Optional.of("Pedido de registro malformado.")
                : EntradaRanking.problemaNoNome(nome);
        if (problema.isPresent()) {
            ctx.status(HttpStatus.BAD_REQUEST).json(new ErroDto("NOME_INVALIDO", problema.get()));
            return;
        }
        Optional<SessaoPartida> sessao = partidas.buscar(corpo.partidaId());
        if (sessao.isEmpty()) {
            ctx.status(HttpStatus.GONE).json(new ErroDto("PARTIDA_EXPIRADA",
                    "Essa partida não está mais no servidor. Ela expira 10 minutos depois de fechada."));
            return;
        }
        if (!sessao.get().motor().estado().encerrada()) {
            ctx.status(HttpStatus.CONFLICT).json(new ErroDto("PARTIDA_EM_ANDAMENTO",
                    "Só dá para salvar no ranking depois que a partida termina."));
            return;
        }
        gravar(ctx, sessao.get(), nome);
    }

    /**
     * Grava a partida já validada. O laço é encerrado antes de ler o placar,
     * para a leitura não concorrer com a thread da partida.
     */
    private void gravar(Context ctx, SessaoPartida sessao, String nome) {
        // RN15: partida de treino (com desfazer) não entra. Hoje o treino é recusado ao
        // criar a partida; quando a #37 existir, recuse aqui a partida que usou o desfazer.
        if (!registradas.add(sessao.id())) {
            ctx.status(HttpStatus.CONFLICT).json(new ErroDto("PARTIDA_JA_REGISTRADA",
                    "Essa partida já está no ranking."));
            return;
        }
        sessao.encerrar();
        Placar placar = sessao.motor().placar();
        EntradaRanking entrada = new EntradaRanking(nome, placar.pontuacao(), placar.nivel(), placar.linhas(),
                placar.colapsos(), LocalDate.now());
        try {
            ctx.status(HttpStatus.CREATED).json(new PosicaoRankingDto(ranking.registrar(entrada)));
        } catch (UncheckedIOException falha) {
            registradas.remove(sessao.id());
            LOG.error("Não foi possível gravar o ranking", falha);
            ctx.status(HttpStatus.INTERNAL_SERVER_ERROR).json(new ErroDto("RANKING_INDISPONIVEL",
                    "Não foi possível salvar no ranking agora. Tente de novo."));
        }
    }

    private static RegistroRankingDto lerCorpo(Context ctx) {
        try {
            return ctx.bodyAsClass(RegistroRankingDto.class);
        } catch (Exception invalida) {
            // o Javalin (em Kotlin) lança as exceções do Jackson sem declará-las: pegar só
            // RuntimeException deixaria o JSON malformado escapar (RNF14)
            return null;
        }
    }
}
