<!-- O volume dos efeitos, de 0 a 100 %. Soltar a barra toca uma amostra. -->
<script lang="ts">
    import type { EventoDto } from "@/api/protocolo";
    import { Som } from "@/estado/som.svelte";

    /** Volume no rascunho, de 0 a 100. */
    let { volume = $bindable() }: { volume: number } = $props();

    const PORCENTO = 100;
    const AMOSTRA: EventoDto = { tipo: "EVENTO", evento: "PECA_FIXADA", dados: {} };

    // A amostra toca mesmo com o som da partida desligado: é para ouvir o volume.
    const amostra = new Som(undefined, null, () => volume / PORCENTO);
</script>

<div class="grupo">
    <h3>Som</h3>
    <label for="volume">Volume dos efeitos</label>
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
        Solte a barra para ouvir. O botão Som, na partida, liga e desliga.
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
