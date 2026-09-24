package canteiro.api.dto;

import canteiro.modelo.Bloco;
import canteiro.modelo.Celula;
import canteiro.modelo.MotorJogo;
import canteiro.modelo.Placar;
import canteiro.modelo.constantes.Dimensoes;
import canteiro.modelo.constantes.Tempo;
import canteiro.modelo.fisica.Estabilidade;
import canteiro.modelo.pecas.Peca;

import java.util.ArrayList;
import java.util.List;

/**
 * O estado completo da partida, enviado pelo WebSocket a cada mudança.
 *
 * <p>Cada mensagem traz tudo: se uma se perder, a próxima corrige, e o
 * frontend nunca precisa juntar pedaços.</p>
 *
 * @param tipo         sempre {@code "ESTADO"}
 * @param ciclo        número do ciclo
 * @param estado       nome do {@code EstadoPartida}
 * @param tabuleiro    grade de 22 × 10 com o código do material de cada bloco, ou {@code null} onde está vazio
 * @param pecaAtual    peça caindo, ou {@code null}
 * @param pecaFantasma onde a peça atual vai pousar, como pares [linha, coluna]
 * @param proximas     as três próximas peças, com as células da forma relativas ao quadrado dela
 * @param placar       pontuação, linhas, nível, colapsos e tempo
 * @param estabilidade índice, desvio, limite, centro de massa, eixo e alerta
 * @author Mateus Vitor Ferreira
 * @version 0.1.0
 */
public record EstadoDto(String tipo, long ciclo, String estado, List<List<String>> tabuleiro, PecaDto pecaAtual,
                        List<List<Integer>> pecaFantasma, List<PecaDto> proximas, PlacarDto placar,
                        EstabilidadeDto estabilidade) {

    /** Valor de {@code tipo}. */
    public static final String TIPO = "ESTADO";

    /** Quantas próximas peças mostrar (RF05). */
    public static final int PROXIMAS = 3;

    /**
     * Converte o motor no formato do protocolo.
     *
     * @param motor motor da partida
     * @return o estado completo
     */
    public static EstadoDto de(MotorJogo motor) {
        Peca atual = motor.pecaAtual();
        return new EstadoDto(TIPO, motor.ciclo(), motor.estado().name(), grade(motor),
                atual == null ? null : PecaDto.de(atual, motor.celulasPecaAtual()),
                pares(motor.celulasFantasma()),
                motor.proximas(PROXIMAS).stream().map(p -> PecaDto.de(p, p.celulas())).toList(),
                PlacarDto.de(motor.placar(), motor.ciclo()), EstabilidadeDto.de(motor.estabilidade()));
    }

    private static List<List<String>> grade(MotorJogo motor) {
        List<List<String>> linhas = new ArrayList<>(Dimensoes.LINHAS);
        for (int linha = 0; linha < Dimensoes.LINHAS; linha++) {
            List<String> colunas = new ArrayList<>(Dimensoes.COLUNAS);
            for (int coluna = 0; coluna < Dimensoes.COLUNAS; coluna++) {
                Bloco bloco = motor.tabuleiro().bloco(linha, coluna);
                colunas.add(bloco == null ? null : bloco.material().codigo());
            }
            linhas.add(colunas);
        }
        return linhas;
    }

    static List<List<Integer>> pares(List<Celula> celulas) {
        return celulas.stream().map(c -> List.of(c.linha(), c.coluna())).toList();
    }

    /**
     * Uma peça: forma, material e, para a peça atual, as células que ocupa.
     *
     * @param forma    letra da forma
     * @param material código do material
     * @param blocos   células como pares [linha, coluna]: no tabuleiro, para a peça atual; no
     *                 quadrado da forma, para as próximas
     */
    public record PecaDto(String forma, String material, List<List<Integer>> blocos) {

        static PecaDto de(Peca peca, List<Celula> celulas) {
            return new PecaDto(String.valueOf(peca.letra()), peca.material().codigo(), pares(celulas));
        }
    }

    /**
     * O placar.
     *
     * @param pontuacao     pontos
     * @param linhas        linhas eliminadas
     * @param nivel         nível atual
     * @param colapsos      quantos colapsos houve
     * @param tempoSegundos tempo de jogo, sem contar as pausas
     */
    public record PlacarDto(int pontuacao, int linhas, int nivel, int colapsos, long tempoSegundos) {

        static PlacarDto de(Placar placar, long ciclo) {
            return new PlacarDto(placar.pontuacao(), placar.linhas(), placar.nivel(), placar.colapsos(),
                    ciclo / Tempo.CICLOS_POR_SEGUNDO);
        }
    }

    /**
     * A estabilidade da estrutura.
     *
     * @param indice        de 1 a 0
     * @param desvio        em colunas
     * @param limite        em colunas
     * @param centroDeMassa em colunas a partir da borda esquerda
     * @param eixo          meio da base de apoio, em colunas
     * @param alerta        se está perto de desabar
     */
    public record EstabilidadeDto(double indice, double desvio, double limite, double centroDeMassa, double eixo,
                                  boolean alerta) {

        static EstabilidadeDto de(Estabilidade e) {
            return new EstabilidadeDto(e.indice(), e.desvio(), e.limite(), e.centroDeMassa(), e.eixo(), e.alerta());
        }
    }
}
