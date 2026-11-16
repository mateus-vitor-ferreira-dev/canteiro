// Confere o RNF04 num navegador: abre o jogo, começa uma partida, joga pelo teclado e confere
// que o estado chega pelo WebSocket, que o tabuleiro é desenhado e que não há erro no console.
//
// Uso: node navegadores.cjs <url do jogo> <chrome|firefox> <executável do navegador>
"use strict";

const puppeteer = require("puppeteer-core");

const [url, tipo, executavel] = process.argv.slice(2);
const espera = (ms) => new Promise((r) => setTimeout(r, ms));

(async () => {
    const navegador = await puppeteer.launch({
        browser: tipo === "firefox" ? "firefox" : "chrome",
        executablePath: executavel,
        headless: true,
    });
    const pagina = await navegador.newPage();
    await pagina.setViewport({ width: 1366, height: 768 });
    const erros = [];
    pagina.on("console", (m) => m.type() === "error" && erros.push(m.text()));
    pagina.on("pageerror", (e) => erros.push(String(e)));

    await pagina.goto(url, { waitUntil: "load" });
    await espera(1500);
    const clicar = (texto) => pagina.evaluate((t) => {
        const b = [...document.querySelectorAll("button")].find((x) => x.innerText.trim().startsWith(t));
        if (b) b.click();
        return Boolean(b);
    }, texto);
    const resultado = { navegador: await navegador.version() };
    resultado.menu = await clicar("Nova partida");
    await espera(800);
    resultado.dificuldade = await clicar("Normal");
    await espera(2000);

    const placar = () => pagina.evaluate(() => document.body.innerText.match(/PONTUAÇÃO\s+(\d+)/i)?.[1] ?? null);
    resultado.partidaAberta = (await placar()) !== null;
    // Conta quantas vezes o canvas muda: se o estado chega e é desenhado, a peça anda.
    const quadro = () => pagina.evaluate(() => document.querySelector("canvas")?.toDataURL().length ?? 0);
    const antes = await quadro();
    for (let i = 0; i < 30; i++) {
        await pagina.keyboard.press(["ArrowLeft", "ArrowRight", "ArrowUp", " "][i % 4]);
        await espera(150);
    }
    resultado.tabuleiroMudou = antes !== (await quadro());
    resultado.pontuacaoDepois = await placar();
    resultado.quadrosPorSegundo = await pagina.evaluate(() => new Promise((fim) => {
        let n = 0;
        const t0 = performance.now();
        const conta = () => { n++; performance.now() - t0 < 3000 ? requestAnimationFrame(conta) : fim(n / 3); };
        requestAnimationFrame(conta);
    }));
    resultado.errosNoConsole = erros;
    console.log(JSON.stringify(resultado));
    await navegador.close();
})().catch((e) => {
    console.log(JSON.stringify({ falhou: String(e) }));
    process.exit(1);
});
