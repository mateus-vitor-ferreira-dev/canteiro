package canteiro.modelo;

import canteiro.modelo.constantes.Dimensoes;
import canteiro.modelo.constantes.Tempo;
import canteiro.modelo.estruturas.FontePecas;
import canteiro.modelo.materiais.Material;
import canteiro.modelo.pecas.Peca;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Coordena a partida: gera a peça, faz ela cair, aplica os comandos, fixa
 * quando ela bate embaixo, elimina as linhas completas e soma os pontos.
 *
 * <p>O motor <strong>não implementa regra, só delega</strong>: pergunta ao
 * {@link Tabuleiro} se há colisão, pede a ele que fixe e elimine linhas, e
 * pede à {@link FontePecas} a próxima peça. A partida é uma máquina de
 * estados ({@link EstadoPartida}) e avança em ciclos, não no relógio: a mesma
 * sequência de comandos sempre dá a mesma partida.</p>
 *
 * <p>Não é seguro para várias threads: quem roda o laço (a sessão) garante
 * que só uma thread chama o motor.</p>
 *
 * @author Mateus Vitor Ferreira
 * @version 0.1.0
 */
public final class MotorJogo {

    /** Linha em que a peça nasce: a primeira das linhas ocultas. */
    static final int LINHA_NASCIMENTO = 0;

    private final Tabuleiro tabuleiro = new Tabuleiro();
    private final FontePecas fonte;
    private final Placar placar;
    private int ciclosPorQueda;
    private final List<ObservadorPartida> observadores = new ArrayList<>();

    private EstadoPartida estado = EstadoPartida.GERANDO_PECA;
    private Peca pecaAtual;
    private int linha;
    private int coluna;
    private long ciclo;
    private int ciclosDesdeQueda;
    private boolean mudou;

    /**
     * Cria a partida, ainda sem peça: a primeira nasce no primeiro ciclo.
     *
     * @param dificuldade dificuldade escolhida, que define o intervalo de queda
     * @param fonte       de onde vêm as peças
     * @throws NullPointerException se algum argumento for nulo
     */
    public MotorJogo(Dificuldade dificuldade, FontePecas fonte) {
        this.fonte = Objects.requireNonNull(fonte, "fonte de peças");
        this.placar = new Placar(Objects.requireNonNull(dificuldade, "dificuldade").nivelInicial());
        this.ciclosPorQueda = Tempo.ciclos(Progressao.intervaloQuedaMs(placar.nivel()));
    }

    /**
     * Avança um ciclo: gera a peça se for a vez, e desce a atual quando o
     * intervalo de queda se esgota. Pausada ou encerrada, a partida não anda.
     */
    public void avancarCiclo() {
        if (estado == EstadoPartida.PAUSA || estado.encerrada()) {
            return;
        }
        ciclo++;
        if (estado == EstadoPartida.GERANDO_PECA) {
            gerarPeca();
        } else if (++ciclosDesdeQueda >= ciclosPorQueda) {
            descerOuFixar();
        }
        avisarSeMudou();
    }

    /**
     * Aplica um comando do jogador. Comandos que não cabem no estado atual,
     * ou que fariam a peça colidir, são ignorados.
     *
     * @param comando comando recebido
     */
    public void aplicar(Comando comando) {
        switch (comando) {
            case PAUSAR -> mudarEstadoSe(EstadoPartida.PECA_CAINDO, EstadoPartida.PAUSA);
            case RETOMAR -> mudarEstadoSe(EstadoPartida.PAUSA, EstadoPartida.PECA_CAINDO);
            default -> {
                if (estado == EstadoPartida.PECA_CAINDO) {
                    aplicarNaPeca(comando);
                }
            }
        }
        avisarSeMudou();
    }

    private void aplicarNaPeca(Comando comando) {
        switch (comando) {
            case ESQUERDA -> mover(0, -1);
            case DIREITA -> mover(0, 1);
            case DESCER -> descerOuFixar();
            case QUEDA_INSTANTANEA -> quedaInstantanea();
            case GIRAR_HORARIO -> girar(true);
            case GIRAR_ANTI_HORARIO -> girar(false);
            default -> {
                // RESERVAR e DESFAZER dependem da reserva e do histórico (#13, #14, #26)
            }
        }
    }

    private void gerarPeca() {
        pecaAtual = fonte.proxima();
        linha = LINHA_NASCIMENTO;
        coluna = (Dimensoes.COLUNAS - pecaAtual.tamanho()) / 2;
        ciclosDesdeQueda = 0;
        mudou = true;
        if (tabuleiro.colide(pecaAtual, linha, coluna)) {
            estado = EstadoPartida.FIM_DE_JOGO;
            emitir(EventoPartida.de(EventoPartida.Tipo.FIM_DE_JOGO));
        } else {
            estado = EstadoPartida.PECA_CAINDO;
        }
    }

    private boolean mover(int linhas, int colunas) {
        if (tabuleiro.colide(pecaAtual, linha + linhas, coluna + colunas)) {
            return false;
        }
        linha += linhas;
        coluna += colunas;
        mudou = true;
        return true;
    }

    /**
     * Gira a peça. Se o giro simples colidir, tenta os deslocamentos
     * corretivos da peça, em ordem, e aplica o primeiro que couber (RF08).
     * Se nenhum couber, desfaz o giro.
     */
    private void girar(boolean horario) {
        girarPeca(horario);
        if (!tabuleiro.colide(pecaAtual, linha, coluna)) {
            mudou = true;
            return;
        }
        for (int[] deslocamento : pecaAtual.deslocamentosCorretivos()) {
            if (mover(deslocamento[0], deslocamento[1])) {
                return;
            }
        }
        girarPeca(!horario);
    }

    private void girarPeca(boolean horario) {
        if (horario) {
            pecaAtual.girarHorario();
        } else {
            pecaAtual.girarAntiHorario();
        }
    }

    private void descerOuFixar() {
        ciclosDesdeQueda = 0;
        if (!mover(1, 0)) {
            assentar();
        }
    }

    private void quedaInstantanea() {
        linha = linhaDePouso();
        assentar();
    }

    private int linhaDePouso() {
        int pouso = linha;
        while (!tabuleiro.colide(pecaAtual, pouso + 1, coluna)) {
            pouso++;
        }
        return pouso;
    }

    private void assentar() {
        estado = EstadoPartida.FIXANDO;
        tabuleiro.fixar(pecaAtual, linha, coluna);
        emitir(EventoPartida.de(EventoPartida.Tipo.PECA_FIXADA));
        eliminarLinhas();
        pecaAtual = null;
        estado = EstadoPartida.GERANDO_PECA;
        gerarPeca();
    }

    private void eliminarLinhas() {
        List<Integer> completas = tabuleiro.linhasCompletas();
        if (completas.isEmpty()) {
            return;
        }
        estado = EstadoPartida.ELIMINANDO_LINHAS;
        Material predominante = Pontuacao.predominante(tabuleiro.materiaisDasLinhas(completas));
        tabuleiro.eliminarLinhasCompletas();
        emitir(new EventoPartida(EventoPartida.Tipo.LINHAS_ELIMINADAS, completas));
        if (placar.registrarLinhas(completas.size(), predominante)) {
            ciclosPorQueda = Tempo.ciclos(Progressao.intervaloQuedaMs(placar.nivel()));
            emitir(EventoPartida.de(EventoPartida.Tipo.NIVEL_SUBIU));
        }
    }

    private void mudarEstadoSe(EstadoPartida esperado, EstadoPartida novo) {
        if (estado == esperado) {
            estado = novo;
            mudou = true;
        }
    }

    private void emitir(EventoPartida evento) {
        for (ObservadorPartida observador : observadores) {
            observador.eventoOcorreu(evento);
        }
    }

    private void avisarSeMudou() {
        if (!mudou) {
            return;
        }
        mudou = false;
        for (ObservadorPartida observador : observadores) {
            observador.estadoMudou(this);
        }
    }

    /**
     * Inscreve quem quer ser avisado das mudanças e dos eventos.
     *
     * @param observador quem vai ouvir
     */
    public void inscrever(ObservadorPartida observador) {
        observadores.add(Objects.requireNonNull(observador, "observador"));
    }

    /**
     * Posições que a peça atual ocupa no tabuleiro.
     *
     * @return as quatro células, ou lista vazia se não houver peça caindo
     */
    public List<Celula> celulasPecaAtual() {
        return pecaAtual == null ? List.of() : posicoes(linha);
    }

    /**
     * Onde a peça atual pousaria com uma queda instantânea: a "peça fantasma"
     * que ajuda o jogador a mirar.
     *
     * @return as quatro células do pouso, ou lista vazia se não houver peça caindo
     */
    public List<Celula> celulasFantasma() {
        return pecaAtual == null ? List.of() : posicoes(linhaDePouso());
    }

    private List<Celula> posicoes(int linhaDaPeca) {
        return pecaAtual.celulas().stream().map(c -> c.deslocada(linhaDaPeca, coluna)).toList();
    }

    /**
     * Devolve as próximas peças, sem tirá-las da fila (RF05).
     *
     * @param quantidade quantas mostrar
     * @return as próximas peças
     */
    public List<Peca> proximas(int quantidade) {
        return fonte.espiar(quantidade);
    }

    /**
     * Devolve o estado atual da partida.
     *
     * @return o estado
     */
    public EstadoPartida estado() {
        return estado;
    }

    /**
     * Devolve a peça que está caindo.
     *
     * @return a peça, ou {@code null} se nenhuma estiver caindo
     */
    public Peca pecaAtual() {
        return pecaAtual;
    }

    /**
     * Devolve o tabuleiro, para leitura.
     *
     * @return o tabuleiro da partida
     */
    public Tabuleiro tabuleiro() {
        return tabuleiro;
    }

    /**
     * Quantos ciclos a partida já rodou, sem contar os pausados.
     *
     * @return número do ciclo atual
     */
    public long ciclo() {
        return ciclo;
    }

    /**
     * Total de linhas eliminadas na partida.
     *
     * @return quantidade de linhas
     */
    public int linhasEliminadas() {
        return placar.linhas();
    }

    /**
     * Devolve o placar: pontuação, linhas e nível.
     *
     * @return o placar da partida
     */
    public Placar placar() {
        return placar;
    }

    /**
     * Quantos ciclos a peça leva para descer uma linha sozinha.
     *
     * @return intervalo de queda, em ciclos
     */
    public int ciclosPorQueda() {
        return ciclosPorQueda;
    }
}
