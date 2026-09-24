<!-- Escolha da dificuldade antes de começar a partida (RF02). -->
<script lang="ts">
    import { listarDificuldades } from "@/api/cliente";
    import { formatarColunas, formatarSegundos } from "@/util/formatacao";

    const NOMES_MATERIAIS: Record<string, string> = {
        MADEIRA: "madeira",
        ALVENARIA: "alvenaria",
        CONCRETO: "concreto",
        ACO: "aço",
    };

    const dificuldades = listarDificuldades();

    function nomesDosMateriais(codigos: string[]): string {
        return codigos.map((codigo) => NOMES_MATERIAIS[codigo] ?? codigo).join(", ");
    }
</script>

<section class="nova-partida">
    <h2>Escolha a dificuldade</h2>

    {#await dificuldades}
        <p class="aviso">Carregando…</p>
    {:then lista}
        <ul class="opcoes">
            {#each lista as dificuldade (dificuldade.codigo)}
                <li class="opcao">
                    <h3>{dificuldade.nome}</h3>
                    <dl>
                        <dt>Queda</dt>
                        <dd>uma linha a cada {formatarSegundos(dificuldade.intervaloQuedaMs)}</dd>
                        <dt>Limite de desvio</dt>
                        <dd>{formatarColunas(dificuldade.limiteDesvio)}</dd>
                        <dt>Materiais</dt>
                        <dd>{nomesDosMateriais(dificuldade.materiaisLiberados)}</dd>
                    </dl>
                </li>
            {/each}
        </ul>
    {:catch erro}
        <p class="aviso erro" role="alert">{erro.message}</p>
    {/await}
</section>

<style>
    .nova-partida h2 {
        margin: 0 0 1.5rem;
        color: var(--cor-marinho);
    }
    .opcoes {
        display: grid;
        grid-template-columns: repeat(3, 1fr);
        gap: 1.25rem;
        padding: 0;
        list-style: none;
    }
    .opcao {
        padding: 1.5rem;
        background: var(--cor-cartao);
        border: 1px solid var(--cor-borda);
        border-radius: 16px;
    }
    .opcao h3 {
        margin: 0 0 1rem;
        color: var(--cor-marinho);
    }
    dl {
        margin: 0;
        display: grid;
        gap: 0.25rem;
    }
    dt {
        font-size: 0.75rem;
        font-weight: 700;
        letter-spacing: 0.08em;
        text-transform: uppercase;
        color: var(--cor-ambar);
    }
    dd {
        margin: 0 0 0.5rem;
        color: var(--cor-texto-suave);
    }
    .aviso {
        color: var(--cor-texto-suave);
    }
    .erro {
        color: var(--cor-vermelho);
    }
</style>
