<!-- Moldura do jogo: o cabeçalho e a tela aberta, escolhida pela navegação (RF01). -->
<script lang="ts">
    import { tick } from "svelte";
    import { Navegacao } from "@/estado/navegacao.svelte";
    import { configuracoes } from "@/estado/configuracoes.svelte";
    import Aviso from "@/telas/Aviso/Aviso.svelte";
    import Configuracoes from "@/telas/Configuracoes/Configuracoes.svelte";
    import FimDePartida from "@/telas/FimDePartida/FimDePartida.svelte";
    import Menu from "@/telas/Menu/Menu.svelte";
    import NovaPartida from "@/telas/NovaPartida/NovaPartida.svelte";
    import Partida from "@/telas/Partida/Partida.svelte";
    import Ranking from "@/telas/Ranking/Ranking.svelte";
    import Relatorio from "@/telas/Relatorio/Relatorio.svelte";
    import Repeticoes from "@/telas/Repeticoes/Repeticoes.svelte";

    /** Troca a navegação nos testes. */
    let { navegacao = new Navegacao() }: { navegacao?: Navegacao } = $props();

    let principal: HTMLElement | undefined = $state();

    // Teclas e volume do jogador: se não carregarem, o jogo segue com o padrão.
    void configuracoes.carregar();

    // Ao trocar de tela, o foco vai para o título dela: quem usa teclado ou
    // leitor de tela sabe onde está. O menu e a partida cuidam do próprio foco.
    $effect(() => {
        const nome = navegacao.tela.nome;
        if (nome !== "menu" && nome !== "partida") {
            void tick().then(() => principal?.querySelector<HTMLElement>("h2")?.focus());
        }
    });
</script>

<svelte:window onpopstate={(evento) => navegacao.restaurar(evento.state)} />

<header>
    <h1>CANTEIRO</h1>
    <p>O jogo de encaixe em que a pilha tem peso e precisa ficar de pé.</p>
</header>

<main bind:this={principal}>
    {#if navegacao.tela.nome === "menu"}
        <Menu aoEscolher={(tela) => navegacao.ir(tela)} />
    {:else if navegacao.tela.nome === "nova-partida"}
        <button type="button" class="voltar" onclick={() => navegacao.voltarAoMenu()}>
            Voltar ao menu
        </button>
        <NovaPartida aoCriar={(id, treino) => navegacao.ir({ nome: "partida", id, treino })} />
    {:else if navegacao.tela.nome === "partida"}
        {@const { id, treino, repeticao } = navegacao.tela}
        {#key id}
            <Partida
                {id}
                {treino}
                {repeticao}
                aoSair={() => navegacao.ir({ nome: repeticao ? "repeticoes" : "nova-partida" })}
                aoTerminar={(placar) => navegacao.ir({ nome: "fim", id, placar, treino })}
            />
        {/key}
    {:else if navegacao.tela.nome === "fim"}
        {@const tela = navegacao.tela}
        <FimDePartida
            partidaId={tela.id}
            placar={tela.placar}
            treino={tela.treino}
            aoSalvo={(nome) => navegacao.ir({ nome: "ranking", destaque: nome })}
            aoJogarDeNovo={() => navegacao.ir({ nome: "nova-partida" })}
            aoVerRelatorio={() => navegacao.ir({ ...tela, nome: "relatorio" })}
            aoVoltar={() => navegacao.voltarAoMenu()}
        />
    {:else if navegacao.tela.nome === "relatorio"}
        {@const tela = navegacao.tela}
        <Relatorio
            partidaId={tela.id}
            aoVoltar={() => navegacao.ir({ ...tela, nome: "fim" })}
            aoJogarDeNovo={() => navegacao.ir({ nome: "nova-partida" })}
        />
    {:else if navegacao.tela.nome === "ranking"}
        <Ranking
            destaque={navegacao.tela.destaque}
            aoJogar={() => navegacao.ir({ nome: "nova-partida" })}
            aoVoltar={() => navegacao.voltarAoMenu()}
        />
    {:else if navegacao.tela.nome === "configuracoes"}
        <Configuracoes aoVoltar={() => navegacao.voltarAoMenu()} />
    {:else if navegacao.tela.nome === "repeticoes"}
        <Repeticoes
            aoReproduzir={(id) => navegacao.ir({ nome: "partida", id, repeticao: true })}
            aoJogar={() => navegacao.ir({ nome: "nova-partida" })}
            aoVoltar={() => navegacao.voltarAoMenu()}
        />
    {:else}
        <Aviso
            titulo="Até a próxima obra"
            texto="Pode fechar esta aba. O navegador não deixa o jogo fechá-la sozinho."
            aoVoltar={() => navegacao.voltarAoMenu()}
        />
    {/if}
</main>

<style>
    header {
        padding: 1rem;
        background: var(--cor-marinho);
        color: #fff;
        border-bottom: 6px solid var(--cor-ambar);
    }
    h1 {
        margin: 0;
        font-family: var(--fonte-destaque);
        font-size: clamp(1.5rem, 6vw, 2rem);
        font-stretch: 125%;
        font-weight: 800;
        letter-spacing: 0.01em;
    }
    header p {
        margin: 0.25rem 0 0;
        color: #d5dcea;
    }
    main {
        padding: 1rem;
    }
    .voltar {
        min-height: 44px;
        margin: 0 0 1rem;
        padding: 0 0.5rem;
        font: inherit;
        color: var(--cor-marinho);
        text-decoration: underline;
        text-underline-offset: 0.2em;
        background: none;
        border: 0;
        cursor: pointer;
    }
    @media (max-width: 599px) {
        header p {
            display: none;
        }
    }
    @media (min-width: 768px) {
        header {
            padding: 1.25rem 3rem;
        }
        main {
            padding: 2rem 3rem;
        }
    }
</style>
