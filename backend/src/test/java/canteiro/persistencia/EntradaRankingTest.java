package canteiro.persistencia;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * As regras do nome e dos números de uma linha do ranking.
 *
 * @author Mateus Vitor Ferreira
 * @version 0.1.0
 */
class EntradaRankingTest {

    private static final LocalDate HOJE = LocalDate.of(2026, 10, 22);

    @ParameterizedTest
    @ValueSource(strings = {"", "   ", "Nome com mais de vinte letras", "Ana;Bia", "Ana\nBia"})
    void nomeInvalidoTemExplicacaoEEhRecusado(String nome) {
        assertTrue(EntradaRanking.problemaNoNome(nome).isPresent());
        assertThrows(IllegalArgumentException.class, () -> new EntradaRanking(nome, 1, 1, 0, 0, HOJE));
    }

    @Test
    void nomeComAcentoEEspacoNoMeioServe() {
        assertTrue(EntradaRanking.problemaNoNome("João da Conceição").isEmpty());
        assertDoesNotThrow(() -> new EntradaRanking("João da Conceição", 1, 1, 0, 0, HOJE));
    }

    @Test
    void recusaNumerosImpossiveis() {
        assertThrows(IllegalArgumentException.class, () -> new EntradaRanking("Ana", -1, 1, 0, 0, HOJE));
        assertThrows(IllegalArgumentException.class, () -> new EntradaRanking("Ana", 1, 0, 0, 0, HOJE));
        assertThrows(NullPointerException.class, () -> new EntradaRanking("Ana", 1, 1, 0, 0, null));
    }
}
