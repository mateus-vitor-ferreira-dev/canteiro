package canteiro.api;

import canteiro.api.dto.ErroDto;
import canteiro.api.rotas.RotasConfiguracoes;
import canteiro.api.rotas.RotasDificuldades;
import canteiro.api.rotas.RotasMateriais;
import canteiro.api.rotas.RotasPartidas;
import canteiro.api.rotas.RotasRanking;
import canteiro.api.rotas.RotasRepeticoes;
import canteiro.api.ws.CanalPartida;
import canteiro.controle.GerenciadorPartidas;
import canteiro.modelo.materiais.CatalogoMateriais;
import canteiro.persistencia.ArquivosDoJogo;
import io.javalin.Javalin;
import io.javalin.config.JavalinConfig;
import io.javalin.config.RoutesConfig;
import io.javalin.http.HttpStatus;
import io.javalin.http.staticfiles.Location;
import io.javalin.json.JavalinJackson;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;

/**
 * Servidor HTTP do jogo: rotas da API e, no JAR final, os arquivos do frontend.
 *
 * <p>Por padrão escuta apenas em {@value #HOST}, nunca em todas as interfaces
 * de rede, para que outro computador da rede não alcance o jogo (RNF13). Só a
 * hospedagem pública escolhe outro endereço, pela configuração. Toda exceção
 * que escapar de uma rota vira uma resposta {@link ErroDto}, sem derrubar o
 * servidor (RNF14).</p>
 *
 * @author Mateus Vitor Ferreira
 * @version 0.1.0
 */
public final class ServidorWeb {

    /** Endereço de escuta padrão: só a própria máquina (RNF13). */
    public static final String HOST = "127.0.0.1";

    /** Porta usada pelo jogo e esperada pelo proxy do Vite. */
    public static final int PORTA_PADRAO = 7070;

    /** Pasta do classpath onde o build coloca o frontend compilado (RNF16). */
    private static final String PASTA_FRONTEND = "/publico";

    private static final Logger LOG = LoggerFactory.getLogger(ServidorWeb.class);

    private final GerenciadorPartidas partidas;
    private final ArquivosDoJogo arquivos;
    private final boolean compartilhado;
    private final CatalogoMateriais catalogo;
    private final boolean frontendEmbutido;
    private final Javalin javalin;

    /**
     * Configura o servidor do jogo local, de um jogador só, e registra as
     * rotas, sem começar a escutar.
     *
     * @param partidas onde as partidas ficam guardadas
     * @param arquivos  os arquivos do jogador que as rotas leem e gravam
     */
    public ServidorWeb(GerenciadorPartidas partidas, ArquivosDoJogo arquivos) {
        this(partidas, arquivos, false);
    }

    /**
     * Configura o servidor e registra as rotas, sem começar a escutar.
     *
     * <p>Num servidor compartilhado, como a versão online, as configurações
     * de um jogador não podem valer para todos: a rota de configurações não
     * é registrada, responde 404, e cada navegador guarda as suas (RF27).</p>
     *
     * @param partidas      onde as partidas ficam guardadas
     * @param arquivos      os arquivos do jogador que as rotas leem e gravam
     * @param compartilhado se vários jogadores usam o mesmo servidor
     */
    public ServidorWeb(GerenciadorPartidas partidas, ArquivosDoJogo arquivos, boolean compartilhado) {
        this(partidas, arquivos, compartilhado, CatalogoMateriais.padrao());
    }

    /**
     * Configura o servidor com o catálogo de materiais em uso pelas partidas
     * e registra as rotas, sem começar a escutar.
     *
     * @param partidas      onde as partidas ficam guardadas
     * @param arquivos      os arquivos do jogador que as rotas leem e gravam
     * @param compartilhado se vários jogadores usam o mesmo servidor
     * @param catalogo      os materiais mostrados em {@code GET /api/materiais}
     */
    public ServidorWeb(GerenciadorPartidas partidas, ArquivosDoJogo arquivos, boolean compartilhado,
                       CatalogoMateriais catalogo) {
        this.catalogo = Objects.requireNonNull(catalogo, "catalogo");
        this.partidas = Objects.requireNonNull(partidas, "partidas");
        this.arquivos = Objects.requireNonNull(arquivos, "arquivos");
        this.compartilhado = compartilhado;
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
        return iniciar(HOST, porta);
    }

    /**
     * Começa a escutar no endereço e na porta indicados. Fora da máquina do
     * jogador, como num contêiner, o endereço costuma ser {@code 0.0.0.0}.
     *
     * @param host  endereço de escuta
     * @param porta porta desejada; {@code 0} escolhe uma porta livre qualquer
     * @return a porta em que o servidor de fato ficou escutando
     * @throws io.javalin.util.JavalinBindException se a porta já estiver em uso
     */
    public int iniciar(String host, int porta) {
        javalin.start(Objects.requireNonNull(host, "host"), porta);
        return javalin.port();
    }

    /**
     * Encerra as partidas, para o servidor e libera a porta.
     */
    public void parar() {
        partidas.encerrarTodas();
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

    private void registrarRotas(RoutesConfig rotas) {
        RotasDificuldades.registrar(rotas);
        new RotasMateriais(catalogo).registrar(rotas);
        new RotasPartidas(partidas).registrar(rotas);
        new CanalPartida(partidas).registrar(rotas);
        new RotasRanking(partidas, arquivos.ranking()).registrar(rotas);
        new RotasRepeticoes(arquivos.repeticoes(), partidas).registrar(rotas);
        if (!compartilhado) {
            new RotasConfiguracoes(arquivos.configuracoes()).registrar(rotas);
        }

        rotas.exception(Exception.class, (erro, ctx) -> {
            LOG.error("Erro inesperado em {} {}", ctx.method(), ctx.path(), erro);
            ctx.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .json(new ErroDto("ERRO_INTERNO", "Ocorreu um erro inesperado no jogo."));
        });
    }
}
