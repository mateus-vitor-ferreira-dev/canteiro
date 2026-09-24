"""Gera os diagramas do README em SVG, no tema escuro, com fundo próprio.

O fundo vem dentro do SVG, então a imagem fica igual no GitHub claro e no escuro.

Para mudar um diagrama: edite este arquivo e rode, da raiz do repositório,
    python3 docs/imagens/diagramas/gerar.py
Os SVGs saem em docs/imagens/. Só usa a biblioteca padrão do Python.
"""
from pathlib import Path
from xml.sax.saxutils import escape

SAIDA = Path(__file__).resolve().parent.parent
FONTE = "Inter, 'Segoe UI', Helvetica, Arial, sans-serif"

TEMAS = {
    "escuro": dict(texto="#E6EDF3", suave="#8B949E", painel="#161B22", borda_painel="#30363D",
                   no="#0D1117", borda_no="#3D444D", seta="#8B949E", fundo_rotulo="#0D1117",
                   modelo="#1F3864", borda_modelo="#4A6AA8", texto_modelo="#FFFFFF", chip="#2D4A80",
                   ambar="#E3A322", verde="#56D364", vermelho="#F47067", titulo_painel="#8B949E",
                   fundo="#0D1117", borda_fundo="#30363D"),
}


def largura_texto(texto, tamanho, negrito=False):
    return len(texto) * tamanho * (0.60 if negrito else 0.55)


class Svg:
    def __init__(self, largura, altura, tema):
        self.l, self.a, self.c = largura, altura, TEMAS[tema]
        self.partes = []

    def add(self, s):
        self.partes.append(s)

    def texto(self, x, y, t, tam=14, cor=None, peso=400, ancora="middle", estilo="normal"):
        self.add(f'<text x="{x}" y="{y}" font-size="{tam}" font-weight="{peso}" font-style="{estilo}" '
                 f'fill="{cor or self.c["texto"]}" text-anchor="{ancora}">{escape(t)}</text>')

    def painel(self, x, y, w, h, titulo):
        c = self.c
        self.add(f'<rect x="{x}" y="{y}" width="{w}" height="{h}" rx="14" fill="{c["painel"]}" stroke="{c["borda_painel"]}"/>')
        self.texto(x + 18, y + 28, titulo.upper(), 12, c["titulo_painel"], 700, "start")

    def no(self, cx, cy, w, h, titulo, sub=None, destaque=None):
        c = self.c
        borda = destaque or c["borda_no"]
        self.add(f'<rect x="{cx - w / 2}" y="{cy - h / 2}" width="{w}" height="{h}" rx="10" fill="{c["no"]}" '
                 f'stroke="{borda}" stroke-width="{2 if destaque else 1.2}"/>')
        if sub:
            self.texto(cx, cy - 4, titulo, 15, peso=700)
            self.texto(cx, cy + 16, sub, 12.5, c["suave"])
        else:
            self.texto(cx, cy + 5, titulo, 15, peso=700)

    def seta(self, pontos, rotulo=None, pos_rotulo=None, tracejada=False, dupla=False, cor=None):
        cor = cor or self.c["seta"]
        d = "M " + " L ".join(f"{x} {y}" for x, y in pontos)
        tr = ' stroke-dasharray="6 5"' if tracejada else ""
        ini = f' marker-start="url(#ponta-{cor[1:]})"' if dupla else ""
        self.add(f'<path d="{d}" fill="none" stroke="{cor}" stroke-width="1.8"{tr}{ini} marker-end="url(#ponta-{cor[1:]})"/>')
        self._marcadores.add(cor)
        if rotulo:
            x, y = pos_rotulo or pontos[len(pontos) // 2]
            self.rotulo(x, y, rotulo)

    def rotulo(self, x, y, t, tam=12.5, cor=None):
        w = largura_texto(t, tam) + 14
        self.add(f'<rect x="{x - w / 2}" y="{y - 11}" width="{w}" height="20" rx="5" fill="{self.c["fundo_rotulo"]}" '
                 f'stroke="{self.c["borda_painel"]}"/>')
        self.texto(x, y + 4, t, tam, cor or self.c["suave"])

    _marcadores = set()

    def salvar(self, nome):
        marcadores = "".join(
            f'<marker id="ponta-{cor[1:]}" viewBox="0 0 10 10" refX="9" refY="5" markerWidth="7" markerHeight="7" '
            f'orient="auto-start-reverse"><path d="M0 0 L10 5 L0 10 z" fill="{cor}"/></marker>'
            for cor in sorted(self._marcadores))
        svg = (f'<svg xmlns="http://www.w3.org/2000/svg" width="{self.l}" height="{self.a}" viewBox="0 0 {self.l} {self.a}" '
               f'font-family="{FONTE}"><defs>{marcadores}</defs>'
               f'<rect x="0.5" y="0.5" width="{self.l - 1}" height="{self.a - 1}" rx="16" fill="{self.c["fundo"]}" '
               f'stroke="{self.c["borda_fundo"]}"/>{"".join(self.partes)}</svg>\n')
        (SAIDA / nome).write_text(svg, encoding="utf-8")
        Svg._marcadores = set()


def arquitetura(tema):
    s = Svg(1120, 600, tema)
    c = s.c
    # navegador
    s.painel(20, 20, 330, 500, "Navegador · Svelte + TypeScript")
    s.no(185, 120, 270, 62, "api/", "cliente REST · conexão WebSocket")
    s.no(185, 260, 270, 62, "estado/", "partida · teclado · navegação")
    s.no(185, 400, 270, 62, "telas/ e componentes/", "desenham; o tabuleiro é um Canvas")
    s.seta([(185, 151), (185, 229)], dupla=True)
    s.seta([(185, 291), (185, 369)], dupla=True)
    s.texto(185, 480, "só desenha e envia teclas: nenhuma regra aqui", 12.5, c["suave"], estilo="italic")
    # canais
    s.seta([(320, 100), (600, 100)], "comandos (teclas)", (460, 86))
    s.seta([(600, 140), (320, 140)], "estado + eventos, até 60×/s", (460, 154))
    s.texto(460, 60, "WebSocket /ws/partidas/{id}", 13, c["ambar"], 700)
    s.seta([(320, 205), (600, 205)], dupla=True)
    s.texto(460, 196, "REST /api/…  (JSON)", 13, c["ambar"], 700)
    s.texto(460, 226, "dificuldades · ranking · configurações", 12, c["suave"])
    # backend
    s.painel(600, 20, 500, 500, "Backend · Java 17 · 127.0.0.1:7070")
    s.no(770, 120, 300, 62, "canteiro.api", "rotas/ · ws/ · dto/ · Swagger em /api/docs")
    s.no(770, 235, 300, 62, "canteiro.controle", "sessões · fila de comandos · laço 60 Hz")
    s.seta([(770, 151), (770, 204)])
    # modelo
    mx, my, mw, mh = 630, 300, 330, 190
    s.add(f'<rect x="{mx}" y="{my}" width="{mw}" height="{mh}" rx="12" fill="{c["modelo"]}" stroke="{c["borda_modelo"]}" stroke-width="2"/>')
    s.texto(mx + mw / 2, my + 32, "canteiro.modelo", 17, c["texto_modelo"], 800)
    s.texto(mx + mw / 2, my + 54, "toda a regra do jogo · MotorJogo · Tabuleiro", 12.5, "#C9D6EE")
    chips = ["pecas", "materiais", "estruturas", "fisica", "constantes"]
    x = mx + 22
    y = my + 78
    for i, nome in enumerate(chips):
        w = largura_texto(nome, 13) + 22
        if x + w > mx + mw - 18:
            x, y = mx + 22, y + 36
        s.add(f'<rect x="{x}" y="{y}" width="{w}" height="26" rx="13" fill="{c["chip"]}" stroke="#4A6AA8"/>')
        s.texto(x + w / 2, y + 17.5, nome, 13, "#FFFFFF", 600)
        x += w + 10
    s.texto(mx + mw / 2, my + mh - 18, "sem Javalin · sem JSON · sem gráfico", 12.5, c["ambar"], 700)
    s.seta([(770, 266), (770, 299)])
    s.seta([(880, 299), (880, 267)], tracejada=True)
    s.rotulo(938, 283, "observador", 11.5)
    # persistência
    s.no(1030, 235, 120, 62, "persistencia", "arquivos")
    s.seta([(920, 235), (969, 235)])
    s.seta([(1030, 266), (1030, 370)], dupla=True)
    cx, cy = 1030, 400
    s.add(f'<path d="M {cx - 48} {cy - 20} L {cx - 48} {cy + 30} A 48 12 0 0 0 {cx + 48} {cy + 30} L {cx + 48} {cy - 20}" '
          f'fill="{c["no"]}" stroke="{c["borda_no"]}" stroke-width="1.2"/>')
    s.add(f'<ellipse cx="{cx}" cy="{cy - 20}" rx="48" ry="12" fill="{c["no"]}" stroke="{c["borda_no"]}" stroke-width="1.2"/>')
    s.texto(cx, cy + 12, "~/.canteiro/", 13, peso=700)
    s.texto(cx, cy + 62, "ranking · configs", 11.5, c["suave"])
    s.texto(cx, cy + 78, "repetições", 11.5, c["suave"])
    # rodapé
    s.texto(560, 562, "Tudo roda na máquina do jogador e sai num único JAR. O servidor só aceita conexões da própria máquina.",
            13.5, c["suave"])
    s.salvar("arquitetura.svg")


def estados(tema):
    s = Svg(1180, 500, tema)
    c = s.c
    Y = 220
    pos = {"GERANDO_PECA": (150, Y), "PECA_CAINDO": (400, Y), "FIXANDO": (680, Y), "ELIMINANDO_LINHAS": (970, Y),
           "PAUSA": (400, 330), "COLAPSO": (680, 400), "FIM_DE_JOGO": (1000, 400)}
    larg = {"GERANDO_PECA": 180, "PECA_CAINDO": 170, "FIXANDO": 130, "ELIMINANDO_LINHAS": 210, "PAUSA": 110,
            "COLAPSO": 130, "FIM_DE_JOGO": 160}
    cores = {"COLAPSO": c["vermelho"], "FIM_DE_JOGO": c["vermelho"], "PAUSA": c["ambar"]}
    # início
    s.add(f'<circle cx="30" cy="{Y}" r="9" fill="{c["texto"]}"/>')
    s.seta([(39, Y), (59, Y)])
    s.rotulo(62, Y - 42, "partida criada", 11.5)
    # caminho principal, com os rótulos no vão entre as caixas
    s.seta([(240, Y), (314, Y)])
    s.seta([(485, Y), (614, Y)], "colidiu embaixo", (550, Y - 17))
    s.seta([(745, Y), (864, Y)], "linha completa", (805, Y - 17))
    # voltas para GERANDO_PECA, por cima
    s.seta([(680, Y - 22), (680, 150), (175, 150), (175, Y - 23)], "estável, sem linha", (560, 150))
    s.seta([(970, Y - 22), (970, 110), (125, 110), (125, Y - 23)], "linhas eliminadas", (820, 110))
    # movimento: volta para o próprio estado
    s.add(f'<path d="M 375 {Y - 22} C 365 {Y - 62}, 435 {Y - 62}, 425 {Y - 22}" fill="none" stroke="{c["seta"]}" '
          f'stroke-width="1.8" marker-end="url(#ponta-{c["seta"][1:]})"/>')
    s._marcadores.add(c["seta"])
    s.texto(400, Y - 56, "move · gira · desce", 11.5, c["suave"])
    # pausa, embaixo da peça em queda
    s.seta([(375, Y + 22), (375, 307)])
    s.rotulo(244, 275, "PAUSAR · conexão caiu · aba sem foco", 11.5)
    s.seta([(425, 307), (425, Y + 23)])
    s.rotulo(464, 275, "RETOMAR", 11.5)
    # colapso
    s.seta([(660, Y + 22), (660, 377)], "desvio > limite", (596, 300))
    s.seta([(940, Y + 22), (940, 330), (720, 330), (720, 377)], "desvio > limite", (830, 330))
    s.seta([(614, 400), (110, 400), (110, Y + 23)], "reacomodou", (360, 400))
    s.seta([(745, 400), (919, 400)], "pilha > 18 linhas", (832, 382))
    s.seta([(75, Y + 22), (75, 465), (1000, 465), (1000, 423)], "peça nasce colidindo", (540, 465))
    # fim
    s.add(f'<circle cx="1120" cy="400" r="10" fill="none" stroke="{c["texto"]}" stroke-width="2"/>'
          f'<circle cx="1120" cy="400" r="5" fill="{c["texto"]}"/>')
    s.seta([(1081, 400), (1106, 400)])
    for nome, (x, y) in pos.items():
        s.no(x, y, larg[nome], 44, nome, destaque=cores.get(nome))
    s.salvar("estados.svg")


if __name__ == "__main__":
    arquitetura("escuro")
    estados("escuro")
    print("diagramas gerados em", SAIDA)
