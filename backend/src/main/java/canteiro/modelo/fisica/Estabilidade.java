package canteiro.modelo.fisica;

/**
 * Quão perto a estrutura está de desabar, num instante (RN09, RN10).
 *
 * @param indice        de 1 (desvio nulo) a 0 (desvio no limite ou além), variando linearmente
 * @param desvio        distância entre o centro de massa e o eixo da base, em colunas
 * @param limite        desvio máximo tolerado no nível, em colunas
 * @param centroDeMassa posição horizontal do centro de massa, em colunas a partir da borda esquerda
 * @param eixo          meio da base de apoio, em colunas a partir da borda esquerda
 * @param alerta        {@code true} quando o índice está abaixo do limiar de alerta (RF16)
 * @author Mateus Vitor Ferreira
 * @version 0.1.0
 */
public record Estabilidade(double indice, double desvio, double limite, double centroDeMassa, double eixo,
                           boolean alerta) {

    /** Abaixo deste índice, a tela avisa que a estrutura está perto de cair (RF16). */
    public static final double LIMIAR_ALERTA = 0.3;

    /**
     * Informa se o desvio passou do limite, o que causa colapso (RN11).
     *
     * @return {@code true} se a estrutura deve desabar
     */
    public boolean passouDoLimite() {
        return desvio > limite;
    }
}
