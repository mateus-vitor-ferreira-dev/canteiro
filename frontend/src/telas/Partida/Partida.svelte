<!-- A partida: tabuleiro, placar, próximas peças e legendas. Teclado e toque viram comando pelo WebSocket. -->
<script lang="ts">
    import { untrack } from "svelte";
    import type { Comando, PlacarDto } from "@/api/protocolo";
    import PainelEstabilidade from "@/componentes/PainelEstabilidade.svelte";
    import Tabuleiro from "@/componentes/Tabuleiro.svelte";
    import { tamanhoDaCelula } from "@/componentes/desenho";
    import { anuncioDoEvento } from "@/estado/anuncios";
    import { configuracoes } from "@/estado/configuracoes.svelte";
    import { PartidaAoVivo, type Conectar } from "@/estado/partida.svelte";
    import { Som } from "@/estado/som.svelte";
    import { comandoDaTecla } from "@/estado/teclado";
    import CamadaPartida from "./CamadaPartida.svelte";
    import ColunaApoio from "./ColunaApoio.svelte";
    import ControlesToque from "./ControlesToque.svelte";
    import Placar from "./Placar.svelte";

    let {
        id,
        aoSair,
        aoTerminar,
        conectar,
        som = new Som(),
    }: {
        /** Id da partida criada em `POST /api/partidas`. */
        id: string;
        /** Chamado quando o jogador sai da partida. */
        aoSair: () => void;
        /** Chamado com o placar final, quando o jogador abre o resultado. */
        aoTerminar: (placar: PlacarDto) => void;
        /** Troca a conexão nos testes. */
        conectar?: Conectar;
        /** Troca os sons nos testes. */
        som?: Som;
    } = $props();

    // A partida é criada uma vez, com o id recebido; trocar de partida é montar a tela de novo.
    // svelte-ignore state_referenced_locally
    const partida = new PartidaAoVivo(id, conectar);

    $effect(() => () => partida.encerrar());

    // Cada evento novo toca o seu som. Ligar o som não repete o último evento.
    $effect(() => {
        const evento = partida.ultimoEvento;
        if (evento) {
            untrack(() => som.tocar(evento));
        }
    });

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
        const tabela = configuracoes.tabela;
        const comando = comandoDaTecla(evento.code, evento.repeat, partida.pausada, tabela);
        if (comando === "SOM") {
            som.alternar();
        } else if (comando && !partida.encerrada) {
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
            <CamadaPartida {partida} {aoSair} {aoTerminar} />
        </div>

        <ControlesToque aoComando={aoTocar} pausada={partida.pausada} />
    </div>

    <ColunaApoio estado={partida.estado} {som} />
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
    .aviso {
        grid-area: lateral;
        margin: 0;
        color: var(--cor-texto-suave);
    }
</style>
