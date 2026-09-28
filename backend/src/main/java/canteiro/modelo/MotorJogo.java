package canteiro.modelo;

import canteiro.modelo.constantes.Dimensoes;
import canteiro.modelo.constantes.Tempo;
import canteiro.modelo.estruturas.FontePecas;
import canteiro.modelo.estruturas.HistoricoJogadas;
import canteiro.modelo.estruturas.Jogada;
import canteiro.modelo.estruturas.Reserva;
import canteiro.modelo.fisica.AnalisadorEstrutural;
import canteiro.modelo.fisica.Colapso;
import canteiro.modelo.fisica.Estabilidade;
import canteiro.modelo.materiais.Material;
import canteiro.modelo.pecas.Peca;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Coordena a partida: gera a peça, faz ela cair, aplica os comandos, fixa
 * quando ela bate embaixo, elimina as linhas completas, soma os pontos e
 * derruba a estrutura quando ela perde o equilíbrio.
 *
 * <p>O motor <strong>não implementa regra, só delega</strong>: pergunta ao
 * {@link Tabuleiro} se há colisão, pede a ele que fixe e elimine linhas, pede
 * ao {@link AnalisadorEstrutural} a estabilidade, ao {@link Colapso} o
 * desabamento e à {@link FontePecas} a próxima peça. A partida é uma máquina de
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

    private final Tabuleiro tabuleiro = new Tabuleiro();
    private final FontePecas fonte;
    private final Placar placar;
    private final AnalisadorEstrutural analisador = new AnalisadorEstrutural();
    private final Reserva reserva = new Reserva();
    private final HistoricoJogadas historico = new HistoricoJogadas();
    private int ciclosPorQueda;
    private final List<ObservadorPartida> observadores = new CopyOnWriteArrayList<>();

    private EstadoPartida estado = EstadoPartida.GERANDO_PECA;
    private PecaEmQueda emQueda;
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
     * Aplica um comando do jogador e o registra no histórico (RF23). Comandos
     * que não cabem no estado atual, ou que fariam a peça colidir, são
     * ignorados, mas também ficam registrados: repetidos no mesmo ciclo, são
     * ignorados de novo, e o replay dá a mesma partida. Depois do fim de jogo,
     * nada é registrado.
     *
     * @param comando comando recebido
     */
    public void aplicar(Comando comando) {
        if (estado.encerrada()) {
            return;
        }
        historico.registrar(new Jogada(ciclo, comando));
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
            case ESQUERDA -> mudou |= emQueda.mover(0, -1);
            case DIREITA -> mudou |= emQueda.mover(0, 1);
            case DESCER -> descerOuFixar();
            case QUEDA_INSTANTANEA -> {
                emQueda.cairAtePouso();
                assentar();
            }
            case GIRAR_HORARIO -> mudou |= emQueda.girar(true);
            case GIRAR_ANTI_HORARIO -> mudou |= emQueda.girar(false);
            case RESERVAR -> reservar();
            default -> {
                // DESFAZER depende do modo treino (#37)
            }
        }
    }

    /**
     * Guarda a peça atual na reserva e põe no topo a que estava guardada, ou
     * a próxima da fila na primeira vez (RN05). Só uma troca por peça.
     */
    private void reservar() {
        if (reserva.podeTrocar()) {
            entrar(reserva.trocar(emQueda.peca()).orElseGet(fonte::proxima));
        }
    }

    private void gerarPeca() {
        reserva.liberar();
        entrar(fonte.proxima());
    }

    private void entrar(Peca peca) {
        emQueda = PecaEmQueda.nascer(peca, tabuleiro);
        ciclosDesdeQueda = 0;
        mudou = true;
        if (emQueda.colide()) {
            estado = EstadoPartida.FIM_DE_JOGO;
            emitir(EventoPartida.de(EventoPartida.Tipo.FIM_DE_JOGO));
        } else {
            estado = EstadoPartida.PECA_CAINDO;
        }
    }

    private void descerOuFixar() {
        ciclosDesdeQueda = 0;
        if (emQueda.mover(1, 0)) {
            mudou = true;
        } else {
            assentar();
        }
    }

    private void assentar() {
        estado = EstadoPartida.FIXANDO;
        for (Celula celula : emQueda.fixar()) {
            analisador.registrar(celula, emQueda.peca().material());
        }
        emitir(EventoPartida.de(EventoPartida.Tipo.PECA_FIXADA));
        eliminarLinhas();
        emQueda = null;
        if (estabilidade().passouDoLimite() && desabou()) {
            return;
        }
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
        for (int linhaCompleta : completas) {
            for (int c = 0; c < Dimensoes.COLUNAS; c++) {
                analisador.remover(new Celula(linhaCompleta, c), tabuleiro.bloco(linhaCompleta, c).material());
            }
        }
        tabuleiro.eliminarLinhasCompletas();
        emitir(new EventoPartida(EventoPartida.Tipo.LINHAS_ELIMINADAS, completas));
        if (placar.registrarLinhas(completas.size(), predominante)) {
            ciclosPorQueda = Tempo.ciclos(Progressao.intervaloQuedaMs(placar.nivel()));
            fonte.nivelMudou(placar.nivel());
            emitir(EventoPartida.de(EventoPartida.Tipo.NIVEL_SUBIU));
        }
    }

    /**
     * Executa o colapso (RN11, RN12). Se a pilha estava acima do limite de
     * altura, a partida acaba (RN13).
     *
     * @return {@code true} se a partida acabou
     */
    private boolean desabou() {
        estado = EstadoPartida.COLAPSO;
        boolean altaDemais = tabuleiro.alturaPilha() > Dimensoes.ALTURA_LIMITE_COLAPSO;
        List<Queda> quedas = Colapso.executar(tabuleiro);
        placar.registrarColapso();
        emitir(new EventoPartida(EventoPartida.Tipo.COLAPSO, List.of(), quedas));
        if (altaDemais) {
            estado = EstadoPartida.FIM_DE_JOGO;
            emitir(EventoPartida.de(EventoPartida.Tipo.FIM_DE_JOGO));
        }
        return altaDemais;
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
     * Tira um observador.
     *
     * @param observador quem para de ouvir
     */
    public void desinscrever(ObservadorPartida observador) {
        observadores.remove(observador);
    }

    /**
     * Posições que a peça atual ocupa no tabuleiro.
     *
     * @return as quatro células, ou lista vazia se não houver peça caindo
     */
    public List<Celula> celulasPecaAtual() {
        return emQueda == null ? List.of() : emQueda.celulas();
    }

    /**
     * Onde a peça atual pousaria com uma queda instantânea: a "peça fantasma"
     * que ajuda o jogador a mirar.
     *
     * @return as quatro células do pouso, ou lista vazia se não houver peça caindo
     */
    public List<Celula> celulasFantasma() {
        return emQueda == null ? List.of() : emQueda.celulasNoPouso();
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
        return emQueda == null ? null : emQueda.peca();
    }

    /**
     * Devolve a peça guardada na reserva (RF10).
     *
     * @return a peça, ou {@code null} se nada foi guardado ainda
     */
    public Peca pecaReservada() {
        return reserva.guardada().orElse(null);
    }

    /**
     * Informa se o jogador ainda pode trocar a peça atual pela reservada.
     *
     * @return {@code false} depois de uma troca, até a próxima peça sair da fila
     */
    public boolean podeReservar() {
        return reserva.podeTrocar();
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
     * Estabilidade atual da estrutura, com o limite do nível atual.
     *
     * @return índice, desvio, centro de massa, eixo da base e alerta
     */
    public Estabilidade estabilidade() {
        return analisador.estabilidade(tabuleiro, Progressao.limiteDesvio(placar.nivel()));
    }

    /**
     * Devolve o analisador estrutural, para leitura.
     *
     * @return o analisador da partida
     */
    public AnalisadorEstrutural analisador() {
        return analisador;
    }

    /**
     * Devolve as jogadas da partida, na ordem em que aconteceram (RF23).
     *
     * @return cópia imutável das jogadas, que o replay percorre
     */
    public List<Jogada> jogadas() {
        return historico.emOrdem();
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
