package canteiro.modelo.metas;

import java.util.Map;
import java.util.Objects;
import java.util.SplittableRandom;

/**
 * A meta do nível atual: sorteia uma quando o nível começa, conta os lances
 * e avisa, uma vez só, quando ela é cumprida (RN19).
 *
 * <p>O sorteio usa a semente da partida e o número do nível, então a mesma
 * partida sempre tem as mesmas metas, e a repetição continua igual. A meta
 * não cumprida até o fim do nível só é trocada, sem castigo.</p>
 *
 * @author Mateus Vitor Ferreira
 * @version 0.1.0
 */
public final class QuadroMetas {

    /** O bônus por cumprir a meta é este valor vezes o nível (RN19). */
    public static final int BONUS_POR_NIVEL = 500;

    private static final int TIPOS = 4;
    private static final int LINHAS_DE_MATERIAL = 2;
    private static final int LINHAS_DE_UMA_VEZ = 3;
    private static final int LINHAS_FIRMES = 4;
    private static final double LIMIAR_FIRME = 0.5;
    private static final long MISTURA = 0x9E3779B97F4A7C15L;
    private static final Map<String, String> NOMES = Map.of(
            "MADEIRA", "madeira", "ALVENARIA", "alvenaria", "CONCRETO", "concreto", "ACO", "aço");

    private final long semente;
    private final double limiarOusadia;
    private Meta atual;
    private boolean premiada;

    /**
     * Cria o quadro, ainda sem meta.
     *
     * @param semente       semente da partida
     * @param limiarOusadia índice abaixo do qual fechar linha é ousado, para a meta de ousadia
     */
    public QuadroMetas(long semente, double limiarOusadia) {
        this.semente = semente;
        this.limiarOusadia = limiarOusadia;
    }

    /**
     * Sorteia a meta de um nível que começa.
     *
     * @param nivel             o nível que começa
     * @param materialMaisPesado código do material mais pesado liberado no nível, para a meta de material
     * @throws NullPointerException se o material for nulo
     */
    public void comecarNivel(int nivel, String materialMaisPesado) {
        Objects.requireNonNull(materialMaisPesado, "material");
        // O SplittableRandom espalha bem sementes vizinhas; o primeiro número do Random, não.
        SplittableRandom sorteio = new SplittableRandom(semente ^ (nivel * MISTURA));
        atual = switch (sorteio.nextInt(TIPOS)) {
            case 0 -> new LinhasDeMaterial(materialMaisPesado,
                    NOMES.getOrDefault(materialMaisPesado, materialMaisPesado.toLowerCase()), LINHAS_DE_MATERIAL);
            case 1 -> new EliminacaoGrande(LINHAS_DE_UMA_VEZ);
            case 2 -> new ObraFirme(LINHAS_FIRMES, LIMIAR_FIRME);
            default -> new JogadaOusada(limiarOusadia);
        };
        premiada = false;
    }

    /**
     * Conta um lance na meta do nível.
     *
     * @param lance o que a peça fez
     * @return {@code true} só no lance que cumpriu a meta
     */
    public boolean registrar(Lance lance) {
        if (atual == null || premiada) {
            return false;
        }
        atual.registrar(lance);
        premiada = atual.cumprida();
        return premiada;
    }

    /**
     * A meta do nível atual.
     *
     * @return a meta, ou {@code null} antes do primeiro nível começar
     */
    public Meta atual() {
        return atual;
    }

    /**
     * O bônus por cumprir a meta no nível dado.
     *
     * @param nivel nível em que a meta foi cumprida
     * @return os pontos do bônus
     */
    public static int bonus(int nivel) {
        return BONUS_POR_NIVEL * nivel;
    }
}
