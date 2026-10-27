<!-- Liga e desliga os efeitos sonoros (RF28). A tecla M faz o mesmo. -->
<script lang="ts">
    import type { Som } from "@/estado/som.svelte";

    /** Os sons da partida. */
    let { som }: { som: Som } = $props();

    /** Alto-falante; com ondas quando ligado, com um X quando desligado. */
    const CAIXA = "M4 9h4l5-4v14l-5-4H4z";
    const ONDAS = "M16.5 8.5a5 5 0 0 1 0 7M19 6a8.5 8.5 0 0 1 0 12";
    const MUDO = "m17 9 5 6m0-6-5 6";
</script>

<!-- O nome para o leitor de tela fica fixo ("Som"); o estado vai no aria-pressed. -->
<button type="button" aria-label="Som" aria-pressed={som.ligado} onclick={() => som.alternar()}>
    <svg viewBox="0 0 24 24" aria-hidden="true">
        <path d={CAIXA} />
        <path d={som.ligado ? ONDAS : MUDO} />
    </svg>
    <span>{som.ligado ? "Som ligado" : "Som desligado"}</span>
    <kbd aria-hidden="true">M</kbd>
</button>

<style>
    button {
        display: inline-flex;
        align-items: center;
        gap: 0.5rem;
        min-height: 44px;
        margin: 0 0 1.25rem;
        padding: 0.4rem 0.75rem;
        font: inherit;
        font-size: 0.9rem;
        font-weight: 600;
        color: var(--cor-marinho);
        background: var(--cor-cartao);
        border: 1px solid var(--cor-borda);
        border-radius: 999px;
        cursor: pointer;
        touch-action: manipulation;
    }
    /* Desligado, o botão apaga: dá para ver de longe que o jogo está mudo. */
    button[aria-pressed="false"] {
        color: var(--cor-texto-suave);
        background: none;
        border-style: dashed;
    }
    svg {
        width: 20px;
        height: 20px;
        fill: none;
        stroke: currentcolor;
        stroke-width: 2;
        stroke-linecap: round;
        stroke-linejoin: round;
    }
    kbd {
        padding: 0 0.35rem;
        font-family: inherit;
        font-size: 0.75rem;
        color: var(--cor-texto-suave);
        border: 1px solid var(--cor-borda);
        border-radius: 4px;
    }
</style>
