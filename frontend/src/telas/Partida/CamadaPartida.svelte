<!-- O aviso por cima do tabuleiro: partida perdida, reconectando, pausada ou encerrada. -->
<script lang="ts">
    import type { PlacarDto } from "@/api/protocolo";
    import type { PartidaAoVivo } from "@/estado/partida.svelte";

    let {
        partida,
        aoSair,
        aoTerminar,
    }: {
        /** A partida, de onde vem o que mostrar. */
        partida: PartidaAoVivo;
        /** Chamado quando o jogador sai para começar outra partida. */
        aoSair: () => void;
        /** Chamado com o placar final, quando o jogador quer ver o resultado. */
        aoTerminar: (placar: PlacarDto) => void;
    } = $props();

    const pontos = new Intl.NumberFormat("pt-BR");

    let verResultado: HTMLButtonElement | undefined = $state();

    // A partida acabou no meio de uma jogada pelo teclado: Enter já leva ao resultado.
    $effect(() => verResultado?.focus());
</script>

{#if partida.perdida}
    <div class="camada" role="alert">
        <h2>Partida perdida</h2>
        <p>O jogo foi reiniciado e esta partida não existe mais.</p>
        <button type="button" onclick={aoSair}>Começar outra</button>
    </div>
{:else if partida.reconectando}
    <div class="camada" role="status">
        <h2>Reconectando…</h2>
        <p>A partida está pausada e volta do mesmo ponto.</p>
    </div>
{:else if partida.pausada}
    <div class="camada" role="status">
        <h2>Pausado</h2>
        <p>Aperte <kbd>P</kbd> para continuar.</p>
    </div>
{:else if partida.encerrada && partida.estado}
    {@const placar = partida.estado.placar}
    <div class="camada" role="status">
        <h2>Fim de jogo</h2>
        <p>{pontos.format(placar.pontuacao)} pontos</p>
        <button bind:this={verResultado} type="button" onclick={() => aoTerminar(placar)}>
            Ver resultado
        </button>
        <button type="button" class="secundario" onclick={aoSair}>Jogar de novo</button>
    </div>
{/if}

<style>
    .camada {
        position: absolute;
        inset: 0;
        display: flex;
        flex-direction: column;
        align-items: center;
        justify-content: center;
        gap: 0.5rem;
        padding: 1.5rem;
        text-align: center;
        color: #fff;
        background: rgb(27 36 50 / 80%);
        border-radius: 8px;
    }
    h2 {
        margin: 0;
        font-size: clamp(1.4rem, 5vw, 2rem);
    }
    button {
        padding: 0.6rem 1.2rem;
        font: inherit;
        font-weight: 700;
        min-height: 44px;
        color: var(--cor-marinho);
        background: var(--cor-ambar);
        border: 0;
        border-radius: 8px;
        cursor: pointer;
    }
    .secundario {
        color: #fff;
        background: none;
        text-decoration: underline;
        text-underline-offset: 0.2em;
    }
</style>
