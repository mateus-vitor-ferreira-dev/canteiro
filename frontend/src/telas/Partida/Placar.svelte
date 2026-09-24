<!-- Pontos, nível, linhas e colapsos. -->
<script lang="ts">
    import type { PlacarDto } from "@/api/protocolo";

    let { placar }: { placar: PlacarDto } = $props();

    const pontos = new Intl.NumberFormat("pt-BR");
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
    .pontos {
        margin: 0 0 1rem;
        font-size: 2rem;
        font-weight: 800;
        color: var(--cor-marinho);
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
