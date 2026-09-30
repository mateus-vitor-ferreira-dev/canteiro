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
    /** Partida de treino: dá para desfazer a última jogada e ela não entra no ranking (RF26, RN15). */
    modoTreino: boolean;
};

/** Resposta de `POST /api/partidas`: o id para conectar em `/ws/partidas/{id}`. Espelho de `PartidaCriadaDto`. */
export type PartidaCriadaDto = {
    id: string;
};

/**
 * Uma linha do ranking, como vem de `GET /api/ranking` (RF22). Espelho de
 * `EntradaRankingDto`, que o backend cria na #31.
 */
export type EntradaRankingDto = {
    nome: string;
    pontuacao: number;
    nivel: number;
    linhas: number;
    colapsos: number;
    /** Dia em que a partida foi registrada, no formato ISO (`"2026-09-28"`). */
    data: string;
};

/**
 * Corpo de `POST /api/ranking` (RF21). A pontuação não vai aqui: o backend lê
 * da partida, para ninguém mandar pontos inventados. Espelho de `RegistroRankingDto`.
 */
export type RegistroRankingDto = {
    partidaId: string;
    nome: string;
};

/**
 * O relatório da partida, como vem de `GET /api/partidas/{id}/relatorio`
 * (RF25). Espelho de `RelatorioDto`, que o backend cria na #70.
 */
export type RelatorioDto = {
    /** Índice de estabilidade logo depois de cada peça fixada, de 1 (desvio nulo) a 0, na ordem. */
    indices: number[];
    /** Número de cada peça (a partir de 1) cuja fixação derrubou a estrutura. */
    colapsos: number[];
    /** Quantas peças de cada material foram fixadas, pelo código do material. */
    materiais: Record<string, number>;
};

/**
 * As configurações do jogador, em `GET` e `PUT /api/configuracoes` (RF27).
 * Espelho de `ConfiguracoesDto`, que o backend cria na #71.
 */
export type ConfiguracoesDto = {
    /**
     * As teclas de cada ação (`ESQUERDA`, `RESERVAR`, `PAUSA`, `SOM`...), como
     * valores de `KeyboardEvent.code`. Ação que não aparece fica com a padrão.
     */
    teclas: Record<string, string[]>;
    /** Volume dos efeitos, de 0 a 100. */
    volume: number;
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
    /** Células ocupadas: no tabuleiro, para a peça atual; no quadrado da forma, para as próximas. */
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
    /** Peça guardada na reserva, com as células no quadrado da forma, ou `null` se não há. */
    reservada: PecaDto | null;
    /** Se a troca com a reserva está liberada nesta peça (RN05). */
    podeReservar: boolean;
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
