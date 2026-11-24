# CT-RF19-01: pausar e retomar com P ou Esc

| Campo | Conteúdo |
|---|---|
| **Requisito** | RF19 (pausar e retomar a partida) e RNF05 (legenda das teclas sempre visível) |
| **Prioridade** | 🟡 importante |
| **Onde testar** | Online ou JAR |
| **Pré-condição** | Partida em andamento, com uma peça caindo |

## Passos

| # | Faça | Deve acontecer |
|---|---|---|
| 1 | Aperte **P** | Aparece a camada **Pausado**, com "Aperte P para continuar." e os botões **Retomar**, **Recomeçar** e **Sair para o menu**. A peça para de cair |
| 2 | Aperte **P** de novo | A camada some e a peça continua do mesmo ponto |
| 3 | Aperte **Esc** | A partida pausa, igual ao passo 1 |
| 4 | Clique em **Retomar** | A partida continua do mesmo ponto |
| 5 | Pause e clique em **Recomeçar** | Abre a escolha de dificuldade para uma partida nova |
| 6 | Comece outra partida, pause e clique em **Sair para o menu** | Volta ao menu principal |
| 7 | Durante a partida, olhe a lateral da tela | A legenda das teclas está visível o tempo todo |

## Caminhos errados

| # | Faça | Deve acontecer |
|---|---|---|
| 1 | Com a partida pausada, aperte **←**, **→** e **Espaço** | A peça não se mexe e o tempo não corre |

## Observações

A pausa automática ao trocar de aba é conferida no CT-RF30-01.
