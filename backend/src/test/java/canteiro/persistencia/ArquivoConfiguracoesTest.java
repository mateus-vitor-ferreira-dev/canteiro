package canteiro.persistencia;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * As teclas e o volume do jogador no arquivo (RF27), sem nunca impedir o jogo
 * de abrir (RNF11, RNF12).
 *
 * @author Mateus Vitor Ferreira
 * @version 0.1.0
 */
class ArquivoConfiguracoesTest {

    private static final int VOLUME_SALVO = 35;

    @TempDir
    Path pasta;

    @Test
    void semArquivoDevolveOPadrao() {
        assertEquals(ConfiguracoesSalvas.padrao(), new ArquivoConfiguracoes(pasta).ler());
    }

    @Test
    void oQueFoiSalvoVoltaIgualNaLeitura() {
        ConfiguracoesSalvas salvas = new ConfiguracoesSalvas(
                Map.of("ESQUERDA", List.of("KeyA"), "PAUSA", List.of("KeyP", "Escape")), VOLUME_SALVO);

        new ArquivoConfiguracoes(pasta).salvar(salvas);

        assertEquals(salvas, new ArquivoConfiguracoes(pasta).ler());
    }

    @Test
    void salvarCriaAPastaDoJogoSePreciso() {
        Path naoExiste = pasta.resolve("canteiro").resolve("dados");

        new ArquivoConfiguracoes(naoExiste).salvar(ConfiguracoesSalvas.padrao());

        assertTrue(Files.isRegularFile(naoExiste.resolve(ArquivoConfiguracoes.ARQUIVO)));
    }

    @Test
    void salvarNaoDeixaArquivoTemporarioParaTras() throws IOException {
        new ArquivoConfiguracoes(pasta).salvar(ConfiguracoesSalvas.padrao());

        try (var arquivos = Files.list(pasta)) {
            assertEquals(List.of(pasta.resolve(ArquivoConfiguracoes.ARQUIVO)), arquivos.toList());
        }
    }

    @Test
    void volumeInvalidoVoltaParaOPadraoETeclaVaziaEhIgnorada() {
        ArquivosDeTeste.copiar("configuracoes-volume-invalido.properties", pasta, ArquivoConfiguracoes.ARQUIVO);

        ConfiguracoesSalvas lidas = new ArquivoConfiguracoes(pasta).ler();

        assertEquals(ConfiguracoesSalvas.VOLUME_PADRAO, lidas.volume());
        assertEquals(Map.of("PAUSA", List.of("KeyP", "Escape")), lidas.teclas());
    }

    @Test
    void arquivoIlegivelDevolveOPadrao() {
        ArquivosDeTeste.copiar("materiais-unicode-invalido.properties", pasta, ArquivoConfiguracoes.ARQUIVO);

        assertEquals(ConfiguracoesSalvas.padrao(), new ArquivoConfiguracoes(pasta).ler());
    }

    @Test
    void falhaAoGravarViraExcecaoNaoVerificada() throws IOException {
        Path ocupadoPorArquivo = Files.writeString(pasta.resolve("nao-e-pasta"), "x");

        ArquivoConfiguracoes arquivo = new ArquivoConfiguracoes(ocupadoPorArquivo);

        assertThrows(UncheckedIOException.class, () -> arquivo.salvar(ConfiguracoesSalvas.padrao()));
        assertEquals(ConfiguracoesSalvas.padrao(), arquivo.ler());
    }
}
