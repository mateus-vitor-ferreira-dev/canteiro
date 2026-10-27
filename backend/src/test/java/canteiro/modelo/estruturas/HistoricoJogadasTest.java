package canteiro.modelo.estruturas;

import canteiro.modelo.Comando;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Confere o histórico de jogadas em pilha (RF23).
 *
 * @author Mateus Vitor Ferreira
 * @version 0.1.0
 */
class HistoricoJogadasTest {

    private final HistoricoJogadas historico = new HistoricoJogadas();
    private final Jogada primeira = new Jogada(1, Comando.ESQUERDA);
    private final Jogada segunda = new Jogada(5, Comando.GIRAR_HORARIO);
    private final Jogada terceira = new Jogada(9, Comando.QUEDA_INSTANTANEA);

    @Test
    void desfazTirandoAMaisRecentePrimeiro() {
        registrarAsTres();
        assertEquals(Optional.of(terceira), historico.desfazerUltima());
        assertEquals(Optional.of(segunda), historico.desfazerUltima());
        assertEquals(1, historico.tamanho());
        assertEquals(Optional.of(primeira), historico.ultima());
    }

    @Test
    void percorreNaOrdemEmQueAconteceram() {
        registrarAsTres();
        assertEquals(List.of(primeira, segunda, terceira), historico.emOrdem());
        assertEquals(3, historico.tamanho(), "percorrer não tira nada");
    }

    @Test
    void percorrerDevolveUmaCopiaQueNaoMuda() {
        registrarAsTres();
        List<Jogada> jogadas = historico.emOrdem();
        assertThrows(UnsupportedOperationException.class, () -> jogadas.add(primeira));
    }

    @Test
    void historicoVazioNaoQuebraAoDesfazer() {
        assertTrue(historico.vazio());
        assertEquals(Optional.empty(), historico.desfazerUltima());
        assertEquals(Optional.empty(), historico.ultima());
        assertEquals(List.of(), historico.emOrdem());
    }

    @Test
    void jogadaInvalidaEErro() {
        assertThrows(NullPointerException.class, () -> historico.registrar(null));
        assertThrows(NullPointerException.class, () -> new Jogada(0, null));
        assertThrows(IllegalArgumentException.class, () -> new Jogada(-1, Comando.DESCER));
    }

    private void registrarAsTres() {
        historico.registrar(primeira);
        historico.registrar(segunda);
        historico.registrar(terceira);
    }
}
