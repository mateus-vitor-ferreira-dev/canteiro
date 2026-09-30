<!-- Uma partida gravada: a linha inteira é o botão que abre a reprodução. -->
<script lang="ts">
    import type { CodigoDificuldade, RepeticaoDto } from "@/api/protocolo";
    import { formatarDataHora } from "@/util/formatacao";

    let {
        repeticao,
        ocupada,
        aoEscolher,
    }: {
        /** A partida gravada. */
        repeticao: RepeticaoDto;
        /** Se uma reprodução já está sendo aberta: a linha espera. */
        ocupada: boolean;
        /** Chamado quando o jogador escolhe assistir a esta partida. */
        aoEscolher: (repeticao: RepeticaoDto) => void;
    } = $props();

    const NOMES: Record<CodigoDificuldade, string> = {
        FACIL: "Fácil",
        NORMAL: "Normal",
        DIFICIL: "Difícil",
    };

    const numero = new Intl.NumberFormat("pt-BR");
</script>

<li>
    <button type="button" disabled={ocupada} onclick={() => aoEscolher(repeticao)}>
        <svg viewBox="0 0 24 24" aria-hidden="true"><path d="M8 5v14l11-7z" /></svg>
        <span class="quando">
            <span class="so-leitor">Assistir à partida de</span>
            <span class="data">{formatarDataHora(repeticao.data)}</span>
            <span class="dificuldade">
                Dificuldade {NOMES[repeticao.dificuldade] ?? repeticao.dificuldade}
            </span>
        </span>
        <span class="pontos">
            {numero.format(repeticao.pontuacao)} <span class="unidade">pontos</span>
        </span>
    </button>
</li>

<style>
    li {
        border-bottom: 1px solid var(--cor-borda);
    }
    button {
        display: grid;
        grid-template-columns: 2.75rem minmax(0, 1fr);
        gap: 0.25rem 0.75rem;
        align-items: center;
        width: 100%;
        min-height: 3.75rem;
        padding: 0.75rem 0.5rem;
        font: inherit;
        text-align: left;
        color: var(--cor-texto);
        background: none;
        border: 0;
        border-radius: 8px;
        cursor: pointer;
        touch-action: manipulation;
    }
    button:disabled {
        cursor: progress;
        opacity: 0.6;
    }
    @media (hover: hover) {
        button:hover:not(:disabled) {
            background: #ece8de;
        }
    }
    /* O "tocar" em âmbar é a única cor forte da linha: é ele que diz que ela é um botão. */
    svg {
        grid-row: span 2;
        width: 2.75rem;
        height: 2.75rem;
        padding: 0.7rem;
        fill: var(--cor-marinho);
        background: var(--cor-ambar);
        border-radius: 8px;
    }
    .quando {
        display: grid;
        min-width: 0;
    }
    .data {
        font-weight: 700;
    }
    .dificuldade {
        font-size: 0.9rem;
        color: var(--cor-texto-suave);
    }
    .pontos {
        grid-column: 2;
        font-family: var(--fonte-destaque);
        font-size: 1.25rem;
        font-stretch: 125%;
        font-weight: 800;
        color: var(--cor-marinho);
        font-variant-numeric: tabular-nums;
    }
    .unidade {
        font-family: Inter, "Noto Sans", system-ui, sans-serif;
        font-size: 0.9rem;
        font-stretch: 100%;
        font-weight: 400;
        color: var(--cor-texto-suave);
    }
    /* Com espaço, os pontos vão para a direita, na mesma linha da data. */
    @media (min-width: 480px) {
        button {
            grid-template-columns: 2.75rem minmax(0, 1fr) auto;
        }
        svg {
            grid-row: auto;
        }
        .pontos {
            grid-column: auto;
            text-align: right;
        }
    }
</style>
