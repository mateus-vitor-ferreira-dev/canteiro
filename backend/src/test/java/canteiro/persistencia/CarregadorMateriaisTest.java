package canteiro.persistencia;

import canteiro.modelo.materiais.Aco;
import canteiro.modelo.materiais.Alvenaria;
import canteiro.modelo.materiais.CatalogoMateriais;
import canteiro.modelo.materiais.Concreto;
import canteiro.modelo.materiais.Madeira;
import canteiro.modelo.materiais.Material;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Materiais lidos do arquivo do jogador, com o padrão do JAR no que faltar ou
 * vier estragado (RNF11, RNF12).
 *
 * @author Mateus Vitor Ferreira
 * @version 0.1.0
 */
class CarregadorMateriaisTest {

    private static final double TOLERANCIA = 1e-9;
    private static final int COR_DA_ALVENARIA_NO_ARQUIVO = 0x112233;
    private static final double DENSIDADE_DO_ACO_NO_ARQUIVO = 9.5;

    @TempDir
    Path pasta;

    @Test
    void semArquivoUsaOsValoresPadrao() {
        assertIguaisAoPadrao(CarregadorMateriais.carregar(pasta));
    }

    @Test
    void semAPastaUsaOsValoresPadrao() {
        assertIguaisAoPadrao(CarregadorMateriais.carregar(pasta.resolve("nao-existe")));
    }

    @ParameterizedTest
    @ValueSource(strings = {"materiais-vazio.properties", "materiais-unicode-invalido.properties",
        "materiais-utf8-invalido.properties"})
    void arquivoIlegivelUsaOsValoresPadrao(String arquivo) {
        ArquivosDeTeste.copiar(arquivo, pasta, CarregadorMateriais.ARQUIVO);

        assertIguaisAoPadrao(CarregadorMateriais.carregar(pasta));
    }

    @Test
    void letraNaDensidadeVoltaParaOPadraoSoDaqueleMaterial() {
        ArquivosDeTeste.copiar("materiais-letra-na-densidade.properties", pasta, CarregadorMateriais.ARQUIVO);

        CatalogoMateriais catalogo = CarregadorMateriais.carregar(pasta);

        assertEquals(new Madeira().densidade(), catalogo.buscar(Madeira.CODIGO).densidade(), TOLERANCIA);
        assertEquals(DENSIDADE_DO_ACO_NO_ARQUIVO, catalogo.buscar(Aco.CODIGO).densidade(), TOLERANCIA);
    }

    @Test
    void corInvalidaVoltaParaOPadraoSoDaqueleMaterial() {
        ArquivosDeTeste.copiar("materiais-cor-invalida.properties", pasta, CarregadorMateriais.ARQUIVO);

        CatalogoMateriais catalogo = CarregadorMateriais.carregar(pasta);

        assertEquals(new Concreto().corRgb(), catalogo.buscar(Concreto.CODIGO).corRgb());
        assertEquals(COR_DA_ALVENARIA_NO_ARQUIVO, catalogo.buscar(Alvenaria.CODIGO).corRgb());
    }

    @Test
    void densidadeZeroOuNegativaVoltaParaOPadrao() {
        ArquivosDeTeste.copiar("materiais-densidade-negativa.properties", pasta, CarregadorMateriais.ARQUIVO);

        assertIguaisAoPadrao(CarregadorMateriais.carregar(pasta));
    }

    @Test
    void valoresValidosDoArquivoValem() throws IOException {
        Files.writeString(pasta.resolve(CarregadorMateriais.ARQUIVO), "MADEIRA.densidade=0.9\nMADEIRA.cor=abcdef\n");

        Material madeira = CarregadorMateriais.carregar(pasta).buscar(Madeira.CODIGO);

        assertEquals(0.9, madeira.densidade(), TOLERANCIA);
        assertEquals(0xABCDEF, madeira.corRgb());
    }

    @Test
    void oArquivoPadraoDoJarTemOsMesmosValoresDasClasses() {
        CatalogoMateriais doJar = CarregadorMateriais.montar(
                LeitorPropriedades.comPadrao(pasta.resolve("nenhum"), CarregadorMateriais.RECURSO_PADRAO));

        assertIguaisAoPadrao(doJar);
    }

    private static void assertIguaisAoPadrao(CatalogoMateriais catalogo) {
        for (Material padrao : List.of(new Madeira(), new Alvenaria(), new Concreto(), new Aco())) {
            Material carregado = catalogo.buscar(padrao.codigo());
            assertEquals(padrao.densidade(), carregado.densidade(), TOLERANCIA, padrao.codigo());
            assertEquals(padrao.corRgb(), carregado.corRgb(), padrao.codigo());
        }
        assertEquals(4, catalogo.todos().size());
    }
}
