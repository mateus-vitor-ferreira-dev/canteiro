<!-- O tabuleiro em Canvas, redesenhado a cada quadro com o último estado recebido (RNF01). -->
<script lang="ts">
    import type { EstadoDto } from "@/api/protocolo";
    import { COLUNAS, LINHAS_VISIVEIS, desenharTabuleiro } from "./desenho";

    /** Último estado da partida, ou `null` antes da primeira mensagem. */
    let { estado, celula = 28 }: { estado: EstadoDto | null; celula?: number } = $props();

    let canvas: HTMLCanvasElement | undefined = $state();

    // O desenho não espera mensagem: a cada quadro, desenha o que tiver. Em máquina
    // lenta, cai a taxa de quadros da tela, nunca o ritmo da partida no backend.
    $effect(() => {
        const ctx = canvas?.getContext("2d");
        if (!ctx) {
            return;
        }
        let quadro = 0;
        const desenhar = () => {
            desenharTabuleiro(ctx, estado, celula);
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
