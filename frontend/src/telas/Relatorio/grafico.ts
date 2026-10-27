import {
    Chart,
    Filler,
    LineController,
    LineElement,
    LinearScale,
    PointElement,
    Tooltip,
    type ChartDataset,
    type ChartOptions,
    type Plugin,
} from "chart.js";
import type { Ponto } from "./series";

Chart.register(LineController, LineElement, PointElement, LinearScale, Tooltip, Filler);

type Escalas = ChartOptions<"line">["scales"];
type Dicas = NonNullable<ChartOptions<"line">["plugins"]>["tooltip"];

/** O gráfico da estabilidade: uma linha, e os colapsos como pontos soltos sobre ela. */
export type GraficoEstabilidade = Chart<"line", { x: number; y: number }[]>;

// Validadas juntas pelo validador de paleta (faixa, croma, daltonismo e contraste).
const COR_SERIE = "#2f5fb3";
const COR_AREA = "rgba(47, 95, 179, 0.1)";
const COR_CRITICO = "#d03b3b";
const COR_SUPERFICIE = "#f6f4ef";
const COR_GRADE = "#e2ded5";
const COR_TEXTO = "#5b6472";
const COR_MIRA = "#8a93a0";
const COR_DICA = "#1b2432";
const FONTE = 'Inter, "Noto Sans", system-ui, sans-serif';
const PORCENTO_MAXIMO = 100;
const PASSO_PORCENTO = 25;

/**
 * Desenha o gráfico no canvas. Sem animação: o relatório abre pronto.
 *
 * @param canvas onde desenhar
 * @param pontos estabilidade depois de cada peça
 * @param colapsos os pontos em que a estrutura desabou
 */
export function criarGrafico(
    canvas: HTMLCanvasElement,
    pontos: Ponto[],
    colapsos: Ponto[],
): GraficoEstabilidade {
    return new Chart(canvas, {
        type: "line",
        data: { datasets: series(pontos, colapsos) },
        options: {
            animation: false,
            responsive: true,
            maintainAspectRatio: false,
            interaction: { mode: "nearest", axis: "x", intersect: false },
            scales: escalas(pontos.length),
            plugins: {
                legend: { display: false },
                tooltip: dicas(new Set(colapsos.map((c) => c.peca))),
            },
        },
        plugins: [MIRA],
    });
}

function series(
    pontos: Ponto[],
    colapsos: Ponto[],
): ChartDataset<"line", { x: number; y: number }[]>[] {
    const xy = (p: Ponto) => ({ x: p.peca, y: p.estabilidade });
    return [
        {
            label: "Estabilidade",
            data: pontos.map(xy),
            borderColor: COR_SERIE,
            borderWidth: 2,
            borderJoinStyle: "round",
            borderCapStyle: "round",
            backgroundColor: COR_AREA,
            fill: "origin",
            pointRadius: 0,
            pointHoverRadius: 5,
            pointHoverBackgroundColor: COR_SERIE,
            pointHoverBorderColor: COR_SUPERFICIE,
            pointHoverBorderWidth: 2,
        },
        {
            label: "Colapso",
            data: colapsos.map(xy),
            showLine: false,
            // Colapso costuma ir a quase 0 %: sem isto, o triângulo sai cortado pelo eixo.
            clip: false,
            pointStyle: "triangle",
            pointRadius: 7,
            pointHoverRadius: 7,
            pointBackgroundColor: COR_CRITICO,
            pointBorderColor: COR_SUPERFICIE,
            pointBorderWidth: 2,
        },
    ];
}

function escalas(pecas: number): Escalas {
    const texto = { color: COR_TEXTO, font: { family: FONTE } };
    return {
        x: {
            type: "linear",
            min: 1,
            max: Math.max(pecas, 2),
            title: { display: true, text: "Peça", ...texto },
            ticks: { precision: 0, ...texto },
            grid: { display: false },
            border: { color: COR_GRADE },
        },
        y: {
            min: 0,
            max: PORCENTO_MAXIMO,
            ticks: { stepSize: PASSO_PORCENTO, ...texto, callback: (valor) => `${valor} %` },
            grid: { color: COR_GRADE, lineWidth: 1 },
            border: { display: false },
        },
    };
}

/** Uma leitura por peça: a da linha, com o valor em destaque. O colapso entra como observação. */
function dicas(pecasQueDesabaram: Set<number>): Dicas {
    return {
        filter: (item) => item.datasetIndex === 0,
        backgroundColor: COR_DICA,
        padding: 10,
        displayColors: false,
        titleFont: { family: FONTE, weight: "normal" },
        bodyFont: { family: FONTE, weight: "bold", size: 14 },
        footerFont: { family: FONTE, weight: "normal" },
        callbacks: {
            title: (itens) => `Peça ${itens[0]?.parsed.x ?? ""}`,
            label: (item) => `${item.parsed.y} % de estabilidade`,
            footer: (itens) =>
                pecasQueDesabaram.has(itens[0]?.parsed.x ?? -1) ? "A estrutura desabou aqui" : "",
        },
    };
}

/** A linha vertical que acompanha o ponteiro: o leitor mira a peça, não a linha fina. */
const MIRA: Plugin<"line"> = {
    id: "mira",
    afterDatasetsDraw(grafico) {
        const ativo = grafico.tooltip?.getActiveElements()[0];
        if (!ativo) {
            return;
        }
        const { ctx, chartArea } = grafico;
        ctx.save();
        ctx.strokeStyle = COR_MIRA;
        ctx.lineWidth = 1;
        ctx.beginPath();
        ctx.moveTo(ativo.element.x, chartArea.top);
        ctx.lineTo(ativo.element.x, chartArea.bottom);
        ctx.stroke();
        ctx.restore();
    },
};
