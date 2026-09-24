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
    let colapso: { quedas: QuedaDto[]; inicio: number } | null = null;

    // Cada evento é um objeto novo: um COLAPSO recém-chegado começa a animação.
    $effect(() => {
        if (evento?.evento === "COLAPSO" && evento.dados.quedas?.length) {
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
    width={COLUNAS * celula}
    height={LINHAS_VISIVEIS * celula}
    aria-label="Tabuleiro da partida"
></canvas>

<style>
    canvas {
        display: block;
        border-radius: 8px;
        box-shadow: 0 0 0 4px var(--cor-marinho);
    }
</style>
