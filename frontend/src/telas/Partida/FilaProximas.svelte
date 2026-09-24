<!-- As próximas peças, com forma, textura e material (RF05). -->
<script lang="ts">
    import type { PecaDto } from "@/api/protocolo";
    import Blocos from "@/componentes/Blocos.svelte";
    import { infoDoMaterial } from "@/estilos/materiais";

    let { proximas }: { proximas: PecaDto[] } = $props();
</script>

<section class="fila" aria-label="Próximas peças">
    <h2>Próximas</h2>
    <ol>
        {#each proximas as peca, i (i)}
            {@const nome = infoDoMaterial(peca.material)?.nome ?? peca.material}
            <li role="img" aria-label="Peça {peca.forma} de {nome}">
                <Blocos blocos={peca.blocos} material={peca.material} lado={18} />
            </li>
        {/each}
    </ol>
</section>

<style>
    h2 {
        margin: 0 0 0.5rem;
        font-size: 0.8rem;
        letter-spacing: 0.08em;
        text-transform: uppercase;
        color: var(--cor-rotulo);
    }
    ol {
        display: flex;
        flex-wrap: wrap;
        gap: 0.75rem;
        margin: 0 0 1.5rem;
        padding: 0;
        list-style: none;
    }
    li {
        display: grid;
        place-items: center;
        width: calc(4 * 18px + 1rem);
        height: calc(2 * 18px + 1rem);
        background: var(--cor-tabuleiro);
        border-radius: 8px;
    }
</style>
