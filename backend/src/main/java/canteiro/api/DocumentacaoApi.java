package canteiro.api;

import io.javalin.config.JavalinConfig;
import io.javalin.openapi.plugin.OpenApiPlugin;
import io.javalin.openapi.plugin.swagger.SwaggerPlugin;

/**
 * Documentação das rotas: a especificação OpenAPI e a tela do Swagger.
 *
 * <p>A especificação é gerada na compilação, a partir das anotações
 * {@code @OpenApi} de cada rota em {@code canteiro.api.rotas}. Toda rota nova
 * precisa da sua anotação. Os dois endereços ficam debaixo de {@code /api},
 * para o proxy do Vite também repassá-los durante o desenvolvimento.</p>
 *
 * @author Mateus Vitor Ferreira
 * @version 0.1.0
 */
public final class DocumentacaoApi {

    /** Especificação OpenAPI, em JSON. */
    public static final String CAMINHO_ESPECIFICACAO = "/api/openapi.json";

    /** Tela do Swagger, para ler e testar as rotas no navegador. */
    public static final String CAMINHO_SWAGGER = "/api/docs";

    private static final String TITULO = "CANTEIRO — API";
    private static final String VERSAO = "0.1.0";
    private static final String DESCRICAO = "Rotas REST do CANTEIRO. O servidor escuta só em 127.0.0.1. "
            + "A partida em si trafega pelo WebSocket /ws/partidas/{id}, que não aparece aqui.";

    private DocumentacaoApi() {
    }

    /**
     * Registra os plugins do OpenAPI e do Swagger na configuração do servidor.
     *
     * @param config configuração do Javalin
     */
    public static void registrar(JavalinConfig config) {
        config.registerPlugin(new OpenApiPlugin(openApi -> openApi
                .withDocumentationPath(CAMINHO_ESPECIFICACAO)
                .withPrettyOutput()
                .withDefinitionConfiguration((versao, definicao) -> definicao
                        .info(info -> info.title(TITULO).version(VERSAO).description(DESCRICAO)))));
        config.registerPlugin(new SwaggerPlugin(swagger -> swagger
                .withDocumentationPath(CAMINHO_ESPECIFICACAO)
                .withUiPath(CAMINHO_SWAGGER)));
    }
}
