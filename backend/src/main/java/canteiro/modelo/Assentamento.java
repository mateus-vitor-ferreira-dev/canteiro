package canteiro.modelo;

import canteiro.modelo.constantes.Dimensoes;
import canteiro.modelo.estruturas.FontePecas;
import canteiro.modelo.fisica.AnalisadorEstrutural;
import canteiro.modelo.fisica.Colapso;
import canteiro.modelo.fisica.Estabilidade;
import canteiro.modelo.materiais.Material;

import java.util.List;
import java.util.function.Consumer;

/**
 * O que acontece depois que a peça assenta: ela entra na conta da massa, as
 * linhas completas somem e pontuam, o nível sobe e, se a estrutura perdeu o
 * equilíbrio, ela desaba (RF11 a RF18).
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
        eliminarLinhas();
        return estabilidade().passouDoLimite() && desabou();
    }

    /**
     * Estabilidade atual da estrutura, com o limite do nível atual.
     *
     * @return índice, desvio, centro de massa, eixo da base e alerta
     */
    Estabilidade estabilidade() {
        return analisador.estabilidade(tabuleiro, Progressao.limiteDesvio(placar.nivel()));
    }

    private void eliminarLinhas() {
        List<Integer> completas = tabuleiro.linhasCompletas();
        if (completas.isEmpty()) {
            return;
        }
        passarPor.accept(EstadoPartida.ELIMINANDO_LINHAS);
        Material predominante = Pontuacao.predominante(tabuleiro.materiaisDasLinhas(completas));
        for (int linhaCompleta : completas) {
            for (int c = 0; c < Dimensoes.COLUNAS; c++) {
                analisador.remover(new Celula(linhaCompleta, c), tabuleiro.bloco(linhaCompleta, c).material());
            }
        }
        tabuleiro.eliminarLinhasCompletas();
        emitir.accept(new EventoPartida(EventoPartida.Tipo.LINHAS_ELIMINADAS, completas));
        if (placar.registrarLinhas(completas.size(), predominante)) {
            fonte.nivelMudou(placar.nivel());
            emitir.accept(EventoPartida.de(EventoPartida.Tipo.NIVEL_SUBIU));
        }
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
        placar.registrarColapso();
        emitir.accept(new EventoPartida(EventoPartida.Tipo.COLAPSO, List.of(), quedas));
        if (altaDemais) {
            passarPor.accept(EstadoPartida.FIM_DE_JOGO);
            emitir.accept(EventoPartida.de(EventoPartida.Tipo.FIM_DE_JOGO));
        }
        return altaDemais;
    }
}
