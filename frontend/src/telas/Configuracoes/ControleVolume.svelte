<!-- O volume, de 0 a 100 %, e a música de fundo. Soltar a barra toca uma amostra. -->
<script lang="ts">
    import type { EventoDto } from "@/api/protocolo";
    import { Som } from "@/estado/som.svelte";

    let {
        volume = $bindable(),
        musica = $bindable(),
    }: {
        /** Volume no rascunho, de 0 a 100. */
        volume: number;
        /** Se a música de fundo toca, no rascunho. */
        musica: boolean;
    } = $props();

    const PORCENTO = 100;
    const AMOSTRA: EventoDto = { tipo: "EVENTO", evento: "PECA_FIXADA", dados: {} };

    // A amostra toca mesmo com o som da partida desligado: é para ouvir o volume.
    const amostra = new Som(undefined, null, () => volume / PORCENTO);
</script>

<div class="grupo">
    <h3>Som</h3>
    <label for="volume">Volume dos efeitos e da música</label>
    <div class="volume">
        <input
            id="volume"
            type="range"
            min="0"
            max="100"
            step="5"
            bind:value={volume}
            aria-describedby="dica-volume"
            onchange={() => amostra.tocar(AMOSTRA)}
        />
        <output for="volume">{volume} %</output>
    </div>
    <p id="dica-volume" class="dica">
        Solte a barra para ouvir. O botão Som, na partida, liga e desliga tudo.
    </p>
    <label class="opcao">
        <input type="checkbox" bind:checked={musica} aria-describedby="dica-musica" />
        Música de fundo
    </label>
    <p id="dica-musica" class="dica">
        Fica mais intensa quanto mais perto de perder. Desligada, os efeitos continuam.
    </p>
</div>

<style>
    .grupo {
        display: grid;
        gap: 0.5rem;
    }
    h3 {
        margin: 0;
        font-size: 1.15rem;
        color: var(--cor-marinho);
    }
    label {
        font-weight: 600;
    }
    .volume {
        display: flex;
        align-items: center;
        gap: 1rem;
    }
    .opcao {
        display: flex;
        align-items: center;
        gap: 0.75rem;
        min-height: 44px;
    }
    .opcao input {
        flex: none;
        width: 1.25rem;
        height: 1.25rem;
        min-height: auto;
    }
    input {
        flex: 1;
        min-height: 44px;
        accent-color: var(--cor-marinho);
    }
    output {
        min-width: 3.5rem;
        font-weight: 700;
        text-align: right;
        font-variant-numeric: tabular-nums;
    }
    .dica {
        margin: 0;
        font-size: 0.9rem;
        color: var(--cor-texto-suave);
    }
</style>
