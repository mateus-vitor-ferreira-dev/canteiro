/**
 * Tipos do protocolo entre backend e frontend.
 *
 * Cada tipo aqui é o espelho de um `record` de `canteiro.api` no backend.
 * Mudou um lado, muda o outro no mesmo PR (RNF15).
 */

/** Código de uma dificuldade, usado ao criar a partida. */
export type CodigoDificuldade = "FACIL" | "NORMAL" | "DIFICIL";

/** Uma dificuldade, como vem de `GET /api/dificuldades` (RF02). Espelho de `DificuldadeDto`. */
export type DificuldadeDto = {
    codigo: CodigoDificuldade;
    nome: string;
    /** Intervalo inicial de queda, em milissegundos. */
    intervaloQuedaMs: number;
    /** Desvio máximo tolerado antes do colapso, em colunas. */
    limiteDesvio: number;
    /** Códigos dos materiais liberados no início da partida. */
    materiaisLiberados: string[];
};

/** Corpo de toda resposta de erro da API. Espelho de `ErroDto`. */
export type ErroDto = {
    erro: string;
    mensagem: string;
};
