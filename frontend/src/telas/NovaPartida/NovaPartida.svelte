<!-- Escolha da dificuldade antes de começar a partida (RF02). -->
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

    /** Chamado com o id da partida criada. */
    let { aoCriar }: { aoCriar: (id: string) => void } = $props();

    const dificuldades = listarDificuldades();
    let criando = $state(false);
    let erroAoCriar = $state<string | null>(null);

    async function escolher(codigo: CodigoDificuldade) {
        criando = true;
        erroAoCriar = null;
        try {
            aoCriar((await criarPartida(codigo)).id);
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
    <h2>Escolha a dificuldade</h2>

    {#await dificuldades}
        <p class="aviso">Carregando…</p>
    {:then lista}
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
    .opcoes {
        display: grid;
        grid-template-columns: repeat(3, 1fr);
        gap: 1.25rem;
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
        border-color: var(--cor-ambar);
        box-shadow: 0 0 0 3px rgb(227 163 34 / 30%);
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
        color: var(--cor-ambar);
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
