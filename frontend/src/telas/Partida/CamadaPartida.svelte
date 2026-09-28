<!-- O aviso por cima do tabuleiro: partida perdida, reconectando, pausada ou encerrada. -->
<script lang="ts">
    import type { PartidaAoVivo } from "@/estado/partida.svelte";

    let {
        partida,
        aoSair,
    }: {
        /** A partida, de onde vem o que mostrar. */
        partida: PartidaAoVivo;
        /** Chamado quando o jogador sai para começar outra partida. */
        aoSair: () => void;
    } = $props();
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
    <div class="camada" role="status">
        <h2>Fim de jogo</h2>
        <p>{partida.estado.placar.pontuacao} pontos</p>
        <button type="button" onclick={aoSair}>Jogar de novo</button>
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
</style>
