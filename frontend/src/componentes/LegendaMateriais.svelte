<!-- Os quatro materiais com textura, nome e peso: a cor nunca é a única pista (RNF06). -->
<script lang="ts">
    import { MATERIAIS } from "@/estilos/materiais";
    import Blocos from "./Blocos.svelte";
</script>

<section class="legenda" aria-label="Materiais">
    <h2>Materiais</h2>
    <ul>
        {#each MATERIAIS as material (material.codigo)}
            <li>
                <Blocos blocos={[[0, 0]]} material={material.codigo} lado={26} />
                <span class="nome">{material.nome}</span>
                <span class="peso">{material.peso}</span>
            </li>
        {/each}
    </ul>
</section>

<style>
    h2 {
        margin: 0 0 0.5rem;
        font-size: 0.8rem;
        letter-spacing: 0.08em;
        text-transform: uppercase;
        color: var(--cor-rotulo);
    }
    ul {
        display: grid;
        gap: 0.4rem;
        margin: 0 0 1.5rem;
        padding: 0;
        list-style: none;
    }
    li {
        display: grid;
        grid-template-columns: auto 1fr auto;
        align-items: center;
        gap: 0.6rem;
        font-size: 0.9rem;
    }
    li :global(canvas) {
        border-radius: 4px;
        background: var(--cor-tabuleiro);
    }
    .nome {
        font-weight: 700;
    }
    .peso {
        color: var(--cor-texto-suave);
    }
</style>
