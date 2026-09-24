import type { EstadoDto } from "@/api/protocolo";

/** Um estado de partida válido para os testes, com o que o teste quiser trocar. */
export function estadoDeTeste(parcial: Partial<EstadoDto> = {}): EstadoDto {
    const tabuleiro = Array.from({ length: 22 }, () => Array<string | null>(10).fill(null));
    return {
        tipo: "ESTADO",
        ciclo: 1,
        estado: "PECA_CAINDO",
        tabuleiro,
        pecaAtual: {
            forma: "T",
            material: "MADEIRA",
            blocos: [
                [2, 4],
                [3, 3],
                [3, 4],
                [3, 5],
            ],
        },
        pecaFantasma: [
            [20, 4],
            [21, 3],
            [21, 4],
            [21, 5],
        ],
        proximas: [
            {
                forma: "O",
                material: "ACO",
                blocos: [
                    [0, 0],
                    [0, 1],
                    [1, 0],
                    [1, 1],
                ],
            },
        ],
        placar: { pontuacao: 1234, linhas: 7, nivel: 2, colapsos: 1, tempoSegundos: 60 },
        estabilidade: {
            indice: 0.68,
            desvio: 1.4,
            limite: 3,
            centroDeMassa: 3.6,
            eixo: 5,
            alerta: false,
        },
        ...parcial,
    };
}
