package canteiro.api.rotas;

import canteiro.api.dto.ConfiguracoesDto;
import canteiro.api.dto.ErroDto;
import canteiro.persistencia.ArquivoConfiguracoes;
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
import java.util.Objects;
import java.util.Optional;

/**
 * Rotas das configurações do jogador: teclas e volume (RF27).
 *
 * <p>Ler e gravar o arquivo é com o {@link ArquivoConfiguracoes}; aqui só se
 * valida o que chega ({@link ValidacaoConfiguracoes}).</p>
 *
 * @author Mateus Vitor Ferreira
 * @version 0.1.0
 */
public final class RotasConfiguracoes {

    private static final String CAMINHO = "/api/configuracoes";
    private static final String ERRO_INVALIDA = "CONFIGURACAO_INVALIDA";
    private static final Logger LOG = LoggerFactory.getLogger(RotasConfiguracoes.class);

    private final ArquivoConfiguracoes arquivo;

    /**
     * Cria as rotas.
     *
     * @param arquivo o arquivo de configurações do jogador
     */
    public RotasConfiguracoes(ArquivoConfiguracoes arquivo) {
        this.arquivo = Objects.requireNonNull(arquivo, "arquivo de configurações");
    }

    /**
     * Registra as rotas deste recurso.
     *
     * @param rotas configuração de rotas do servidor
     */
    public void registrar(RoutesConfig rotas) {
        rotas.get(CAMINHO, this::ler);
        rotas.put(CAMINHO, this::salvar);
    }

    /**
     * Devolve as teclas trocadas e o volume. Sem nada salvo, nenhuma tecla e o volume padrão.
     *
     * @param ctx contexto da requisição
     */
    @OpenApi(
            path = CAMINHO,
            methods = HttpMethod.GET,
            operationId = "lerConfiguracoes",
            tags = "Configurações",
            summary = "Lê as configurações",
            description = "As teclas que o jogador trocou e o volume. Ação que não aparece usa a tecla padrão.",
            responses = @OpenApiResponse(status = "200", description = "As configurações",
                    content = @OpenApiContent(from = ConfiguracoesDto.class)))
    void ler(Context ctx) {
        ctx.json(ConfiguracoesDto.de(arquivo.ler()));
    }

    /**
     * Valida e grava as configurações, substituindo as anteriores.
     *
     * @param ctx contexto da requisição
     */
    @OpenApi(
            path = CAMINHO,
            methods = HttpMethod.PUT,
            operationId = "salvarConfiguracoes",
            tags = "Configurações",
            summary = "Salva as configurações",
            description = "Recusa ação desconhecida, ação sem tecla, a mesma tecla em duas ações (contando as "
                    + "padrão das ações que não vieram) e volume fora de 0 a 100.",
            requestBody = @OpenApiRequestBody(content = @OpenApiContent(from = ConfiguracoesDto.class),
                    required = true),
            responses = {
                @OpenApiResponse(status = "204", description = "Salvas"),
                @OpenApiResponse(status = "400", description = "Corpo malformado ou configuração inválida",
                        content = @OpenApiContent(from = ErroDto.class)),
                @OpenApiResponse(status = "500", description = "Não foi possível gravar o arquivo",
                        content = @OpenApiContent(from = ErroDto.class))
            })
    void salvar(Context ctx) {
        ConfiguracoesDto corpo = lerCorpo(ctx);
        Optional<String> problema = ValidacaoConfiguracoes.problema(corpo);
        if (problema.isPresent()) {
            ctx.status(HttpStatus.BAD_REQUEST).json(new ErroDto(ERRO_INVALIDA, problema.get()));
            return;
        }
        try {
            arquivo.salvar(corpo.paraSalvar());
            ctx.status(HttpStatus.NO_CONTENT);
        } catch (UncheckedIOException falha) {
            LOG.error("Não foi possível gravar as configurações", falha);
            ctx.status(HttpStatus.INTERNAL_SERVER_ERROR).json(new ErroDto("CONFIGURACOES_INDISPONIVEIS",
                    "Não foi possível salvar as configurações agora. Tente de novo."));
        }
    }

    private static ConfiguracoesDto lerCorpo(Context ctx) {
        try {
            return ctx.bodyAsClass(ConfiguracoesDto.class);
        } catch (Exception invalida) {
            // o Javalin (em Kotlin) lança as exceções do Jackson sem declará-las: pegar só
            // RuntimeException deixaria o JSON malformado escapar (RNF14)
            return null;
        }
    }
}
