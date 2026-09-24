<!-- Pontos, linhas, nível e colapsos, e a estabilidade em números. O painel completo vem na #29. -->
<script lang="ts">
    import type { EstabilidadeDto, PlacarDto } from "@/api/protocolo";
    import { formatarColunas } from "@/util/formatacao";

    let { placar, estabilidade }: { placar: PlacarDto; estabilidade: EstabilidadeDto } = $props();

    const pontos = new Intl.NumberFormat("pt-BR");
    const indice = $derived(Math.round(estabilidade.indice * 100));
</script>

<section class="placar" aria-label="Placar">
    <h2>Pontuação</h2>
    <p class="pontos">{pontos.format(placar.pontuacao)}</p>
    <dl>
        <dt>Nível</dt>
        <dd>{placar.nivel}</dd>
        <dt>Linhas</dt>
        <dd>{placar.linhas}</dd>
        <dt>Colapsos</dt>
        <dd>{placar.colapsos}</dd>
    </dl>
    <h2>Estabilidade</h2>
    <p class="indice" class:alerta={estabilidade.alerta}>{indice} %</p>
    <dl>
        <dt>Desvio</dt>
        <dd>{formatarColunas(estabilidade.desvio)}</dd>
        <dt>Limite</dt>
        <dd>{formatarColunas(estabilidade.limite)}</dd>
    </dl>
</section>

<style>
    .placar {
        min-width: 200px;
    }
    h2 {
        margin: 0 0 0.25rem;
        font-size: 0.8rem;
        letter-spacing: 0.08em;
        text-transform: uppercase;
        color: var(--cor-ambar);
    }
    .pontos,
    .indice {
        margin: 0 0 1rem;
        font-size: 2rem;
        font-weight: 800;
        color: var(--cor-marinho);
    }
    .indice.alerta {
        color: var(--cor-vermelho);
    }
    dl {
        display: grid;
        grid-template-columns: auto auto;
        gap: 0.25rem 1rem;
        margin: 0 0 1.5rem;
    }
    dt {
        color: var(--cor-texto-suave);
    }
    dd {
        margin: 0;
        font-weight: 700;
        text-align: right;
    }
</style>
