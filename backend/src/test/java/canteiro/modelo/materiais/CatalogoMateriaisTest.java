package canteiro.modelo.materiais;

import canteiro.modelo.Dificuldade;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Confere o catálogo de materiais: busca pelo código, cadastro e ordem.
 *
 * @author Mateus Vitor Ferreira
 * @version 0.1.0
 */
class CatalogoMateriaisTest {

    private final CatalogoMateriais catalogo = CatalogoMateriais.padrao();

    @Test
    void padraoTemOsQuatroMateriaisDoMaisLeveAoMaisPesado() {
        List<String> codigos = catalogo.todos().stream().map(Material::codigo).toList();
        assertEquals(List.of("MADEIRA", "ALVENARIA", "CONCRETO", "ACO"), codigos);
    }

    @Test
    void buscaOMaterialPeloCodigo() {
        assertInstanceOf(Aco.class, catalogo.buscar(Aco.CODIGO));
        assertInstanceOf(Madeira.class, catalogo.buscar(Madeira.CODIGO));
    }

    @Test
    void devolveSempreOMesmoObjeto() {
        assertSame(catalogo.buscar(Concreto.CODIGO), catalogo.buscar(Concreto.CODIGO));
    }

    @Test
    void codigoDesconhecidoEErro() {
        assertFalse(catalogo.contem("OURO"));
        assertThrows(IllegalArgumentException.class, () -> catalogo.buscar("OURO"));
    }

    @Test
    void naoAceitaDoisMateriaisComOMesmoCodigo() {
        assertThrows(IllegalArgumentException.class, () -> catalogo.registrar(new Aco()));
    }

    @Test
    void quintoMaterialEntraSoComRegistro() {
        CatalogoMateriais vazio = new CatalogoMateriais();
        vazio.registrar(new Madeira());
        assertTrue(vazio.contem(Madeira.CODIGO));
        assertEquals(1, vazio.todos().size());
    }

    @Test
    void listagemNaoPodeSerAlteradaPorFora() {
        assertThrows(UnsupportedOperationException.class, () -> catalogo.todos().clear());
    }

    @Test
    void todoMaterialLiberadoNasDificuldadesExisteNoCatalogo() {
        for (Dificuldade dificuldade : Dificuldade.values()) {
            for (String codigo : dificuldade.materiaisLiberados()) {
                assertTrue(catalogo.contem(codigo), dificuldade + " libera um material inexistente: " + codigo);
            }
        }
    }
}
