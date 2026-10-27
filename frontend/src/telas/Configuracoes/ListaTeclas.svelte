<!-- Uma ação por linha, com a tecla atual e o botão de trocar (RF27). Trocar espera a próxima tecla apertada. -->
<script lang="ts">
    import { tick } from "svelte";
    import { teclaReservada, rotuloDaTecla, type AcaoTecla, type Tecla } from "@/estado/teclado";

    let {
        tabela,
        aoTrocar,
    }: {
        /** As teclas como estão no rascunho. */
        tabela: Tecla[];
        /** Chamado com a ação e a tecla nova, já conferida. */
        aoTrocar: (acao: AcaoTecla, codigo: string) => void;
    } = $props();

    let capturando = $state<AcaoTecla | null>(null);
    let mensagem = $state("");
    let lista: HTMLUListElement | undefined = $state();

    function comecar(acao: AcaoTecla) {
        capturando = acao;
        mensagem = "";
    }

    async function terminar(acao: AcaoTecla) {
        capturando = null;
        await tick();
        lista?.querySelector<HTMLButtonElement>(`[data-acao="${acao}"]`)?.focus();
    }

    function aoTeclar(evento: KeyboardEvent) {
        const acao = capturando;
        if (!acao) {
            return;
        }
        if (evento.code === "Tab") {
            capturando = null;
            return;
        }
        evento.preventDefault();
        if (evento.code === "Escape") {
            void terminar(acao);
            return;
        }
        if (teclaReservada(evento.code)) {
            mensagem = "Tab, Shift, Ctrl e Alt sozinhas não servem. Aperte outra tecla.";
            return;
        }
        const dona = tabela.find((t) => t.comando !== acao && t.codigos.includes(evento.code));
        if (dona) {
            mensagem = `${rotuloDaTecla(evento.code)} já serve para "${dona.acao}". Aperte outra tecla.`;
            return;
        }
        aoTrocar(acao, evento.code);
        mensagem = `${tabela.find((t) => t.comando === acao)?.acao ?? "Ação"}: agora é ${rotuloDaTecla(evento.code)}.`;
        void terminar(acao);
    }
</script>

<svelte:window onkeydown={aoTeclar} />

<ul bind:this={lista}>
    {#each tabela as tecla (tecla.comando)}
        {@const esperando = capturando === tecla.comando}
        <li class:esperando>
            <span class="acao">{tecla.acao}</span>
            <span class="teclas">
                {#if esperando}
                    <span class="aguardando">Aperte a tecla nova (Esc cancela)</span>
                {:else}
                    <kbd>{tecla.rotulo}</kbd>
                {/if}
            </span>
            <button
                type="button"
                data-acao={tecla.comando}
                aria-label="Trocar a tecla de {tecla.acao}"
                aria-pressed={esperando}
                onclick={() => (esperando ? terminar(tecla.comando) : comecar(tecla.comando))}
            >
                {esperando ? "Cancelar" : "Trocar"}
            </button>
        </li>
    {/each}
</ul>
<p class="mensagem" role="status">{mensagem}</p>

<style>
    ul {
        margin: 0;
        padding: 0;
        list-style: none;
        border-top: 1px solid var(--cor-borda);
    }
    li {
        display: grid;
        grid-template-columns: minmax(0, 1fr) auto auto;
        gap: 0.75rem;
        align-items: center;
        min-height: 3.5rem;
        padding: 0.5rem 0.5rem 0.5rem 0.75rem;
        border-bottom: 1px solid var(--cor-borda);
    }
    /* A linha que espera a tecla ganha a faixa âmbar, como o primeiro lugar do ranking. */
    .esperando {
        background: #fbf0d6;
        box-shadow: inset 6px 0 0 var(--cor-ambar);
    }
    .acao {
        font-weight: 600;
    }
    .teclas {
        text-align: right;
    }
    kbd {
        display: inline-block;
        min-width: 2rem;
        padding: 0.15rem 0.5rem;
        font-family: inherit;
        font-weight: 700;
        text-align: center;
        background: var(--cor-cartao);
        border: 1px solid var(--cor-borda);
        border-bottom-width: 3px;
        border-radius: 6px;
    }
    .aguardando {
        font-size: 0.875rem;
        font-weight: 600;
        color: var(--cor-rotulo);
    }
    button {
        min-width: 6rem;
        min-height: 44px;
        padding: 0 1rem;
        font: inherit;
        font-weight: 700;
        color: var(--cor-marinho);
        background: var(--cor-cartao);
        border: 1px solid var(--cor-borda);
        border-radius: 8px;
        cursor: pointer;
    }
    .mensagem {
        min-height: 1.5rem;
        margin: 0.5rem 0 0;
        font-weight: 600;
        color: var(--cor-marinho);
    }
    /* Celular: a tecla e o botão descem para baixo do nome da ação. */
    @media (max-width: 479px) {
        li {
            grid-template-columns: minmax(0, 1fr) auto;
        }
        .acao {
            grid-column: 1 / -1;
        }
        .teclas {
            text-align: left;
        }
    }
</style>
