<!-- O nome do jogador para o ranking (RF21). A pontuação o backend lê da própria partida. -->
<script lang="ts">
    import { mensagemDoRanking, registrarNoRanking } from "@/api/cliente";

    let {
        partidaId,
        aoSalvo,
    }: {
        /** Partida encerrada que vai para o ranking. */
        partidaId: string;
        /** Chamado com o nome, depois que o backend registrou. */
        aoSalvo: (nome: string) => void;
    } = $props();

    /** Mesmo limite que o backend aceita (#31). */
    const NOME_MAXIMO = 20;

    let nome = $state("");
    let salvando = $state(false);
    let erro = $state<string | null>(null);
    let campo: HTMLInputElement | undefined = $state();

    async function salvar(evento: SubmitEvent) {
        evento.preventDefault();
        const limpo = nome.trim();
        if (!limpo) {
            falhar("Digite um nome para salvar.");
            return;
        }
        salvando = true;
        erro = null;
        try {
            await registrarNoRanking(partidaId, limpo);
            aoSalvo(limpo);
        } catch (falha) {
            falhar(mensagemDoRanking(falha));
        } finally {
            salvando = false;
        }
    }

    /** Mostra o erro e devolve o foco ao campo, para o jogador corrigir sem procurar. */
    function falhar(mensagem: string) {
        erro = mensagem;
        campo?.focus();
    }
</script>

<form onsubmit={salvar} novalidate>
    <label for="nome-ranking">Seu nome no ranking</label>
    <input
        bind:this={campo}
        id="nome-ranking"
        name="nome"
        autocomplete="nickname"
        autocapitalize="words"
        enterkeyhint="send"
        maxlength={NOME_MAXIMO}
        bind:value={nome}
        oninput={() => (erro = null)}
        aria-describedby={erro ? "dica-nome erro-nome" : "dica-nome"}
        aria-invalid={erro ? "true" : undefined}
    />
    <p id="dica-nome" class="dica">
        Até {NOME_MAXIMO} caracteres. Se o nome já estiver lá, fica a maior pontuação.
    </p>
    {#if erro}
        <p id="erro-nome" class="erro" role="alert">{erro}</p>
    {/if}
    <button type="submit" disabled={salvando}>
        {salvando ? "Salvando…" : "Salvar no ranking"}
    </button>
</form>

<style>
    form {
        display: grid;
        gap: 0.5rem;
    }
    label {
        font-weight: 700;
    }
    input {
        min-height: 48px;
        padding: 0.5rem 0.75rem;
        font: inherit;
        /* 16px no mínimo: abaixo disso o iPhone dá zoom ao focar o campo. */
        font-size: max(1rem, 16px);
        color: var(--cor-texto);
        background: var(--cor-cartao);
        border: 2px solid var(--cor-borda);
        border-radius: 8px;
    }
    input:focus-visible {
        border-color: var(--cor-marinho);
    }
    input[aria-invalid="true"] {
        border-color: var(--cor-vermelho);
    }
    .dica,
    .erro {
        margin: 0;
        font-size: 0.9rem;
    }
    .dica {
        color: var(--cor-texto-suave);
    }
    .erro {
        font-weight: 600;
        color: var(--cor-vermelho);
    }
    button {
        min-height: 52px;
        margin-top: 0.5rem;
        padding: 0.75rem 1.25rem;
        font: inherit;
        font-size: 1.1rem;
        font-weight: 700;
        color: var(--cor-marinho);
        background: var(--cor-ambar);
        border: 0;
        border-radius: 12px;
        box-shadow: 0 4px 0 #b37d12;
        cursor: pointer;
        touch-action: manipulation;
        transition:
            transform 120ms ease-out,
            box-shadow 120ms ease-out;
    }
    button:active:not(:disabled) {
        transform: translateY(3px);
        box-shadow: 0 1px 0 #b37d12;
    }
    button:disabled {
        cursor: progress;
        opacity: 0.7;
    }
</style>
