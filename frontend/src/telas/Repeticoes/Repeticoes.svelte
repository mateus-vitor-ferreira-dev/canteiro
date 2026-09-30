<!-- As partidas gravadas, para escolher qual assistir de novo (RF24). -->
<script lang="ts">
    import { listarRepeticoes, mensagemIndisponivel, reproduzirRepeticao } from "@/api/cliente";
    import type { RepeticaoDto } from "@/api/protocolo";
    import LinhaRepeticao from "./LinhaRepeticao.svelte";

    let {
        aoReproduzir,
        aoJogar,
        aoVoltar,
    }: {
        /** Chamado com o id da reprodução criada, para abrir o tabuleiro. */
        aoReproduzir: (id: string) => void;
        /** Chamado para começar uma partida. */
        aoJogar: () => void;
        /** Chamado para voltar ao menu. */
        aoVoltar: () => void;
    } = $props();

    let carregamento = $state(listarRepeticoes());
    let abrindo = $state(false);
    let erroAoAbrir = $state<string | null>(null);

    async function assistir(repeticao: RepeticaoDto) {
        abrindo = true;
        erroAoAbrir = null;
        try {
            aoReproduzir((await reproduzirRepeticao(repeticao.id)).id);
        } catch (erro) {
            erroAoAbrir = mensagemIndisponivel(erro, "Esta repetição");
        } finally {
            abrindo = false;
        }
    }
</script>

<section class="repeticoes" aria-labelledby="titulo-repeticoes">
    <h2 id="titulo-repeticoes" tabindex="-1">Repetições</h2>

    {#await carregamento}
        <p class="aviso">Carregando…</p>
    {:then repeticoes}
        {#if repeticoes.length === 0}
            <div class="vazio">
                <p class="aviso">Nenhuma partida gravada ainda.</p>
                <p class="aviso">Toda partida que chega ao fim fica guardada aqui.</p>
                <button type="button" class="principal" onclick={aoJogar}>Jogar agora</button>
            </div>
        {:else}
            <p class="aviso">Escolha uma partida para assistir de novo, jogada por jogada.</p>
            <!-- role="list": o Safari tira a semântica de lista quando some o marcador. -->
            <ul role="list" aria-busy={abrindo}>
                {#each repeticoes as repeticao (repeticao.id)}
                    <LinhaRepeticao {repeticao} ocupada={abrindo} aoEscolher={assistir} />
                {/each}
            </ul>
            {#if erroAoAbrir}
                <p class="aviso erro" role="alert">{erroAoAbrir}</p>
            {/if}
        {/if}
    {:catch erro}
        <div class="vazio">
            <p class="aviso erro" role="alert">
                {mensagemIndisponivel(erro, "A lista de repetições")}
            </p>
            <button
                type="button"
                class="principal"
                onclick={() => (carregamento = listarRepeticoes())}
            >
                Tentar de novo
            </button>
        </div>
    {/await}

    <button type="button" class="voltar" onclick={aoVoltar}>Voltar ao menu</button>
</section>

<style>
    .repeticoes {
        display: grid;
        gap: 1.25rem;
        max-width: 40rem;
        margin: 0 auto;
    }
    h2 {
        margin: 0;
        font-family: var(--fonte-destaque);
        font-size: clamp(1.75rem, 4.5vw, 2.75rem);
        font-stretch: 125%;
        font-weight: 800;
        color: var(--cor-marinho);
    }
    ul {
        margin: 0;
        padding: 0;
        list-style: none;
        border-top: 1px solid var(--cor-borda);
    }
    .vazio {
        display: grid;
        gap: 0.5rem;
        justify-items: start;
    }
    .aviso {
        margin: 0;
        color: var(--cor-texto-suave);
    }
    .erro {
        font-weight: 600;
        color: var(--cor-vermelho);
    }
    .principal,
    .voltar {
        min-height: 44px;
        padding: 0.6rem 1.2rem;
        font: inherit;
        font-weight: 700;
        border: 0;
        border-radius: 8px;
        cursor: pointer;
    }
    .principal {
        margin-top: 0.5rem;
        color: #fff;
        background: var(--cor-marinho);
    }
    .voltar {
        justify-self: start;
        padding: 0 0.5rem;
        color: var(--cor-marinho);
        text-decoration: underline;
        text-underline-offset: 0.2em;
        background: none;
    }
</style>
