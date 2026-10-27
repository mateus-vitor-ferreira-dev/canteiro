import type { Comando } from "@/api/protocolo";

/** Até quantos pixels o dedo pode andar e ainda contar como toque, não como arrasto. */
export const FOLGA_TOQUE_PX = 10;

/** Toque mais longo que isso não gira: o jogador só encostou o dedo e desistiu. */
export const DURACAO_TOQUE_MS = 250;

/** Velocidade para baixo, ao soltar, que vira queda instantânea, em pixels por milissegundo. */
export const VELOCIDADE_QUEDA = 0.9;

/** Quanto tempo atrás o gesto rápido é medido: o fim do arrasto, não o arrasto inteiro. */
const JANELA_VELOCIDADE_MS = 100;

type Eixo = "horizontal" | "vertical" | null;
type Ponto = { x: number; y: number; t: number };

/**
 * Transforma o arrasto do dedo no tabuleiro em comandos (RNF05):
 *
 * - arrastar para os lados move uma coluna a cada célula percorrida;
 * - arrastar para baixo desce uma linha a cada célula;
 * - um toque rápido gira no sentido horário;
 * - um gesto rápido para baixo, ao soltar, faz a queda instantânea;
 * - arrastar para cima guarda a peça na reserva.
 *
 * O primeiro movimento decide o eixo, para um arrasto para o lado não descer
 * a peça sem querer. É só cálculo: quem ouve o dedo é o tabuleiro.
 */
export class ReconhecedorGestos {
    #celula: number;
    #inicio: Ponto | null = null;
    #ultimo: Ponto | null = null;
    #recentes: Ponto[] = [];
    #eixo: Eixo = null;
    /** Onde o último comando foi dado, para contar as células a partir dali. */
    #ancora = { x: 0, y: 0 };

    /** @param celula lado de um quadrado do tabuleiro, em pixels */
    constructor(celula: number) {
        this.#celula = celula;
    }

    /** O tabuleiro mudou de tamanho. */
    set celula(valor: number) {
        this.#celula = valor;
    }

    /** O dedo encostou. */
    comecar(x: number, y: number, t: number): void {
        this.#inicio = { x, y, t };
        this.#ultimo = this.#inicio;
        this.#recentes = [this.#inicio];
        this.#eixo = null;
        this.#ancora = { x, y };
    }

    /**
     * O dedo andou.
     *
     * @returns os movimentos que o trecho percorrido pede, na ordem
     */
    mover(x: number, y: number, t: number): Comando[] {
        if (!this.#inicio) {
            return [];
        }
        this.#guardar({ x, y, t });
        this.#eixo ??= this.#decidirEixo(this.#inicio, x, y);
        if (this.#eixo === "horizontal") {
            return this.#passos(x - this.#ancora.x, "x", "DIREITA", "ESQUERDA");
        }
        if (this.#eixo === "vertical" && y > this.#ancora.y) {
            return this.#passos(y - this.#ancora.y, "y", "DESCER", null);
        }
        return [];
    }

    /**
     * O dedo saiu da tela.
     *
     * @returns o comando do gesto inteiro (girar, cair ou reservar), se houver
     */
    terminar(x: number, y: number, t: number): Comando[] {
        const inicio = this.#inicio;
        if (!inicio) {
            return [];
        }
        this.#guardar({ x, y, t });
        const eixo = this.#eixo;
        const dy = y - inicio.y;
        this.#inicio = null;
        if (eixo === null) {
            const toque = Math.hypot(x - inicio.x, dy) <= FOLGA_TOQUE_PX;
            return toque && t - inicio.t <= DURACAO_TOQUE_MS ? ["GIRAR_HORARIO"] : [];
        }
        if (eixo === "vertical" && dy > 0 && this.#velocidadeParaBaixo() >= VELOCIDADE_QUEDA) {
            return ["QUEDA_INSTANTANEA"];
        }
        if (eixo === "vertical" && dy < -2 * this.#celula) {
            return ["RESERVAR"];
        }
        return [];
    }

    /** O sistema tirou o dedo (outra janela, rolagem): o gesto não vale. */
    cancelar(): void {
        this.#inicio = null;
    }

    #guardar(ponto: Ponto): void {
        this.#ultimo = ponto;
        this.#recentes.push(ponto);
        this.#recentes = this.#recentes.filter((p) => ponto.t - p.t <= JANELA_VELOCIDADE_MS);
    }

    #decidirEixo(inicio: Ponto, x: number, y: number): Eixo {
        const dx = Math.abs(x - inicio.x);
        const dy = Math.abs(y - inicio.y);
        if (Math.max(dx, dy) <= FOLGA_TOQUE_PX) {
            return null;
        }
        return dx > dy ? "horizontal" : "vertical";
    }

    /** Um comando por célula inteira percorrida desde a âncora, que anda junto. */
    #passos(
        distancia: number,
        eixo: "x" | "y",
        positivo: Comando,
        negativo: Comando | null,
    ): Comando[] {
        const celulas = Math.trunc(distancia / this.#celula);
        const comando = celulas > 0 ? positivo : negativo;
        if (celulas === 0 || !comando) {
            return [];
        }
        this.#ancora[eixo] += celulas * this.#celula;
        return Array.from({ length: Math.abs(celulas) }, () => comando);
    }

    #velocidadeParaBaixo(): number {
        const primeiro = this.#recentes[0];
        const ultimo = this.#ultimo;
        if (!primeiro || !ultimo || ultimo.t === primeiro.t) {
            return 0;
        }
        return (ultimo.y - primeiro.y) / (ultimo.t - primeiro.t);
    }
}
