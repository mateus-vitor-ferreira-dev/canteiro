# CT-RF17-01: a estrutura desaba quando a carga fica de um lado só

| Campo | Conteúdo |
|---|---|
| **Requisito** | RF16 (avisar antes do limite) e RF17 (desabar quando o desvio passa do limite, reacomodando os blocos) · RN11, RN12 |
| **Prioridade** | 🔴 essencial: é a mecânica que diferencia o jogo |
| **Onde testar** | Online ou JAR |
| **Pré-condição** | Jogo aberto no menu |

## Passos

| # | Faça | Deve acontecer |
|---|---|---|
| 1 | Clique em **Nova partida** e escolha **Difícil** | A partida começa. À esquerda aparece o painel **Estabilidade**, com o índice perto de 100 % |
| 2 | Antes de cada peça cair, aperte **←** várias vezes até ela encostar na parede esquerda, e então **Espaço** para derrubar | A peça cai direto e fica encostada na esquerda. O índice de estabilidade **diminui** a cada peça |
| 3 | Repita o passo 2, sempre do lado esquerdo, e acompanhe o painel | Antes de desabar, o painel fica em alerta e mostra **"Atenção: carga demais à esquerda!"** |
| 4 | Continue até a estrutura desabar. **Conte as peças** desde o começo | A estrutura desaba **em até 12 peças**. Aparece a animação do colapso, de cerca de 1 segundo, com os blocos de cima caindo |
| 5 | Olhe o placar depois do colapso | **Colapsos** passa de 0 para 1, e a **pontuação** diminui (ou continua 0, se ainda não tinha pontos) |
| 6 | Continue jogando normalmente | A partida segue, sem travar, com a estrutura mais baixa |

## Caminhos errados

| # | Faça | Deve acontecer |
|---|---|---|
| 1 | Numa partida nova, derrube as peças **alternando** entre a esquerda e a direita | O índice **não** cai até o alerta: a carga está equilibrada, então não desaba |

## Observações

- Leva uns 2 minutos.
- No **Difícil** o limite de desvio é o menor (2,0 colunas), por isso o colapso vem mais rápido. No Fácil pode precisar de mais peças: é esperado, não é bug.
- O número de peças até o colapso depende das formas e dos materiais sorteados. Se passar de 12, anote quantas foram e quais materiais caíram (dá para ver pela cor e pela textura): pode ser bug ou pode ser o balanceamento, e o Mateus decide.
