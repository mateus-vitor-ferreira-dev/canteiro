# Apresentação final

| Arquivo | O que é |
|---|---|
| [`slides.html`](slides.html) | Os slides. Cada `<section>` é um slide de 1280 × 720 |
| [`ROTEIRO.md`](ROTEIRO.md) | O que falar em cada slide, com os tempos |
| [`img/codigo/`](img/codigo/) | Os prints de código, gerados dos arquivos do repositório |
| [`prints/gerar.py`](prints/gerar.py) | O script que gera esses prints |
| [`img/`](img/) | Prints do jogo (versão 0.3.0) e o diagrama de arquitetura |
| [`../Apresentacao_Final_Canteiro.pdf`](../Apresentacao_Final_Canteiro.pdf) | O PDF que vai para o Campus Virtual |

As caixas amarelas tracejadas são partes que ainda faltam preencher.

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
