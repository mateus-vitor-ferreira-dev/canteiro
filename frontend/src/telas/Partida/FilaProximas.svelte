<!-- As próximas peças, com forma e material (RF05). -->
<script lang="ts">
    import type { PecaDto } from "@/api/protocolo";
    import { corDoMaterial } from "@/estilos/materiais";

    let { proximas }: { proximas: PecaDto[] } = $props();

    /** Índices das linhas e colunas do quadrado 4 × 4 em que a forma é desenhada. */
    const INDICES = [0, 1, 2, 3];

    function ocupada(peca: PecaDto, linha: number, coluna: number): boolean {
        return peca.blocos.some(([l, c]) => l === linha && c === coluna);
    }
</script>

<section class="fila" aria-label="Próximas peças">
    <h2>Próximas</h2>
    <ol>
        {#each proximas as peca, i (i)}
            <li aria-label="Peça {peca.forma} de {peca.material}">
                {#each INDICES as linha (linha)}
                    {#each INDICES as coluna (coluna)}
                        <span
                            class="celula"
                            style:background={ocupada(peca, linha, coluna)
                                ? corDoMaterial(peca.material)
                                : "transparent"}
                        ></span>
                    {/each}
                {/each}
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
        color: var(--cor-ambar);
    }
    ol {
        display: grid;
        gap: 0.75rem;
        margin: 0 0 1.5rem;
        padding: 0;
        list-style: none;
    }
    li {
        display: grid;
        grid-template-columns: repeat(4, 16px);
        grid-auto-rows: 16px;
        gap: 2px;
        padding: 0.5rem;
        width: max-content;
        background: var(--cor-cartao);
        border: 1px solid var(--cor-borda);
        border-radius: 8px;
    }
    .celula {
        border-radius: 3px;
    }
</style>
