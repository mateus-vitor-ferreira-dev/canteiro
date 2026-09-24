import type { Comando } from "@/api/protocolo";

/** Uma linha da tabela de teclas: é dela que saem o comando e a legenda. */
export type Tecla = {
    /** Valores de `KeyboardEvent.code` que disparam o comando. */
    codigos: string[];
    /** Como a tecla aparece na legenda. */
    rotulo: string;
    /** O que ela faz, para a legenda. */
    acao: string;
    /** Comando enviado; `PAUSA` alterna entre pausar e retomar. */
    comando: Comando | "PAUSA";
};

/** As teclas do jogo (RF07 a RF10, RF19). A legenda da tela é gerada daqui (RNF05). */
export const TECLAS: readonly Tecla[] = [
    { codigos: ["ArrowLeft"], rotulo: "←", acao: "Mover para a esquerda", comando: "ESQUERDA" },
    { codigos: ["ArrowRight"], rotulo: "→", acao: "Mover para a direita", comando: "DIREITA" },
    { codigos: ["ArrowDown"], rotulo: "↓", acao: "Descer uma linha", comando: "DESCER" },
    {
        codigos: ["Space"],
        rotulo: "Espaço",
        acao: "Queda instantânea",
        comando: "QUEDA_INSTANTANEA",
    },
    {
        codigos: ["ArrowUp", "KeyX"],
        rotulo: "↑ ou X",
        acao: "Girar no sentido horário",
        comando: "GIRAR_HORARIO",
    },
    {
        codigos: ["KeyZ"],
        rotulo: "Z",
        acao: "Girar no sentido anti-horário",
        comando: "GIRAR_ANTI_HORARIO",
    },
    { codigos: ["KeyC"], rotulo: "C", acao: "Guardar na reserva", comando: "RESERVAR" },
    {
        codigos: ["KeyP", "Escape"],
        rotulo: "P ou Esc",
        acao: "Pausar ou continuar",
        comando: "PAUSA",
    },
];

/**
 * Traduz uma tecla em comando. Tecla segurada (repetição automática do
 * teclado) não gera comando: cada toque vale um movimento.
 *
 * @param codigo `KeyboardEvent.code`
 * @param repetida `KeyboardEvent.repeat`
 * @param pausada se a partida está pausada, para a tecla de pausa virar "retomar"
 * @returns o comando, ou `null` se a tecla não faz nada
 */
export function comandoDaTecla(
    codigo: string,
    repetida: boolean,
    pausada: boolean,
): Comando | null {
    if (repetida) {
        return null;
    }
    const tecla = TECLAS.find((t) => t.codigos.includes(codigo));
    if (!tecla) {
        return null;
    }
    if (tecla.comando === "PAUSA") {
        return pausada ? "RETOMAR" : "PAUSAR";
    }
    return tecla.comando;
}
