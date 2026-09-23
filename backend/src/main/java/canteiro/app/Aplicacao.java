package canteiro.app;

import canteiro.visao.JanelaPrincipal;

import javax.swing.SwingUtilities;

/**
 * Ponto de entrada do CANTEIRO.
 *
 * <p>Toda criação e alteração de componentes Swing acontece na thread de
 * eventos, por isso a janela é aberta via {@link SwingUtilities#invokeLater}.</p>
 *
 * @author Mateus Vitor Ferreira
 * @version 0.1.0
 */
public final class Aplicacao {

    private Aplicacao() {
    }

    /**
     * Inicia a aplicação e abre a janela principal.
     *
     * @param args argumentos de linha de comando; não utilizados
     */
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new JanelaPrincipal().setVisible(true));
    }
}
