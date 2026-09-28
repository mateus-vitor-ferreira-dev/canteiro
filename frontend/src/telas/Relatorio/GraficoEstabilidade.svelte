<!-- A estabilidade depois de cada peça, com os colapsos marcados (RF25). Os mesmos números estão na tabela logo abaixo. -->
<script lang="ts">
    import type { GraficoEstabilidade } from "./grafico";
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

    let canvas: HTMLCanvasElement | undefined = $state();

    $effect(() => {
        if (!canvas) {
            return;
        }
        const alvo = canvas;
        let grafico: GraficoEstabilidade | undefined;
        let desmontado = false;
        // O Chart.js só é baixado quando o relatório abre: a partida não carrega o peso dele.
        import("./grafico")
            .then(({ criarGrafico }) => {
                if (!desmontado) {
                    grafico = criarGrafico(alvo, pontos, colapsos);
                }
            })
            .catch(() => {
                // Sem Canvas (testes, navegador antigo): a tabela continua mostrando tudo.
            });
        return () => {
            desmontado = true;
            grafico?.destroy();
        };
    });
</script>

<figure>
    <figcaption>
        <span class="titulo">Estabilidade depois de cada peça</span>
        <span class="legenda">
            <span class="chave"><span class="linha" aria-hidden="true"></span>Estabilidade</span>
            <span class="chave"><span class="triangulo" aria-hidden="true"></span>Colapso</span>
        </span>
    </figcaption>
    <div
        class="area"
        role="img"
        aria-label="Gráfico da estabilidade, de 0 a 100 %, depois de cada uma das {pontos.length} peças, com {colapsos.length} colapsos marcados. Os números estão na tabela abaixo."
    >
        <canvas bind:this={canvas} aria-hidden="true"></canvas>
    </div>
</figure>

<style>
    figure {
        margin: 0;
    }
    figcaption {
        display: flex;
        flex-wrap: wrap;
        align-items: baseline;
        justify-content: space-between;
        gap: 0.25rem 1.5rem;
        margin-bottom: 0.75rem;
    }
    .titulo {
        font-weight: 700;
    }
    .legenda {
        display: flex;
        gap: 1rem;
        font-size: 0.875rem;
        color: var(--cor-texto-suave);
    }
    .chave {
        display: inline-flex;
        align-items: center;
        gap: 0.4rem;
    }
    .linha {
        width: 16px;
        height: 2px;
        background: var(--cor-serie);
        border-radius: 1px;
    }
    .triangulo {
        width: 0;
        height: 0;
        border-right: 6px solid transparent;
        border-bottom: 10px solid var(--cor-critico);
        border-left: 6px solid transparent;
    }
    /* A altura inclui os rótulos dos eixos: o gráfico nunca ganha rolagem própria. */
    .area {
        position: relative;
        height: clamp(220px, 40vh, 320px);
    }
</style>
