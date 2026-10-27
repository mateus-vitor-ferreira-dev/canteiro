<!-- O tabuleiro em Canvas, redesenhado a cada quadro com o último estado recebido (RNF01), a animação do colapso (RF17) e os gestos de toque. -->
<script lang="ts">
    import type { Comando, EstadoDto, EventoDto, QuedaDto } from "@/api/protocolo";
    import { ReconhecedorGestos } from "@/estado/gestos";
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
        aoComando,
    }: {
        /** Último estado da partida, ou `null` antes da primeira mensagem. */
        estado: EstadoDto | null;
        /** Último evento recebido: um `COLAPSO` dispara a animação. */
        evento?: EventoDto | null;
        /** Lado de cada quadrado, em pixels. */
        celula?: number;
        /** Recebe os comandos dos gestos no tabuleiro; sem ele, o tabuleiro não ouve o dedo. */
        aoComando?: (comando: Comando) => void;
    } = $props();

    // svelte-ignore state_referenced_locally
    const gestos = new ReconhecedorGestos(celula);
    $effect(() => {
        gestos.celula = celula;
    });

    /** O mouse já tem o teclado; os gestos são para o dedo e a caneta. */
    function doDedo(evento: PointerEvent): boolean {
        return Boolean(aoComando) && evento.pointerType !== "mouse";
    }

    function enviar(comandos: Comando[]) {
        for (const comando of comandos) {
            aoComando?.(comando);
        }
    }

    function aoEncostar(evento: PointerEvent) {
        if (doDedo(evento)) {
            canvas?.setPointerCapture?.(evento.pointerId);
            gestos.comecar(evento.clientX, evento.clientY, evento.timeStamp);
        }
    }

    function aoArrastar(evento: PointerEvent) {
        if (doDedo(evento)) {
            enviar(gestos.mover(evento.clientX, evento.clientY, evento.timeStamp));
        }
    }

    function aoSoltar(evento: PointerEvent) {
        if (doDedo(evento)) {
            enviar(gestos.terminar(evento.clientX, evento.clientY, evento.timeStamp));
        }
    }

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
    class:com-gestos={aoComando}
    aria-label="Tabuleiro da partida"
    onpointerdown={aoEncostar}
    onpointermove={aoArrastar}
    onpointerup={aoSoltar}
    onpointercancel={() => gestos.cancelar()}
></canvas>

<style>
    canvas {
        display: block;
        border-radius: 8px;
        box-shadow: 0 0 0 4px var(--cor-marinho);
    }
    /* O dedo no tabuleiro é jogada, não rolagem nem zoom da página. */
    .com-gestos {
        touch-action: none;
        user-select: none;
        -webkit-user-select: none;
    }
</style>
