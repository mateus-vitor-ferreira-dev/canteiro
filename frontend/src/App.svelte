<!-- Moldura do jogo: o cabeçalho e a tela aberta, escolhida pela navegação (RF01). -->
<script lang="ts">
    import { tick } from "svelte";
    import { Navegacao, type NomeTela } from "@/estado/navegacao.svelte";
    import Aviso from "@/telas/Aviso/Aviso.svelte";
    import FimDePartida from "@/telas/FimDePartida/FimDePartida.svelte";
    import Menu from "@/telas/Menu/Menu.svelte";
    import NovaPartida from "@/telas/NovaPartida/NovaPartida.svelte";
    import Partida from "@/telas/Partida/Partida.svelte";
    import Ranking from "@/telas/Ranking/Ranking.svelte";

    /** Troca a navegação nos testes. */
    let { navegacao = new Navegacao() }: { navegacao?: Navegacao } = $props();

    /** Telas que ainda não existem: o menu já leva até elas, com um aviso. */
    const EM_BREVE: Partial<Record<NomeTela, string>> = {
        repeticoes: "Repetições",
        configuracoes: "Configurações",
    };

    let principal: HTMLElement | undefined = $state();

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
        <NovaPartida aoCriar={(id) => navegacao.ir({ nome: "partida", id })} />
    {:else if navegacao.tela.nome === "partida"}
        {@const id = navegacao.tela.id}
        {#key id}
            <Partida
                {id}
                aoSair={() => navegacao.ir({ nome: "nova-partida" })}
                aoTerminar={(placar) => navegacao.ir({ nome: "fim", id, placar })}
            />
        {/key}
    {:else if navegacao.tela.nome === "fim"}
        <FimDePartida
            partidaId={navegacao.tela.id}
            placar={navegacao.tela.placar}
            aoSalvo={(nome) => navegacao.ir({ nome: "ranking", destaque: nome })}
            aoJogarDeNovo={() => navegacao.ir({ nome: "nova-partida" })}
            aoVoltar={() => navegacao.voltarAoMenu()}
        />
    {:else if navegacao.tela.nome === "ranking"}
        <Ranking
            destaque={navegacao.tela.destaque}
            aoJogar={() => navegacao.ir({ nome: "nova-partida" })}
            aoVoltar={() => navegacao.voltarAoMenu()}
        />
    {:else if navegacao.tela.nome === "saida"}
        <Aviso
            titulo="Até a próxima obra"
            texto="Pode fechar esta aba. O navegador não deixa o jogo fechá-la sozinho."
            aoVoltar={() => navegacao.voltarAoMenu()}
        />
    {:else}
        <Aviso
            titulo={EM_BREVE[navegacao.tela.nome] ?? "Em construção"}
            texto="Esta parte do jogo ainda está em construção. Por enquanto, dá para jogar uma partida nova."
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
