<!-- Os textos que sobem sobre o tabuleiro: pontos ganhos, desconto do colapso, meta cumprida e nível novo. Decorativo: o leitor de tela já ouve o anúncio. -->
<script lang="ts">
    import { untrack } from "svelte";
    import type { EventoDto } from "@/api/protocolo";
    import { destaqueDoEvento, type Destaque } from "@/componentes/efeitos";

    let {
        evento,
        nivel,
    }: {
        /** O último evento recebido. */
        evento: EventoDto | null;
        /** Nível mostrado quando o evento chega, para o destaque de subida. */
        nivel?: number;
    } = $props();

    /** Quanto cada texto fica na tela, em milissegundos. */
    const DURACAO_MS = 1100;

    let proximoId = 0;
    let ativos: (Destaque & { id: number })[] = $state([]);

    // Cada evento é um objeto novo: o destaque entra e sai sozinho depois da animação.
    $effect(() => {
        const atual = evento;
        const destaque = atual
            ? destaqueDoEvento(
                  atual,
                  untrack(() => nivel),
              )
            : null;
        if (!destaque) {
            return;
        }
        const id = proximoId++;
        untrack(() => (ativos = [...ativos, { ...destaque, id }]));
        setTimeout(() => (ativos = ativos.filter((a) => a.id !== id)), DURACAO_MS);
    });
</script>

<div class="destaques" aria-hidden="true">
    {#each ativos as destaque, i (destaque.id)}
        <span class="destaque {destaque.tipo}" style:top="{30 + i * 9}%">{destaque.texto}</span>
    {/each}
</div>

<style>
    .destaques {
        position: absolute;
        inset: 0;
        overflow: hidden;
        pointer-events: none;
    }
    .destaque {
        position: absolute;
        left: 50%;
        font-family: var(--fonte-destaque);
        font-size: 1.5rem;
        font-weight: 800;
        color: #fff;
        white-space: nowrap;
        text-shadow:
            0 2px 0 var(--cor-marinho),
            0 0 8px rgb(0 0 0 / 60%);
        transform: translateX(-50%);
        animation: subir 1100ms ease-out forwards;
    }
    .ganho {
        color: #ffe9a8;
    }
    .perda {
        color: #ff8a80;
    }
    .meta {
        color: #b9f6ca;
    }
    .nivel {
        font-size: 2.2rem;
        color: var(--cor-ambar);
    }
    @keyframes subir {
        0% {
            opacity: 0;
            transform: translate(-50%, 12px) scale(0.85);
        }
        15% {
            opacity: 1;
            transform: translate(-50%, 0) scale(1.08);
        }
        70% {
            opacity: 1;
        }
        100% {
            opacity: 0;
            transform: translate(-50%, -48px) scale(1);
        }
    }
    /* Com movimento reduzido, o texto aparece e some no lugar. */
    @media (prefers-reduced-motion: reduce) {
        .destaque {
            animation: sumir 1100ms step-end forwards;
        }
        @keyframes sumir {
            to {
                opacity: 0;
            }
        }
    }
</style>
