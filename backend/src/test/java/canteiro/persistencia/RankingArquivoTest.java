package canteiro.persistencia;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * O ranking em arquivo: persiste, guarda a melhor partida de cada jogador e
 * sobrevive a linhas estragadas (RF21, RF22, RNF11, RNF12).
 *
 * @author Mateus Vitor Ferreira
 * @version 0.1.0
 */
class RankingArquivoTest {

    private static final LocalDate HOJE = LocalDate.of(2026, 10, 22);
    private static final int JOGADORES_A_MAIS = 5;

    @TempDir
    Path pasta;

    @Test
    void semArquivoORankingEstaVazio() {
        assertTrue(new RankingArquivo(pasta).dezMelhores().isEmpty());
    }

    @Test
    void oQueFoiRegistradoContinuaDepoisDeReabrir() {
        EntradaRanking ana = entrada("Ana", 5000);
        new RankingArquivo(pasta).registrar(ana);

        assertEquals(List.of(ana), new RankingArquivo(pasta).dezMelhores());
    }

    @Test
    void devolveSoAsDezMelhoresDaMaiorParaAMenor() {
        RankingArquivo ranking = new RankingArquivo(pasta);
        for (int i = 1; i <= RankingArquivo.TAMANHO + JOGADORES_A_MAIS; i++) {
            ranking.registrar(entrada("Jogador " + i, i * 100));
        }

        List<EntradaRanking> dez = ranking.dezMelhores();

        assertEquals(RankingArquivo.TAMANHO, dez.size());
        assertEquals(1500, dez.get(0).pontuacao());
        assertEquals(600, dez.get(RankingArquivo.TAMANHO - 1).pontuacao());
    }

    @Test
    void nomeRepetidoFicaComAMaiorPontuacao() {
        RankingArquivo ranking = new RankingArquivo(pasta);
        ranking.registrar(entrada("Ana", 3000));
        ranking.registrar(entrada("Ana", 5000));
        ranking.registrar(entrada("Ana", 1000));

        assertEquals(List.of(entrada("Ana", 5000)), new RankingArquivo(pasta).dezMelhores());
    }

    @Test
    void devolveAPosicaoDoJogador() {
        RankingArquivo ranking = new RankingArquivo(pasta);
        assertEquals(1, ranking.registrar(entrada("Ana", 3000)));
        assertEquals(1, ranking.registrar(entrada("Bruno", 5000)));
        assertEquals(3, ranking.registrar(entrada("Carla", 1000)));
        assertEquals(2, ranking.registrar(entrada("Ana", 2000)), "a melhor partida da Ana continua a de 3000");
    }

    @Test
    void linhasEstragadasSaoIgnoradasEAsOutrasValem() {
        ArquivosDeTeste.copiar("ranking-com-linhas-estragadas.csv", pasta, RankingArquivo.ARQUIVO);

        List<String> nomes = new RankingArquivo(pasta).dezMelhores().stream().map(EntradaRanking::nome).toList();

        assertEquals(List.of("Ana", "Elisa"), nomes);
        assertEquals(5000, new RankingArquivo(pasta).dezMelhores().get(0).pontuacao(), "fica a maior da Ana");
    }

    @Test
    void arquivoIlegivelViraRankingVazio() throws IOException {
        Files.write(pasta.resolve(RankingArquivo.ARQUIVO), new byte[] {(byte) 0xFF, (byte) 0xFE, '\n'});

        assertTrue(new RankingArquivo(pasta).dezMelhores().isEmpty());
    }

    @Test
    void seNaoDaParaGravarAMemoriaNaoMuda() throws IOException {
        Path ocupadoPorArquivo = Files.writeString(pasta.resolve("nao-e-pasta"), "x");
        RankingArquivo ranking = new RankingArquivo(ocupadoPorArquivo);

        assertThrows(UncheckedIOException.class, () -> ranking.registrar(entrada("Ana", 5000)));
        assertTrue(ranking.dezMelhores().isEmpty());
    }

    private static EntradaRanking entrada(String nome, int pontuacao) {
        return new EntradaRanking(nome, pontuacao, 2, 10, 0, HOJE);
    }
}
