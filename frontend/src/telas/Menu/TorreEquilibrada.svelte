<!-- Uma torre em pirâmide, com o aço embaixo e a madeira em cima: o centro de massa fica no prumo do eixo da base. É o que o jogo ensina. -->
<script lang="ts">
    import type { Posicao } from "@/api/protocolo";
    import Blocos from "@/componentes/Blocos.svelte";

    /** Lado de cada bloco, em pixels. */
    let { lado }: { lado: number } = $props();

    type Peca = { material: string; blocos: Posicao[] };

    const COLUNAS = 10;
    const LINHAS = 9;
    /** Coluna do eixo da base: a torre é simétrica em torno dele. */
    const EIXO = 5;
    /** Altura do centro de massa, em linhas a partir do topo, calculada com as densidades dos materiais. */
    const ALTURA_CENTRO = 7.9;
    /** Intervalo entre a queda de uma peça e a da seguinte. */
    const PASSO_MS = 110;

    /** De baixo para cima, na ordem em que as peças caem. A última ainda está caindo. */
    const PECAS: readonly Peca[] = [
        { material: "ACO", blocos: linha(8, 1, 4) },
        { material: "ACO", blocos: linha(8, 5, 4) },
        { material: "ALVENARIA", blocos: quadrado(6, 2) },
        { material: "CONCRETO", blocos: quadrado(6, 4) },
        { material: "ALVENARIA", blocos: quadrado(6, 6) },
        { material: "MADEIRA", blocos: linha(5, 3, 4) },
        { material: "MADEIRA", blocos: quadrado(3, 4) },
        {
            material: "MADEIRA",
            blocos: [
                [0, 5],
                [1, 4],
                [1, 5],
                [1, 6],
            ],
        },
    ];

    function linha(l: number, inicio: number, tamanho: number): Posicao[] {
        return Array.from({ length: tamanho }, (_, i): Posicao => [l, inicio + i]);
    }

    function quadrado(l: number, c: number): Posicao[] {
        return [
            [l, c],
            [l, c + 1],
            [l + 1, c],
            [l + 1, c + 1],
        ];
    }

    function canto(blocos: Posicao[]): { topo: number; esquerda: number } {
        return {
            topo: Math.min(...blocos.map(([l]) => l)),
            esquerda: Math.min(...blocos.map(([, c]) => c)),
        };
    }
</script>

<div
    class="torre"
    role="img"
    aria-label="Torre de blocos em pirâmide, com o aço embaixo e a madeira em cima. O centro de massa fica sobre o eixo da base."
    style:--lado="{lado}px"
    style:width="{COLUNAS * lado}px"
    style:height="{LINHAS * lado}px"
>
    <span class="eixo" style:left="{EIXO * lado}px"></span>
    {#each PECAS as peca, i (i)}
        {@const { topo, esquerda } = canto(peca.blocos)}
        <span
            class="peca"
            class:caindo={i === PECAS.length - 1}
            style:top="{topo * lado}px"
            style:left="{esquerda * lado}px"
            style:--atraso="{i * PASSO_MS}ms"
        >
            <Blocos blocos={peca.blocos} material={peca.material} {lado} />
        </span>
    {/each}
    <span class="centro" style:left="{EIXO * lado}px" style:top="{ALTURA_CENTRO * lado}px"></span>
    <span class="base"></span>
</div>

<style>
    .torre {
        position: relative;
        flex: none;
    }
    .peca {
        position: absolute;
        animation: assentar 420ms cubic-bezier(0.5, 0, 0.75, 0) both;
        animation-delay: var(--atraso);
    }
    /* A última peça para no ar, ainda caindo: a obra continua. */
    .caindo {
        animation-name: surgir;
        animation-timing-function: ease-out;
    }
    .eixo {
        position: absolute;
        top: 0;
        bottom: 0;
        border-left: 2px dashed #f2b53a;
        transform: translateX(-1px);
    }
    .centro {
        position: absolute;
        width: calc(var(--lado) * 0.5);
        height: calc(var(--lado) * 0.5);
        background: #ff6b5e;
        border: 2px solid #fff;
        border-radius: 50%;
        transform: translate(-50%, -50%);
        animation: surgir 300ms ease-out both;
        animation-delay: 900ms;
    }
    .base {
        position: absolute;
        right: calc(var(--lado) * -0.5);
        bottom: -8px;
        left: calc(var(--lado) * -0.5);
        height: 6px;
        background: #3a4658;
        border-radius: 3px;
    }
    /* Movimento reduzido: a torre já aparece montada, sem esperar a vez de cada peça. */
    @media (prefers-reduced-motion: reduce) {
        .peca,
        .centro {
            animation: none;
        }
    }
    @keyframes assentar {
        from {
            opacity: 0;
            transform: translateY(calc(var(--lado) * -6));
        }
        30% {
            opacity: 1;
        }
    }
    @keyframes surgir {
        from {
            opacity: 0;
        }
    }
</style>
