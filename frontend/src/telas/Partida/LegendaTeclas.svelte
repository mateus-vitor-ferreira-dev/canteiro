<!-- A legenda das teclas, sempre visível durante a partida (RNF05). Sai da mesma tabela que o teclado usa. -->
<script lang="ts">
    import { TECLAS } from "@/estado/teclado";
</script>

<section class="legenda" aria-label="Teclas">
    <h2>Teclas</h2>
    <dl>
        {#each TECLAS as tecla (tecla.rotulo)}
            <dt><kbd>{tecla.rotulo}</kbd></dt>
            <dd>{tecla.acao}</dd>
        {/each}
    </dl>
</section>

<style>
    h2 {
        margin: 0 0 0.5rem;
        font-size: 0.8rem;
        letter-spacing: 0.08em;
        text-transform: uppercase;
        color: var(--cor-ambar);
    }
    dl {
        display: grid;
        grid-template-columns: auto 1fr;
        gap: 0.35rem 0.75rem;
        margin: 0;
        font-size: 0.85rem;
    }
    dd {
        margin: 0;
        color: var(--cor-texto-suave);
    }
    kbd {
        display: inline-block;
        min-width: 1.6rem;
        padding: 0.1rem 0.4rem;
        text-align: center;
        font-family: inherit;
        font-weight: 700;
        background: var(--cor-cartao);
        border: 1px solid var(--cor-borda);
        border-bottom-width: 3px;
        border-radius: 6px;
    }
</style>
