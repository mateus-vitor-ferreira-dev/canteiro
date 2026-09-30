<!-- Botões de toque para jogar no celular ou no tablet. Somem quando há mouse e teclado. -->
<script lang="ts">
    import type { Comando } from "@/api/protocolo";

    let {
        aoComando,
        pausada,
        treino = false,
        soPausa = false,
    }: {
        /** Chamado com o comando do botão tocado. */
        aoComando: (comando: Comando) => void;
        /** Se a partida está pausada, para o botão de pausa virar "continuar". */
        pausada: boolean;
        /** Se a partida é de treino: só aí o botão de desfazer aparece (RF26). */
        treino?: boolean;
        /** Numa repetição, o jogador só assiste: sobra o botão de pausa (RF24). */
        soPausa?: boolean;
    } = $props();

    /** Desenho de cada ícone, num quadro de 24 × 24. */
    const ICONES = {
        esquerda: "M15 5 8 12l7 7",
        direita: "m9 5 7 7-7 7",
        girar: "M20 12a8 8 0 1 1-2.34-5.66M20 4v5h-5",
        descer: "M12 5v14m-6-6 6 6 6-6",
        cair: "m6 5 6 6 6-6M6 12l6 6 6-6",
        reservar: "M4 9h16v10H4zM8 5h8M10 13h4",
        desfazer: "M9 14 4 9l5-5M4 9h10a6 6 0 0 1 0 12h-3",
        pausar: "M9 5v14M15 5v14",
        continuar: "M8 5v14l11-7z",
    };

    type Botao = { comando: Comando; rotulo: string; icone: string };

    const DESFAZER: Botao = {
        comando: "DESFAZER",
        rotulo: "Desfazer a última jogada",
        icone: ICONES.desfazer,
    };

    const BOTOES: Botao[] = [
        { comando: "ESQUERDA", rotulo: "Mover para a esquerda", icone: ICONES.esquerda },
        { comando: "GIRAR_HORARIO", rotulo: "Girar", icone: ICONES.girar },
        { comando: "DIREITA", rotulo: "Mover para a direita", icone: ICONES.direita },
        { comando: "DESCER", rotulo: "Descer uma linha", icone: ICONES.descer },
        { comando: "QUEDA_INSTANTANEA", rotulo: "Queda instantânea", icone: ICONES.cair },
        { comando: "RESERVAR", rotulo: "Guardar na reserva", icone: ICONES.reservar },
    ];

    const botoes = $derived.by(() => {
        if (soPausa) {
            return [];
        }
        return treino ? [...BOTOES, DESFAZER] : BOTOES;
    });
</script>

<div class="controles" role="group" aria-label="Controles de toque">
    {#each botoes as botao (botao.comando)}
        <button
            type="button"
            aria-label={botao.rotulo}
            disabled={pausada}
            onclick={() => aoComando(botao.comando)}
        >
            <svg viewBox="0 0 24 24" aria-hidden="true"><path d={botao.icone} /></svg>
        </button>
    {/each}
    <button
        type="button"
        aria-label={pausada ? "Continuar" : "Pausar"}
        onclick={() => aoComando(pausada ? "RETOMAR" : "PAUSAR")}
    >
        <svg viewBox="0 0 24 24" aria-hidden="true">
            <path d={pausada ? ICONES.continuar : ICONES.pausar} />
        </svg>
    </button>
</div>

<style>
    .controles {
        display: grid;
        grid-template-columns: repeat(auto-fit, minmax(44px, 56px));
        gap: 0.5rem;
        justify-content: center;
    }
    button {
        display: grid;
        place-items: center;
        min-width: 44px;
        min-height: 52px;
        padding: 0;
        color: #fff;
        background: var(--cor-marinho);
        border: 0;
        border-radius: 12px;
        cursor: pointer;
        touch-action: manipulation;
        user-select: none;
        -webkit-tap-highlight-color: transparent;
    }
    button:active:not(:disabled) {
        background: #2c4c86;
    }
    button:disabled {
        opacity: 0.45;
        cursor: not-allowed;
    }
    svg {
        width: 26px;
        height: 26px;
        fill: none;
        stroke: currentcolor;
        stroke-width: 2.4;
        stroke-linecap: round;
        stroke-linejoin: round;
    }
    /* Com mouse e teclado, as teclas já bastam. */
    @media (hover: hover) and (pointer: fine) {
        .controles {
            display: none;
        }
    }
</style>
