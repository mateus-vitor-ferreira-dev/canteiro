<!-- A legenda das teclas, sempre visível durante a partida (RNF05). Sai da mesma tabela que o teclado usa, com as teclas que o jogador trocou. -->
<script lang="ts">
    import { configuracoes } from "@/estado/configuracoes.svelte";
    import { teclasDaRepeticao, teclasDoModo } from "@/estado/teclado";

    let {
        treino = false,
        repeticao = false,
    }: {
        /** Se a partida é de treino: só aí a tecla de desfazer aparece. */
        treino?: boolean;
        /** Se é uma repetição: só a pausa e o som valem. */
        repeticao?: boolean;
    } = $props();

    const teclas = $derived(
        repeticao
            ? teclasDaRepeticao(configuracoes.tabela)
            : teclasDoModo(configuracoes.tabela, treino),
    );
</script>

<section class="legenda" aria-label="Teclas">
    <h2>Teclas</h2>
    <dl>
        {#each teclas as tecla (tecla.comando)}
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
        color: var(--cor-rotulo);
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
