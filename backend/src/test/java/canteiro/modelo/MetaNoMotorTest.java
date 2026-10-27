package canteiro.modelo;

import canteiro.modelo.constantes.Dimensoes;
import canteiro.modelo.materiais.Madeira;
import canteiro.modelo.metas.ObraFirme;
import canteiro.modelo.metas.QuadroMetas;
import canteiro.modelo.pecas.Forma;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * A meta do nível rodando dentro do {@link MotorJogo} (RN19).
 *
 * <p>Cinco O de madeira, da esquerda para a direita, fecham as duas linhas de
 * baixo sem nunca tirar o centro de massa do eixo: é o jeito de cumprir a
 * meta da obra firme (4 linhas sem a estabilidade cair abaixo de 50 %).</p>
 *
 * @author Mateus Vitor Ferreira
 * @version 0.1.0
 */
class MetaNoMotorTest {

    private final Madeira madeira = new Madeira();

    @Test
    void aPartidaJaComecaComAMetaDoNivel() {
        MotorJogo motor = new MotorJogo(Dificuldade.NORMAL, FonteLista.fixa().depois(Forma.O, madeira));
        assertNotNull(motor.meta());
        assertFalse(motor.meta().cumprida());
    }

    @Test
    void cumprirAMetaDaOBonusEAvisaUmaVez() {
        MotorJogo motor = new MotorJogo(Dificuldade.FACIL,
                FonteLista.fixa().comSemente(sementeDaObraFirme()).depois(Forma.O, madeira));
        motor.avancarCiclo();
        List<EventoPartida> eventos = new ArrayList<>();
        motor.inscrever(new ObservadorPartida() {
            @Override
            public void estadoMudou(MotorJogo m) {
                // só os eventos interessam
            }

            @Override
            public void eventoOcorreu(EventoPartida evento) {
                eventos.add(evento);
            }
        });

        fecharDuasLinhas(motor);
        assertEquals(2, motor.meta().progresso());
        int antes = motor.placar().pontuacao();
        fecharDuasLinhas(motor);

        assertTrue(motor.meta().cumprida());
        List<EventoPartida> cumpridas = eventos.stream()
                .filter(e -> e.tipo() == EventoPartida.Tipo.META_CUMPRIDA).toList();
        assertEquals(1, cumpridas.size());
        assertEquals(QuadroMetas.bonus(1), cumpridas.get(0).pontos());
        int linhas = Pontuacao.pontos(2, 1, madeira); // as quatro O do meio quebraram a sequência
        assertEquals(antes + linhas + QuadroMetas.bonus(1), motor.placar().pontuacao());

        fecharDuasLinhas(motor);
        assertEquals(1, eventos.stream().filter(e -> e.tipo() == EventoPartida.Tipo.META_CUMPRIDA).count());
    }

    private static void fecharDuasLinhas(MotorJogo motor) {
        for (int coluna = 0; coluna < Dimensoes.COLUNAS; coluna += 2) {
            for (int i = 0; i < Math.abs(coluna - 4); i++) {
                motor.aplicar(coluna < 4 ? Comando.ESQUERDA : Comando.DIREITA);
            }
            motor.aplicar(Comando.QUEDA_INSTANTANEA);
        }
    }

    /** Uma semente cuja meta do nível 1 é a obra firme. */
    private static long sementeDaObraFirme() {
        for (long semente = 0; semente < 1000; semente++) {
            QuadroMetas quadro = new QuadroMetas(semente, Pontuacao.LIMIAR_OUSADIA);
            quadro.comecarNivel(1, "ALVENARIA");
            if (quadro.atual() instanceof ObraFirme) {
                return semente;
            }
        }
        throw new AssertionError("nenhuma semente sorteou a obra firme");
    }
}
