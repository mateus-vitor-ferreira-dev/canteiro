package canteiro.api.rotas;

import canteiro.api.dto.DificuldadeDto;
import io.javalin.config.RoutesConfig;

/**
 * Rotas das dificuldades oferecidas ao criar uma partida (RF02).
 *
 * @author Mateus Vitor Ferreira
 * @version 0.1.0
 */
public final class RotasDificuldades {

    private RotasDificuldades() {
    }

    /**
     * Registra {@code GET /api/dificuldades}, que lista as três dificuldades.
     *
     * @param rotas configuração de rotas do servidor
     */
    public static void registrar(RoutesConfig rotas) {
        rotas.get("/api/dificuldades", ctx -> ctx.json(DificuldadeDto.todas()));
    }
}
