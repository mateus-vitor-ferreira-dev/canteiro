package canteiro.persistencia;

import java.nio.file.Path;
import java.util.Objects;

/**
 * Os arquivos que o servidor lê e grava enquanto o jogo roda, todos na mesma
 * pasta. O {@code main} monta uma vez e entrega ao servidor; nos testes, a
 * pasta é temporária.
 *
 * @param configuracoes teclas e volume do jogador (RF27)
 * @param ranking       as melhores partidas (RF21, RF22)
 * @param repeticoes    as partidas gravadas (RF24)
 * @author Mateus Vitor Ferreira
 * @version 0.1.0
 */
public record ArquivosDoJogo(ArquivoConfiguracoes configuracoes, RankingArquivo ranking,
                             ArquivoRepeticoes repeticoes) {

    /**
     * Confere que nada falta.
     *
     * @param configuracoes teclas e volume do jogador
     * @param ranking       as melhores partidas
     * @param repeticoes    as partidas gravadas
     * @throws NullPointerException se algum arquivo for nulo
     */
    public ArquivosDoJogo {
        Objects.requireNonNull(configuracoes, "configurações");
        Objects.requireNonNull(ranking, "ranking");
        Objects.requireNonNull(repeticoes, "repetições");
    }

    /**
     * Aponta todos os arquivos para a mesma pasta. Não lê nem cria nada ainda.
     *
     * @param pasta pasta do jogo, como {@code ~/.canteiro}
     * @return os arquivos do jogo nessa pasta
     */
    public static ArquivosDoJogo em(Path pasta) {
        return new ArquivosDoJogo(new ArquivoConfiguracoes(pasta), new RankingArquivo(pasta),
                new ArquivoRepeticoes(pasta));
    }
}
