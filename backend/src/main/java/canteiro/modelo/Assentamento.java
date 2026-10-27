package canteiro.modelo;

import canteiro.modelo.constantes.Dimensoes;
import canteiro.modelo.estruturas.FontePecas;
import canteiro.modelo.fisica.AnalisadorEstrutural;
import canteiro.modelo.fisica.Colapso;
import canteiro.modelo.fisica.Estabilidade;
import canteiro.modelo.materiais.Material;
import canteiro.modelo.metas.Lance;
import canteiro.modelo.metas.Meta;
import canteiro.modelo.metas.QuadroMetas;

import java.util.List;
import java.util.function.Consumer;

/**
 * O que acontece depois que a peça assenta: ela entra na conta da massa, as
 * linhas completas somem e pontuam, o nível sobe e, se a estrutura perdeu o
 * equilíbrio, ela desaba (RF11 a RF18). No fim, o lance conta na meta do
 * nível (RN19).
 *
 * <p>Fica fora do {@link MotorJogo} para ele continuar só coordenando: o motor
 * decide <em>quando</em> a peça assenta; esta classe, <em>o que vem depois</em>.</p>
 *
 * @author Mateus Vitor Ferreira
 * @version 0.1.0
 */
final class Assentamento {

    private final Tabuleiro tabuleiro;
    private final AnalisadorEstrutural analisador;
    private final Placar placar;
    private final FontePecas fonte;
    private final Consumer<EventoPartida> emitir;
    private final Consumer<EstadoPartida> passarPor;
    private final QuadroMetas metas;
    private Material predominante;
    private boolean subiu;

    /**
     * @param tabuleiro  onde a peça assenta
     * @param analisador a conta da massa
     * @param placar     pontos, linhas e nível
     * @param fonte      avisada quando o nível sobe
     * @param emitir     recebe cada evento, na ordem
     * @param passarPor  recebe cada estado de passagem (fixando, eliminando, colapso)
     */
    Assentamento(Tabuleiro tabuleiro, AnalisadorEstrutural analisador, Placar placar, FontePecas fonte,
                 Consumer<EventoPartida> emitir, Consumer<EstadoPartida> passarPor) {
        this.tabuleiro = tabuleiro;
        this.analisador = analisador;
        this.placar = placar;
        this.fonte = fonte;
        this.emitir = emitir;
        this.passarPor = passarPor;
        this.metas = new QuadroMetas(fonte.semente(), Pontuacao.LIMIAR_OUSADIA);
        comecarNivel();
    }

    /**
     * Assenta a peça e resolve tudo o que vem depois, no mesmo ciclo.
     *
     * @param peca a peça que encostou
     * @return {@code true} se a partida acabou num colapso com a pilha alta demais (RN13)
     */
    boolean executar(PecaEmQueda peca) {
        passarPor.accept(EstadoPartida.FIXANDO);
        for (Celula celula : peca.fixar()) {
            analisador.registrar(celula, peca.peca().material());
        }
        emitir.accept(EventoPartida.de(EventoPartida.Tipo.PECA_FIXADA));
        boolean ousada = Pontuacao.ousada(estabilidade().indice());
        int nivel = placar.nivel();
        predominante = null;
        subiu = false;
        int linhas = eliminarLinhas(ousada);
        if (linhas == 0) {
            placar.quebrarSequencia();
        }
        boolean desaba = estabilidade().passouDoLimite();
        boolean acabou = desaba && desabou();
        if (!acabou) {
            contarNaMeta(new Lance(linhas, predominante == null ? null : predominante.codigo(), ousada && linhas > 0,
                    estabilidade().indice(), desaba), nivel);
        }
        return acabou;
    }

    /**
     * A meta do nível atual (RN19).
     *
     * @return a meta
     */
    Meta meta() {
        return metas.atual();
    }

    /** Conta o lance na meta do nível em que ele foi feito; se o nível subiu, sorteia a meta nova. */
    private void contarNaMeta(Lance lance, int nivel) {
        if (metas.registrar(lance)) {
            int bonus = QuadroMetas.bonus(nivel);
            placar.registrarBonus(bonus);
            emitir.accept(new EventoPartida(EventoPartida.Tipo.META_CUMPRIDA, List.of(), List.of(), bonus, false));
        }
        if (subiu) {
            comecarNivel();
        }
    }

    private void comecarNivel() {
        List<String> liberados = Progressao.materiaisLiberados(placar.nivel());
        metas.comecarNivel(placar.nivel(), liberados.get(liberados.size() - 1));
    }

    /**
     * Estabilidade atual da estrutura, com o limite do nível atual.
     *
     * @return índice, desvio, centro de massa, eixo da base e alerta
     */
    Estabilidade estabilidade() {
        return analisador.estabilidade(tabuleiro, Progressao.limiteDesvio(placar.nivel()));
    }

    /**
     * Elimina as linhas completas e soma os pontos.
     *
     * @param ousada se a estrutura estava perto de cair ao fechar as linhas (RN17)
     * @return quantas linhas foram eliminadas
     */
    private int eliminarLinhas(boolean ousada) {
        List<Integer> completas = tabuleiro.linhasCompletas();
        if (completas.isEmpty()) {
            return 0;
        }
        passarPor.accept(EstadoPartida.ELIMINANDO_LINHAS);
        predominante = Pontuacao.predominante(tabuleiro.materiaisDasLinhas(completas));
        for (int linhaCompleta : completas) {
            for (int c = 0; c < Dimensoes.COLUNAS; c++) {
                analisador.remover(new Celula(linhaCompleta, c), tabuleiro.bloco(linhaCompleta, c).material());
            }
        }
        tabuleiro.eliminarLinhasCompletas();
        int antes = placar.pontuacao();
        subiu = placar.registrarLinhas(completas.size(), predominante, ousada);
        emitir.accept(new EventoPartida(EventoPartida.Tipo.LINHAS_ELIMINADAS, completas, List.of(),
                placar.pontuacao() - antes, ousada));
        if (subiu) {
            fonte.nivelMudou(placar.nivel());
            emitir.accept(EventoPartida.de(EventoPartida.Tipo.NIVEL_SUBIU));
        }
        return completas.size();
    }

    /**
     * Executa o colapso (RN11, RN12). Se a pilha estava acima do limite de
     * altura, a partida acaba (RN13).
     *
     * @return {@code true} se a partida acabou
     */
    private boolean desabou() {
        passarPor.accept(EstadoPartida.COLAPSO);
        boolean altaDemais = tabuleiro.alturaPilha() > Dimensoes.ALTURA_LIMITE_COLAPSO;
        List<Queda> quedas = Colapso.executar(tabuleiro);
        int antes = placar.pontuacao();
        placar.registrarColapso();
        emitir.accept(new EventoPartida(EventoPartida.Tipo.COLAPSO, List.of(), quedas, placar.pontuacao() - antes,
                false));
        if (altaDemais) {
            passarPor.accept(EstadoPartida.FIM_DE_JOGO);
            emitir.accept(EventoPartida.de(EventoPartida.Tipo.FIM_DE_JOGO));
        }
        return altaDemais;
    }
}
