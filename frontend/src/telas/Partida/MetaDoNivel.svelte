<!-- A meta do nível (RN19): o que fazer, o progresso e o aviso de cumprida. -->
<script lang="ts">
    import type { MetaDto } from "@/api/protocolo";

    let { meta }: { meta: MetaDto } = $props();

    const percentual = $derived(Math.round((meta.progresso / meta.alvo) * 100));
</script>

<section class="meta" class:cumprida={meta.cumprida} aria-label="Meta do nível">
    <h2>Meta do nível</h2>
    <p class="descricao">{meta.descricao}</p>
    <div
        class="barra"
        role="progressbar"
        aria-label="Progresso da meta"
        aria-valuemin="0"
        aria-valuemax={meta.alvo}
        aria-valuenow={meta.progresso}
    >
        <div class="preenchimento" style:width="{percentual}%"></div>
    </div>
    <p class="progresso">
        {#if meta.cumprida}
            Cumprida! Bônus garantido
        {:else}
            {meta.progresso} de {meta.alvo}
        {/if}
    </p>
</section>

<style>
    .meta {
        margin: 0 0 1rem;
        padding: 0.75rem;
        background: var(--cor-cartao);
        border: 1px solid var(--cor-borda);
        border-radius: 8px;
    }
    .cumprida {
        border-color: var(--cor-verde);
    }
    h2 {
        margin: 0 0 0.25rem;
        font-size: 0.8rem;
        letter-spacing: 0.08em;
        text-transform: uppercase;
        color: var(--cor-rotulo);
    }
    .descricao {
        margin: 0 0 0.5rem;
        font-weight: 700;
        line-height: 1.3;
    }
    .barra {
        height: 8px;
        overflow: hidden;
        background: var(--cor-borda);
        border-radius: 4px;
    }
    .preenchimento {
        height: 100%;
        background: var(--cor-ambar);
        transition: width 200ms ease-out;
    }
    .cumprida .preenchimento {
        background: var(--cor-verde);
    }
    .progresso {
        margin: 0.25rem 0 0;
        font-size: 0.875rem;
        color: var(--cor-texto-suave);
    }
    .cumprida .progresso {
        font-weight: 700;
        color: var(--cor-verde);
    }
</style>
