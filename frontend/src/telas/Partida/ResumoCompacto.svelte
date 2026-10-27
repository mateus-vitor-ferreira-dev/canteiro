<!-- No celular, uma faixa só com o essencial da partida: pontos, nível, linhas, estabilidade e a meta. -->
<script lang="ts">
    import type { EstadoDto } from "@/api/protocolo";
    import type { Som } from "@/estado/som.svelte";

    let {
        estado,
        som,
    }: {
        /** O último estado da partida. */
        estado: EstadoDto;
        /** Os sons, para o botão de som caber na faixa. */
        som: Som;
    } = $props();

    const pontos = new Intl.NumberFormat("pt-BR");
    const estabilidade = $derived(Math.round(estado.estabilidade.indice * 100));
    const previa = $derived(estado.estabilidade.previa);
    const previsto = $derived(previa ? Math.round(previa.indice * 100) : null);
    const bonus = $derived(1 + 0.1 * Math.min(estado.placar.sequencia, 10));
</script>

<section class="resumo" aria-label="Resumo da partida">
    <dl>
        <div class="pontos">
            <dt>Pontos</dt>
            <dd>{pontos.format(estado.placar.pontuacao)}</dd>
        </div>
        <div>
            <dt>Nível</dt>
            <dd>{estado.placar.nivel}</dd>
        </div>
        <div>
            <dt>Linhas</dt>
            <dd>{estado.placar.linhas}</dd>
        </div>
        <div class:alerta={estado.estabilidade.alerta || previa?.derruba}>
            <dt>Estab.</dt>
            <dd>
                {estabilidade}%{#if previsto !== null && previsto !== estabilidade}<span
                        class="previsto">→{previsto}%</span
                    >{/if}
            </dd>
        </div>
        {#if estado.placar.sequencia > 0}
            <div>
                <dt>Seq.</dt>
                <dd>×{bonus.toFixed(1).replace(".", ",")}</dd>
            </div>
        {/if}
    </dl>
    <button
        type="button"
        class="som"
        aria-label="Som"
        aria-pressed={som.ligado}
        onclick={() => som.alternar()}
    >
        <svg viewBox="0 0 24 24" aria-hidden="true">
            <path d="M4 9h4l5-4v14l-5-4H4z" />
            <path
                d={som.ligado
                    ? "M16.5 8.5a5 5 0 0 1 0 7M19 6a8.5 8.5 0 0 1 0 12"
                    : "m17 9 5 6m0-6-5 6"}
            />
        </svg>
    </button>
    {#if estado.meta}
        <p class="meta" class:cumprida={estado.meta.cumprida}>
            <span class="so-leitor">Meta do nível: </span>
            {estado.meta.descricao}
            <strong
                >{estado.meta.cumprida
                    ? "✓"
                    : `${estado.meta.progresso}/${estado.meta.alvo}`}</strong
            >
        </p>
    {/if}
</section>

<style>
    .resumo {
        display: grid;
        grid-template-columns: 1fr auto;
        align-items: center;
        gap: 0.25rem 0.5rem;
        width: 100%;
    }
    dl {
        display: flex;
        justify-content: space-between;
        gap: 0.5rem;
        margin: 0;
    }
    dl div {
        min-width: 0;
    }
    dt {
        font-size: 0.65rem;
        font-weight: 700;
        letter-spacing: 0.06em;
        text-transform: uppercase;
        color: var(--cor-rotulo);
    }
    dd {
        margin: 0;
        font-weight: 800;
        font-variant-numeric: tabular-nums;
        color: var(--cor-marinho);
        white-space: nowrap;
    }
    .pontos dd {
        font-size: 1.1rem;
    }
    .alerta dd {
        color: var(--cor-vermelho);
    }
    .previsto {
        margin-left: 0.15rem;
        font-size: 0.75rem;
        font-weight: 700;
    }
    .som {
        display: grid;
        place-items: center;
        width: 44px;
        height: 44px;
        padding: 0;
        color: var(--cor-marinho);
        background: none;
        border: 1px solid var(--cor-borda);
        border-radius: 8px;
    }
    .som svg {
        width: 22px;
        height: 22px;
        fill: none;
        stroke: currentcolor;
        stroke-width: 2;
        stroke-linecap: round;
        stroke-linejoin: round;
    }
    .meta {
        grid-column: 1 / -1;
        margin: 0;
        overflow: hidden;
        font-size: 0.75rem;
        color: var(--cor-texto-suave);
        white-space: nowrap;
        text-overflow: ellipsis;
    }
    .meta strong {
        color: var(--cor-marinho);
    }
    .cumprida strong {
        color: var(--cor-verde);
    }
</style>
