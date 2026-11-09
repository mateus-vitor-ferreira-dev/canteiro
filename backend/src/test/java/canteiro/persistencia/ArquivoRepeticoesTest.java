package canteiro.persistencia;

import canteiro.modelo.Comando;
import canteiro.modelo.Dificuldade;
import canteiro.modelo.estruturas.Jogada;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Grava e lê as partidas gravadas numa pasta temporária (RF24, RNF11, RNF12).
 *
 * @author Mateus Vitor Ferreira
 * @version 0.1.0
 */
class ArquivoRepeticoesTest {

    private static final LocalDateTime FIM = LocalDateTime.of(2026, 9, 28, 14, 32, 5, 123_000_000);
    private static final long SEMENTE = -4_962_768_465_676_381_896L;
    private static final List<Jogada> JOGADAS = List.of(
            new Jogada(12, Comando.ESQUERDA), new Jogada(12, Comando.GIRAR_HORARIO),
            new Jogada(30, Comando.QUEDA_INSTANTANEA));

    @TempDir
    Path pasta;

    @Test
    void gravaELeAMesmaPartida() {
        ArquivoRepeticoes arquivo = new ArquivoRepeticoes(pasta);

        String id = arquivo.gravar(Dificuldade.DIFICIL, SEMENTE, 12_340, JOGADAS, FIM);
        Repeticao lida = arquivo.ler(id).orElseThrow();

        assertEquals("2026-09-28_14-32-05", id);
        assertEquals(LocalDateTime.of(2026, 9, 28, 14, 32, 5), lida.data());
        assertEquals(Dificuldade.DIFICIL, lida.dificuldade());
        assertEquals(SEMENTE, lida.semente());
        assertEquals(12_340, lida.pontuacao());
        assertEquals(JOGADAS, lida.jogadas());
    }

    @Test
    void semPastaAListaEVazia() {
        assertTrue(new ArquivoRepeticoes(pasta).listar().isEmpty());
    }

    @Test
    void listaDaMaisRecenteParaAMaisAntigaSoComOCabecalho() {
        ArquivoRepeticoes arquivo = new ArquivoRepeticoes(pasta);
        arquivo.gravar(Dificuldade.FACIL, 1, 10, JOGADAS, FIM);
        arquivo.gravar(Dificuldade.NORMAL, 2, 20, JOGADAS, FIM.plusDays(1));

        List<Repeticao> lista = arquivo.listar();

        assertEquals(2, lista.size());
        assertEquals(Dificuldade.NORMAL, lista.get(0).dificuldade());
        assertEquals(20, lista.get(0).pontuacao());
        assertTrue(lista.get(0).jogadas().isEmpty(), "a lista não lê as jogadas");
    }

    @Test
    void duasPartidasNoMesmoSegundoNaoSeSobrescrevem() {
        ArquivoRepeticoes arquivo = new ArquivoRepeticoes(pasta);

        String primeira = arquivo.gravar(Dificuldade.FACIL, 1, 10, JOGADAS, FIM);
        String segunda = arquivo.gravar(Dificuldade.FACIL, 2, 20, JOGADAS, FIM);

        assertEquals("2026-09-28_14-32-05-2", segunda);
        assertEquals(10, arquivo.ler(primeira).orElseThrow().pontuacao());
        assertEquals(20, arquivo.ler(segunda).orElseThrow().pontuacao());
        assertEquals(2, arquivo.listar().size());
    }

    @Test
    void guardaSoAsMaisRecentes() {
        ArquivoRepeticoes arquivo = new ArquivoRepeticoes(pasta);
        String maisAntiga = arquivo.gravar(Dificuldade.FACIL, 0, 0, List.of(), FIM);
        for (int i = 1; i <= ArquivoRepeticoes.MAXIMO; i++) {
            arquivo.gravar(Dificuldade.FACIL, i, i, List.of(), FIM.plusMinutes(i));
        }

        assertEquals(ArquivoRepeticoes.MAXIMO, arquivo.listar().size());
        assertEquals(Optional.empty(), arquivo.ler(maisAntiga));
    }

    @Test
    void idForaDoFormatoNaoSaiDaPasta() throws IOException {
        Files.writeString(pasta.resolve("ranking.txt"), "não é uma repetição");
        ArquivoRepeticoes arquivo = new ArquivoRepeticoes(pasta);

        assertEquals(Optional.empty(), arquivo.ler("../ranking"));
        assertEquals(Optional.empty(), arquivo.ler("qualquer-coisa"));
        assertEquals(Optional.empty(), arquivo.ler(null));
        assertEquals(Optional.empty(), arquivo.ler("2026-01-01_00-00-00"));
    }

    @Test
    void arquivoEstragadoSaiDaListaEDaErroAoLer() throws IOException {
        ArquivoRepeticoes arquivo = new ArquivoRepeticoes(pasta);
        String boa = arquivo.gravar(Dificuldade.FACIL, 1, 10, JOGADAS, FIM);
        Path pastaRepeticoes = pasta.resolve(ArquivoRepeticoes.PASTA);
        Files.writeString(pastaRepeticoes.resolve("2026-01-01_00-00-00.txt"), "lixo\n");
        Files.writeString(pastaRepeticoes.resolve("2026-01-02_00-00-00.txt"),
                "CANTEIRO-REPETICAO 1\nNORMAL;1;10;2026-01-02T00:00\n12;VOAR\n");

        List<Repeticao> lista = arquivo.listar();

        assertEquals(2, lista.size(), "a de cabeçalho bom entra; a sem cabeçalho fica de fora");
        assertFalse(lista.stream().anyMatch(r -> r.id().equals("2026-01-01_00-00-00")));
        assertThrows(RepeticaoCorrompidaException.class, () -> arquivo.ler("2026-01-01_00-00-00"));
        assertThrows(RepeticaoCorrompidaException.class, () -> arquivo.ler("2026-01-02_00-00-00"));
        assertTrue(arquivo.ler(boa).isPresent());
    }
}
