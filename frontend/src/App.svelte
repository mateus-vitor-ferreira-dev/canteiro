<!-- Moldura do jogo. As telas passam a ser trocadas por estado/navegacao.svelte.ts quando houver mais de uma. -->
<script lang="ts">
    import NovaPartida from "./telas/NovaPartida.svelte";
</script>

<header>
    <h1>CANTEIRO</h1>
    <p>O jogo de encaixe em que a pilha tem peso e precisa ficar de pé.</p>
</header>

<main>
    <NovaPartida />
</main>

<style>
    header {
        padding: 1.5rem 3rem;
        background: var(--cor-marinho);
        color: #fff;
        border-bottom: 6px solid var(--cor-ambar);
    }
    h1 {
        margin: 0;
        font-size: 2rem;
        letter-spacing: -0.02em;
    }
    header p {
        margin: 0.25rem 0 0;
        color: #d5dcea;
    }
    main {
        padding: 2.5rem 3rem;
    }
</style>
