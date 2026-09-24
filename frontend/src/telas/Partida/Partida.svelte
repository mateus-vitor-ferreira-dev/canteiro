<!-- A partida: tabuleiro, placar, próximas peças e legenda. Teclado vira comando pelo WebSocket. -->
<script lang="ts">
    import PainelEstabilidade from "@/componentes/PainelEstabilidade.svelte";
    import Tabuleiro from "@/componentes/Tabuleiro.svelte";
    import { PartidaAoVivo, type Conectar } from "@/estado/partida.svelte";
    import { comandoDaTecla } from "@/estado/teclado";
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

<svelte:window onkeydown={aoTeclar} onblur={aoPerderFoco} />

<div class="partida">
    {#if partida.estado}
        <div class="lateral">
            <Placar placar={partida.estado.placar} />
            <PainelEstabilidade estabilidade={partida.estado.estabilidade} />
        </div>
    {:else}
        <p class="aviso">{partida.conectado ? "Preparando a obra…" : "Conectando…"}</p>
    {/if}

    <div class="palco">
        <Tabuleiro estado={partida.estado} evento={partida.ultimoEvento} />
        {#if partida.pausada}
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

    <aside>
        {#if partida.estado}
            <FilaProximas proximas={partida.estado.proximas} />
        {/if}
        <LegendaTeclas />
    </aside>
</div>

<style>
    .partida {
        display: grid;
        grid-template-columns: 220px auto 260px;
        gap: 2.5rem;
        align-items: start;
        justify-content: center;
    }
    .palco {
        position: relative;
    }
    .camada {
        position: absolute;
        inset: 0;
        display: flex;
        flex-direction: column;
        align-items: center;
        justify-content: center;
        gap: 0.5rem;
        color: #fff;
        background: rgb(27 36 50 / 80%);
        border-radius: 8px;
    }
    .camada h2 {
        margin: 0;
        font-size: 2rem;
    }
    .camada button {
        padding: 0.6rem 1.2rem;
        font: inherit;
        font-weight: 700;
        color: var(--cor-marinho);
        background: var(--cor-ambar);
        border: 0;
        border-radius: 8px;
        cursor: pointer;
    }
    .aviso {
        color: var(--cor-texto-suave);
    }
</style>
