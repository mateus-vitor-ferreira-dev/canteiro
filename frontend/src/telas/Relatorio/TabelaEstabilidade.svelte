<!-- Os números do gráfico em tabela: o mesmo conteúdo, sem depender de ver o gráfico nem de passar o mouse. -->
<script lang="ts">
    import type { Ponto } from "./series";

    let {
        pontos,
        colapsos,
    }: {
        /** Estabilidade depois de cada peça. */
        pontos: Ponto[];
        /** Os pontos em que a estrutura desabou. */
        colapsos: Ponto[];
    } = $props();

    const desabou = $derived(new Set(colapsos.map((c) => c.peca)));
</script>

<details>
    <summary>Ver os números em tabela</summary>
    <div class="rolagem">
        <table>
            <caption class="so-leitor">Estabilidade depois de cada peça</caption>
            <thead>
                <tr>
                    <th scope="col">Peça</th>
                    <th scope="col">Estabilidade</th>
                    <th scope="col">Colapso</th>
                </tr>
            </thead>
            <tbody>
                {#each pontos as ponto (ponto.peca)}
                    <tr class:critico={desabou.has(ponto.peca)}>
                        <td>{ponto.peca}</td>
                        <td>{ponto.estabilidade} %</td>
                        <td>{desabou.has(ponto.peca) ? "Desabou" : ""}</td>
                    </tr>
                {/each}
            </tbody>
        </table>
    </div>
</details>

<style>
    summary {
        display: inline-flex;
        align-items: center;
        min-height: 44px;
        font-weight: 600;
        color: var(--cor-marinho);
        cursor: pointer;
    }
    /* Partidas longas têm centenas de peças: a tabela rola dentro de si, com o cabeçalho fixo. */
    .rolagem {
        max-height: 20rem;
        overflow-y: auto;
        border: 1px solid var(--cor-borda);
        border-radius: 8px;
    }
    table {
        width: 100%;
        border-collapse: collapse;
        font-size: 0.9rem;
        font-variant-numeric: tabular-nums;
    }
    th {
        position: sticky;
        top: 0;
        padding: 0.5rem 0.75rem;
        text-align: left;
        font-weight: 600;
        color: var(--cor-texto-suave);
        background: var(--cor-fundo);
        border-bottom: 1px solid var(--cor-borda);
    }
    td {
        padding: 0.35rem 0.75rem;
        border-bottom: 1px solid var(--cor-borda);
    }
    .critico td {
        font-weight: 700;
        color: var(--cor-vermelho);
    }
</style>
