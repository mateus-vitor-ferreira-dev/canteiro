import type { PlacarDto } from "@/api/protocolo";

/**
 * As telas do jogo. A partida leva o id que o backend devolveu ao criá-la; o
 * fim leva também o placar final; o ranking pode destacar um nome.
 */
export type Tela =
    | { nome: "menu" }
    | { nome: "nova-partida" }
    | { nome: "partida"; id: string }
    | { nome: "fim"; id: string; placar: PlacarDto }
    | { nome: "ranking"; destaque?: string }
    | { nome: "repeticoes" }
    | { nome: "configuracoes" }
    | { nome: "saida" };

/** Nome de cada tela, sem os dados. */
export type NomeTela = Tela["nome"];

/** O pedaço do histórico do navegador que a navegação usa: os testes trocam por um falso. */
export type Historico = Pick<History, "pushState" | "replaceState">;

/**
 * Qual tela está aberta (RF01). Não há biblioteca de rotas: trocar de tela é
 * trocar este estado, e o `App` mostra a tela certa. Nada recarrega a página.
 *
 * Cada troca entra no histórico do navegador, então o botão voltar volta
 * uma tela, em vez de sair do jogo.
 */
export class Navegacao {
    /** A tela aberta agora. O jogo começa no menu. */
    tela = $state<Tela>({ nome: "menu" });

    readonly #historico: Historico;

    constructor(historico: Historico = window.history) {
        this.#historico = historico;
        this.#historico.replaceState({ tela: $state.snapshot(this.tela) }, "");
    }

    /** Abre outra tela e a registra no histórico. */
    ir(tela: Tela): void {
        this.tela = tela;
        // O navegador só guarda objetos simples: o estado reativo do Svelte não pode ser copiado.
        this.#historico.pushState({ tela: $state.snapshot(tela) }, "");
    }

    /** Volta para o menu principal. */
    voltarAoMenu(): void {
        this.ir({ nome: "menu" });
    }

    /**
     * Mostra a tela guardada no histórico, quando o jogador usa o voltar ou o
     * avançar do navegador. Sem tela guardada, volta ao menu.
     *
     * @param estado o `state` do evento `popstate`
     */
    restaurar(estado: unknown): void {
        const tela = (estado as { tela?: Tela } | null)?.tela;
        this.tela = tela ?? { nome: "menu" };
    }
}
