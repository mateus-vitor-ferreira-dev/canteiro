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
| 2 | Faça a base: derrube uma peça encostada na parede **esquerda** e a seguinte encostada na parede **direita** (**←** ou **→** várias vezes, e **Espaço**) | A linha tracejada (o eixo da base) fica perto do meio do tabuleiro |
| 3 | A partir daí, derrube as peças **mais pesadas** (concreto e aço, as cinzas) **só de um lado**, e as leves (madeira) do outro. Antes de soltar cada peça, olhe o painel | Antes de a obra cair, o painel avisa: a prévia mostra **"Esta jogada derruba a obra!"** e o percentual para onde o índice vai; com a carga já pendendo, aparece **"Atenção: carga demais à esquerda!"** (ou à direita) |
| 4 | Solte uma peça com o aviso **"Esta jogada derruba a obra!"** na tela | A obra desaba: aparece a animação do colapso, de cerca de 1 segundo, com os blocos de cima caindo |
| 5 | Olhe o placar depois do colapso | **Colapsos** passa de 0 para 1, e a **pontuação** diminui (ou continua 0, se ainda não tinha pontos) |
| 6 | Continue jogando normalmente | A partida segue, sem travar, com a estrutura mais baixa |

## Caminhos errados

| # | Faça | Deve acontecer |
|---|---|---|
| 1 | Numa partida nova, derrube **todas** as peças encostadas na parede esquerda, uma em cima da outra | A obra **não** desaba e **Colapsos** fica em 0: a base também está na parede, então o centro de massa fica em cima dela e o índice fica perto de 100 %. A partida acaba por altura. Pode aparecer **"Quebrou!"** quando um bloco em balanço cai ou o aço esmaga o de baixo: são outras regras (RN20 e RN21), não o colapso |

## Observações

- Leva uns 2 minutos.
- O desvio é medido em relação ao **eixo da base** (RN09), a linha tracejada, e não ao meio do tabuleiro. Por isso empilhar tudo numa parede não derruba: a base vai junto. O que derruba é **peso de um lado da base**.
- No **Difícil** o limite de desvio é o menor (1,6 coluna) e o aço já aparece, por isso o colapso vem mais rápido. No Fácil pode precisar de mais peças: é esperado, não é bug.
- O número de peças até o colapso depende das formas e dos materiais sorteados. Com concreto ou aço de um lado da base, costuma vir em poucas peças. Se passar de 12 sem o aviso aparecer, anote quantas foram e quais materiais caíram: pode ser bug ou pode ser o balanceamento, e o Mateus decide.
