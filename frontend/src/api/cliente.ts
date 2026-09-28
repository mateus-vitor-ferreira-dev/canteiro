import type {
    CodigoDificuldade,
    DificuldadeDto,
    EntradaRankingDto,
    ErroDto,
    NovaPartidaDto,
    PartidaCriadaDto,
    RegistroRankingDto,
    RelatorioDto,
} from "./protocolo";

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

/**
 * Cria uma partida na dificuldade escolhida (RF02). Ela começa quando o
 * navegador conecta em `/ws/partidas/{id}`.
 */
export function criarPartida(dificuldade: CodigoDificuldade): Promise<PartidaCriadaDto> {
    const corpo: NovaPartidaDto = { dificuldade };
    return buscarJson<PartidaCriadaDto>("/api/partidas", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify(corpo),
    });
}

/** As dez maiores pontuações, da maior para a menor (RF22). */
export function listarRanking(): Promise<EntradaRankingDto[]> {
    return buscarJson<EntradaRankingDto[]>("/api/ranking");
}

/**
 * Registra no ranking a partida encerrada, com o nome do jogador (RF21). A
 * pontuação o backend lê da própria partida.
 */
export async function registrarNoRanking(partidaId: string, nome: string): Promise<void> {
    const corpo: RegistroRankingDto = { partidaId, nome };
    await buscar("/api/ranking", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify(corpo),
    });
}

const NAO_ENCONTRADO = 404;

/** O relatório da partida: estabilidade a cada peça, colapsos e materiais (RF25). */
export function buscarRelatorio(partidaId: string): Promise<RelatorioDto> {
    return buscarJson<RelatorioDto>(`/api/partidas/${encodeURIComponent(partidaId)}/relatorio`);
}

/**
 * Texto para o jogador quando uma parte do jogo que depende do backend falha.
 * Enquanto o backend não tem a rota, ela responde 404: aí o aviso diz que a
 * parte ainda não existe, em vez de mostrar um código de erro.
 *
 * @param erro o que foi lançado
 * @param parte como a parte é chamada na frase, como `"O ranking"`
 */
export function mensagemIndisponivel(erro: unknown, parte: string): string {
    if (erro instanceof ErroApi && erro.status === NAO_ENCONTRADO) {
        return `${parte} ainda não está disponível nesta versão do jogo.`;
    }
    return erro instanceof Error ? erro.message : "Não foi possível falar com o jogo.";
}

async function buscarJson<T>(caminho: string, opcoes: RequestInit = {}): Promise<T> {
    return (await (await buscar(caminho, opcoes)).json()) as T;
}

/** Faz a requisição e transforma falha de rede ou resposta de erro em {@link ErroApi}. */
async function buscar(caminho: string, opcoes: RequestInit = {}): Promise<Response> {
    let resposta: Response;
    try {
        resposta = await fetch(caminho, {
            ...opcoes,
            headers: { Accept: "application/json", ...opcoes.headers },
        });
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
    return resposta;
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
