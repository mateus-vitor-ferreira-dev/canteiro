<!-- A faixa que lembra, durante a partida, que ela é uma repetição (RF24) ou de treino (RN15). -->
<script lang="ts">
    let {
        treino,
        repeticao,
    }: {
        /** Se a partida é de treino. */
        treino: boolean;
        /** Se é a reprodução de uma partida gravada. */
        repeticao: boolean;
    } = $props();

    const texto = $derived.by(() => {
        if (repeticao) {
            return "Repetição: dá para assistir e pausar, não para jogar";
        }
        return treino ? "Modo treino · esta partida não entra no ranking" : null;
    });
</script>

{#if texto}
    <p>{texto}</p>
{/if}

<style>
    p {
        margin: 0;
        padding: 0.25rem 0.75rem;
        font-size: 0.85rem;
        font-weight: 700;
        text-align: center;
        color: var(--cor-marinho);
        background: var(--cor-ambar);
        border-radius: 999px;
    }
</style>
