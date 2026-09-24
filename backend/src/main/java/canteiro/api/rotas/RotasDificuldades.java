package canteiro.api.rotas;

import canteiro.api.dto.DificuldadeDto;
import canteiro.api.dto.ErroDto;
import io.javalin.config.RoutesConfig;
import io.javalin.http.Context;
import io.javalin.openapi.HttpMethod;
import io.javalin.openapi.OpenApi;
import io.javalin.openapi.OpenApiContent;
import io.javalin.openapi.OpenApiResponse;

/**
 * Rotas das dificuldades oferecidas ao criar uma partida (RF02).
 *
 * @author Mateus Vitor Ferreira
 * @version 0.1.0
 */
public final class RotasDificuldades {

    private static final String CAMINHO = "/api/dificuldades";

    private RotasDificuldades() {
    }

    /**
     * Registra as rotas deste recurso.
     *
     * @param rotas configuração de rotas do servidor
     */
    public static void registrar(RoutesConfig rotas) {
        rotas.get(CAMINHO, RotasDificuldades::listar);
    }

    /**
     * Lista as três dificuldades, da mais fácil para a mais difícil.
     *
     * @param ctx contexto da requisição
     */
    @OpenApi(
            path = CAMINHO,
            methods = HttpMethod.GET,
            operationId = "listarDificuldades",
            tags = "Partida",
            summary = "Lista as dificuldades",
            description = "As três dificuldades, da mais fácil para a mais difícil, com o intervalo inicial "
                    + "de queda, o limite de desvio e os materiais liberados (RF02).",
            responses = {
                @OpenApiResponse(status = "200", description = "As três dificuldades",
                        content = @OpenApiContent(from = DificuldadeDto[].class)),
                @OpenApiResponse(status = "500", description = "Erro inesperado no servidor",
                        content = @OpenApiContent(from = ErroDto.class))
            })
    static void listar(Context ctx) {
        ctx.json(DificuldadeDto.todas());
    }
}
