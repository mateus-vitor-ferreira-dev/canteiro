<!-- Tela de uma mensagem só, com a volta para o menu: telas que ainda não existem e a saída do jogo. -->
<script lang="ts">
    let {
        titulo,
        texto,
        aoVoltar,
    }: {
        /** O que aconteceu, em poucas palavras. */
        titulo: string;
        /** O que o jogador pode fazer agora. */
        texto: string;
        /** Chamado quando o jogador volta ao menu. */
        aoVoltar: () => void;
    } = $props();
</script>

<section class="aviso">
    <h2 tabindex="-1">{titulo}</h2>
    <p>{texto}</p>
    <button type="button" onclick={aoVoltar}>Voltar ao menu</button>
</section>

<style>
    .aviso {
        display: grid;
        gap: 1rem;
        justify-items: start;
        max-width: 30rem;
        margin: 2rem auto;
    }
    h2 {
        margin: 0;
        font-family: var(--fonte-destaque);
        font-size: clamp(1.6rem, 4vw, 2.25rem);
        font-stretch: 125%;
        font-weight: 800;
        line-height: 1.1;
        color: var(--cor-marinho);
    }
    p {
        margin: 0;
        color: var(--cor-texto-suave);
    }
    button {
        min-height: 44px;
        padding: 0.6rem 1.2rem;
        font: inherit;
        font-weight: 700;
        color: #fff;
        background: var(--cor-marinho);
        border: 0;
        border-radius: 8px;
        cursor: pointer;
    }
</style>
