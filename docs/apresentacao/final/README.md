# Apresentação final

| Arquivo | O que é |
|---|---|
| [`slides.html`](slides.html) | Os slides. Cada `<section>` é um slide de 1280 × 720 |
| [`ROTEIRO.md`](ROTEIRO.md) | O que falar em cada slide, com os tempos |
| [`img/codigo/`](img/codigo/) | Os prints de código, gerados dos arquivos do repositório |
| [`prints/gerar.py`](prints/gerar.py) | O script que gera esses prints |
| [`img/`](img/) | Prints do jogo (versão 0.3.0) e o diagrama de arquitetura |
| [`img/jogo/`](img/jogo/) | Os prints "No jogo" dos slides de código, tirados do JAR da 0.3.2 |
| [`prints/jogo/`](prints/jogo/) | O robô que joga pelo navegador e tira esses prints |
| [`img/qr-jogo.svg`](img/qr-jogo.svg) · [`img/qr-jogo-ufla.svg`](img/qr-jogo-ufla.svg) | Os QR codes do último slide: o endereço do jogo e o alternativo, para a rede da UFLA |
| [`../Apresentacao_Final_Canteiro.pdf`](../Apresentacao_Final_Canteiro.pdf) | O PDF que vai para o Campus Virtual |

As caixas amarelas tracejadas (classe `preencher`) marcam partes que ainda faltam preencher. Hoje não há nenhuma.

## Como mudar um slide e gerar o PDF

1. Edite o texto em `slides.html` (dá para abrir o arquivo no navegador para ver como ficou).
2. Gere o PDF com o Chrome ou o Chromium, de dentro desta pasta:

   ```bash
   google-chrome --headless --no-pdf-header-footer \
     --print-to-pdf=../Apresentacao_Final_Canteiro.pdf "file://$PWD/slides.html"
   ```

   No Windows, troque `google-chrome` pelo caminho do `chrome.exe` e `$PWD` pelo caminho da pasta. Sem terminal, abra o `slides.html` no Chrome, use **Imprimir → Salvar como PDF**, com margens **Nenhuma** e **Gráficos de plano de fundo** ligado.

## Como refazer os prints de código

Os prints mostram o código como ele está no repositório, com os números de linha reais. Se o código mudar, gere os prints de novo, da raiz do repositório:

```bash
pip install pygments
python3 docs/apresentacao/final/prints/gerar.py
```

Cada print é uma entrada de `PRINTS` no script, com o arquivo, os trechos e as linhas que ficam destacadas. Os trechos são achados por texto, então continuam certos se o código descer algumas linhas. **Os números de linha citados nos slides ("linha 59") estão escritos à mão no `slides.html` e no roteiro.** Se um print mudar, confira esses números também.

## Como refazer os prints do jogo

Os slides de código mostram, embaixo do código (no 19 e no 23, na coluna da direita), um print do jogo com o requisito funcionando. Eles saem de um robô que joga sozinho: ele lê o estado da partida pelo WebSocket, escolhe cada jogada simulando a queda da peça e tira o print no momento certo (a linha fechando, o colapso, o esmagamento).

```bash
PORT=7171 CANTEIRO_PASTA=/tmp/canteiro-prints CANTEIRO_ABRIR_NAVEGADOR=false java -jar canteiro.jar &
cd docs/apresentacao/final/prints/jogo && npm i puppeteer-core
OUT=/tmp/prints URL=http://127.0.0.1:7171 node cenas.cjs normal   # também: alerta, pesado, rotacao, fim Ana Bruno Carla Diego ultimo, telas
```

A pasta de dados separada (`CANTEIRO_PASTA`) evita misturar o ranking e as repetições do robô com os seus. As partidas têm sorteio, então cada rodada sai diferente: escolha os prints bons e recorte a região do slide. Os slides 10, 18, 24 e 27 não têm print do jogo, porque o que eles mostram (arquitetura, teste, threads e arquivos) não aparece na tela.
