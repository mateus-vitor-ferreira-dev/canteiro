package canteiro.api;

import canteiro.api.dto.ErroDto;
import canteiro.api.rotas.RotasDificuldades;
import io.javalin.Javalin;
import io.javalin.config.JavalinConfig;
import io.javalin.config.RoutesConfig;
import io.javalin.http.HttpStatus;
import io.javalin.http.staticfiles.Location;
import io.javalin.json.JavalinJackson;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Servidor HTTP do jogo: rotas da API e, no JAR final, os arquivos do frontend.
 *
 * <p>Escuta apenas em {@value #HOST}, nunca em todas as interfaces de rede,
 * para que outro computador da rede não alcance o jogo (RNF13). Toda exceção
 * que escapar de uma rota vira uma resposta {@link ErroDto}, sem derrubar o
 * servidor (RNF14).</p>
 *
 * @author Mateus Vitor Ferreira
 * @version 0.1.0
 */
public final class ServidorWeb {

    /** Endereço de escuta: só a própria máquina (RNF13). */
    public static final String HOST = "127.0.0.1";

    /** Porta usada pelo jogo e esperada pelo proxy do Vite. */
    public static final int PORTA_PADRAO = 7070;

    /** Pasta do classpath onde o build coloca o frontend compilado (RNF16). */
    private static final String PASTA_FRONTEND = "/publico";

    private static final Logger LOG = LoggerFactory.getLogger(ServidorWeb.class);

    private final boolean frontendEmbutido;
    private final Javalin javalin;

    /**
     * Configura o servidor e registra as rotas, sem começar a escutar.
     */
    public ServidorWeb() {
        this.frontendEmbutido = ServidorWeb.class.getResource(PASTA_FRONTEND) != null;
        this.javalin = Javalin.create(this::configurar);
    }

    /**
     * Começa a escutar em {@value #HOST}, na porta indicada.
     *
     * @param porta porta desejada; {@code 0} escolhe uma porta livre qualquer
     * @return a porta em que o servidor de fato ficou escutando
     * @throws io.javalin.util.JavalinBindException se a porta já estiver em uso
     */
    public int iniciar(int porta) {
        javalin.start(HOST, porta);
        return javalin.port();
    }

    /**
     * Para o servidor e libera a porta.
     */
    public void parar() {
        javalin.stop();
    }

    /**
     * Informa se o JAR traz o frontend compilado. Durante o desenvolvimento ele
     * não vem: quem serve as telas é o Vite.
     *
     * @return {@code true} se as telas são servidas por este servidor
     */
    public boolean temFrontendEmbutido() {
        return frontendEmbutido;
    }

    private void configurar(JavalinConfig config) {
        config.startup.showJavalinBanner = false;
        config.jsonMapper(new JavalinJackson());
        DocumentacaoApi.registrar(config);
        if (frontendEmbutido) {
            config.staticFiles.add(PASTA_FRONTEND, Location.CLASSPATH);
        }
        registrarRotas(config.routes);
    }

    private static void registrarRotas(RoutesConfig rotas) {
        RotasDificuldades.registrar(rotas);

        rotas.exception(Exception.class, (erro, ctx) -> {
            LOG.error("Erro inesperado em {} {}", ctx.method(), ctx.path(), erro);
            ctx.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .json(new ErroDto("ERRO_INTERNO", "Ocorreu um erro inesperado no jogo."));
        });
    }
}
