package canteiro.api.rotas;

import canteiro.api.dto.ErroDto;
import canteiro.api.dto.MaterialDto;
import canteiro.modelo.materiais.CatalogoMateriais;
import io.javalin.config.RoutesConfig;
import io.javalin.http.Context;
import io.javalin.openapi.HttpMethod;
import io.javalin.openapi.OpenApi;
import io.javalin.openapi.OpenApiContent;
import io.javalin.openapi.OpenApiResponse;

import java.util.Objects;

/**
 * Rota do catálogo de materiais em uso pelo jogo (RF04, RNF06).
 *
 * <p>Os valores são os do catálogo carregado na partida, incluindo a
 * densidade e a cor que o jogador tiver mudado no {@code materiais.properties}.</p>
 *
 * @author Mateus Vitor Ferreira
 * @version 0.1.0
 */
public final class RotasMateriais {

    private static final String CAMINHO = "/api/materiais";

    private final CatalogoMateriais catalogo;

    /**
     * Cria as rotas sobre o catálogo em uso.
     *
     * @param catalogo catálogo de materiais do jogo
     */
    public RotasMateriais(CatalogoMateriais catalogo) {
        this.catalogo = Objects.requireNonNull(catalogo, "catalogo");
    }

    /**
     * Registra as rotas deste recurso.
     *
     * @param rotas configuração de rotas do servidor
     */
    public void registrar(RoutesConfig rotas) {
        rotas.get(CAMINHO, this::listar);
    }

    /**
     * Lista os materiais, do mais leve ao mais pesado.
     *
     * @param ctx contexto da requisição
     */
    @OpenApi(
            path = CAMINHO,
            methods = HttpMethod.GET,
            operationId = "listarMateriais",
            tags = "Partida",
            summary = "Lista os materiais",
            description = "O catálogo de materiais: código, nome, densidade, cor, bônus por linha e a carga "
                    + "máxima que cada bloco aguenta em cima (RF04, RN21). O aço não tem limite: "
                    + "a carga máxima vem nula.",
            responses = {
                @OpenApiResponse(status = "200", description = "Os materiais do catálogo",
                        content = @OpenApiContent(from = MaterialDto[].class)),
                @OpenApiResponse(status = "500", description = "Erro inesperado no servidor",
                        content = @OpenApiContent(from = ErroDto.class))
            })
    void listar(Context ctx) {
        ctx.json(MaterialDto.todos(catalogo));
    }
}
