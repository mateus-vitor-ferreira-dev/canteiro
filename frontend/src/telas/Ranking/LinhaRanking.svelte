<!-- Uma linha do ranking: posição, nome com os detalhes da partida e pontos. -->
<script lang="ts">
    import type { EntradaRankingDto } from "@/api/protocolo";
    import { formatarData } from "@/util/formatacao";

    let {
        entrada,
        posicao,
        destacada,
    }: {
        /** A pontuação registrada. */
        entrada: EntradaRankingDto;
        /** Posição no ranking, a partir de 1. */
        posicao: number;
        /** Se é o nome que acabou de ser registrado. */
        destacada: boolean;
    } = $props();

    const numero = new Intl.NumberFormat("pt-BR");
    /** Espaço que não quebra: "1 colapso" nunca se separa em duas linhas. */
    const JUNTO = "\u00a0";

    const detalhes = $derived(
        [
            `nível${JUNTO}${entrada.nivel}`,
            `${entrada.linhas}${JUNTO}${entrada.linhas === 1 ? "linha" : "linhas"}`,
            `${entrada.colapsos}${JUNTO}${entrada.colapsos === 1 ? "colapso" : "colapsos"}`,
            formatarData(entrada.data),
        ].join(", "),
    );
</script>

<li class:primeiro={posicao === 1} class:destacada aria-current={destacada ? "true" : undefined}>
    <span class="posicao" aria-hidden="true">{posicao}</span>
    <span class="quem">
        <span class="nome">
            {entrada.nome}
            {#if destacada}<span class="selo">Seu nome</span>{/if}
        </span>
        <span class="detalhes">{detalhes}</span>
    </span>
    <span class="pontos">
        {numero.format(entrada.pontuacao)} <span class="so-leitor">pontos</span>
    </span>
</li>

<style>
    li {
        display: grid;
        grid-template-columns: 2.75rem minmax(0, 1fr) auto;
        gap: 0.75rem;
        align-items: center;
        min-height: 3.75rem;
        padding: 0.75rem 0.5rem;
        border-bottom: 1px solid var(--cor-borda);
    }
    /* O primeiro lugar leva a faixa âmbar, como a base do cabeçalho. */
    .primeiro {
        box-shadow: inset 6px 0 0 var(--cor-ambar);
    }
    .destacada {
        background: #fbf0d6;
    }
    .posicao {
        font-family: var(--fonte-destaque);
        font-size: 1.5rem;
        font-stretch: 125%;
        font-weight: 800;
        color: var(--cor-marinho);
        text-align: center;
        font-variant-numeric: tabular-nums;
    }
    .quem {
        display: grid;
        min-width: 0;
    }
    .nome {
        display: flex;
        flex-wrap: wrap;
        align-items: center;
        gap: 0.5rem;
        font-weight: 700;
        overflow-wrap: anywhere;
    }
    .detalhes {
        font-size: 0.9rem;
        color: var(--cor-texto-suave);
    }
    .selo {
        padding: 0.05rem 0.5rem;
        font-size: 0.75rem;
        font-weight: 600;
        color: var(--cor-rotulo);
        border: 1px solid currentcolor;
        border-radius: 999px;
    }
    .pontos {
        font-size: 1.25rem;
        font-weight: 800;
        color: var(--cor-marinho);
        font-variant-numeric: tabular-nums;
    }
</style>
