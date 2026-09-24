/**
 * Tipos do protocolo entre backend e frontend.
 *
 * Cada tipo aqui é o espelho de um `record` de `canteiro.api` no backend.
 * Mudou um lado, muda o outro no mesmo PR (RNF15).
 */

/** Código de uma dificuldade, usado ao criar a partida. */
export type CodigoDificuldade = "FACIL" | "NORMAL" | "DIFICIL";

/** Uma dificuldade, como vem de `GET /api/dificuldades` (RF02). Espelho de `DificuldadeDto`. */
export type DificuldadeDto = {
    codigo: CodigoDificuldade;
    nome: string;
    /** Intervalo inicial de queda, em milissegundos. */
    intervaloQuedaMs: number;
    /** Desvio máximo tolerado antes do colapso, em colunas. */
    limiteDesvio: number;
    /** Códigos dos materiais liberados no início da partida. */
    materiaisLiberados: string[];
};

/** Corpo de toda resposta de erro da API. Espelho de `ErroDto`. */
export type ErroDto = {
    erro: string;
    mensagem: string;
};

/** Corpo de `POST /api/partidas`. Espelho de `NovaPartidaDto`. */
export type NovaPartidaDto = {
    dificuldade: CodigoDificuldade;
};

/** Resposta de `POST /api/partidas`: o id para conectar em `/ws/partidas/{id}`. Espelho de `PartidaCriadaDto`. */
export type PartidaCriadaDto = {
    id: string;
};

/** Comandos que o jogador envia pelo WebSocket. Espelho do enum `Comando`. */
export type Comando =
    | "ESQUERDA"
    | "DIREITA"
    | "DESCER"
    | "QUEDA_INSTANTANEA"
    | "GIRAR_HORARIO"
    | "GIRAR_ANTI_HORARIO"
    | "RESERVAR"
    | "PAUSAR"
    | "RETOMAR"
    | "DESFAZER";

/** Mensagem do navegador para o backend. Espelho de `ComandoDto`. */
export type ComandoDto = {
    tipo: "COMANDO";
    comando: Comando;
};

/** Estados da partida. Espelho do enum `EstadoPartida`. */
export type EstadoPartida =
    | "GERANDO_PECA"
    | "PECA_CAINDO"
    | "FIXANDO"
    | "ELIMINANDO_LINHAS"
    | "COLAPSO"
    | "PAUSA"
    | "FIM_DE_JOGO";

/** Posição na grade: `[linha, coluna]`, a partir de zero no topo à esquerda. */
export type Posicao = [number, number];

/** Uma peça. Espelho de `EstadoDto.PecaDto`. */
export type PecaDto = {
    forma: "I" | "O" | "T" | "S" | "Z" | "J" | "L";
    material: string;
    /** Células ocupadas; vazia para as próximas peças. */
    blocos: Posicao[];
};

/** Espelho de `EstadoDto.PlacarDto`. */
export type PlacarDto = {
    pontuacao: number;
    linhas: number;
    nivel: number;
    colapsos: number;
    tempoSegundos: number;
};

/** Espelho de `EstadoDto.EstabilidadeDto`. */
export type EstabilidadeDto = {
    /** De 1 (desvio nulo) a 0 (desvio no limite). */
    indice: number;
    desvio: number;
    limite: number;
    centroDeMassa: number;
    eixo: number;
    alerta: boolean;
};

/** O estado completo, enviado a cada mudança. Espelho de `EstadoDto`. */
export type EstadoDto = {
    tipo: "ESTADO";
    ciclo: number;
    estado: EstadoPartida;
    /** 22 linhas × 10 colunas, com o código do material ou `null` onde está vazio. */
    tabuleiro: (string | null)[][];
    pecaAtual: PecaDto | null;
    pecaFantasma: Posicao[];
    proximas: PecaDto[];
    placar: PlacarDto;
    estabilidade: EstabilidadeDto;
};

/** Tipos de evento. Espelho de `EventoPartida.Tipo`, mais o `ERRO` do canal. */
export type TipoEvento =
    "PECA_FIXADA" | "LINHAS_ELIMINADAS" | "COLAPSO" | "NIVEL_SUBIU" | "FIM_DE_JOGO" | "ERRO";

/** Um bloco que caiu no colapso. Espelho de `EventoDto.QuedaDto`. */
export type QuedaDto = {
    origem: Posicao;
    destino: Posicao;
};

/** Algo que aconteceu, para animar e tocar som. Espelho de `EventoDto`; campos vazios não vêm. */
export type EventoDto = {
    tipo: "EVENTO";
    evento: TipoEvento;
    dados: {
        linhas?: number[];
        quedas?: QuedaDto[];
        mensagem?: string;
    };
};

/** Qualquer mensagem do backend pelo WebSocket. */
export type MensagemDoServidor = EstadoDto | EventoDto;
