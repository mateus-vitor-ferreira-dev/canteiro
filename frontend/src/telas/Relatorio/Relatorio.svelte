<!-- Relatório da partida (RF25): como a estabilidade andou, onde a obra caiu e com quais materiais ela foi feita. -->
<script lang="ts">
    import { buscarRelatorio, mensagemIndisponivel } from "@/api/cliente";
    import BarrasMateriais from "./BarrasMateriais.svelte";
    import GraficoEstabilidade from "./GraficoEstabilidade.svelte";
    import TabelaEstabilidade from "./TabelaEstabilidade.svelte";
    import { distribuicao, pontosDaEstabilidade, pontosDosColapsos, resumir } from "./series";

    let {
        partidaId,
        aoVoltar,
        aoJogarDeNovo,
    }: {
        /** Partida encerrada. */
        partidaId: string;
        /** Chamado para voltar ao resultado da partida. */
        aoVoltar: () => void;
        /** Chamado para escolher a dificuldade de outra partida. */
        aoJogarDeNovo: () => void;
    } = $props();

    // O relatório é de uma partida só; outra partida é outra tela.
    // svelte-ignore state_referenced_locally
    let carregamento = $state(buscarRelatorio(partidaId));
</script>

<section class="relatorio" aria-labelledby="titulo-relatorio">
    <h2 id="titulo-relatorio" tabindex="-1">Relatório da obra</h2>

    {#await carregamento}
        <p class="aviso">Carregando…</p>
    {:then relatorio}
        {@const resumo = resumir(relatorio)}
        {#if resumo.pecas === 0}
            <p class="aviso">Nenhuma peça foi fixada nesta partida, então não há o que mostrar.</p>
        {:else}
            {@const pontos = pontosDaEstabilidade(relatorio)}
            {@const colapsos = pontosDosColapsos(relatorio)}
            <dl class="numeros">
                <div>
                    <dt>Peças fixadas</dt>
                    <dd>{resumo.pecas}</dd>
                </div>
                <div>
                    <dt>Estabilidade média</dt>
                    <dd>{resumo.media} %</dd>
                </div>
                {#if resumo.pior}
                    <div>
                        <dt>Mais perto de cair</dt>
                        <dd>
                            {resumo.pior.estabilidade} %
                            <span class="detalhe">na peça {resumo.pior.peca}</span>
                        </dd>
                    </div>
                {/if}
            </dl>

            <div class="grafico">
                <GraficoEstabilidade {pontos} {colapsos} />
                <TabelaEstabilidade {pontos} {colapsos} />
            </div>

            <BarrasMateriais fatias={distribuicao(relatorio)} />
        {/if}
    {:catch erro}
        <div class="falha">
            <p class="aviso erro" role="alert">{mensagemIndisponivel(erro, "O relatório")}</p>
            <button
                type="button"
                class="secundario"
                onclick={() => (carregamento = buscarRelatorio(partidaId))}
            >
                Tentar de novo
            </button>
        </div>
    {/await}

    <div class="acoes">
        <button type="button" class="primario" onclick={aoJogarDeNovo}>Jogar de novo</button>
        <button type="button" class="voltar" onclick={aoVoltar}>Voltar ao resultado</button>
    </div>
</section>

<style>
    .relatorio {
        display: grid;
        gap: 2rem;
        max-width: 52rem;
        margin: 0 auto;
    }
    h2 {
        margin: 0;
        font-family: var(--fonte-destaque);
        font-size: clamp(1.75rem, 4.5vw, 2.75rem);
        font-stretch: 125%;
        font-weight: 800;
        color: var(--cor-marinho);
    }
    .numeros {
        display: grid;
        grid-template-columns: repeat(auto-fit, minmax(8.5rem, 1fr));
        gap: 1rem 2rem;
        margin: 0;
        padding-bottom: 1.5rem;
        border-bottom: 1px solid var(--cor-borda);
    }
    dt {
        font-size: 0.9rem;
        color: var(--cor-texto-suave);
    }
    dd {
        margin: 0;
        font-size: 2rem;
        font-weight: 800;
        color: var(--cor-marinho);
    }
    .detalhe {
        font-size: 0.95rem;
        font-weight: 600;
        color: var(--cor-texto-suave);
    }
    .grafico {
        display: grid;
        gap: 0.5rem;
    }
    .falha {
        display: grid;
        gap: 0.75rem;
        justify-items: start;
    }
    .aviso {
        margin: 0;
        color: var(--cor-texto-suave);
    }
    .erro {
        font-weight: 600;
        color: var(--cor-vermelho);
    }
    .acoes {
        display: flex;
        flex-wrap: wrap;
        align-items: center;
        gap: 0.5rem 1rem;
    }
    button {
        min-height: 44px;
        padding: 0.6rem 1.2rem;
        font: inherit;
        font-weight: 700;
        border: 0;
        border-radius: 8px;
        cursor: pointer;
    }
    .primario {
        color: #fff;
        background: var(--cor-marinho);
    }
    .secundario {
        color: var(--cor-marinho);
        background: var(--cor-cartao);
        border: 1px solid var(--cor-borda);
    }
    .voltar {
        color: var(--cor-marinho);
        text-decoration: underline;
        text-underline-offset: 0.2em;
        background: none;
    }
</style>
