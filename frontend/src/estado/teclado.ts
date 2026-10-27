import type { Comando } from "@/api/protocolo";

/** Uma linha da tabela de teclas: é dela que saem o comando e a legenda. */
export type Tecla = {
    /** Valores de `KeyboardEvent.code` que disparam o comando. */
    codigos: string[];
    /** Como a tecla aparece na legenda. */
    rotulo: string;
    /** O que ela faz, para a legenda. */
    acao: string;
    /** Comando enviado; `PAUSA` alterna entre pausar e retomar; `SOM` fica no navegador. */
    comando: Comando | "PAUSA" | "SOM";
    /** A tecla só vale no modo treino (RF26). */
    soTreino?: boolean;
};

/** As teclas do jogo (RF07 a RF10, RF19, RF26, RF28). A legenda da tela é gerada daqui (RNF05). */
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
        codigos: ["KeyU"],
        rotulo: "U",
        acao: "Desfazer a última jogada",
        comando: "DESFAZER",
        soTreino: true,
    },
    {
        codigos: ["KeyP", "Escape"],
        rotulo: "P ou Esc",
        acao: "Pausar ou continuar",
        comando: "PAUSA",
    },
    { codigos: ["KeyM"], rotulo: "M", acao: "Ligar ou desligar o som", comando: "SOM" },
];

/**
 * Traduz uma tecla em comando. Tecla segurada (repetição automática do
 * teclado) não gera comando: cada toque vale um movimento.
 *
 * @param codigo `KeyboardEvent.code`
 * @param repetida `KeyboardEvent.repeat`
 * @param pausada se a partida está pausada, para a tecla de pausa virar "retomar"
 * @param teclas a tabela em uso, com as teclas que o jogador trocou
 * @returns o comando; `"SOM"` para ligar ou desligar o som, que não vai ao
 *     backend; ou `null` se a tecla não faz nada
 */
export function comandoDaTecla(
    codigo: string,
    repetida: boolean,
    pausada: boolean,
    teclas: readonly Tecla[] = TECLAS,
): Comando | "SOM" | null {
    if (repetida) {
        return null;
    }
    const tecla = teclas.find((t) => t.codigos.includes(codigo));
    if (!tecla) {
        return null;
    }
    if (tecla.comando === "PAUSA") {
        return pausada ? "RETOMAR" : "PAUSAR";
    }
    return tecla.comando;
}

/** O atalho de desfazer que não muda com as configurações: Ctrl+Z, ou ⌘Z no Mac. */
const ATALHO_DESFAZER = "Ctrl+Z";

/**
 * A tabela que vale numa partida: fora do modo treino, as teclas só de treino
 * saem; no treino, a de desfazer ganha o atalho fixo na legenda.
 *
 * @param teclas a tabela em uso, com as teclas que o jogador trocou
 * @param treino se a partida é de treino
 */
export function teclasDoModo(teclas: readonly Tecla[], treino: boolean): Tecla[] {
    if (!treino) {
        return teclas.filter((tecla) => !tecla.soTreino);
    }
    return teclas.map((tecla) =>
        tecla.comando === "DESFAZER"
            ? { ...tecla, rotulo: `${tecla.rotulo} ou ${ATALHO_DESFAZER}` }
            : tecla,
    );
}

/**
 * A tabela que vale numa repetição: o jogador só assiste, então sobram a pausa
 * e o som.
 *
 * @param teclas a tabela em uso, com as teclas que o jogador trocou
 */
export function teclasDaRepeticao(teclas: readonly Tecla[]): Tecla[] {
    return teclas.filter((tecla) => tecla.comando === "PAUSA" || tecla.comando === "SOM");
}

/**
 * Se a tecla é o atalho de desfazer (Ctrl+Z ou ⌘Z). Ele é conferido antes da
 * tabela, porque o Z sozinho gira a peça.
 *
 * @param codigo `KeyboardEvent.code`
 * @param comControle se Ctrl ou ⌘ está apertado
 */
export function ehAtalhoDesfazer(codigo: string, comControle: boolean): boolean {
    return comControle && codigo === "KeyZ";
}

/**
 * O comando de um toque no teclado durante a partida: primeiro o atalho de
 * desfazer, depois a tabela do modo.
 *
 * @param evento a tecla apertada
 * @param pausada se a partida está pausada
 * @param teclas a tabela em uso, com as teclas que o jogador trocou
 * @param treino se a partida é de treino
 */
export function comandoDoEvento(
    evento: Pick<KeyboardEvent, "code" | "repeat" | "ctrlKey" | "metaKey">,
    pausada: boolean,
    teclas: readonly Tecla[],
    treino: boolean,
): Comando | "SOM" | null {
    if (treino && ehAtalhoDesfazer(evento.code, evento.ctrlKey || evento.metaKey)) {
        return "DESFAZER";
    }
    return comandoDaTecla(evento.code, evento.repeat, pausada, teclasDoModo(teclas, treino));
}

/** O que uma tecla faz: um comando do jogo, a pausa (que alterna) ou o som. */
export type AcaoTecla = Tecla["comando"];

/** As teclas que o jogador trocou, por ação. Ação que não aparece fica com a padrão. */
export type TeclasPersonalizadas = Partial<Record<AcaoTecla, string[]>>;

/**
 * A tabela em uso: a padrão, com as teclas trocadas pelo jogador (RF27). O
 * rótulo da legenda sai das teclas novas.
 */
export function montarTeclas(personalizadas: TeclasPersonalizadas): Tecla[] {
    return TECLAS.map((tecla) => {
        const codigos = personalizadas[tecla.comando];
        if (!codigos || codigos.length === 0) {
            return tecla;
        }
        return { ...tecla, codigos, rotulo: codigos.map(rotuloDaTecla).join(" ou ") };
    });
}

const NOMES: Record<string, string> = {
    ArrowLeft: "←",
    ArrowRight: "→",
    ArrowUp: "↑",
    ArrowDown: "↓",
    Space: "Espaço",
    Escape: "Esc",
    Enter: "Enter",
    Backspace: "Apagar",
    Minus: "-",
    Equal: "=",
    Comma: ",",
    Period: ".",
};

/**
 * Como uma tecla aparece para o jogador: `KeyA` vira "A", `Digit1` vira "1",
 * `ArrowUp` vira "↑". Pontuação que muda de lugar entre layouts (ABNT2, EUA)
 * fica com o nome do código, para a legenda nunca mostrar a tecla errada.
 */
export function rotuloDaTecla(codigo: string): string {
    if (NOMES[codigo]) {
        return NOMES[codigo];
    }
    const letraOuNumero = /^(?:Key|Digit)(.)$/.exec(codigo);
    if (letraOuNumero?.[1]) {
        return letraOuNumero[1];
    }
    const teclado = /^Numpad(.+)$/.exec(codigo);
    return teclado?.[1] ? `Num ${teclado[1]}` : codigo;
}

/**
 * Teclas que não podem virar comando: Tab é como se anda pela página, e as
 * modificadoras (Shift, Ctrl, Alt) sozinhas não são um toque de verdade.
 */
export function teclaReservada(codigo: string): boolean {
    return /^(Tab|Shift|Control|Alt|Meta|OS|CapsLock|Fn)/.test(codigo);
}
