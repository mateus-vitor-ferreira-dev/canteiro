import type { EventoDto } from "@/api/protocolo";

/**
 * O texto que o leitor de tela fala quando algo acontece na partida. Peça
 * fixada não é anunciada: acontece o tempo todo e só atrapalharia.
 *
 * @param evento o evento recebido
 * @param nivel nível atual, para anunciar a subida
 * @returns o aviso, ou `null` se o evento não merece ser falado
 */
export function anuncioDoEvento(evento: EventoDto, nivel?: number): string | null {
    switch (evento.evento) {
        case "LINHAS_ELIMINADAS": {
            const n = evento.dados.linhas?.length ?? 0;
            return n === 1 ? "1 linha eliminada" : `${n} linhas eliminadas`;
        }
        case "COLAPSO":
            return "A estrutura desabou!";
        case "NIVEL_SUBIU":
            return nivel ? `Subiu para o nível ${nivel}` : "Subiu de nível";
        case "FIM_DE_JOGO":
            return "Fim de jogo";
        case "ERRO":
            return evento.dados.mensagem ?? "Erro na partida";
        default:
            return null;
    }
}
