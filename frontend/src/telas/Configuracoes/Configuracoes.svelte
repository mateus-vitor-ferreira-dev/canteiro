<!-- Configurações (RF27): o volume dos efeitos e as teclas de cada ação. Nada muda até o jogador salvar. -->
<script lang="ts">
    import { VOLUME_PADRAO, configuracoes } from "@/estado/configuracoes.svelte";
    import { montarTeclas, type AcaoTecla, type TeclasPersonalizadas } from "@/estado/teclado";
    import ControleVolume from "./ControleVolume.svelte";
    import ListaTeclas from "./ListaTeclas.svelte";

    /** Chamado para voltar ao menu. */
    let { aoVoltar }: { aoVoltar: () => void } = $props();

    let teclas = $state<TeclasPersonalizadas>({ ...configuracoes.teclas });
    let volume = $state(configuracoes.volume);
    let salvando = $state(false);
    let aviso = $state<{ texto: string; erro: boolean } | null>(null);

    const tabela = $derived(montarTeclas(teclas));
    const mudou = $derived(
        volume !== configuracoes.volume ||
            JSON.stringify(tabela) !== JSON.stringify(configuracoes.tabela),
    );

    function trocar(acao: AcaoTecla, codigo: string) {
        teclas = { ...teclas, [acao]: [codigo] };
        aviso = null;
    }

    function restaurar() {
        teclas = {};
        volume = VOLUME_PADRAO;
        aviso = { texto: "Voltaram as teclas e o volume padrão. Salve para manter.", erro: false };
    }

    async function salvar() {
        salvando = true;
        aviso = null;
        try {
            const destino = await configuracoes.salvar(teclas, volume);
            aviso = {
                texto:
                    destino === "servidor"
                        ? "Configurações salvas."
                        : "Configurações salvas neste navegador.",
                erro: false,
            };
        } catch (erro) {
            aviso = {
                texto: erro instanceof Error ? erro.message : "Não foi possível salvar.",
                erro: true,
            };
        } finally {
            salvando = false;
        }
    }
</script>

<section class="configuracoes" aria-labelledby="titulo-configuracoes">
    <h2 id="titulo-configuracoes" tabindex="-1">Configurações</h2>

    <ControleVolume bind:volume />

    <div class="grupo">
        <h3>Teclas</h3>
        <p class="dica">Escolha Trocar e aperte a tecla nova. A legenda da partida muda junto.</p>
        <ListaTeclas {tabela} aoTrocar={trocar} />
    </div>

    <div class="acoes">
        <button type="button" class="salvar" disabled={!mudou || salvando} onclick={salvar}>
            {salvando ? "Salvando…" : "Salvar"}
        </button>
        <button type="button" class="secundario" onclick={restaurar}>Restaurar padrão</button>
        <button type="button" class="voltar" onclick={aoVoltar}>Voltar ao menu</button>
    </div>
    <p
        class="aviso"
        class:erro={aviso?.erro}
        class:salvo={aviso && !aviso.erro}
        role={aviso?.erro ? "alert" : "status"}
    >
        {aviso?.texto ?? (mudou ? "Há mudanças não salvas." : "")}
    </p>
</section>

<style>
    .configuracoes {
        display: grid;
        gap: 2rem;
        max-width: 40rem;
        margin: 0 auto;
    }
    h2 {
        margin: 0;
        font-family: var(--fonte-destaque);
        font-size: clamp(1.75rem, 4.5vw, 2.75rem);
        font-stretch: 125%;
        font-weight: 800;
        color: var(--cor-marinho);
    }
    h3 {
        margin: 0;
        font-size: 1.15rem;
        color: var(--cor-marinho);
    }
    .grupo {
        display: grid;
        gap: 0.5rem;
    }
    .dica {
        margin: 0;
        font-size: 0.9rem;
        color: var(--cor-texto-suave);
    }
    .acoes {
        display: flex;
        flex-wrap: wrap;
        align-items: center;
        gap: 0.75rem 1rem;
    }
    button {
        min-height: 44px;
        padding: 0.6rem 1.2rem;
        font: inherit;
        font-weight: 700;
        border: 0;
        border-radius: 8px;
        cursor: pointer;
    }
    .salvar {
        min-width: 8rem;
        color: var(--cor-marinho);
        background: var(--cor-ambar);
        box-shadow: 0 4px 0 #b37d12;
    }
    .salvar:disabled {
        cursor: not-allowed;
        opacity: 0.5;
        box-shadow: none;
    }
    .secundario {
        color: var(--cor-marinho);
        background: var(--cor-cartao);
        border: 1px solid var(--cor-borda);
    }
    .voltar {
        color: var(--cor-marinho);
        text-decoration: underline;
        text-underline-offset: 0.2em;
        background: none;
    }
    .aviso {
        min-height: 1.5rem;
        margin: -1rem 0 0;
        font-weight: 600;
        color: var(--cor-texto-suave);
    }
    .aviso.salvo {
        color: var(--cor-verde);
    }
    .aviso:empty {
        display: none;
    }
    .aviso.erro {
        color: var(--cor-vermelho);
    }
</style>
