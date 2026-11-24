# CT-RF15-01: o índice de estabilidade muda no mesmo instante em que a peça é fixada

| Campo | Conteúdo |
|---|---|
| **Requisito** | RF14 (recalcular o centro de massa sempre que o tabuleiro muda) e RF15 (mostrar sempre o índice, o desvio e o limite) · RN09, RN10 |
| **Prioridade** | 🔴 essencial |
| **Onde testar** | Online ou JAR |
| **Pré-condição** | Partida nova, com o tabuleiro vazio |

## Passos

| # | Faça | Deve acontecer |
|---|---|---|
| 1 | Olhe o painel **Estabilidade** antes de jogar | Mostra o índice em porcentagem, o **Desvio** e o **Limite** do nível |
| 2 | Leve a primeira peça para o meio e aperte **Espaço** | No mesmo instante em que a peça é fixada, o índice e o desvio mudam (sem esperar a próxima peça) |
| 3 | Fixe as próximas peças sempre do mesmo lado, sem fechar linha | A cada peça fixada, o desvio sobe e o índice cai |
| 4 | Continue até o índice ficar abaixo de 30 % | O painel muda para o alerta e avisa de que lado está a carga |
| 5 | Fixe uma peça do lado oposto | O desvio diminui e o índice sobe na hora |

## Caminhos errados

| # | Faça | Deve acontecer |
|---|---|---|
| 1 | Mova a peça de um lado para o outro sem fixar | O índice e o desvio do painel não mudam; só a prévia do centro de massa acompanha a peça |

## Observações

O limite muda com a dificuldade e com o nível. O índice é 100 % com desvio zero e 0 % quando o desvio chega ao limite (RN10). O colapso em si é conferido no CT-RF17-01.
