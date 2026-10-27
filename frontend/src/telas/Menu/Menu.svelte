<!-- Menu principal (RF01): nova partida, ranking, repetições, configurações e sair. -->
<script lang="ts">
    import type { Tela } from "@/estado/navegacao.svelte";
    import OpcoesMenu from "./OpcoesMenu.svelte";
    import TorreEquilibrada from "./TorreEquilibrada.svelte";

    /** Chamado com a tela escolhida. */
    let { aoEscolher }: { aoEscolher: (tela: Tela) => void } = $props();

    /** A torre cresce com a janela: lado de cada bloco a partir de cada largura. */
    const LARGURA_COMPUTADOR = 900;
    const LARGURA_TABLET = 600;

    let larguraJanela = $state(1280);
    const lado = $derived(
        larguraJanela >= LARGURA_COMPUTADOR ? 26 : larguraJanela >= LARGURA_TABLET ? 22 : 16,
    );

    let principal: HTMLButtonElement | undefined = $state();

    // Quem chega pelo teclado já cai na ação principal: Enter começa a partida.
    $effect(() => principal?.focus());
</script>

<svelte:window bind:innerWidth={larguraJanela} />

<section class="menu" aria-labelledby="titulo-menu">
    <figure class="painel">
        <TorreEquilibrada {lado} />
        <figcaption>Pesado embaixo, centro de massa no prumo.</figcaption>
    </figure>

    <div class="conteudo">
        <h2 id="titulo-menu">A obra precisa ficar de pé</h2>
        <p class="intro">
            Cada peça tem peso. Encaixe, feche pavimentos e mantenha o centro de massa em cima da
            base.
        </p>

        <button
            bind:this={principal}
            type="button"
            class="principal"
            onclick={() => aoEscolher({ nome: "nova-partida" })}
        >
            <span class="nome">Nova partida</span>
            <span class="descricao">Escolha a dificuldade e comece a obra.</span>
        </button>

        <OpcoesMenu {aoEscolher} />

        <button type="button" class="sair" onclick={() => aoEscolher({ nome: "saida" })}>
            Sair do jogo
        </button>
    </div>
</section>

<style>
    .menu {
        display: grid;
        gap: 1.5rem;
        justify-items: center;
        max-width: 60rem;
        margin: 0 auto;
    }
    .painel {
        display: grid;
        justify-items: center;
        gap: 1.25rem;
        width: 100%;
        max-width: 30rem;
        margin: 0;
        padding: 1.5rem 1rem 1rem;
        background: var(--cor-tabuleiro);
        border-radius: 16px;
    }
    figcaption {
        font-size: 0.875rem;
        color: #c9d1dd;
    }
    .conteudo {
        display: grid;
        gap: 1rem;
        width: 100%;
        max-width: 30rem;
    }
    h2 {
        margin: 0;
        font-family: var(--fonte-destaque);
        font-size: clamp(1.75rem, 4.5vw, 2.75rem);
        font-stretch: 125%;
        font-weight: 800;
        line-height: 1.05;
        color: var(--cor-marinho);
        text-wrap: balance;
    }
    .intro {
        max-width: 34ch;
        margin: 0 0 0.5rem;
        color: var(--cor-texto-suave);
    }
    button {
        font: inherit;
        text-align: left;
        cursor: pointer;
        touch-action: manipulation;
    }
    .nome {
        font-weight: 700;
    }
    .descricao {
        font-size: 0.9rem;
    }
    .principal {
        display: grid;
        gap: 0.15rem;
        min-height: 4.5rem;
        padding: 1rem 1.25rem;
        color: var(--cor-marinho);
        background: var(--cor-ambar);
        border: 0;
        border-radius: 12px;
        box-shadow: 0 4px 0 #b37d12;
        transition:
            transform 120ms ease-out,
            box-shadow 120ms ease-out;
    }
    .principal .nome {
        font-size: 1.3rem;
    }
    /* Apertar afunda o botão, como um bloco que assenta. */
    .principal:active {
        transform: translateY(3px);
        box-shadow: 0 1px 0 #b37d12;
    }
    @media (hover: hover) {
        .sair:hover {
            color: var(--cor-texto);
        }
    }
    .sair {
        justify-self: start;
        min-height: 44px;
        padding: 0 0.5rem;
        color: var(--cor-texto-suave);
        text-decoration: underline;
        text-underline-offset: 0.2em;
        background: none;
        border: 0;
    }
    /* Computador: a torre à esquerda e as opções ao lado, alinhadas pelo meio. */
    @media (min-width: 900px) {
        .menu {
            grid-template-columns: auto minmax(0, 30rem);
            gap: 3.5rem;
            align-items: center;
            justify-content: center;
        }
        .painel {
            max-width: none;
            padding: 2.5rem 2.5rem 1.5rem;
        }
    }
</style>
