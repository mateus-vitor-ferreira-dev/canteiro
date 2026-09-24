<!-- O índice de estabilidade, com alerta antes do limite (RF15, RF16). -->
<script lang="ts">
    import type { EstabilidadeDto } from "@/api/protocolo";
    import { formatarColunas } from "@/util/formatacao";

    /** Estabilidade da estrutura, como veio na última mensagem do backend. */
    let { estabilidade }: { estabilidade: EstabilidadeDto } = $props();

    const percentual = $derived(Math.round(estabilidade.indice * 100));
    const lado = $derived(estabilidade.centroDeMassa < estabilidade.eixo ? "esquerda" : "direita");
    /** Onde o centro de massa fica no medidor: 0 % no limite da esquerda, 50 % no eixo, 100 % no da direita. */
    const ponteiro = $derived(
        Math.min(
            100,
            Math.max(
                0,
                50 + ((estabilidade.centroDeMassa - estabilidade.eixo) / estabilidade.limite) * 50,
            ),
        ),
    );
</script>

<section class="painel" class:alerta={estabilidade.alerta} aria-label="Estabilidade">
    <h2>Estabilidade</h2>
    <p class="percentual">{percentual} %</p>
    <div
        class="barra"
        role="meter"
        aria-label="Índice de estabilidade"
        aria-valuemin="0"
        aria-valuemax="100"
        aria-valuenow={percentual}
    >
        <!-- O degradê ocupa a barra inteira: a cor do fim do preenchimento diz onde ele parou. -->
        <div
            class="preenchimento"
            style:width="{percentual}%"
            style:background-size="{10000 / Math.max(percentual, 1)}% 100%"
        ></div>
    </div>

    <div class="medidor" aria-hidden="true">
        <span class="eixo"></span>
        <span class="ponteiro" style:left="{ponteiro}%"></span>
    </div>

    <dl>
        <dt>Desvio</dt>
        <dd>{formatarColunas(estabilidade.desvio)}</dd>
        <dt>Limite</dt>
        <dd>{formatarColunas(estabilidade.limite)}</dd>
    </dl>

    {#if estabilidade.alerta}
        <p class="aviso" role="alert">Atenção: carga demais à {lado}!</p>
    {/if}
</section>

<style>
    h2 {
        margin: 0 0 0.25rem;
        font-size: 0.8rem;
        letter-spacing: 0.08em;
        text-transform: uppercase;
        color: var(--cor-rotulo);
    }
    .percentual {
        margin: 0 0 0.5rem;
        font-size: 2rem;
        font-weight: 800;
        color: var(--cor-marinho);
    }
    .alerta .percentual,
    .aviso {
        color: var(--cor-vermelho);
    }
    .barra {
        height: 12px;
        overflow: hidden;
        background: var(--cor-borda);
        border-radius: 6px;
    }
    .preenchimento {
        height: 100%;
        background: linear-gradient(
            90deg,
            var(--cor-vermelho),
            var(--cor-ambar) 40%,
            var(--cor-verde)
        );
        border-radius: 6px;
        transition: width 120ms linear;
    }
    .medidor {
        position: relative;
        height: 18px;
        margin: 0.75rem 0;
        border-bottom: 2px solid var(--cor-borda);
    }
    .eixo {
        position: absolute;
        left: 50%;
        top: 0;
        bottom: -2px;
        border-left: 2px dashed var(--cor-rotulo);
    }
    .ponteiro {
        position: absolute;
        bottom: -7px;
        width: 12px;
        height: 12px;
        margin-left: -6px;
        background: var(--cor-vermelho);
        border: 2px solid #fff;
        border-radius: 50%;
        transition: left 120ms linear;
    }
    dl {
        display: grid;
        grid-template-columns: auto auto;
        gap: 0.25rem 1rem;
        margin: 0 0 0.75rem;
    }
    dt {
        color: var(--cor-texto-suave);
    }
    dd {
        margin: 0;
        font-weight: 700;
        text-align: right;
    }
    .aviso {
        margin: 0;
        font-weight: 700;
        animation: pulsar 0.8s ease-in-out infinite alternate;
    }
    @keyframes pulsar {
        from {
            opacity: 1;
        }
        to {
            opacity: 0.45;
        }
    }
    /* No celular, o painel encolhe para o tabuleiro caber na tela. */
    @media (max-width: 767px) {
        h2 {
            font-size: 0.75rem;
        }
        .percentual {
            margin-bottom: 0.25rem;
            font-size: 1.5rem;
        }
        .medidor {
            margin: 0.4rem 0;
        }
        dl {
            margin-bottom: 0;
            gap: 0 0.5rem;
            font-size: 0.875rem;
        }
    }
</style>
