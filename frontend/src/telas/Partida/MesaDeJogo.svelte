<!-- O centro da partida: a faixa do modo, o resumo do celular, o tabuleiro com as miniaturas e os botões de toque. -->
<script lang="ts">
    import type { Comando, PlacarDto } from "@/api/protocolo";
    import Tabuleiro from "@/componentes/Tabuleiro.svelte";
    import type { PartidaAoVivo } from "@/estado/partida.svelte";
    import type { Som } from "@/estado/som.svelte";
    import { LIMIAR_RANGIDO } from "@/componentes/efeitos";
    import { Musica } from "@/estado/musica.svelte";
    import { configuracoes } from "@/estado/configuracoes.svelte";
    import { intensidadeDoPerigo } from "@/sons/musica";
    import CamadaPartida from "./CamadaPartida.svelte";
    import Destaques from "./Destaques.svelte";
    import ControlesToque from "./ControlesToque.svelte";
    import FaixaModo from "./FaixaModo.svelte";
    import MiniaturasCompactas from "./MiniaturasCompactas.svelte";
    import ResumoCompacto from "./ResumoCompacto.svelte";

    let {
        partida,
        som,
        celula,
        treino,
        repeticao,
        aoTocar,
        aoSair,
        aoTerminar,
    }: {
        /** A partida ao vivo. */
        partida: PartidaAoVivo;
        /** Os sons: o botão do resumo, o rangido e a música seguem o mesmo liga e desliga. */
        som: Som;
        /** Lado de cada quadrado do tabuleiro, em pixels. */
        celula: number;
        /** Se a partida é de treino. */
        treino: boolean;
        /** Se é a reprodução de uma partida gravada. */
        repeticao: boolean;
        /** Recebe os comandos dos botões e dos gestos. */
        aoTocar: (comando: Comando) => void;
        /** Chamado quando o jogador sai da partida. */
        aoSair: () => void;
        /** Chamado com o placar final, quando o jogador abre o resultado. */
        aoTerminar: (placar: PlacarDto) => void;
    } = $props();

    /** De quanto em quanto tempo a estrutura range, enquanto está para cair. */
    const INTERVALO_RANGIDO_MS = 2400;

    const rangendo = $derived(
        partida.estado?.estado === "PECA_CAINDO" &&
            partida.estado.estabilidade.indice < LIMIAR_RANGIDO &&
            partida.estado.tabuleiro.some((linha) => linha.some(Boolean)),
    );

    // Perto do limite, a obra range de tempos em tempos, até sair do perigo.
    $effect(() => {
        if (!rangendo) {
            return;
        }
        som.ranger();
        const relogio = setInterval(() => som.ranger(), INTERVALO_RANGIDO_MS);
        return () => clearInterval(relogio);
    });

    // A música acompanha o perigo: mais cheia e mais rápida quanto mais perto de perder.
    // Para na pausa e no fim de jogo, e fica calada com o som ou a música desligados.
    const musica = new Musica(() => som.ligado && configuracoes.musica);
    const emJogo = $derived(
        partida.estado !== null &&
            partida.estado.estado !== "PAUSA" &&
            partida.estado.estado !== "FIM_DE_JOGO",
    );
    $effect(() => {
        musica.intensidade = partida.estado ? intensidadeDoPerigo(partida.estado) : 0;
    });
    $effect(() => {
        if (!emJogo) {
            return;
        }
        musica.tocar();
        return () => musica.parar();
    });
</script>

<div class="mesa-de-jogo">
    <FaixaModo {treino} {repeticao} />
    {#if partida.estado}
        <div class="so-celular resumo">
            <ResumoCompacto estado={partida.estado} {som} />
        </div>
    {/if}
    <div class="mesa">
        <div class="palco">
            <Tabuleiro
                estado={partida.estado}
                evento={partida.ultimoEvento}
                {celula}
                aoComando={aoTocar}
            />
            <Destaques evento={partida.ultimoEvento} nivel={partida.estado?.placar.nivel} />
            <CamadaPartida {partida} {repeticao} {aoSair} {aoTerminar} />
        </div>
        {#if partida.estado}
            <div class="so-celular">
                <MiniaturasCompactas estado={partida.estado} />
            </div>
        {/if}
    </div>

    <ControlesToque aoComando={aoTocar} pausada={partida.pausada} {treino} soPausa={repeticao} />
</div>

<style>
    /* O tabuleiro e os botões de toque ficam sempre juntos. */
    .mesa-de-jogo {
        display: grid;
        gap: 0.5rem;
        justify-items: center;
    }
    .resumo {
        width: 100%;
    }
    .mesa {
        display: flex;
        gap: 0.5rem;
        align-items: flex-start;
    }
    .palco {
        position: relative;
    }
    @media (min-width: 768px) {
        .mesa-de-jogo {
            gap: 1rem;
        }
        .so-celular {
            display: none;
        }
    }
</style>
