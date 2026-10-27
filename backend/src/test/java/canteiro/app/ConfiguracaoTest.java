package canteiro.app;

import canteiro.api.ServidorWeb;
import canteiro.controle.GerenciadorPartidas;
import canteiro.persistencia.PastaDoJogo;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * A configuração lida das variáveis de ambiente.
 *
 * @author Mateus Vitor Ferreira
 * @version 0.1.0
 */
class ConfiguracaoTest {

    @Test
    void semVariaveisValeOJogoLocal() {
        Configuracao config = Configuracao.doAmbiente(Map.of());

        assertEquals(ServidorWeb.HOST, config.host());
        assertEquals(ServidorWeb.PORTA_PADRAO, config.porta());
        assertTrue(config.abrirNavegador());
        assertEquals(GerenciadorPartidas.SEM_LIMITE, config.limitePartidas());
        assertEquals(PastaDoJogo.padrao(), config.pasta());
    }

    @Test
    void leAsVariaveisDaHospedagem() {
        Configuracao config = Configuracao.doAmbiente(Map.of(
                Configuracao.VAR_HOST, "0.0.0.0",
                Configuracao.VAR_PORTA, "10000",
                Configuracao.VAR_ABRIR_NAVEGADOR, "false",
                Configuracao.VAR_LIMITE_PARTIDAS, "8",
                Configuracao.VAR_PASTA, "/app/dados"));

        assertEquals("0.0.0.0", config.host());
        assertEquals(10000, config.porta());
        assertFalse(config.abrirNavegador());
        assertEquals(8, config.limitePartidas());
        assertEquals(Path.of("/app/dados"), config.pasta());
    }

    @Test
    void valorInvalidoCaiNoPadrao() {
        Configuracao config = Configuracao.doAmbiente(Map.of(
                Configuracao.VAR_HOST, "  ",
                Configuracao.VAR_PORTA, "setenta",
                Configuracao.VAR_LIMITE_PARTIDAS, "0"));

        assertEquals(ServidorWeb.HOST, config.host());
        assertEquals(ServidorWeb.PORTA_PADRAO, config.porta());
        assertEquals(GerenciadorPartidas.SEM_LIMITE, config.limitePartidas());
    }

    @Test
    void recusaPortaForaDaFaixa() {
        assertThrows(IllegalArgumentException.class, () -> new Configuracao("127.0.0.1", 70_000, true, 1, Path.of("x")));
        assertThrows(IllegalArgumentException.class, () -> new Configuracao("127.0.0.1", 7070, true, 0, Path.of("x")));
    }
}
