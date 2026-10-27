import type { EventoDto } from "@/api/protocolo";

/** Quanto o aparelho vibra em cada evento, em milissegundos (vibra, pausa, vibra...). */
const PADROES: Partial<Record<EventoDto["evento"], number | number[]>> = {
    PECA_FIXADA: 12,
    LINHAS_ELIMINADAS: 30,
    COLAPSO: [70, 50, 140],
    META_CUMPRIDA: [30, 40, 30],
};

/**
 * Vibra o aparelho no evento, quando ele sabe vibrar (quase sempre, só no
 * Android). Sem suporte, ou se o jogador pediu menos movimento, não faz nada.
 *
 * @param evento o evento recebido
 * @returns se pediu para vibrar
 */
export function vibrarNoEvento(evento: EventoDto): boolean {
    const padrao = PADROES[evento.evento];
    if (padrao === undefined || typeof navigator.vibrate !== "function") {
        return false;
    }
    if (typeof window.matchMedia === "function" && window.matchMedia("(pointer: fine)").matches) {
        return false;
    }
    return navigator.vibrate(padrao);
}
