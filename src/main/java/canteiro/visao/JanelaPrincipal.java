package canteiro.visao;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.SwingConstants;
import java.awt.Dimension;

/**
 * Janela única da aplicação, onde as telas do jogo são exibidas.
 *
 * <p>Por enquanto mostra apenas o título; menu, partida e demais telas serão
 * acrescentados conforme o cronograma.</p>
 *
 * @author Mateus Vitor Ferreira
 * @version 0.1.0
 */
public class JanelaPrincipal extends JFrame {

    private static final long serialVersionUID = 1L;

    private static final String TITULO = "CANTEIRO";
    private static final int LARGURA_MINIMA = 1024;
    private static final int ALTURA_MINIMA = 768;

    /**
     * Cria a janela com o tamanho mínimo previsto nas premissas do projeto
     * (1024 por 768 pixels), centralizada na tela.
     */
    public JanelaPrincipal() {
        super(TITULO);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setMinimumSize(new Dimension(LARGURA_MINIMA, ALTURA_MINIMA));
        add(new JLabel(TITULO, SwingConstants.CENTER));
        pack();
        setLocationRelativeTo(null);
    }
}
