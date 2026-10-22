package canteiro.app;

import canteiro.api.ServidorWeb;
import canteiro.controle.GerenciadorPartidas;
import canteiro.modelo.estruturas.GeradorPecas;
import canteiro.modelo.materiais.CatalogoMateriais;
import canteiro.persistencia.ArquivosDoJogo;
import canteiro.persistencia.CarregadorMateriais;
import io.javalin.util.JavalinBindException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.Desktop;
import java.io.IOException;
import java.net.URI;
import java.time.Duration;

/**
 * Ponto de entrada do CANTEIRO.
 *
 * <p>Sobe o servidor em {@value ServidorWeb#HOST} e abre o jogo no navegador
 * padrão (RF29). Se não for possível abrir o navegador, o endereço fica no
 * terminal para o jogador abrir à mão. Na hospedagem pública, endereço, porta,
 * navegador e limite de partidas vêm das variáveis de ambiente
 * ({@link Configuracao}).</p>
 *
 * @author Mateus Vitor Ferreira
 * @version 0.1.0
 */
public final class Aplicacao {

    private static final Logger LOG = LoggerFactory.getLogger(Aplicacao.class);

    /** Endereço do servidor de desenvolvimento do Vite. */
    private static final String ENDERECO_VITE = "http://localhost:5173";

    /** Tempo sem conexão depois do qual a partida é descartada (RF31). */
    private static final Duration TOLERANCIA_SEM_CONEXAO = Duration.ofMinutes(10);

    /** De quanto em quanto tempo procurar partidas abandonadas. */
    private static final Duration INTERVALO_LIMPEZA = Duration.ofMinutes(1);

    private Aplicacao() {
    }

    /**
     * Inicia o servidor e abre o navegador.
     *
     * @param args argumentos de linha de comando; não utilizados
     */
    public static void main(String[] args) {
        Configuracao config = Configuracao.doAmbiente(System.getenv());
        GerenciadorPartidas partidas = criarGerenciador(CarregadorMateriais.carregar(config.pasta()),
                config.limitePartidas());
        partidas.iniciarLimpeza(TOLERANCIA_SEM_CONEXAO, INTERVALO_LIMPEZA);
        ServidorWeb servidor = new ServidorWeb(partidas, ArquivosDoJogo.em(config.pasta()));
        int porta;
        try {
            porta = servidor.iniciar(config.host(), config.porta());
        } catch (JavalinBindException e) {
            LOG.error("A porta {} já está em uso. O CANTEIRO já está aberto em outra janela?", config.porta());
            System.exit(1);
            return;
        }
        Runtime.getRuntime().addShutdownHook(new Thread(servidor::parar));

        URI endereco = URI.create("http://" + config.host() + ":" + porta);
        LOG.info("CANTEIRO rodando em {}", endereco);
        if (!servidor.temFrontendEmbutido()) {
            LOG.info("Frontend não embutido (modo de desenvolvimento). Suba o Vite e abra {}", ENDERECO_VITE);
        } else if (config.abrirNavegador()) {
            abrirNavegador(endereco);
        }
    }

    /**
     * Monta o gerenciador de partidas. Cada partida ganha um gerador de peças
     * próprio, pelo método da sacola, com uma semente nova, e os materiais com
     * a densidade e a cor do arquivo do jogador (#24).
     */
    private static GerenciadorPartidas criarGerenciador(CatalogoMateriais catalogo, int limitePartidas) {
        return new GerenciadorPartidas(dificuldade ->
                new GeradorPecas(System.nanoTime(), catalogo, dificuldade.nivelInicial()),
                limitePartidas, System::nanoTime);
    }

    private static void abrirNavegador(URI endereco) {
        boolean suportado = Desktop.isDesktopSupported()
                && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE);
        if (!suportado) {
            LOG.info("Não foi possível abrir o navegador. Abra {} manualmente.", endereco);
            return;
        }
        try {
            Desktop.getDesktop().browse(endereco);
        } catch (IOException | UnsupportedOperationException e) {
            LOG.info("Não foi possível abrir o navegador. Abra {} manualmente.", endereco);
        }
    }
}
