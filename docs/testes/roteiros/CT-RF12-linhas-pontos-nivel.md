# CT-RF12-01: fechar linhas dá pontos, e 10 linhas sobem de nível

| Campo | Conteúdo |
|---|---|
| **Requisito** | RF12 (eliminar as linhas completas, descendo as de cima), RF13 (pontuação por linhas, material e nível) e RF18 (subir de nível a cada 10 linhas) · RN06, RN14 |
| **Prioridade** | 🔴 essencial |
| **Onde testar** | Online ou JAR |
| **Pré-condição** | Partida nova no Fácil, com **Pontuação** 0, **Nível** 1 e **Linhas** 0 no placar |

## Passos

| # | Faça | Deve acontecer |
|---|---|---|
| 1 | Complete uma linha inteira (10 colunas) | A linha some, acende e os blocos de cima descem uma linha |
| 2 | Olhe o placar | **Linhas** passa para 1 e a **Pontuação** sobe |
| 3 | Complete duas linhas com a mesma peça | As duas somem juntas, e os pontos ganhos são mais que o dobro de uma linha sozinha (bônus de linhas simultâneas) |
| 4 | Continue até **Linhas** chegar a 10 | **Nível** passa para 2 e o jogo avisa o nível novo |
| 5 | Olhe a caixa **Meta do nível** | Mostra a meta do nível 2 |

## Caminhos errados

| # | Faça | Deve acontecer |
|---|---|---|
| 1 | Deixe uma linha com um buraco | Ela não some e não conta em **Linhas** |

## Observações

Chegar a 10 linhas leva alguns minutos; no Fácil a queda é mais lenta. A velocidade e o limite de desvio mudam a cada dois níveis (a primeira vez no nível 3, com 20 linhas), como na seção **A curva de dificuldade** do README; isso é conferido pelos testes automáticos. Os valores exatos dos pontos dependem do material e dos bônus (obra ousada, sequência de linhas) e são conferidos pelos testes automáticos; aqui basta ver que a pontuação sobe.
