<!-- As dez maiores pontuações (RF22). -->
<script lang="ts">
    import { listarRanking, mensagemIndisponivel } from "@/api/cliente";
    import LinhaRanking from "./LinhaRanking.svelte";

    let {
        destaque,
        aoJogar,
        aoVoltar,
    }: {
        /** Nome que acabou de ser registrado, para aparecer destacado. */
        destaque?: string;
        /** Chamado para começar uma partida. */
        aoJogar: () => void;
        /** Chamado para voltar ao menu. */
        aoVoltar: () => void;
    } = $props();

    let carregamento = $state(listarRanking());
</script>

<section class="ranking" aria-labelledby="titulo-ranking">
    <h2 id="titulo-ranking" tabindex="-1">Ranking</h2>

    {#await carregamento}
        <p class="aviso">Carregando…</p>
    {:then entradas}
        {#if entradas.length === 0}
            <div class="vazio">
                <p class="aviso">Ninguém entrou no ranking ainda.</p>
                <p class="aviso">Termine uma partida e salve seu nome para aparecer aqui.</p>
                <button type="button" class="principal" onclick={aoJogar}>Jogar agora</button>
            </div>
        {:else}
            <!-- role="list": o Safari tira a semântica de lista quando some o marcador. -->
            <ol role="list">
                {#each entradas as entrada, i (entrada.nome)}
                    <LinhaRanking {entrada} posicao={i + 1} destacada={entrada.nome === destaque} />
                {/each}
            </ol>
        {/if}
    {:catch erro}
        <div class="vazio">
            <p class="aviso erro" role="alert">{mensagemIndisponivel(erro, "O ranking")}</p>
            <button
                type="button"
                class="principal"
                onclick={() => (carregamento = listarRanking())}
            >
                Tentar de novo
            </button>
        </div>
    {/await}

    <button type="button" class="voltar" onclick={aoVoltar}>Voltar ao menu</button>
</section>

<style>
    .ranking {
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
    ol {
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
    button {
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
