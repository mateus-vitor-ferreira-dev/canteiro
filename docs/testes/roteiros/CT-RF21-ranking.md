# CT-RF21: salvar no ranking

## CT-RF21-01: o nome entra no ranking e continua lá depois de fechar o jogo

| Campo | Conteúdo |
|---|---|
| **Requisito** | RF21 (registrar a pontuação com o nome, de forma persistente) e RF22 (mostrar as dez melhores) |
| **Prioridade** | 🔴 essencial |
| **Onde testar** | **JAR** (no online, o ranking zera a cada atualização, então o passo 7 não vale lá) |
| **Pré-condição** | JAR aberto no menu |

| # | Faça | Deve acontecer |
|---|---|---|
| 1 | Clique em **Nova partida** e escolha **Difícil** | A partida começa |
| 2 | Aperte **Espaço** várias vezes seguidas, sem mover as peças | As peças empilham no meio até o topo, e a partida acaba em menos de um minuto |
| 3 | Veja o tabuleiro | Aparece **Fim de jogo** por cima dele, com a pontuação e os botões **Ver resultado** e **Jogar de novo** |
| 4 | Clique em **Ver resultado** (ou aperte **Enter**) | Abre a tela **Obra encerrada**, com pontuação, nível, linhas, colapsos e tempo, e o campo **Seu nome no ranking** |
| 5 | Digite `Teste Alisson` e clique em **Salvar no ranking** | O botão mostra **Salvando…** e depois o jogo abre a tela **Ranking**, com `Teste Alisson` **em destaque** e a mesma pontuação da tela de fim |
| 6 | Feche o jogo: feche a aba e encerre o JAR (feche a janela do terminal ou aperte `Ctrl + C` nele) | O jogo para |
| 7 | Abra o JAR de novo e clique em **Ranking** no menu | `Teste Alisson` continua lá, com a mesma pontuação |

## CT-RF21-02: nome inválido é recusado com uma mensagem clara

| Campo | Conteúdo |
|---|---|
| **Requisito** | RF21 · RNF14 (entrada inválida gera erro, não derruba o jogo) |
| **Prioridade** | 🔴 essencial |
| **Onde testar** | Online ou JAR |
| **Pré-condição** | Uma partida terminada, na tela **Obra encerrada** (passos 1 a 4 do caso anterior) |

| # | Faça | Deve acontecer |
|---|---|---|
| 1 | Deixe o nome vazio, ou só com espaços, e clique em **Salvar no ranking** | Aparece **"Digite um nome para salvar."** e o cursor volta para o campo |
| 2 | Tente digitar mais de 20 letras | O campo para de aceitar depois da vigésima |
| 3 | Digite `Ana;Bia` e salve | Aparece **"O nome não pode ter ponto e vírgula nem quebra de linha."** |
| 4 | Corrija para `Ana` e salve | Salva normalmente e abre o ranking |

## CT-RF21-03: o ranking mostra só as dez melhores, uma por pessoa

| Campo | Conteúdo |
|---|---|
| **Requisito** | RF22 |
| **Prioridade** | 🟡 importante |
| **Onde testar** | JAR |
| **Pré-condição** | Jogo aberto no menu |

| # | Faça | Deve acontecer |
|---|---|---|
| 1 | Jogue duas partidas com o **mesmo nome**, fazendo pontuações diferentes (numa delas, feche algumas linhas) | No ranking, o nome aparece **uma vez só**, com a **maior** das duas pontuações |
| 2 | Salve 11 partidas com nomes diferentes (`T1`, `T2`… `T11`) | O ranking mostra **só 10**, da maior pontuação para a menor |

## Observações

- O caso 01 é o mais importante: é o critério de aceitação do RF21 ("as pontuações continuam lá depois de fechar e reabrir o jogo").
- O ranking do JAR fica no arquivo `ranking.csv`, na pasta `.canteiro` dentro da sua pasta de usuário. Para começar um teste do zero, apague esse arquivo com o jogo fechado.
