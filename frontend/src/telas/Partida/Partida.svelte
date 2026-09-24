<!-- A partida: tabuleiro, placar, próximas peças e legendas. Teclado e toque viram comando pelo WebSocket. -->
<script lang="ts">
    import type { Comando } from "@/api/protocolo";
    import LegendaMateriais from "@/componentes/LegendaMateriais.svelte";
    import PainelEstabilidade from "@/componentes/PainelEstabilidade.svelte";
    import Tabuleiro from "@/componentes/Tabuleiro.svelte";
    import { tamanhoDaCelula } from "@/componentes/desenho";
    import { anuncioDoEvento } from "@/estado/anuncios";
    import { PartidaAoVivo, type Conectar } from "@/estado/partida.svelte";
    import { comandoDaTecla } from "@/estado/teclado";
    import ControlesToque from "./ControlesToque.svelte";
    import FilaProximas from "./FilaProximas.svelte";
    import LegendaTeclas from "./LegendaTeclas.svelte";
    import Placar from "./Placar.svelte";

    let {
        id,
        aoSair,
        conectar,
    }: {
        /** Id da partida criada em `POST /api/partidas`. */
        id: string;
        /** Chamado quando o jogador sai da partida. */
        aoSair: () => void;
        /** Troca a conexão nos testes. */
        conectar?: Conectar;
    } = $props();

    // A partida é criada uma vez, com o id recebido; trocar de partida é montar a tela de novo.
    // svelte-ignore state_referenced_locally
    const partida = new PartidaAoVivo(id, conectar);

    $effect(() => () => partida.encerrar());

    let larguraJanela = $state(1280);
    let alturaJanela = $state(800);

    /**
     * O tabuleiro cresce e encolhe com a janela. Desconta o que fica ao lado
     * dele (colunas laterais) ou em cima e embaixo (cabeçalho, placar e botões).
     */
    const celula = $derived.by(() => {
        if (larguraJanela >= 1100) {
            return tamanhoDaCelula(larguraJanela - 700, alturaJanela - 190);
        }
        if (larguraJanela >= 768) {
            return tamanhoDaCelula(larguraJanela - 400, alturaJanela - 260);
        }
        return tamanhoDaCelula(larguraJanela - 40, alturaJanela - 300);
    });

    const anuncio = $derived(
        partida.ultimoEvento
            ? (anuncioDoEvento(partida.ultimoEvento, partida.estado?.placar.nivel) ?? "")
            : "",
    );

    function aoTocar(comando: Comando) {
        if (!partida.encerrada) {
            partida.enviar(comando);
        }
    }

    function aoTeclar(evento: KeyboardEvent) {
        const comando = comandoDaTecla(evento.code, evento.repeat, partida.pausada);
        if (comando && !partida.encerrada) {
            evento.preventDefault();
            partida.enviar(comando);
        }
    }

    /** A aba perdeu o foco: pausa, para o jogador não perder a partida sem ver (RF30). */
    function aoPerderFoco() {
        if (partida.estado?.estado === "PECA_CAINDO") {
            partida.enviar("PAUSAR");
        }
    }
</script>

<svelte:window
    onkeydown={aoTeclar}
    onblur={aoPerderFoco}
    bind:innerWidth={larguraJanela}
    bind:innerHeight={alturaJanela}
/>

<p class="so-leitor" aria-live="polite">{anuncio}</p>

<div class="partida">
    {#if partida.estado}
        <div class="lateral">
            <Placar placar={partida.estado.placar} />
            <PainelEstabilidade estabilidade={partida.estado.estabilidade} />
        </div>
    {:else}
        <p class="aviso">{partida.conectado ? "Preparando a obra…" : "Conectando…"}</p>
    {/if}

    <div class="jogo">
        <div class="palco">
            <Tabuleiro estado={partida.estado} evento={partida.ultimoEvento} {celula} />
            {#if partida.perdida}
                <div class="camada" role="alert">
                    <h2>Partida perdida</h2>
                    <p>O jogo foi reiniciado e esta partida não existe mais.</p>
                    <button type="button" onclick={aoSair}>Começar outra</button>
                </div>
            {:else if partida.reconectando}
                <div class="camada" role="status">
                    <h2>Reconectando…</h2>
                    <p>A partida está pausada e volta do mesmo ponto.</p>
                </div>
            {:else if partida.pausada}
                <div class="camada" role="status">
                    <h2>Pausado</h2>
                    <p>Aperte <kbd>P</kbd> para continuar.</p>
                </div>
            {:else if partida.encerrada && partida.estado}
                <div class="camada" role="status">
                    <h2>Fim de jogo</h2>
                    <p>{partida.estado.placar.pontuacao} pontos</p>
                    <button type="button" onclick={aoSair}>Jogar de novo</button>
                </div>
            {/if}
        </div>

        <ControlesToque aoComando={aoTocar} pausada={partida.pausada} />
    </div>

    <aside>
        {#if partida.estado}
            <FilaProximas proximas={partida.estado.proximas} />
        {/if}
        <LegendaMateriais />
        <LegendaTeclas />
    </aside>
</div>

<style>
    /* Celular: uma coluna, com o placar em cima e os botões embaixo do tabuleiro. */
    .partida {
        display: grid;
        grid-template-areas: "lateral" "jogo" "aside";
        gap: 1rem;
        justify-items: center;
    }
    .lateral {
        grid-area: lateral;
        display: grid;
        grid-template-columns: 1fr 1fr;
        gap: 1rem;
        width: 100%;
    }
    /* O tabuleiro e os botões de toque ficam sempre juntos. */
    .jogo {
        grid-area: jogo;
        display: grid;
        gap: 1rem;
        justify-items: center;
    }
    .palco {
        position: relative;
    }
    aside {
        grid-area: aside;
        width: 100%;
    }
    /* Tablet: o tabuleiro à esquerda e todo o resto numa coluna ao lado. */
    @media (min-width: 768px) {
        .partida {
            grid-template-columns: auto minmax(240px, 300px);
            grid-template-areas: "jogo lateral" "jogo aside";
            grid-template-rows: auto 1fr;
            gap: 1.5rem 2rem;
            justify-content: center;
            align-items: start;
            justify-items: stretch;
        }
        .lateral {
            grid-template-columns: 1fr;
            gap: 0;
        }
    }
    /* Computador: placar à esquerda, tabuleiro no meio, próximas e legendas à direita. */
    @media (min-width: 1100px) {
        .partida {
            grid-template-columns: 220px auto 260px;
            grid-template-areas: "lateral jogo aside";
            grid-template-rows: auto;
            gap: 1.5rem 2.5rem;
        }
    }
    .camada {
        position: absolute;
        inset: 0;
        display: flex;
        flex-direction: column;
        align-items: center;
        justify-content: center;
        gap: 0.5rem;
        padding: 1.5rem;
        text-align: center;
        color: #fff;
        background: rgb(27 36 50 / 80%);
        border-radius: 8px;
    }
    .camada h2 {
        margin: 0;
        font-size: clamp(1.4rem, 5vw, 2rem);
    }
    .camada button {
        padding: 0.6rem 1.2rem;
        font: inherit;
        font-weight: 700;
        min-height: 44px;
        color: var(--cor-marinho);
        background: var(--cor-ambar);
        border: 0;
        border-radius: 8px;
        cursor: pointer;
    }
    .aviso {
        grid-area: lateral;
        margin: 0;
        color: var(--cor-texto-suave);
    }
</style>
