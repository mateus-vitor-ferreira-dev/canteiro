<!-- O tabuleiro em Canvas, redesenhado a cada quadro com o último estado recebido (RNF01), e a animação do colapso (RF17). -->
<script lang="ts">
    import type { EstadoDto, EventoDto, QuedaDto } from "@/api/protocolo";
    import {
        COLUNAS,
        DURACAO_COLAPSO_MS,
        LINHAS_VISIVEIS,
        desenharColapso,
        desenharTabuleiro,
    } from "./desenho";
    import { densidadeDaTela, prefereMenosMovimento } from "@/util/tela";

    let {
        estado,
        evento = null,
        celula = 28,
    }: {
        /** Último estado da partida, ou `null` antes da primeira mensagem. */
        estado: EstadoDto | null;
        /** Último evento recebido: um `COLAPSO` dispara a animação. */
        evento?: EventoDto | null;
        /** Lado de cada quadrado, em pixels. */
        celula?: number;
    } = $props();

    let canvas: HTMLCanvasElement | undefined = $state();
    const densidade = densidadeDaTela();
    const largura = $derived(COLUNAS * celula);
    const altura = $derived(LINHAS_VISIVEIS * celula);
    let colapso: { quedas: QuedaDto[]; inicio: number } | null = null;

    // Cada evento é um objeto novo: um COLAPSO recém-chegado começa a animação.
    // Com movimento reduzido, os blocos já aparecem no lugar, sem queda nem clarão.
    $effect(() => {
        if (
            evento?.evento === "COLAPSO" &&
            evento.dados.quedas?.length &&
            !prefereMenosMovimento()
        ) {
            colapso = { quedas: evento.dados.quedas, inicio: performance.now() };
        }
    });

    // O desenho não espera mensagem: a cada quadro, desenha o que tiver. Em máquina
    // lenta, cai a taxa de quadros da tela, nunca o ritmo da partida no backend.
    $effect(() => {
        const ctx = canvas?.getContext("2d");
        if (!ctx) {
            return;
        }
        let quadro = 0;
        const desenhar = (agora: number) => {
            // Desenha em pixels do CSS; a escala deixa o traço nítido em telas de alta densidade.
            ctx.setTransform(densidade, 0, 0, densidade, 0, 0);
            desenharTabuleiro(ctx, estado, celula);
            if (colapso && estado) {
                const progresso = (agora - colapso.inicio) / DURACAO_COLAPSO_MS;
                if (progresso < 1) {
                    desenharColapso(ctx, estado, colapso.quedas, progresso, celula);
                } else {
                    colapso = null;
                }
            }
            quadro = requestAnimationFrame(desenhar);
        };
        quadro = requestAnimationFrame(desenhar);
        return () => cancelAnimationFrame(quadro);
    });
</script>

<canvas
    bind:this={canvas}
    width={largura * densidade}
    height={altura * densidade}
    style:width="{largura}px"
    style:height="{altura}px"
    aria-label="Tabuleiro da partida"
></canvas>

<style>
    canvas {
        display: block;
        border-radius: 8px;
        box-shadow: 0 0 0 4px var(--cor-marinho);
    }
</style>
