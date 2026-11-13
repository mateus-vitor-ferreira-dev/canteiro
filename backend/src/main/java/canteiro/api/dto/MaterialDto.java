package canteiro.api.dto;

import canteiro.modelo.materiais.CatalogoMateriais;
import canteiro.modelo.materiais.Material;

import java.util.List;

/**
 * Material como é enviado ao frontend pela rota {@code GET /api/materiais}.
 *
 * @param codigo      identificador do material, como {@code "CONCRETO"}
 * @param nome        nome mostrado ao jogador
 * @param densidade   massa de um bloco de volume unitário (RN07)
 * @param cor         cor dos blocos no formato {@code #RRGGBB}
 * @param bonusLinha  pontos extras de cada linha em que o material predomina (RN03)
 * @param cargaMaxima a maior massa que um bloco aguenta em cima, ou {@code null} se não há limite (RN21)
 * @author Mateus Vitor Ferreira
 * @version 0.1.0
 */
public record MaterialDto(String codigo, String nome, double densidade, String cor,
                          int bonusLinha, Double cargaMaxima) {

    /**
     * Converte um material do modelo no formato do protocolo.
     *
     * @param material material a converter
     * @return o material no formato enviado ao frontend
     */
    public static MaterialDto de(Material material) {
        double carga = material.cargaMaxima();
        return new MaterialDto(material.codigo(), material.nome(), material.densidade(),
                String.format("#%06X", material.corRgb() & 0xFFFFFF), material.bonusLinha(),
                Double.isInfinite(carga) ? null : carga);
    }

    /**
     * Converte todo o catálogo, na ordem em que os materiais foram cadastrados.
     *
     * @param catalogo catálogo em uso pelo jogo
     * @return lista com uma entrada por material
     */
    public static List<MaterialDto> todos(CatalogoMateriais catalogo) {
        return catalogo.todos().stream().map(MaterialDto::de).toList();
    }
}
