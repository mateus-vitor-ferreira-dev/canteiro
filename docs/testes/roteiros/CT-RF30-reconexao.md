# CT-RF30: pausa automática e reconexão

## CT-RF30-01: a partida pausa sozinha quando a aba perde o foco

| Campo | Conteúdo |
|---|---|
| **Requisito** | RF30 (pausar quando a aba perde o foco ou a conexão cai) |
| **Prioridade** | 🔴 essencial |
| **Onde testar** | Online ou JAR |
| **Pré-condição** | Uma partida em andamento |

| # | Faça | Deve acontecer |
|---|---|---|
| 1 | Com uma peça caindo, troque para outra aba do navegador e espere uns 5 segundos | — |
| 2 | Volte para a aba do jogo | Aparece **Pausado**, com **"Aperte P para continuar."** A peça está no mesmo lugar em que você saiu |
| 3 | Aperte **P** | A partida continua do mesmo ponto |

## CT-RF31-01: reconecta sozinho quando a internet volta

| Campo | Conteúdo |
|---|---|
| **Requisito** | RF30 e RF31 (reconectar sozinho e voltar do ponto em que parou) |
| **Prioridade** | 🔴 essencial |
| **Onde testar** | Online, no **Chrome** ou no **Edge** (precisa das ferramentas do desenvolvedor) |
| **Pré-condição** | Uma partida em andamento, com algumas peças já fixadas |

| # | Faça | Deve acontecer |
|---|---|---|
| 1 | Aperte **F12** para abrir as ferramentas do desenvolvedor e vá na aba **Network** (Rede) | — |
| 2 | Onde está escrito **No throttling**, escolha **Offline** | Em poucos segundos aparece **Reconectando…** por cima do tabuleiro, com **"A partida está pausada e volta do mesmo ponto."** |
| 3 | Espere uns 10 segundos e volte para **No throttling** | O aviso some sozinho e aparece **Pausado**, com as peças fixadas no mesmo lugar |
| 4 | Feche o F12 e aperte **P** | A partida continua do mesmo ponto, com a mesma pontuação |

## CT-RF31-02: avisa quando a partida se perdeu porque o servidor reiniciou

| Campo | Conteúdo |
|---|---|
| **Requisito** | RF31 · RNF11 (o jogo não fica em tela branca nem travado) |
| **Prioridade** | 🟡 importante |
| **Onde testar** | JAR |
| **Pré-condição** | Uma partida em andamento, pelo JAR |

| # | Faça | Deve acontecer |
|---|---|---|
| 1 | Sem fechar a aba, encerre o JAR (feche a janela do terminal ou aperte `Ctrl + C` nele) | Aparece **Reconectando…** |
| 2 | Abra o JAR de novo e volte para a aba antiga do jogo | Aparece **Partida perdida**, com **"O jogo foi reiniciado e esta partida não existe mais."** e o botão **Começar outra**. Nada de tela branca |

## Observações

- O CT-RF30-01 é o mais simples e já pega a maior parte dos problemas: comece por ele.
- O modo Offline do passo 2 do CT-RF31-01 só desliga a internet **daquela aba**: o resto do computador continua conectado.
- Na versão online, a partida sem conexão por **10 minutos** é descartada de propósito. Não deixe o Offline ligado por tanto tempo.
