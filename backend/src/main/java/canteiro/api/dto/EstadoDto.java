package canteiro.api.dto;

import canteiro.modelo.Bloco;
import canteiro.modelo.Celula;
import canteiro.modelo.MotorJogo;
import canteiro.modelo.Placar;
import canteiro.modelo.Previa;
import canteiro.modelo.constantes.Dimensoes;
import canteiro.modelo.constantes.Tempo;
import canteiro.modelo.fisica.Estabilidade;
import canteiro.modelo.metas.Meta;
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
 * @param rachaduras   os blocos rachando sob o peso de cima, como trincas [linha, coluna, nível de 1 a 3] (RN21)
 * @param pecaAtual    peça caindo, ou {@code null}
 * @param pecaFantasma onde a peça atual vai pousar, como pares [linha, coluna]
 * @param proximas     as três próximas peças, com as células da forma relativas ao quadrado dela
 * @param reservada    peça guardada na reserva, com as células da forma, ou {@code null} se não há
 * @param podeReservar se a troca com a reserva está liberada nesta peça
 * @param placar       pontuação, linhas, nível, colapsos e tempo
 * @param estabilidade índice, desvio, limite, centro de massa, eixo e alerta
 * @param meta         a meta do nível, com o progresso, ou {@code null} se não houver
 * @author Mateus Vitor Ferreira
 * @version 0.1.0
 */
public record EstadoDto(String tipo, long ciclo, String estado, List<List<String>> tabuleiro,
                        List<List<Integer>> rachaduras, PecaDto pecaAtual,
                        List<List<Integer>> pecaFantasma, List<PecaDto> proximas, PecaDto reservada,
                        boolean podeReservar, PlacarDto placar, EstabilidadeDto estabilidade, MetaDto meta) {

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
        Peca reservada = motor.pecaReservada();
        return new EstadoDto(TIPO, motor.ciclo(), motor.estado().name(), grade(motor), rachaduras(motor),
                atual == null ? null : PecaDto.de(atual, motor.celulasPecaAtual()),
                pares(motor.celulasFantasma()),
                motor.proximas(PROXIMAS).stream().map(p -> PecaDto.de(p, p.celulas())).toList(),
                reservada == null ? null : PecaDto.de(reservada, reservada.celulas()), motor.podeReservar(),
                PlacarDto.de(motor.placar(), motor.ciclo()),
                EstabilidadeDto.de(motor.estabilidade(), motor.previa()),
                motor.meta() == null ? null : MetaDto.de(motor.meta()));
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

    private static List<List<Integer>> rachaduras(MotorJogo motor) {
        return motor.rachaduras().stream()
                .map(r -> List.of(r.celula().linha(), r.celula().coluna(), r.nivel())).toList();
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
     * @param sequencia     quantas peças seguidas fecharam linha (RN18)
     */
    public record PlacarDto(int pontuacao, int linhas, int nivel, int colapsos, long tempoSegundos, int sequencia) {

        static PlacarDto de(Placar placar, long ciclo) {
            return new PlacarDto(placar.pontuacao(), placar.linhas(), placar.nivel(), placar.colapsos(),
                    ciclo / Tempo.CICLOS_POR_SEGUNDO, placar.sequencia());
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
     * @param previa        como a estrutura ficaria com a peça atual pousada na posição da peça
     *                      fantasma, ou {@code null} se não há peça caindo
     */
    public record EstabilidadeDto(double indice, double desvio, double limite, double centroDeMassa, double eixo,
                                  boolean alerta, PreviaDto previa) {

        static EstabilidadeDto de(Estabilidade e, Previa previa) {
            return new EstabilidadeDto(e.indice(), e.desvio(), e.limite(), e.centroDeMassa(), e.eixo(), e.alerta(),
                    previa == null ? null : PreviaDto.de(previa));
        }
    }

    /**
     * A estabilidade prevista para a jogada da peça atual.
     *
     * @param indice        de 1 a 0
     * @param desvio        em colunas
     * @param centroDeMassa em colunas a partir da borda esquerda
     * @param eixo          meio da base de apoio, em colunas
     * @param derruba       se a jogada passaria do limite e causaria colapso
     * @param quebras       os blocos da peça que se soltariam do apoio, como pares [linha, coluna] (RN20)
     * @param esmagados     os blocos que seriam esmagados, como pares [linha, coluna] (RN21)
     */
    public record PreviaDto(double indice, double desvio, double centroDeMassa, double eixo, boolean derruba,
                            List<List<Integer>> quebras, List<List<Integer>> esmagados) {

        static PreviaDto de(Previa previa) {
            Estabilidade e = previa.estabilidade();
            return new PreviaDto(e.indice(), e.desvio(), e.centroDeMassa(), e.eixo(), e.passouDoLimite(),
                    pares(previa.quebras()), pares(previa.esmagados()));
        }
    }

    /**
     * A meta do nível (RN19).
     *
     * @param descricao o que fazer, numa frase
     * @param progresso quanto já foi feito
     * @param alvo      quanto é preciso
     * @param cumprida  se a meta já foi cumprida neste nível
     */
    public record MetaDto(String descricao, int progresso, int alvo, boolean cumprida) {

        static MetaDto de(Meta meta) {
            return new MetaDto(meta.descricao(), meta.progresso(), meta.alvo(), meta.cumprida());
        }
    }
}
