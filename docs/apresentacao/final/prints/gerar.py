#!/usr/bin/env python3
"""Gera os prints de código dos slides a partir dos arquivos de verdade.

Cada print mostra trechos do arquivo com os números de linha reais. Entre dois
trechos aparece uma linha dobrada (⋯), como no editor. As linhas que a fala
aponta ficam destacadas.

Uso, da raiz do repositório:

    pip install pygments          # uma vez
    CHROME=/caminho/do/chrome python3 docs/apresentacao/final/prints/gerar.py

Sem CHROME, o script procura google-chrome, chromium e chromium-browser.
Os PNGs vão para docs/apresentacao/final/img/codigo/.

Se o código mudar de lugar, o print muda junto: cada trecho começa numa linha
achada por texto (o "inicio"), não num número fixo.
"""

from __future__ import annotations

import html
import os
import re
import shutil
import subprocess
import sys
import tempfile
from pathlib import Path

from pygments import highlight
from pygments.formatters import HtmlFormatter
from pygments.lexers import HtmlLexer, JavaLexer, TypeScriptLexer

RAIZ = Path(__file__).resolve().parents[4]
SAIDA = Path(__file__).resolve().parents[1] / "img" / "codigo"
JAVA = "backend/src/main/java/canteiro/"
TESTE = "backend/src/test/java/canteiro/"

ESTILO = "one-dark"
FONTE_PX = 17
ALTURA_LINHA = 26
LARGURA = 1000

# Cada trecho: (texto que acha a primeira linha, quantas linhas; None = até fechar a chave).
PRINTS = {
    "arquitetura": (TESTE + "ArquiteturaTest.java", [
        ("private static final List<String> SERVIDOR_E_JSON", 1),
        ("void pacoteNaoImportaOQueEProibido", None),
    ], ["SERVIDOR_E_JSON =", "violacoes.isEmpty"]),
    "sacola": (JAVA + "modelo/estruturas/GeradorPecas.java", [
        ("private final Deque<Forma> sacola", 2),
        ("public Peca proxima()", None),
        ("private Forma tirarDaSacola()", None),
    ], ["Collections.shuffle", "return sacola.poll", "return fila.poll"]),
    "reserva": (JAVA + "modelo/estruturas/Reserva.java", [
        ("private final Deque<Peca> pilha", 2),
        ("public Optional<Peca> trocar(Peca atual)", None),
    ], ["travada = true", "pilha.poll()", "pilha.push"]),
    "rotacao": (JAVA + "modelo/PecaEmQueda.java", [
        ("public boolean girar(boolean horario)", None),
    ], ["deslocamentosCorretivos", "girarPeca(!horario)"]),
    "fixar": (JAVA + "modelo/Tabuleiro.java", [
        ("public void fixar(List<Celula> celulas, Material material)", None),
    ], ["material.aoFixar"]),
    "aco": (JAVA + "modelo/materiais/Aco.java", [
        ("public final class Aco extends Material", 1),
        ("public double cargaMaxima()", None),
        ("public void aoFixar(Tabuleiro tabuleiro", None),
    ], ["POSITIVE_INFINITY", "consolidarAbaixo"]),
    "pontuacao": (JAVA + "modelo/Pontuacao.java", [
        ("private static final int[] BASE", 1),
        ("public static int pontos(int linhas, int nivel, Material predominante, boolean", 1),
        ("double multiplicador = (1 + predominante", 5),
    ], ["double multiplicador", "BONUS_OUSADIA", "bonusSequencia(sequencia)", "BASE[linhas] * nivel"]),
    "centro": (JAVA + "modelo/fisica/AnalisadorEstrutural.java", [
        ("private double massaTotal;", 2),
        ("public void registrar(Celula celula", None),
        ("public void remover(Celula celula", 4),
        ("public double centroDeMassa()", None),
    ], ["momento +=", "momento -=", "return momento / massaTotal"]),
    "estabilidade": (JAVA + "modelo/fisica/AnalisadorEstrutural.java", [
        ("public Estabilidade estabilidade(Tabuleiro tabuleiro", None),
        ("private static Estabilidade medir(", None),
    ], ["topo.desvio() > inteira.desvio()", "double desvio =", "double indice ="]),
    "teste": (TESTE + "modelo/EquilibrioTest.java", [
        ("void incrementalCoincideComAVarreduraApos500Pecas", None),
    ], ["while (pecas < 500)", "centroPorVarredura", "centroDeMassa(), 0.001"]),
    "colapso": (JAVA + "modelo/fisica/Colapso.java", [
        ("public static List<Queda> executar(Tabuleiro tabuleiro)", None),
        ("static Celula cair(Tabuleiro tabuleiro", None),
    ], ["linhaCritica(tabuleiro);", "!bloco.consolidado()", "return cair(tabuleiro, abaixo)"]),
    "balanco": (JAVA + "modelo/fisica/Balanco.java", [
        ("public static List<Queda> executar(", None),
    ], ["emBalanco.add", "material.densidade() <= apoioMaisForte"]),
    "esmagamento": (JAVA + "modelo/fisica/Esmagamento.java", [
        ("public static Resultado executar(", None),
        ("public static double carga(", None),
    ], ["while (cedeu != null)", "carga += "]),
    "fimdejogo": (JAVA + "modelo/MotorJogo.java", [
        ("public Previa previa()", None),
    ], ["estado.encerrada()"]),
    "concorrencia": (JAVA + "controle/SessaoPartida.java", [
        ("private final Queue<Comando> comandos", 1),
        ("laco.scheduleAtFixedRate", 1),
        ("public void enfileirar(Comando comando)", None),
        ("    void ciclo()", None),
    ], ["new ConcurrentLinkedQueue", "comandos.add", "comandos.poll()"]),
    "repeticao": (JAVA + "modelo/Reprodutor.java", [
        ("public void passo(MotorJogo motor)", None),
        ("private int aplicarDoCiclo(", None),
    ], ["jogadas.get(aplicadas).ciclo() == motor.ciclo()", "motor.aplicar"]),
    "ranking": (JAVA + "api/rotas/RotasRanking.java", [
        ("private void gravar(Context ctx", 22),
    ], ["sessao.modoTreino()", "placar.pontuacao()"]),
    "arquivos": (JAVA + "persistencia/LeitorPropriedades.java", [
        ("public static Optional<Properties> lerArquivo(Path arquivo)", None),
    ], ["return Optional.empty();", "catch (IOException | IllegalArgumentException"]),
    "painel": ("frontend/src/componentes/PainelEstabilidade.svelte", [
        ("const previa = $derived", 3),
        ("{#if previa?.derruba}", 7),
    ], ["previa?.derruba", "estabilidade.alerta"]),
}


def achar(linhas: list[str], texto: str, arquivo: str) -> int:
    for i, linha in enumerate(linhas):
        if texto in linha:
            return i
    sys.exit(f"{arquivo}: não achei '{texto}'")


def ate_fechar(linhas: list[str], inicio: int) -> int:
    """Índice da linha que fecha a chave aberta na linha de início."""
    saldo = 0
    abriu = False
    for i in range(inicio, len(linhas)):
        sem_texto = re.sub(r'"(\\.|[^"\\])*"|\'(\\.|[^\'\\])*\'', "", linhas[i])
        sem_texto = sem_texto.split("//")[0]
        saldo += sem_texto.count("{") - sem_texto.count("}")
        abriu = abriu or "{" in sem_texto
        if abriu and saldo == 0:
            return i
    return len(linhas) - 1


def lexer_de(arquivo: str):
    if arquivo.endswith(".java"):
        return JavaLexer()
    if arquivo.endswith(".ts"):
        return TypeScriptLexer()
    return HtmlLexer()


def montar(nome: str, arquivo: str, trechos, destaques) -> tuple[str, int]:
    linhas = (RAIZ / arquivo).read_text(encoding="utf-8").splitlines()
    escolhidas: list[int | None] = []
    for texto, quantas in trechos:
        i = achar(linhas, texto, arquivo)
        fim = ate_fechar(linhas, i) if quantas is None else i + quantas - 1
        if escolhidas:
            escolhidas.append(None)
        escolhidas.extend(range(i, fim + 1))

    recuo = min(len(linhas[i]) - len(linhas[i].lstrip()) for i in escolhidas if i is not None and linhas[i].strip())
    fmt = HtmlFormatter(style=ESTILO, nowrap=True, noclasses=True)
    lexer = lexer_de(arquivo)
    corpo = []
    for i in escolhidas:
        if i is None:
            corpo.append('<div class="l dobra"><span class="n"></span><span class="c">⋯</span></div>')
            continue
        texto = linhas[i][recuo:] if linhas[i].strip() else ""
        colorido = highlight(texto, lexer, fmt).rstrip("\n") if texto else ""
        classe = "l marca" if any(d in linhas[i] for d in destaques) else "l"
        corpo.append(f'<div class="{classe}"><span class="n">{i + 1}</span><span class="c">{colorido}</span></div>')

    caminho = html.escape(arquivo)
    nome_arquivo = html.escape(Path(arquivo).name)
    fundo = HtmlFormatter(style=ESTILO).style.background_color
    altura = 46 + 34 + len(escolhidas) * ALTURA_LINHA + 24
    mais_longa = max(len(linhas[i]) - recuo for i in escolhidas if i is not None)
    largura = max(LARGURA, int(64 + 18 + mais_longa * FONTE_PX * 0.61 + 30))
    pagina = f"""<!DOCTYPE html><html><head><meta charset="utf-8"><style>
    * {{ margin: 0; padding: 0; box-sizing: border-box; }}
    html, body {{ width: {largura}px; height: {altura}px; background: transparent; overflow: hidden; }}
    .janela {{ width: {largura}px; height: {altura}px; background: {fundo}; border-radius: 12px; overflow: hidden;
               font-family: "JetBrains Mono", "DejaVu Sans Mono", monospace; }}
    .barra {{ height: 46px; background: #21252b; display: flex; align-items: center; padding: 0 16px; gap: 8px; }}
    .barra i {{ width: 13px; height: 13px; border-radius: 50%; display: inline-block; }}
    .aba {{ margin-left: 18px; background: {fundo}; color: #d7dae0; font: 600 15px Inter, sans-serif;
            padding: 13px 18px; border-radius: 8px 8px 0 0; align-self: flex-end; }}
    .trilha {{ height: 34px; color: #7f848e; font: 13px Inter, sans-serif; padding: 9px 20px; border-bottom: 1px solid #2c313a; }}
    .codigo {{ padding: 10px 0 14px; }}
    .l {{ display: flex; height: {ALTURA_LINHA}px; line-height: {ALTURA_LINHA}px; font-size: {FONTE_PX}px; white-space: pre; color: #abb2bf; }}
    .n {{ width: 64px; text-align: right; padding-right: 18px; color: #4b5263; flex: none; }}
    .marca {{ background: rgba(227, 163, 34, .16); box-shadow: inset 4px 0 0 #e3a322; }}
    .marca .n {{ color: #e3a322; }}
    .dobra .c {{ color: #5c6370; padding-left: 4px; }}
    </style></head><body><div class="janela">
    <div class="barra"><i style="background:#ff5f57"></i><i style="background:#febc2e"></i><i style="background:#28c840"></i>
    <span class="aba">{nome_arquivo}</span></div>
    <div class="trilha">{caminho}</div>
    <div class="codigo">{''.join(corpo)}</div></div></body></html>"""
    return pagina, altura, largura


def chrome() -> str:
    if os.environ.get("CHROME"):
        return os.environ["CHROME"]
    for nome in ("google-chrome", "chromium", "chromium-browser"):
        if shutil.which(nome):
            return nome
    sys.exit("Chrome não encontrado: defina a variável CHROME")


def main() -> None:
    SAIDA.mkdir(parents=True, exist_ok=True)
    navegador = chrome()
    with tempfile.TemporaryDirectory() as pasta:
        for nome, (arquivo, trechos, destaques) in PRINTS.items():
            pagina, altura, largura = montar(nome, arquivo, trechos, destaques)
            fonte = Path(pasta) / f"{nome}.html"
            fonte.write_text(pagina, encoding="utf-8")
            destino = SAIDA / f"{nome}.png"
            subprocess.run([navegador, "--headless", "--no-sandbox", "--hide-scrollbars",
                            "--force-device-scale-factor=2", "--default-background-color=00000000",
                            f"--window-size={largura},{altura}", f"--screenshot={destino}",
                            fonte.as_uri()], check=True, capture_output=True)
            print(f"{destino.relative_to(RAIZ)}  ({largura} × {altura})")


if __name__ == "__main__":
    main()
