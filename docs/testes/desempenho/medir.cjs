// Mede o RNF01 (60 quadros/s na tela) e o RNF03 (tecla → estado novo) jogando de verdade
// no navegador, pelo teclado, durante um tempo fixo. O backend se mede sozinho: o laço da
// partida escreve no log, a cada 10 s, os ciclos por segundo, o tempo do ciclo e a memória.
//
// Uso (veja o README desta pasta para o passo a passo com os limites de CPU e memória):
//   node medir.cjs <url do jogo> <segundos> <saida.json>
//
// Precisa do pacote puppeteer-core (npm install puppeteer-core numa pasta qualquer, e
// NODE_PATH apontando para o node_modules dela) e do Chrome em CHROME (padrão: google-chrome).
//
// Só conta o tempo com partida em jogo: depois de cada fim de jogo, a página é recarregada
// e uma partida nova começa, e os quadros do menu são descartados. Cada janela de 10 s
// registra quantos estados chegaram do backend, como prova de que havia partida rodando.
"use strict";

const fs = require("node:fs");
const puppeteer = require("puppeteer-core");

const [url, segundosTexto, saida] = process.argv.slice(2);
const DURACAO_MS = Number(segundosTexto) * 1000;
const JANELA_MS = 10_000;
const PAUSA_TECLA_MS = 110;
const espera = (ms) => new Promise((r) => setTimeout(r, ms));

// Roda dentro da página antes do jogo: conta quadros, estados recebidos e tecla → ESTADO.
function instrumentar() {
    const medidas = { quadros: [], latencias: [], estados: 0 };
    window.__medidas = medidas;
    let anterior = 0;
    const quadro = (t) => {
        if (anterior) medidas.quadros.push(t - anterior);
        anterior = t;
        requestAnimationFrame(quadro);
    };
    requestAnimationFrame(quadro);

    const Original = window.WebSocket;
    window.WebSocket = class extends Original {
        constructor(...args) {
            super(...args);
            this.__pendente = null;
            this.addEventListener("message", (e) => {
                if (typeof e.data !== "string" || !e.data.includes('"tipo":"ESTADO"')) return;
                medidas.estados++;
                if (this.__pendente !== null) {
                    medidas.latencias.push(performance.now() - this.__pendente);
                    this.__pendente = null;
                }
            });
        }
        send(dados) {
            if (typeof dados === "string" && dados.includes('"COMANDO"') && this.__pendente === null) {
                this.__pendente = performance.now();
            }
            return super.send(dados);
        }
    };
}

function percentil(valores, p) {
    if (valores.length === 0) return null;
    const ordenados = [...valores].sort((a, b) => a - b);
    return ordenados[Math.min(ordenados.length - 1, Math.floor((p / 100) * ordenados.length))];
}

// Memória (RSS) somada do Chrome e de todos os processos filhos dele.
function memoriaMb(pidRaiz) {
    const filhos = new Map();
    for (const nome of fs.readdirSync("/proc")) {
        if (!/^\d+$/.test(nome)) continue;
        try {
            const stat = fs.readFileSync(`/proc/${nome}/stat`, "utf8");
            const ppid = Number(stat.slice(stat.lastIndexOf(")") + 2).split(" ")[1]);
            if (!filhos.has(ppid)) filhos.set(ppid, []);
            filhos.get(ppid).push(Number(nome));
        } catch {
            // o processo terminou no meio da leitura
        }
    }
    let total = 0;
    const fila = [pidRaiz];
    while (fila.length) {
        const pid = fila.pop();
        try {
            const rss = /VmRSS:\s+(\d+)/.exec(fs.readFileSync(`/proc/${pid}/status`, "utf8"));
            if (rss) total += Number(rss[1]);
        } catch {
            continue;
        }
        fila.push(...(filhos.get(pid) || []));
    }
    return Math.round(total / 1024);
}

async function clicar(pagina, inicio) {
    return pagina.evaluate((texto) => {
        const botao = [...document.querySelectorAll("button")].find((b) => b.innerText.trim().startsWith(texto));
        if (botao) botao.click();
        return Boolean(botao);
    }, inicio);
}

const textoDaPagina = (pagina) => pagina.evaluate(() => document.body.innerText);

// Abre uma partida nova no Normal, a partir de uma página recarregada, e descarta o que o
// menu mediu. Só volta com a partida na tela.
async function comecarPartida(pagina) {
    for (let tentativa = 1; ; tentativa++) {
        await pagina.goto(url, { waitUntil: "networkidle0" });
        await clicar(pagina, "Nova partida");
        await espera(500);
        await clicar(pagina, "Normal");
        await espera(1500);
        if ((await textoDaPagina(pagina)).includes("PONTUAÇÃO")) break;
        if (tentativa === 5) throw new Error("a partida não abriu");
    }
    await pagina.evaluate(() => {
        window.__medidas.quadros = [];
        window.__medidas.latencias = [];
        window.__medidas.estados = 0;
    });
}

// Tira da página o que ela mediu desde a última coleta e soma na janela atual.
async function coletar(pagina, janela) {
    const m = await pagina.evaluate(() => {
        const dados = { ...window.__medidas };
        window.__medidas.quadros = [];
        window.__medidas.latencias = [];
        window.__medidas.estados = 0;
        return dados;
    });
    janela.quadros.push(...m.quadros);
    janela.latencias.push(...m.latencias);
    janela.estados += m.estados;
}

const novaJanela = () => ({ quadros: [], latencias: [], estados: 0 });

(async () => {
    const navegador = await puppeteer.launch({
        executablePath: process.env.CHROME || "google-chrome",
        headless: true,
        args: ["--window-size=1366,768"],
    });
    const pagina = await navegador.newPage();
    await pagina.setViewport({ width: 1366, height: 768 });
    await pagina.evaluateOnNewDocument(instrumentar);
    await comecarPartida(pagina);

    const janelas = [];
    let janela = novaJanela();
    let partidas = 1;
    let teclas = 0;
    let pecas = 0;
    const inicio = Date.now();
    let fimJanela = inicio + JANELA_MS;
    while (Date.now() - inicio < DURACAO_MS) {
        // Uma peça: gira ao acaso, leva até uma coluna da vez (da esquerda à direita) e solta.
        const alvo = pecas % 10;
        const sequencia = [];
        for (let i = 0; i < pecas % 3; i++) sequencia.push("ArrowUp");
        for (let i = 0; i < 5; i++) sequencia.push("ArrowLeft");
        for (let i = 0; i < alvo; i++) sequencia.push("ArrowRight");
        sequencia.push("ArrowDown", "Space");
        for (const tecla of sequencia) {
            await pagina.keyboard.press(tecla);
            teclas++;
            await espera(PAUSA_TECLA_MS);
        }
        pecas++;

        if (/Fim de jogo|Obra encerrada/.test(await textoDaPagina(pagina))) {
            await coletar(pagina, janela);
            await comecarPartida(pagina);
            partidas++;
        }
        if (Date.now() >= fimJanela) {
            await coletar(pagina, janela);
            const soma = janela.quadros.reduce((a, b) => a + b, 0);
            janelas.push({
                segundo: Math.round((Date.now() - inicio) / 1000),
                quadrosPorSegundo: soma ? (janela.quadros.length * 1000) / soma : 0,
                quadroP99: percentil(janela.quadros, 99),
                quadrosLentos: janela.quadros.filter((d) => d > 33.4).length,
                quadros: janela.quadros.length,
                estadosRecebidos: janela.estados,
                latencias: janela.latencias,
                memoriaNavegadorMb: memoriaMb(navegador.process().pid),
            });
            janela = novaJanela();
            fimJanela += JANELA_MS;
        }
    }
    await navegador.close();

    const todasLatencias = janelas.flatMap((j) => j.latencias);
    const resumo = {
        duracaoS: DURACAO_MS / 1000,
        partidas,
        pecas,
        teclas,
        janelasSemEstado: janelas.filter((j) => j.estadosRecebidos === 0).length,
        quadrosPorSegundo: {
            media: janelas.reduce((a, j) => a + j.quadrosPorSegundo, 0) / janelas.length,
            minimoJanela: Math.min(...janelas.map((j) => j.quadrosPorSegundo)),
            maximoJanela: Math.max(...janelas.map((j) => j.quadrosPorSegundo)),
            quadrosLentos: janelas.reduce((a, j) => a + j.quadrosLentos, 0),
            quadrosTotais: janelas.reduce((a, j) => a + j.quadros, 0),
        },
        teclaAteEstadoMs: {
            amostras: todasLatencias.length,
            mediana: percentil(todasLatencias, 50),
            p95: percentil(todasLatencias, 95),
            maximo: Math.max(...todasLatencias),
        },
        memoriaNavegadorMb: { maximo: Math.max(...janelas.map((j) => j.memoriaNavegadorMb)) },
        janelas: janelas.map(({ latencias, ...resto }) => ({ ...resto, amostrasLatencia: latencias.length })),
    };
    fs.writeFileSync(saida, JSON.stringify(resumo, null, 2));
    console.log(JSON.stringify({ ...resumo, janelas: undefined }, null, 2));
})().catch((erro) => {
    console.error(erro);
    process.exit(1);
});
