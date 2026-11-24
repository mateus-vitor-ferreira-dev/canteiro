# CT-RF20-01: a partida acaba e a tela de resultado mostra pontuação, nível, linhas, colapsos e tempo

| Campo | Conteúdo |
|---|---|
| **Requisito** | RF20 (encerrar a partida nas condições de fim de jogo e mostrar o resultado) · RN13 |
| **Prioridade** | 🔴 essencial |
| **Onde testar** | Online ou JAR |
| **Pré-condição** | Partida nova no Difícil, que acaba mais rápido |

## Passos

| # | Faça | Deve acontecer |
|---|---|---|
| 1 | Derrube as peças sempre no meio com **Espaço**, sem fechar linhas, até a pilha chegar ao topo | Quando a peça nova não tem onde aparecer, surge a camada **Fim de jogo** com a pontuação e os botões **Ver resultado** e **Jogar de novo** |
| 2 | Aperte **←**, **→** e **Espaço** | Nada se mexe: a partida acabou |
| 3 | Clique em **Ver resultado** | Abre a tela **Obra encerrada**, com os pontos, **Nível**, **Linhas**, **Colapsos** e **Tempo** iguais aos do placar no fim da partida |
| 4 | Volte e clique em **Jogar de novo** | Abre a escolha de dificuldade para uma partida nova |

## Caminhos errados

| # | Faça | Deve acontecer |
|---|---|---|
| 1 | Feche a aba na camada **Fim de jogo** e abra o jogo de novo | O jogo abre no menu, sem travar e sem a partida antiga |

## Observações

A outra condição de fim (colapso com a pilha acima de 18 linhas) é conferida pelos testes automáticos. Na tela de resultado também dá para salvar o nome no ranking, que é conferido no CT-RF21-01.
