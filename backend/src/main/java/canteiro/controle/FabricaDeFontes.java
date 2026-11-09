package canteiro.controle;

import canteiro.modelo.Dificuldade;
import canteiro.modelo.estruturas.FontePecas;

/**
 * Cria a fonte de peças de uma partida. A mesma dificuldade com a mesma
 * semente dá sempre as mesmas peças, na mesma ordem: é isso que permite
 * desfazer no modo treino e reproduzir uma partida gravada.
 *
 * @author Mateus Vitor Ferreira
 * @version 0.1.0
 */
@FunctionalInterface
public interface FabricaDeFontes {

    /**
     * Cria uma fonte nova, no começo da sequência.
     *
     * @param dificuldade dificuldade da partida, que define os materiais do nível inicial
     * @param semente     semente do sorteio das peças
     * @return a fonte de peças
     */
    FontePecas criar(Dificuldade dificuldade, long semente);
}
