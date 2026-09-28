<!-- A peça guardada na reserva (RF10), e se a troca ainda está liberada nesta peça (RN05). -->
<script lang="ts">
    import type { PecaDto } from "@/api/protocolo";
    import Blocos from "@/componentes/Blocos.svelte";
    import { infoDoMaterial } from "@/estilos/materiais";

    let {
        peca,
        liberada,
    }: {
        /** A peça guardada, ou `null` se o jogador ainda não reservou nenhuma. */
        peca: PecaDto | null;
        /** Se o jogador ainda pode trocar a peça atual pela reservada. */
        liberada: boolean;
    } = $props();

    const nome = $derived(peca ? (infoDoMaterial(peca.material)?.nome ?? peca.material) : "");
</script>

<section class="reserva" aria-label="Reserva">
    <h2>Reserva</h2>
    <div class="caixa" class:travada={!liberada}>
        {#if peca}
            <span role="img" aria-label="Peça {peca.forma} de {nome}">
                <Blocos blocos={peca.blocos} material={peca.material} lado={18} />
            </span>
        {:else}
            <span class="vazia">C guarda a peça</span>
        {/if}
    </div>
    {#if !liberada}
        <p>Troca liberada na próxima peça</p>
    {/if}
</section>

<style>
    h2 {
        margin: 0 0 0.5rem;
        font-size: 0.8rem;
        letter-spacing: 0.08em;
        text-transform: uppercase;
        color: var(--cor-rotulo);
    }
    .reserva {
        margin: 0 0 1.5rem;
    }
    .caixa {
        display: grid;
        place-items: center;
        width: calc(4 * 18px + 1rem);
        height: calc(2 * 18px + 1rem);
        background: var(--cor-tabuleiro);
        border-radius: 8px;
    }
    /* Depois da troca, a caixa fica apagada até a próxima peça. */
    .travada {
        opacity: 0.45;
    }
    /* Texto claro sobre o fundo escuro da caixa: o âmbar escuro dos rótulos não passaria de 4,5:1 aqui. */
    .vazia {
        padding: 0 0.25rem;
        font-size: 0.75rem;
        line-height: 1.3;
        text-align: center;
        color: #c9d1dd;
    }
    p {
        margin: 0.4rem 0 0;
        font-size: 0.75rem;
        color: var(--cor-rotulo);
    }
</style>
