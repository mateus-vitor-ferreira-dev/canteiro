<!-- Quantas peças de cada material (RF25). Uma série só, numa cor só: a amostra com a textura do jogo diz qual é o material. -->
<script lang="ts">
    import Blocos from "@/componentes/Blocos.svelte";
    import type { FatiaMaterial } from "./series";

    /** As barras, do material mais leve ao mais pesado. */
    let { fatias }: { fatias: FatiaMaterial[] } = $props();

    const maior = $derived(Math.max(1, ...fatias.map((f) => f.pecas)));
</script>

<figure>
    <figcaption>Peças por material</figcaption>
    <ul>
        {#each fatias as fatia (fatia.codigo)}
            <li>
                <span class="material">
                    <Blocos blocos={[[0, 0]]} material={fatia.codigo} lado={16} />
                    {fatia.nome}
                </span>
                <span class="trilho">
                    <span class="barra" style:width="{(fatia.pecas / maior) * 100}%"></span>
                </span>
                <span class="valor">
                    {fatia.pecas}
                    {fatia.pecas === 1 ? "peça" : "peças"}
                    <span class="fracao">{Math.round(fatia.fracao * 100)} %</span>
                </span>
            </li>
        {/each}
    </ul>
</figure>

<style>
    figure {
        margin: 0;
    }
    figcaption {
        margin-bottom: 0.75rem;
        font-weight: 700;
    }
    ul {
        display: grid;
        gap: 0.75rem;
        margin: 0;
        padding: 0;
        list-style: none;
    }
    li {
        display: grid;
        grid-template-columns: 7.5rem minmax(0, 1fr) auto;
        gap: 0.75rem;
        align-items: center;
    }
    .material {
        display: inline-flex;
        align-items: center;
        gap: 0.5rem;
        font-weight: 600;
    }
    .trilho {
        display: block;
    }
    /* Barra fina, com a ponta arredondada e a base reta, crescendo da mesma linha. */
    .barra {
        display: block;
        min-width: 2px;
        height: 16px;
        background: var(--cor-serie);
        border-radius: 0 4px 4px 0;
    }
    .valor {
        min-width: 6.5rem;
        font-weight: 600;
        text-align: right;
        font-variant-numeric: tabular-nums;
    }
    .fracao {
        margin-left: 0.25rem;
        font-weight: 400;
        color: var(--cor-texto-suave);
    }
</style>
