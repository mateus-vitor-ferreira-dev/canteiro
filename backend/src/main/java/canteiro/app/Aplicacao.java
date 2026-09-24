package canteiro.app;

import canteiro.api.ServidorWeb;
import canteiro.controle.GerenciadorPartidas;
import canteiro.modelo.estruturas.FonteSimples;
import canteiro.modelo.materiais.CatalogoMateriais;
import io.javalin.util.JavalinBindException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.Desktop;
import java.io.IOException;
import java.net.URI;

/**
 * Ponto de entrada do CANTEIRO.
 *
 * <p>Sobe o servidor em {@value ServidorWeb#HOST} e abre o jogo no navegador
 * padrão (RF29). Se não for possível abrir o navegador, o endereço fica no
 * terminal para o jogador abrir à mão.</p>
 *
 * @author Mateus Vitor Ferreira
 * @version 0.1.0
 */
public final class Aplicacao {

    private static final Logger LOG = LoggerFactory.getLogger(Aplicacao.class);

    /** Endereço do servidor de desenvolvimento do Vite. */
    private static final String ENDERECO_VITE = "http://localhost:5173";

    private Aplicacao() {
    }

    /**
     * Inicia o servidor e abre o navegador.
     *
     * @param args argumentos de linha de comando; não utilizados
     */
    public static void main(String[] args) {
        ServidorWeb servidor = new ServidorWeb(criarGerenciador());
        int porta;
        try {
            porta = servidor.iniciar(ServidorWeb.PORTA_PADRAO);
        } catch (JavalinBindException e) {
            LOG.error("A porta {} já está em uso. O CANTEIRO já está aberto em outra janela?",
                    ServidorWeb.PORTA_PADRAO);
            System.exit(1);
            return;
        }
        Runtime.getRuntime().addShutdownHook(new Thread(servidor::parar));

        URI endereco = URI.create("http://" + ServidorWeb.HOST + ":" + porta);
        LOG.info("CANTEIRO rodando em {}", endereco);
        if (servidor.temFrontendEmbutido()) {
            abrirNavegador(endereco);
        } else {
            LOG.info("Frontend não embutido (modo de desenvolvimento). Suba o Vite e abra {}", ENDERECO_VITE);
        }
    }

    /**
     * Monta o gerenciador de partidas. A fonte de peças ainda é a provisória;
     * quando o gerador por sacola (#12) ficar pronto, é só trocar aqui.
     */
    private static GerenciadorPartidas criarGerenciador() {
        CatalogoMateriais catalogo = CatalogoMateriais.padrao();
        return new GerenciadorPartidas(dificuldade ->
                new FonteSimples(System.nanoTime(), catalogo, dificuldade.materiaisLiberados()));
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
