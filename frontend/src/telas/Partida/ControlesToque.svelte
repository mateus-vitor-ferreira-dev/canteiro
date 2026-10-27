<!-- Botões de toque para jogar no celular ou no tablet, em duas linhas. Somem quando há mouse e teclado. -->
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

    /** `repete`: segurar o botão repete o comando, como segurar a tecla. `largo`: ocupa duas colunas. */
    type Botao = {
        comando: Comando;
        rotulo: string;
        icone: string;
        repete?: boolean;
        largo?: boolean;
    };

    /** Quanto o botão espera segurado antes de repetir, e o intervalo entre as repetições. */
    const ESPERA_REPETICAO_MS = 170;
    const INTERVALO_REPETICAO_MS = 55;

    const DESFAZER: Botao = {
        comando: "DESFAZER",
        rotulo: "Desfazer a última jogada",
        icone: ICONES.desfazer,
    };

    /** Primeira linha: mover, descer e girar. Segunda: reserva, queda (larga), desfazer no treino e pausa. */
    const MOVIMENTOS: Botao[] = [
        {
            comando: "ESQUERDA",
            rotulo: "Mover para a esquerda",
            icone: ICONES.esquerda,
            repete: true,
        },
        { comando: "DESCER", rotulo: "Descer uma linha", icone: ICONES.descer, repete: true },
        { comando: "DIREITA", rotulo: "Mover para a direita", icone: ICONES.direita, repete: true },
        { comando: "GIRAR_HORARIO", rotulo: "Girar", icone: ICONES.girar },
        { comando: "RESERVAR", rotulo: "Guardar na reserva", icone: ICONES.reservar },
    ];
    const QUEDA: Botao = {
        comando: "QUEDA_INSTANTANEA",
        rotulo: "Queda instantânea",
        icone: ICONES.cair,
    };

    const botoes = $derived.by(() => {
        if (soPausa) {
            return [];
        }
        return treino
            ? [...MOVIMENTOS, QUEDA, DESFAZER]
            : [...MOVIMENTOS, { ...QUEDA, largo: true }];
    });

    let espera: ReturnType<typeof setTimeout> | undefined;
    let repeticao: ReturnType<typeof setInterval> | undefined;

    function pararRepeticao() {
        clearTimeout(espera);
        clearInterval(repeticao);
    }

    $effect(() => pararRepeticao);

    /** Segurar o botão manda o comando na hora e, depois da espera, repete até soltar. */
    function aoSegurar(evento: PointerEvent, comando: Comando) {
        if (evento.button !== 0) {
            return;
        }
        pararRepeticao();
        aoComando(comando);
        espera = setTimeout(() => {
            repeticao = setInterval(() => aoComando(comando), INTERVALO_REPETICAO_MS);
        }, ESPERA_REPETICAO_MS);
    }

    /**
     * O clique do botão que repete só vale vindo do teclado (detail 0): pelo
     * dedo, o comando já saiu no pointerdown.
     */
    function aoClicar(evento: MouseEvent, botao: Botao) {
        if (!botao.repete || evento.detail === 0) {
            aoComando(botao.comando);
        }
    }
</script>

<div class="controles" role="group" aria-label="Controles de toque">
    {#each botoes as botao (botao.comando)}
        <button
            type="button"
            class:largo={botao.largo}
            aria-label={botao.rotulo}
            disabled={pausada}
            onpointerdown={botao.repete ? (e) => aoSegurar(e, botao.comando) : undefined}
            onpointerup={pararRepeticao}
            onpointerleave={pararRepeticao}
            onpointercancel={pararRepeticao}
            oncontextmenu={(e) => e.preventDefault()}
            onclick={(e) => aoClicar(e, botao)}
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
    /* Quatro colunas que dividem a largura toda: nunca vira uma coluna só. */
    .controles {
        display: grid;
        grid-template-columns: repeat(4, minmax(0, 1fr));
        gap: 0.5rem;
        width: 100%;
        max-width: 420px;
    }
    .largo {
        grid-column: span 2;
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
