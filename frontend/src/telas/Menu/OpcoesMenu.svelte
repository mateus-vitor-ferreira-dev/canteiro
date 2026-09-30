<!-- As opções secundárias do menu, numa lista: ranking, repetições e configurações. -->
<script lang="ts">
    import type { Tela } from "@/estado/navegacao.svelte";

    /** Chamado com a tela escolhida. */
    let { aoEscolher }: { aoEscolher: (tela: Tela) => void } = $props();

    type Opcao = { tela: Tela; nome: string; descricao: string };

    const OPCOES: readonly Opcao[] = [
        {
            tela: { nome: "ranking" },
            nome: "Ranking",
            descricao: "As dez maiores pontuações.",
        },
        {
            tela: { nome: "repeticoes" },
            nome: "Repetições",
            descricao: "Assista de novo a uma partida gravada.",
        },
        {
            tela: { nome: "configuracoes" },
            nome: "Configurações",
            descricao: "Teclas e volume.",
        },
    ];
</script>

<ul>
    {#each OPCOES as opcao (opcao.nome)}
        <li>
            <button type="button" onclick={() => aoEscolher(opcao.tela)}>
                <span class="nome">{opcao.nome}</span>
                <span class="descricao">{opcao.descricao}</span>
            </button>
        </li>
    {/each}
</ul>

<style>
    ul {
        margin: 0;
        padding: 0;
        list-style: none;
        border-top: 1px solid var(--cor-borda);
    }
    li {
        border-bottom: 1px solid var(--cor-borda);
    }
    button {
        display: grid;
        gap: 0.1rem;
        width: 100%;
        min-height: 3.5rem;
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
    @media (hover: hover) {
        button:hover {
            background: #ece8de;
        }
    }
    .nome {
        display: flex;
        flex-wrap: wrap;
        align-items: center;
        gap: 0.5rem;
        font-weight: 700;
    }
    .descricao {
        font-size: 0.9rem;
        color: var(--cor-texto-suave);
    }
</style>
