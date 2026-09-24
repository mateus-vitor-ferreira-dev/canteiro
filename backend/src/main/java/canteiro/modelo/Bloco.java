package canteiro.modelo;

import canteiro.modelo.materiais.Material;

import java.util.Objects;

/**
 * Um bloco já fixado no tabuleiro.
 *
 * <p>Guarda o material de que veio e se está consolidado. Um bloco
 * consolidado resiste ao desprendimento no colapso: é o efeito do aço
 * assentado logo acima dele.</p>
 *
 * @author Mateus Vitor Ferreira
 * @version 0.1.0
 */
public final class Bloco {

    private final Material material;
    private boolean consolidado;

    /**
     * Cria um bloco solto, ainda não consolidado.
     *
     * @param material material do bloco
     * @throws NullPointerException se o material for nulo
     */
    public Bloco(Material material) {
        this.material = Objects.requireNonNull(material, "o bloco precisa de um material");
    }

    /**
     * Massa do bloco, que é a do seu material (RN07).
     *
     * @return massa, em toneladas
     */
    public double massa() {
        return material.massaPorBloco();
    }

    /**
     * Marca o bloco como consolidado. Consolidar de novo não muda nada.
     */
    public void consolidar() {
        consolidado = true;
    }

    /**
     * Informa se o bloco resiste ao desprendimento no colapso.
     *
     * @return {@code true} se estiver consolidado
     */
    public boolean consolidado() {
        return consolidado;
    }

    /**
     * Devolve o material do bloco.
     *
     * @return o material
     */
    public Material material() {
        return material;
    }
}
