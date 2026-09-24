import { conectarPartida, type ConexaoPartida, type OuvinteConexao } from "@/api/conexao";
import type { Comando, EstadoDto, EventoDto, MensagemDoServidor } from "@/api/protocolo";

/** Abre a conexão; os testes trocam por uma falsa. */
export type Conectar = (id: string, ouvinte: OuvinteConexao) => ConexaoPartida;

/**
 * Uma partida acontecendo agora: guarda o último estado recebido pelo
 * WebSocket, de forma reativa, e manda os comandos do jogador.
 */
export class PartidaAoVivo {
    /** Último estado completo recebido; `null` até a primeira mensagem. */
    estado = $state<EstadoDto | null>(null);
    /** Último evento recebido, para animação e som. */
    ultimoEvento = $state<EventoDto | null>(null);
    /** Se a conexão está aberta. */
    conectado = $state(false);

    readonly #conexao: ConexaoPartida;

    constructor(id: string, conectar: Conectar = conectarPartida) {
        this.#conexao = conectar(id, {
            aoAbrir: () => (this.conectado = true),
            aoFechar: () => (this.conectado = false),
            aoReceber: (mensagem) => this.#receber(mensagem),
        });
    }

    /** A partida está pausada. */
    get pausada(): boolean {
        return this.estado?.estado === "PAUSA";
    }

    /** A partida acabou. */
    get encerrada(): boolean {
        return this.estado?.estado === "FIM_DE_JOGO";
    }

    /** Manda um comando do jogador. */
    enviar(comando: Comando): void {
        this.#conexao.enviar(comando);
    }

    /** Fecha a conexão; no backend, a partida pausa. */
    encerrar(): void {
        this.#conexao.fechar();
    }

    #receber(mensagem: MensagemDoServidor): void {
        if (mensagem.tipo === "ESTADO") {
            this.estado = mensagem;
        } else {
            this.ultimoEvento = mensagem;
        }
    }
}
