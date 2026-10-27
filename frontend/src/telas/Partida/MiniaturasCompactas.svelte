<!-- No celular, ao lado do tabuleiro: a reserva e as duas próximas peças, em miniatura. -->
<script lang="ts">
    import type { EstadoDto } from "@/api/protocolo";
    import Blocos from "@/componentes/Blocos.svelte";
    import { infoDoMaterial } from "@/estilos/materiais";

    /** O último estado da partida. */
    let { estado }: { estado: EstadoDto } = $props();

    const LADO = 11;
    const nome = (material: string) => infoDoMaterial(material)?.nome ?? material;
</script>

<div class="miniaturas">
    <section aria-label="Reserva">
        <h2>Res.</h2>
        <div class="caixa" class:travada={!estado.podeReservar}>
            {#if estado.reservada}
                <span
                    role="img"
                    aria-label="Peça {estado.reservada.forma} de {nome(estado.reservada.material)}"
                >
                    <Blocos
                        blocos={estado.reservada.blocos}
                        material={estado.reservada.material}
                        lado={LADO}
                    />
                </span>
            {/if}
        </div>
    </section>
    <section aria-label="Próximas peças">
        <h2>Próx.</h2>
        <ol>
            {#each estado.proximas.slice(0, 2) as peca, i (i)}
                <li
                    class="caixa"
                    role="img"
                    aria-label="Peça {peca.forma} de {nome(peca.material)}"
                >
                    <Blocos blocos={peca.blocos} material={peca.material} lado={LADO} />
                </li>
            {/each}
        </ol>
    </section>
</div>

<style>
    .miniaturas {
        display: grid;
        align-content: start;
        gap: 0.75rem;
        width: 60px;
    }
    h2 {
        margin: 0 0 0.25rem;
        font-size: 0.65rem;
        letter-spacing: 0.06em;
        text-transform: uppercase;
        color: var(--cor-rotulo);
    }
    ol {
        display: grid;
        gap: 0.35rem;
        margin: 0;
        padding: 0;
        list-style: none;
    }
    .caixa {
        display: grid;
        place-items: center;
        width: 60px;
        height: 36px;
        background: var(--cor-tabuleiro);
        border-radius: 6px;
    }
    .travada {
        opacity: 0.45;
    }
</style>
