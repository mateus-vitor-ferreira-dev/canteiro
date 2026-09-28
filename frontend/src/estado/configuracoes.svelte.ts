import { ErroApi, lerConfiguracoes, salvarConfiguracoes } from "@/api/cliente";
import type { ConfiguracoesDto } from "@/api/protocolo";
import { TECLAS, montarTeclas, type AcaoTecla, type TeclasPersonalizadas } from "./teclado";

/** Volume inicial dos efeitos, de 0 a 100. */
export const VOLUME_PADRAO = 60;
const VOLUME_MAXIMO = 100;
const NAO_ENCONTRADO = 404;
const CHAVE = "canteiro.configuracoes";

/** Onde as configurações ficaram guardadas. */
export type Destino = "servidor" | "navegador";

/** As chamadas ao backend; os testes trocam por falsas. */
export type ApiConfiguracoes = {
    ler: () => Promise<ConfiguracoesDto>;
    salvar: (configuracoes: ConfiguracoesDto) => Promise<void>;
};

const ACOES = new Set<string>(TECLAS.map((t) => t.comando));

/**
 * As teclas e o volume do jogador (RF27), compartilhados pela partida, pela
 * legenda, pelos sons e pela tela de configurações.
 *
 * Moram no servidor (#71). Enquanto ele não tem a rota, ficam no navegador,
 * e o jogo continua funcionando com os valores padrão se nada der certo (RNF11).
 */
export class Configuracoes {
    /** As teclas que o jogador trocou. */
    teclas = $state<TeclasPersonalizadas>({});
    /** Volume dos efeitos, de 0 a 100. */
    volume = $state(VOLUME_PADRAO);
    /** A tabela de teclas em uso, já com as trocas. */
    readonly tabela = $derived(montarTeclas(this.teclas));

    readonly #api: ApiConfiguracoes;
    readonly #armazenamento: Pick<Storage, "getItem" | "setItem"> | null;

    constructor(
        api: ApiConfiguracoes = { ler: lerConfiguracoes, salvar: salvarConfiguracoes },
        armazenamento = armazenamentoDoNavegador(),
    ) {
        this.#api = api;
        this.#armazenamento = armazenamento;
    }

    /** Busca no servidor; se ele não tiver, no navegador; se nada servir, fica o padrão. */
    async carregar(): Promise<void> {
        try {
            this.#aplicar(validarConfiguracoes(await this.#api.ler()));
        } catch {
            this.#aplicar(this.#lerDoNavegador());
        }
    }

    /**
     * Salva e passa a usar. Sem a rota no servidor, salva no navegador.
     *
     * @returns onde ficou guardado
     * @throws ErroApi se o servidor recusar (tecla repetida, volume inválido) ou não responder
     */
    async salvar(teclas: TeclasPersonalizadas, volume: number): Promise<Destino> {
        const dados = validarConfiguracoes({ teclas, volume }) ?? {
            teclas: {},
            volume: VOLUME_PADRAO,
        };
        try {
            await this.#api.salvar(dados);
            this.#aplicar(dados);
            return "servidor";
        } catch (erro) {
            if (!(erro instanceof ErroApi && erro.status === NAO_ENCONTRADO)) {
                throw erro;
            }
            this.#armazenamento?.setItem(CHAVE, JSON.stringify(dados));
            this.#aplicar(dados);
            return "navegador";
        }
    }

    #aplicar(dados: ConfiguracoesDto | null): void {
        this.teclas = (dados?.teclas ?? {}) as TeclasPersonalizadas;
        this.volume = dados?.volume ?? VOLUME_PADRAO;
    }

    #lerDoNavegador(): ConfiguracoesDto | null {
        try {
            const texto = this.#armazenamento?.getItem(CHAVE);
            return texto ? validarConfiguracoes(JSON.parse(texto)) : null;
        } catch {
            return null;
        }
    }
}

/**
 * Confere o que veio de fora (servidor ou navegador) antes de usar (RNF12):
 * só ações conhecidas, teclas como texto, volume inteiro de 0 a 100. O que
 * não serve é descartado.
 *
 * @returns as configurações limpas, ou `null` se o formato nem for de configuração
 */
export function validarConfiguracoes(dado: unknown): ConfiguracoesDto | null {
    if (typeof dado !== "object" || dado === null) {
        return null;
    }
    const { teclas, volume } = dado as { teclas?: unknown; volume?: unknown };
    const limpas: Record<string, string[]> = {};
    if (typeof teclas === "object" && teclas !== null) {
        for (const [acao, codigos] of Object.entries(teclas)) {
            const validos = Array.isArray(codigos)
                ? codigos.filter((c) => typeof c === "string")
                : [];
            if (ACOES.has(acao) && validos.length > 0) {
                limpas[acao as AcaoTecla] = validos;
            }
        }
    }
    const numero = typeof volume === "number" && Number.isFinite(volume) ? volume : VOLUME_PADRAO;
    return { teclas: limpas, volume: Math.round(Math.min(VOLUME_MAXIMO, Math.max(0, numero))) };
}

function armazenamentoDoNavegador(): Pick<Storage, "getItem" | "setItem"> | null {
    try {
        return window.localStorage;
    } catch {
        return null;
    }
}

/** As configurações do jogo, uma só para todas as telas. */
export const configuracoes = new Configuracoes();
