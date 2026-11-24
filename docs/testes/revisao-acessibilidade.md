# Revisão de usabilidade e acessibilidade

| Campo | Conteúdo |
|---|---|
| **Issue** | #34 · RNF05, RNF06 |
| **Feita por** | Alisson Antônio Oliveira Santos (contraste conferido por Mateus) |
| **Data** | 2026-11-23 e 2026-11-24 |
| **Versão** | Online e JAR da `develop` (0.3.1), no Google Chrome, no Windows 11 |

## Resultado

| Item | O que foi feito | Resultado |
|---|---|---|
| **Daltonismo** | Chrome, F12 → Rendering → *Emulate vision deficiencies*, em Protanopia, Deuteranopia, Tritanopia e Achromatopsia (tons de cinza) | ✅ Madeira, alvenaria, concreto e aço continuam distinguíveis em todas, porque cada material tem uma textura própria, além da cor |
| **Contraste** | Razão de contraste (WCAG) das cores de texto definidas em `frontend/src/estilos/global.css` e na camada de pausa | ✅ Todas acima de 4,5:1 (tabela abaixo) |
| **Primeira vez** | Uma pessoa que nunca tinha jogado começou uma partida sem ajuda | ✅ Achou os comandos intuitivos e entendeu a legenda das teclas |
| **Só teclado** | Jogar e navegar pelas telas sem o mouse (Tab, Enter e as setas) | ✅ Deu para fazer tudo |
| **Textos** | Leitura das telas de escolha de dificuldade, partida, pausa e fim de jogo | ✅ Sem erro de digitação. Um termo técnico: a mensagem de erro de conexão dizia "O backend está rodando?" ([#151](https://github.com/mateus-vitor-ferreira-dev/canteiro/issues/151), corrigido para "Não foi possível conectar ao jogo. Tente novamente.") |

## Contraste

| Texto | Fundo | Razão |
|---|---|---|
| Texto principal `#1b2432` | Fundo `#f6f4ef` / cartão `#ffffff` | 14,2:1 / 15,6:1 |
| Texto suave `#5b6472` | Fundo / cartão | 5,4:1 / 6,0:1 |
| Rótulo âmbar escuro `#8a5a00` | Fundo / cartão | 5,4:1 / 5,9:1 |
| Marinho `#1f3864` | Fundo | 10,6:1 |
| Branco | Camada de pausa e fim de jogo (`#1b2432` a 80 %) | 15,6:1 |
| Marinho `#1f3864` | Botão âmbar `#e3a322` | 5,3:1 |
| Vermelho `#c0392b` / verde `#2e7d32` | Cartão | 5,4:1 / 5,1:1 |

## Ajustes registrados

- [#151](https://github.com/mateus-vitor-ferreira-dev/canteiro/issues/151): mensagem de erro de conexão sem o termo "backend". Corrigido.
