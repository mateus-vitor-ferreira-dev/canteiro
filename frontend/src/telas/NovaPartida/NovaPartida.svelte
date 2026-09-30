<!-- Escolha da dificuldade e do modo treino antes de começar a partida (RF02, RF26). -->
<script lang="ts">
    import { criarPartida, listarDificuldades } from "@/api/cliente";
    import type { CodigoDificuldade } from "@/api/protocolo";
    import { formatarColunas, formatarSegundos } from "@/util/formatacao";

    const NOMES_MATERIAIS: Record<string, string> = {
        MADEIRA: "madeira",
        ALVENARIA: "alvenaria",
        CONCRETO: "concreto",
        ACO: "aço",
    };

    /** Chamado com o id da partida criada e se ela é de treino. */
    let { aoCriar }: { aoCriar: (id: string, treino: boolean) => void } = $props();

    const dificuldades = listarDificuldades();
    let criando = $state(false);
    let treino = $state(false);
    let erroAoCriar = $state<string | null>(null);

    async function escolher(codigo: CodigoDificuldade) {
        criando = true;
        erroAoCriar = null;
        try {
            aoCriar((await criarPartida(codigo, treino)).id, treino);
        } catch (erro) {
            erroAoCriar =
                erro instanceof Error ? erro.message : "Não foi possível criar a partida.";
        } finally {
            criando = false;
        }
    }

    function nomesDosMateriais(codigos: string[]): string {
        return codigos.map((codigo) => NOMES_MATERIAIS[codigo] ?? codigo).join(", ");
    }
</script>

<section class="nova-partida">
    <h2 tabindex="-1">Escolha a dificuldade</h2>

    {#await dificuldades}
        <p class="aviso">Carregando…</p>
    {:then lista}
        <label class="treino">
            <input type="checkbox" bind:checked={treino} disabled={criando} />
            <span>
                <strong>Modo treino</strong>
                <span class="explicacao">
                    Dá para desfazer a última jogada. Partidas de treino não entram no ranking.
                </span>
            </span>
        </label>
        <ul class="opcoes">
            {#each lista as dificuldade (dificuldade.codigo)}
                <li>
                    <button
                        type="button"
                        class="opcao"
                        disabled={criando}
                        onclick={() => escolher(dificuldade.codigo)}
                    >
                        <h3>{dificuldade.nome}</h3>
                        <dl>
                            <dt>Queda</dt>
                            <dd>
                                uma linha a cada {formatarSegundos(dificuldade.intervaloQuedaMs)}
                            </dd>
                            <dt>Limite de desvio</dt>
                            <dd>{formatarColunas(dificuldade.limiteDesvio)}</dd>
                            <dt>Materiais</dt>
                            <dd>{nomesDosMateriais(dificuldade.materiaisLiberados)}</dd>
                        </dl>
                    </button>
                </li>
            {/each}
        </ul>
        {#if erroAoCriar}
            <p class="aviso erro" role="alert">{erroAoCriar}</p>
        {/if}
    {:catch erro}
        <p class="aviso erro" role="alert">{erro.message}</p>
    {/await}
</section>

<style>
    .nova-partida h2 {
        margin: 0 0 1.5rem;
        color: var(--cor-marinho);
    }
    /* A linha inteira é clicável, com o alvo de toque de 44 px. */
    .treino {
        display: flex;
        align-items: flex-start;
        gap: 0.75rem;
        min-height: 44px;
        margin: 0 0 1.25rem;
        padding: 0.75rem 1rem;
        background: var(--cor-cartao);
        border: 1px solid var(--cor-borda);
        border-left: 6px solid var(--cor-ambar);
        border-radius: 12px;
        cursor: pointer;
    }
    .treino input {
        flex: none;
        width: 1.25rem;
        height: 1.25rem;
        margin: 0.15rem 0 0;
        accent-color: var(--cor-marinho);
    }
    .treino strong {
        display: block;
        color: var(--cor-marinho);
    }
    .explicacao {
        color: var(--cor-texto-suave);
    }
    .opcoes {
        display: grid;
        grid-template-columns: repeat(auto-fit, minmax(240px, 1fr));
        gap: 1.25rem;
        margin: 0;
        padding: 0;
        list-style: none;
    }
    .opcao {
        width: 100%;
        height: 100%;
        font: inherit;
        text-align: left;
        cursor: pointer;
        padding: 1.5rem;
        background: var(--cor-cartao);
        border: 1px solid var(--cor-borda);
        border-radius: 16px;
    }
    .opcao:hover:not(:disabled),
    .opcao:focus-visible {
        border-color: var(--cor-rotulo);
        box-shadow: 0 0 0 3px rgb(227 163 34 / 30%);
    }
    .opcao:disabled {
        cursor: progress;
    }
    .opcao h3 {
        margin: 0 0 1rem;
        color: var(--cor-marinho);
    }
    dl {
        margin: 0;
        display: grid;
        gap: 0.25rem;
    }
    dt {
        font-size: 0.75rem;
        font-weight: 700;
        letter-spacing: 0.08em;
        text-transform: uppercase;
        color: var(--cor-rotulo);
    }
    dd {
        margin: 0 0 0.5rem;
        color: var(--cor-texto-suave);
    }
    .aviso {
        color: var(--cor-texto-suave);
    }
    .erro {
        color: var(--cor-vermelho);
    }
</style>
