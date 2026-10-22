package canteiro.persistencia;

import canteiro.modelo.materiais.Aco;
import canteiro.modelo.materiais.Alvenaria;
import canteiro.modelo.materiais.CatalogoMateriais;
import canteiro.modelo.materiais.Concreto;
import canteiro.modelo.materiais.Madeira;
import canteiro.modelo.materiais.Material;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Path;
import java.util.List;
import java.util.Properties;
import java.util.function.BiFunction;
import java.util.function.Supplier;
import java.util.regex.Pattern;

/**
 * Monta o catálogo de materiais com a densidade e a cor do arquivo
 * {@code materiais.properties}, para ajustar o equilíbrio sem recompilar.
 *
 * <p>O arquivo do jogador fica em {@code ~/.canteiro/}; o padrão vem dentro
 * do JAR. Valor que falte ou seja inválido (letra na densidade, densidade
 * negativa, cor que não é RRGGBB) volta para o padrão daquele material, com
 * aviso no log, sem afetar os outros (RNF11, RNF12).</p>
 *
 * @author Mateus Vitor Ferreira
 * @version 0.1.0
 */
public final class CarregadorMateriais {

    /** Nome do arquivo, dentro da pasta do jogo. */
    public static final String ARQUIVO = "materiais.properties";

    /** Arquivo padrão, dentro do JAR. */
    static final String RECURSO_PADRAO = "/dados/" + ARQUIVO;

    private static final String SUFIXO_DENSIDADE = ".densidade";
    private static final String SUFIXO_COR = ".cor";
    private static final Pattern COR_RRGGBB = Pattern.compile("#?[0-9A-Fa-f]{6}");
    private static final int BASE_HEXADECIMAL = 16;
    private static final Logger LOG = LoggerFactory.getLogger(CarregadorMateriais.class);

    /** Como criar cada material: com os valores padrão e com os valores lidos. */
    private record Fabrica(Supplier<Material> padrao, BiFunction<Double, Integer, Material> comValores) {
    }

    private static final List<Fabrica> FABRICAS = List.of(
            new Fabrica(Madeira::new, Madeira::new),
            new Fabrica(Alvenaria::new, Alvenaria::new),
            new Fabrica(Concreto::new, Concreto::new),
            new Fabrica(Aco::new, Aco::new));

    private CarregadorMateriais() {
    }

    /**
     * Lê o arquivo de materiais da pasta do jogo e monta o catálogo.
     *
     * @param pasta pasta do jogo, como {@code ~/.canteiro}
     * @return o catálogo com os quatro materiais
     */
    public static CatalogoMateriais carregar(Path pasta) {
        return montar(LeitorPropriedades.comPadrao(pasta.resolve(ARQUIVO), RECURSO_PADRAO));
    }

    /**
     * Monta o catálogo a partir de propriedades já lidas.
     *
     * @param propriedades as chaves {@code CODIGO.densidade} e {@code CODIGO.cor}
     * @return o catálogo com os quatro materiais
     */
    static CatalogoMateriais montar(Properties propriedades) {
        CatalogoMateriais catalogo = new CatalogoMateriais();
        for (Fabrica fabrica : FABRICAS) {
            Material padrao = fabrica.padrao().get();
            double densidade = densidade(propriedades, padrao);
            int cor = cor(propriedades, padrao);
            catalogo.registrar(fabrica.comValores().apply(densidade, cor));
        }
        return catalogo;
    }

    private static double densidade(Properties propriedades, Material padrao) {
        String chave = padrao.codigo() + SUFIXO_DENSIDADE;
        String valor = propriedades.getProperty(chave);
        if (valor == null) {
            return padrao.densidade();
        }
        try {
            double densidade = Double.parseDouble(valor.strip());
            if (Double.isFinite(densidade) && densidade > 0) {
                return densidade;
            }
        } catch (NumberFormatException invalida) {
            // cai no aviso abaixo, como uma densidade fora da faixa
        }
        return avisar(chave, valor, padrao.densidade());
    }

    private static int cor(Properties propriedades, Material padrao) {
        String chave = padrao.codigo() + SUFIXO_COR;
        String valor = propriedades.getProperty(chave);
        if (valor == null) {
            return padrao.corRgb();
        }
        String limpo = valor.strip();
        if (!COR_RRGGBB.matcher(limpo).matches()) {
            return avisar(chave, valor, padrao.corRgb());
        }
        return Integer.parseInt(limpo.replace("#", ""), BASE_HEXADECIMAL);
    }

    private static <T> T avisar(String chave, String valor, T padrao) {
        LOG.warn("{}={} é inválido; usando o padrão {}", chave, valor, padrao);
        return padrao;
    }
}
