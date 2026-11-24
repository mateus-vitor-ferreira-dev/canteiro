# CT-RF07-01: mover, girar e derrubar a peça, inclusive encostada nas paredes

| Campo | Conteúdo |
|---|---|
| **Requisito** | RF07 (mover respeitando bordas e blocos), RF08 (girar nos dois sentidos, com deslocamento corretivo) e RF09 (queda instantânea até o primeiro apoio) |
| **Prioridade** | 🔴 essencial |
| **Onde testar** | Online ou JAR |
| **Pré-condição** | Partida nova começada, com a primeira peça caindo |

## Passos

| # | Faça | Deve acontecer |
|---|---|---|
| 1 | Aperte **←** uma vez | A peça anda uma coluna para a esquerda |
| 2 | Aperte **→** uma vez | A peça anda uma coluna para a direita |
| 3 | Segure **←** até a peça encostar na parede esquerda e aperte **←** de novo | A peça para na parede e não entra nela |
| 4 | Faça o mesmo com **→** na parede direita | A peça para na parede e não entra nela |
| 5 | Aperte **↑** (ou **X**) | A peça gira no sentido horário |
| 6 | Aperte **Z** | A peça gira no sentido anti-horário |
| 7 | Aperte **↓** algumas vezes | A peça desce uma linha a cada aperto |
| 8 | Aperte **Espaço** | A peça cai de uma vez até onde a peça fantasma mostrava e é fixada na hora |

## Caminhos errados

| # | Faça | Deve acontecer |
|---|---|---|
| 1 | Encoste uma peça comprida (a I, de pé) na parede e aperte **↑** | A peça gira e é empurrada para dentro do tabuleiro; nenhum bloco fica fora da borda |
| 2 | Encoste a peça ao lado de blocos já fixados e aperte a seta na direção deles | A peça não anda e não sobrepõe os blocos |

## Observações

As teclas são as padrão. Se alguém trocou alguma nas configurações, a legenda da partida mostra as atuais. Para achar uma peça I, use a fila das próximas, à direita.
