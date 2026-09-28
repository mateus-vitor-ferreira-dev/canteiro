<!-- O resultado da partida (RF20) e o nome para o ranking (RF21). -->
<script lang="ts">
    import type { PlacarDto } from "@/api/protocolo";
    import { formatarDuracao } from "@/util/formatacao";
    import FormularioRanking from "./FormularioRanking.svelte";

    let {
        partidaId,
        placar,
        aoSalvo,
        aoJogarDeNovo,
        aoVerRelatorio,
        aoVoltar,
    }: {
        /** Partida encerrada. */
        partidaId: string;
        /** Placar final. */
        placar: PlacarDto;
        /** Chamado com o nome registrado no ranking. */
        aoSalvo: (nome: string) => void;
        /** Chamado para escolher a dificuldade de outra partida. */
        aoJogarDeNovo: () => void;
        /** Chamado para abrir o relatório da partida. */
        aoVerRelatorio: () => void;
        /** Chamado para voltar ao menu. */
        aoVoltar: () => void;
    } = $props();

    const numero = new Intl.NumberFormat("pt-BR");
</script>

<section class="fim" aria-labelledby="titulo-fim">
    <div class="resultado">
        <h2 id="titulo-fim" tabindex="-1">Obra encerrada</h2>
        <p class="pontuacao">
            <span class="numero">{numero.format(placar.pontuacao)}</span>
            <span class="unidade">pontos</span>
        </p>
        <dl>
            <div>
                <dt>Nível</dt>
                <dd>{placar.nivel}</dd>
            </div>
            <div>
                <dt>Linhas</dt>
                <dd>{placar.linhas}</dd>
            </div>
            <div>
                <dt>Colapsos</dt>
                <dd>{placar.colapsos}</dd>
            </div>
            <div>
                <dt>Tempo</dt>
                <dd>{formatarDuracao(placar.tempoSegundos)}</dd>
            </div>
        </dl>
    </div>

    <div class="acoes">
        <FormularioRanking {partidaId} {aoSalvo} />
        <div class="outras">
            <button type="button" class="jogar" onclick={aoJogarDeNovo}>Jogar de novo</button>
            <button type="button" class="relatorio" onclick={aoVerRelatorio}>Ver relatório</button>
            <button type="button" class="voltar" onclick={aoVoltar}>Voltar ao menu</button>
        </div>
    </div>
</section>

<style>
    .fim {
        display: grid;
        gap: 2rem;
        max-width: 60rem;
        margin: 0 auto;
    }
    h2 {
        margin: 0;
        font-family: var(--fonte-destaque);
        font-size: clamp(1.5rem, 4vw, 2rem);
        font-stretch: 125%;
        font-weight: 800;
        color: var(--cor-marinho);
    }
    .pontuacao {
        display: flex;
        flex-wrap: wrap;
        align-items: baseline;
        gap: 0.25rem 0.75rem;
        margin: 0.5rem 0 1.5rem;
        color: var(--cor-marinho);
    }
    /* A pontuação é o que o jogador veio ver: é ela que ocupa a tela. */
    .numero {
        font-family: var(--fonte-destaque);
        font-size: clamp(3.25rem, 13vw, 6rem);
        font-stretch: 125%;
        font-weight: 800;
        line-height: 1;
        font-variant-numeric: tabular-nums;
    }
    .unidade {
        font-size: 1.25rem;
        font-weight: 700;
    }
    dl {
        display: grid;
        grid-template-columns: repeat(2, minmax(0, 1fr));
        gap: 1rem;
        margin: 0;
        padding-top: 1rem;
        border-top: 4px solid var(--cor-ambar);
    }
    dt {
        font-size: 0.9rem;
        color: var(--cor-texto-suave);
    }
    dd {
        margin: 0;
        white-space: nowrap;
        font-size: 1.5rem;
        font-weight: 700;
        font-variant-numeric: tabular-nums;
    }
    /* Com espaço, os quatro números numa linha só. */
    @media (min-width: 480px) {
        dl {
            /* O tempo é o valor mais comprido: a última coluna cresce para ele. */
            grid-template-columns: repeat(3, minmax(0, 1fr)) minmax(max-content, 1.4fr);
        }
    }
    .acoes {
        display: grid;
        gap: 1.5rem;
        align-content: start;
    }
    .outras {
        display: flex;
        flex-wrap: wrap;
        align-items: center;
        gap: 0.5rem 1rem;
    }
    .outras button {
        min-height: 44px;
        padding: 0.6rem 1.2rem;
        font: inherit;
        font-weight: 700;
        border: 0;
        border-radius: 8px;
        cursor: pointer;
    }
    .jogar {
        color: #fff;
        background: var(--cor-marinho);
    }
    .relatorio {
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
    /* Computador: o resultado à esquerda e o que fazer agora à direita. */
    @media (min-width: 900px) {
        .fim {
            grid-template-columns: minmax(0, 1.2fr) minmax(0, 1fr);
            gap: 4rem;
            align-items: start;
        }
    }
</style>
