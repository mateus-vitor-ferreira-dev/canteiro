import type { DificuldadeDto, ErroDto } from "./protocolo";

/** Código usado quando o backend nem chegou a responder. */
export const SEM_CONEXAO = "SEM_CONEXAO";

/** Erro devolvido pela API, já com a mensagem que pode ser mostrada ao jogador. */
export class ErroApi extends Error {
    /**
     * @param codigo código do erro, como `NOME_INVALIDO` ou {@link SEM_CONEXAO}
     * @param mensagem texto para o jogador
     * @param status status HTTP da resposta; `0` quando não houve resposta
     */
    constructor(
        readonly codigo: string,
        mensagem: string,
        readonly status: number,
    ) {
        super(mensagem);
        this.name = "ErroApi";
    }
}

/** Lista as três dificuldades, da mais fácil para a mais difícil (RF02). */
export function listarDificuldades(): Promise<DificuldadeDto[]> {
    return buscarJson<DificuldadeDto[]>("/api/dificuldades");
}

async function buscarJson<T>(caminho: string): Promise<T> {
    let resposta: Response;
    try {
        resposta = await fetch(caminho, { headers: { Accept: "application/json" } });
    } catch {
        throw new ErroApi(
            SEM_CONEXAO,
            "Não foi possível falar com o jogo. O backend está rodando?",
            0,
        );
    }
    if (!resposta.ok) {
        const erro = await lerErro(resposta);
        throw new ErroApi(erro.erro, erro.mensagem, resposta.status);
    }
    return (await resposta.json()) as T;
}

async function lerErro(resposta: Response): Promise<ErroDto> {
    try {
        return (await resposta.json()) as ErroDto;
    } catch {
        return {
            erro: "ERRO_DESCONHECIDO",
            mensagem: `O jogo respondeu com erro ${resposta.status}.`,
        };
    }
}
