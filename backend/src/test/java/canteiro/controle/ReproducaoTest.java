package canteiro.controle;

import canteiro.modelo.Comando;
import canteiro.modelo.Dificuldade;
import canteiro.modelo.EstadoPartida;
import canteiro.modelo.MotorJogo;
import canteiro.modelo.constantes.Dimensoes;
import canteiro.modelo.estruturas.GeradorPecas;
import canteiro.modelo.materiais.CatalogoMateriais;
import canteiro.persistencia.ArquivoRepeticoes;
import canteiro.persistencia.Repeticao;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Grava uma partida jogada pela sessão, reproduz a gravação e compara (RF23, RF24).
 *
 * @author Mateus Vitor Ferreira
 * @version 0.1.0
 */
class ReproducaoTest {

    private static final long SEMENTE_DOS_COMANDOS = 99;
    private static final int LIMITE_DE_CICLOS = 300_000;
    private static final int UM_COMANDO_A_CADA = 5;
    private static final Comando[] COMANDOS = {Comando.ESQUERDA, Comando.DIREITA, Comando.GIRAR_HORARIO,
        Comando.GIRAR_ANTI_HORARIO, Comando.DESCER, Comando.QUEDA_INSTANTANEA, Comando.RESERVAR};

    @TempDir
    Path pasta;

    private final CatalogoMateriais catalogo = CatalogoMateriais.padrao();
    private final GerenciadorPartidas partidas = new GerenciadorPartidas(
            (dificuldade, semente) -> new GeradorPecas(semente, catalogo, dificuldade.nivelInicial()));

    @Test
    void reproduzirAGravacaoDaAMesmaPontuacaoEOMesmoTabuleiro() {
        ArquivoRepeticoes arquivo = new ArquivoRepeticoes(pasta);
        partidas.gravarRepeticoesEm(arquivo);
        SessaoPartida original = jogarAteOFim(partidas.criar(Dificuldade.DIFICIL));
        List<Repeticao> gravadas = arquivo.listar();
        assertEquals(1, gravadas.size(), "a partida que terminou foi gravada");
        assertEquals(original.motor().placar().pontuacao(), gravadas.get(0).pontuacao());

        SessaoPartida reproducao = partidas.reproduzir(arquivo.ler(gravadas.get(0).id()).orElseThrow());
        for (int i = 0; i < LIMITE_DE_CICLOS && !reproducao.motor().estado().encerrada(); i++) {
            reproducao.ciclo();
        }

        assertTrue(reproducao.reproducao());
        assertEquals(EstadoPartida.FIM_DE_JOGO, reproducao.motor().estado());
        assertEquals(original.motor().placar().pontuacao(), reproducao.motor().placar().pontuacao());
        assertEquals(original.motor().placar().colapsos(), reproducao.motor().placar().colapsos());
        assertEquals(ocupadas(original.motor()), ocupadas(reproducao.motor()));
        assertEquals(1, arquivo.listar().size(), "a reprodução não é gravada de novo");
    }

    @Test
    void naReproducaoOTecladoNaoJogaEPausarSegura() {
        ArquivoRepeticoes arquivo = new ArquivoRepeticoes(pasta);
        partidas.gravarRepeticoesEm(arquivo);
        jogarAteOFim(partidas.criar(Dificuldade.NORMAL));
        SessaoPartida reproducao = partidas.reproduzir(arquivo.ler(arquivo.listar().get(0).id()).orElseThrow());
        reproducao.ciclo();
        List<?> antes = reproducao.motor().celulasPecaAtual();

        reproducao.enfileirar(Comando.ESQUERDA);
        reproducao.enfileirar(Comando.PAUSAR);
        reproducao.ciclo();
        long cicloPausado = reproducao.motor().ciclo();
        reproducao.ciclo();

        assertEquals(EstadoPartida.PAUSA, reproducao.motor().estado());
        assertEquals(cicloPausado, reproducao.motor().ciclo(), "pausada, a gravação espera");
        assertEquals(antes, reproducao.motor().celulasPecaAtual(), "a seta do jogador não move a peça");

        reproducao.enfileirar(Comando.RETOMAR);
        reproducao.ciclo();
        assertEquals(cicloPausado + 1, reproducao.motor().ciclo());
    }

    @Test
    void partidaDeTreinoNaoEGravada() {
        ArquivoRepeticoes arquivo = new ArquivoRepeticoes(pasta);
        partidas.gravarRepeticoesEm(arquivo);

        jogarAteOFim(partidas.criar(Dificuldade.FACIL, true));

        assertTrue(arquivo.listar().isEmpty());
    }

    /** Joga pela sessão, como o laço faria, com comandos sorteados por uma semente fixa. */
    private static SessaoPartida jogarAteOFim(SessaoPartida sessao) {
        Random sorteio = new Random(SEMENTE_DOS_COMANDOS);
        for (int i = 0; i < LIMITE_DE_CICLOS && !sessao.motor().estado().encerrada(); i++) {
            if (sorteio.nextInt(UM_COMANDO_A_CADA) == 0) {
                sessao.enfileirar(COMANDOS[sorteio.nextInt(COMANDOS.length)]);
            }
            sessao.ciclo();
        }
        assertEquals(EstadoPartida.FIM_DE_JOGO, sessao.motor().estado(), "a partida de teste precisa terminar");
        return sessao;
    }

    private static List<String> ocupadas(MotorJogo motor) {
        List<String> ocupadas = new ArrayList<>();
        for (int linha = 0; linha < Dimensoes.LINHAS; linha++) {
            for (int coluna = 0; coluna < Dimensoes.COLUNAS; coluna++) {
                if (motor.tabuleiro().ocupada(linha, coluna)) {
                    ocupadas.add(linha + "," + coluna + "," + motor.tabuleiro().bloco(linha, coluna).material());
                }
            }
        }
        return ocupadas;
    }
}
