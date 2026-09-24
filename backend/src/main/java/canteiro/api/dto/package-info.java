/**
 * DTOs do protocolo: os {@code record}s que viram JSON, e a conversão entre
 * o modelo e eles.
 *
 * <p>O modelo nunca é serializado diretamente. Cada DTO tem um tipo espelho em
 * {@code frontend/src/api/protocolo.ts}; mudou um, muda o outro no mesmo PR
 * (RNF15). Os DTOs não dependem do Javalin.</p>
 */
package canteiro.api.dto;
