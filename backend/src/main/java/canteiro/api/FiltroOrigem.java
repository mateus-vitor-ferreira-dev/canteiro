package canteiro.api;

import canteiro.api.dto.ErroDto;
import io.javalin.config.RoutesConfig;
import io.javalin.http.Context;
import io.javalin.http.HttpStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.util.Locale;

/**
 * Aceita só pedidos vindos da própria origem do jogo (RNF13).
 *
 * <p>Escutar em {@code 127.0.0.1} impede que outro computador alcance o jogo,
 * mas não impede que uma página aberta em outra aba, de outro site, faça o
 * navegador do jogador chamar a API ou abrir o WebSocket de uma partida. O
 * navegador manda o cabeçalho {@code Origin} nesses pedidos, e o filtro só
 * deixa passar quando a origem aponta para o mesmo endereço que o pedido
 * ({@code Host}). Pedido sem {@code Origin}, como o de um teste ou do
 * {@code curl}, não vem de outra página e passa.</p>
 *
 * <p>No desenvolvimento, o proxy do Vite repassa o {@code Host} da página
 * ({@code localhost:5173}), então a origem do Vite casa com o pedido sem
 * nenhuma exceção no código. No JAR, uma página servida pelo Vite não tem
 * como falar com o servidor: as origens não casam.</p>
 *
 * @author Mateus Vitor Ferreira
 * @version 0.1.0
 */
public final class FiltroOrigem {

    private static final Logger LOG = LoggerFactory.getLogger(FiltroOrigem.class);

    private FiltroOrigem() {
    }

    /**
     * Instala o filtro antes das rotas HTTP e antes de aceitar um WebSocket.
     *
     * @param rotas configuração de rotas do servidor
     */
    public static void registrar(RoutesConfig rotas) {
        rotas.before(FiltroOrigem::conferir);
        rotas.wsBeforeUpgrade(FiltroOrigem::conferir);
    }

    /**
     * Diz se um pedido com esta origem e este {@code Host} vem do próprio jogo.
     *
     * @param origem o cabeçalho {@code Origin}, ou {@code null} se o pedido não trouxe
     * @param host   o cabeçalho {@code Host} do pedido, com a porta, se houver
     * @return {@code true} se o pedido pode passar
     */
    public static boolean permitida(String origem, String host) {
        if (origem == null) {
            return true;
        }
        if (host == null || host.isBlank()) {
            return false;
        }
        try {
            URI uri = URI.create(origem.trim());
            String autoridade = uri.getRawAuthority();
            return autoridade != null && autoridade.toLowerCase(Locale.ROOT).equals(host.trim().toLowerCase(Locale.ROOT));
        } catch (IllegalArgumentException malformada) {
            return false;
        }
    }

    private static void conferir(Context ctx) {
        String origem = ctx.header("Origin");
        if (permitida(origem, ctx.header("Host"))) {
            return;
        }
        LOG.warn("Pedido recusado de outra origem: {} {} (Origin: {})", ctx.method(), ctx.path(), origem);
        ctx.status(HttpStatus.FORBIDDEN)
                .json(new ErroDto("ORIGEM_RECUSADA", "Este pedido não veio da página do jogo."));
        ctx.skipRemainingHandlers();
    }
}
