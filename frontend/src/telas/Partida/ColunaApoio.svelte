<!-- A coluna de apoio da partida: som, reserva, próximas peças e as legendas. -->
<script lang="ts">
    import type { EstadoDto } from "@/api/protocolo";
    import LegendaMateriais from "@/componentes/LegendaMateriais.svelte";
    import type { Som } from "@/estado/som.svelte";
    import BotaoSom from "./BotaoSom.svelte";
    import FilaProximas from "./FilaProximas.svelte";
    import LegendaTeclas from "./LegendaTeclas.svelte";
    import PecaReservada from "./PecaReservada.svelte";

    let {
        estado,
        som,
        treino = false,
        repeticao = false,
    }: {
        /** O último estado da partida; `null` antes da primeira mensagem. */
        estado: EstadoDto | null;
        /** Os sons da partida. */
        som: Som;
        /** Se a partida é de treino, para a legenda mostrar o desfazer. */
        treino?: boolean;
        /** Se é uma repetição: a legenda mostra só a pausa e o som. */
        repeticao?: boolean;
    } = $props();
</script>

<aside>
    <BotaoSom {som} />
    {#if estado}
        <PecaReservada peca={estado.reservada} liberada={estado.podeReservar} />
        <FilaProximas proximas={estado.proximas} />
    {/if}
    <LegendaMateriais />
    <LegendaTeclas {treino} {repeticao} />
</aside>

<style>
    /* A área "aside" é do grid da partida: aqui só se diz onde a coluna entra. */
    aside {
        grid-area: aside;
        width: 100%;
    }
</style>
