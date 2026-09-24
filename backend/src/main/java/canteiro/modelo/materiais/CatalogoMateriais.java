package canteiro.modelo.materiais;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Catálogo dos materiais do jogo, indexado pelo código.
 *
 * <p>É uma tabela hash: a cada peça gerada, o material é buscado pelo código,
 * então a busca precisa ser O(1). A ordem de cadastro é mantida, para que a
 * listagem saia sempre do mais leve para o mais pesado.</p>
 *
 * @author Mateus Vitor Ferreira
 * @version 0.1.0
 */
public final class CatalogoMateriais {

    private final Map<String, Material> materiais = new LinkedHashMap<>();

    /**
     * Cria um catálogo vazio.
     */
    public CatalogoMateriais() {
        // materiais são cadastrados com registrar(...)
    }

    /**
     * Cria o catálogo com os quatro materiais do jogo e os valores padrão.
     *
     * @return catálogo com madeira, alvenaria, concreto e aço, nessa ordem
     */
    public static CatalogoMateriais padrao() {
        CatalogoMateriais catalogo = new CatalogoMateriais();
        catalogo.registrar(new Madeira());
        catalogo.registrar(new Alvenaria());
        catalogo.registrar(new Concreto());
        catalogo.registrar(new Aco());
        return catalogo;
    }

    /**
     * Cadastra um material. Criar um quinto material é escrever a subclasse e
     * registrá-la aqui, sem tocar no motor.
     *
     * @param material material a cadastrar
     * @throws IllegalArgumentException se já houver um material com o mesmo código
     */
    public void registrar(Material material) {
        Material anterior = materiais.putIfAbsent(material.codigo(), material);
        if (anterior != null) {
            throw new IllegalArgumentException("material repetido: " + material.codigo());
        }
    }

    /**
     * Busca um material pelo código, em O(1).
     *
     * @param codigo código do material, como {@code "ACO"}
     * @return o material cadastrado com esse código
     * @throws IllegalArgumentException se não houver material com esse código
     */
    public Material buscar(String codigo) {
        Material material = materiais.get(codigo);
        if (material == null) {
            throw new IllegalArgumentException("material desconhecido: " + codigo);
        }
        return material;
    }

    /**
     * Informa se há um material com o código dado.
     *
     * @param codigo código a procurar
     * @return {@code true} se o material estiver cadastrado
     */
    public boolean contem(String codigo) {
        return materiais.containsKey(codigo);
    }

    /**
     * Lista os materiais na ordem em que foram cadastrados.
     *
     * @return coleção somente leitura dos materiais
     */
    public Collection<Material> todos() {
        return Collections.unmodifiableCollection(materiais.values());
    }
}
