/**
 * Camada de persistência: leitura e gravação dos arquivos do jogador, na
 * pasta {@code ~/.canteiro/}: materiais, configurações, ranking e repetições.
 *
 * <p>Falhas de leitura são registradas em log e substituídas por valores
 * padrão, sem interromper a aplicação (RNF11 e RNF12).</p>
 */
package canteiro.persistencia;
