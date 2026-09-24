<!-- Alguns blocos de um material, num Canvas pequeno: as próximas peças e as amostras da legenda. Decorativo: quem usa dá o rótulo. -->
<script lang="ts">
    import type { Posicao } from "@/api/protocolo";
    import { desenharBloco } from "@/estilos/materiais";
    import { densidadeDaTela } from "@/util/tela";

    let {
        blocos,
        material,
        lado = 16,
    }: {
        /** Posições dos blocos; o desenho começa no menor índice de linha e de coluna. */
        blocos: readonly Posicao[];
        /** Código do material. */
        material: string;
        /** Lado de cada bloco, em pixels. */
        lado?: number;
    } = $props();

    const linhaInicial = $derived(Math.min(...blocos.map(([l]) => l)));
    const colunaInicial = $derived(Math.min(...blocos.map(([, c]) => c)));
    const largura = $derived((Math.max(...blocos.map(([, c]) => c)) - colunaInicial + 1) * lado);
    const altura = $derived((Math.max(...blocos.map(([l]) => l)) - linhaInicial + 1) * lado);
    const densidade = densidadeDaTela();

    let canvas: HTMLCanvasElement | undefined = $state();

    $effect(() => {
        const ctx = canvas?.getContext("2d");
        if (!ctx) {
            return;
        }
        ctx.setTransform(densidade, 0, 0, densidade, 0, 0);
        ctx.clearRect(0, 0, largura, altura);
        for (const [l, c] of blocos) {
            const x = (c - colunaInicial) * lado;
            const y = (l - linhaInicial) * lado;
            desenharBloco(ctx, x + 1, y + 1, lado - 2, material);
        }
    });
</script>

<canvas
    bind:this={canvas}
    width={largura * densidade}
    height={altura * densidade}
    style:width="{largura}px"
    style:height="{altura}px"
    aria-hidden="true"
></canvas>

<style>
    canvas {
        display: block;
    }
</style>
