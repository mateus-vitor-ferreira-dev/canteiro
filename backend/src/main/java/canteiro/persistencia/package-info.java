/**
 * Camada de persistência: leitura e gravação do catálogo de materiais, do
 * ranking e dos arquivos de repetição.
 *
 * <p>Falhas de leitura são registradas em log e substituídas por valores
 * padrão, sem interromper a aplicação (RNF11 e RNF12).</p>
 */
package canteiro.persistencia;
