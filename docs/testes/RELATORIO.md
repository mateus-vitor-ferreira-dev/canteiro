# Relatório de testes do CANTEIRO

| Campo | Conteúdo |
|---|---|
| **Responsável** | Alisson Antônio Oliveira Santos |
| **Período** | 2026-11-22 a 2026-11-24 (rodada final, revisão de acessibilidade e sessões com jogadores) |
| **Última atualização** | 2026-11-24 |

## 1. Resumo

Os sete casos com roteiro pronto e um teste exploratório foram executados no JAR da `develop` (versão 0.3.1), e todos passaram. Nenhum defeito foi encontrado no jogo. O único problema foi no roteiro do colapso (CT-RF17-01), escrito antes do eixo passar a ser o centro da base: seguido à risca, ele não derruba a obra. O roteiro foi corrigido e, com ele, o caso passou. Os testes automáticos passaram todos: 406 no backend e 234 no frontend. A revisão de acessibilidade (#34) achou um problema de texto, a mensagem de erro de conexão com o termo "backend" (#151), já corrigido. Cinco pessoas de fora do grupo jogaram (#30): a maioria entendeu sozinha os comandos e a relação entre peso e estabilidade, mas duas nem sempre souberam por que a obra caiu. Do ponto de vista dos testes, o jogo está pronto para a entrega.

| Casos de teste | Executados | ✅ Passaram | ❌ Falharam | ⚠️ Bloqueados | Bugs abertos | Bugs corrigidos |
|---|---|---|---|---|---|---|
| 8 (7 de roteiro e 1 exploratório) | 8 | 8 (100 %) | 0 | 0 | 0 | 1 (#151, da revisão de acessibilidade) |

## 2. Escopo

**Testado:** RF16 e RF17 (aviso e colapso), RF21 e RF22 (ranking persistente, validação do nome, dez melhores), RF30 e RF31 (pausa automática e reconexão), RNF14 (entrada inválida não derruba o jogo) e, no exploratório, o relatório, as repetições, o modo treino e as configurações. Na revisão de acessibilidade (RNF05, RNF06, [`revisao-acessibilidade.md`](revisao-acessibilidade.md)): os quatro materiais em Protanopia, Deuteranopia, Tritanopia e Achromatopsia, o contraste das cores, o uso só pelo teclado, uma pessoa de primeira viagem e os textos das telas. E as sessões com cinco jogadores de fora do grupo ([`sessoes/`](sessoes/)).

**Fora deste relatório:** as regras conferidas pelos testes automáticos (JUnit e Vitest, que rodam a cada PR no CI); os casos da tabela do [guia](README.md#o-que-precisa-de-roteiro) que ainda não têm roteiro escrito; os roteiros CT-RF07, CT-RF12, CT-RF15, CT-RF19 e CT-RF20, escritos depois desta rodada e ainda não executados; e o duplo clique no JAR em máquina real (#133).

## 3. Ambiente

| Versão | Sistema | Navegador | Rodadas |
|---|---|---|---|
| JAR da `develop` (commit `4552f89`, versão 0.3.1), com pasta de dados vazia | Ubuntu 26.04.1 LTS | Chrome 157 | 1 |

## 4. Resultados por requisito

O estado **mais recente** de cada caso. O histórico completo está em [`execucoes/`](execucoes/).

| Caso | Requisito | O que confere | Resultado | Data | Bug |
|---|---|---|---|---|---|
| CT-RF17-01 | RF16, RF17 | A estrutura desaba com a carga de um lado só | ✅ (roteiro corrigido) | 2026-11-23 | |
| CT-RF21-01 | RF21, RF22 | O ranking continua depois de fechar o jogo | ✅ | 2026-11-23 | |
| CT-RF21-02 | RF21, RNF14 | Nome inválido é recusado com mensagem clara | ✅ | 2026-11-23 | |
| CT-RF21-03 | RF22 | Só as dez melhores, uma por pessoa | ✅ | 2026-11-23 | |
| CT-RF30-01 | RF30 | Pausa ao trocar de aba | ✅ | 2026-11-23 | |
| CT-RF31-01 | RF30, RF31 | Reconecta quando a internet volta | ✅ | 2026-11-23 | |
| CT-RF31-02 | RF31 | Avisa quando a partida se perdeu | ✅ | 2026-11-23 | |
| EXP-01 | RF24, RF25, RF26 | Exploratório: relatório, repetições, desfazer no modo treino e a tela de configurações (sem trocar tecla), sem erro | ✅ | 2026-11-23 | |

## 5. Defeitos encontrados

| Bug | Caso | O que acontecia | Gravidade | Situação |
|---|---|---|---|---|
| [#151](https://github.com/mateus-vitor-ferreira-dev/canteiro/issues/151) | Revisão de acessibilidade | A mensagem de erro de conexão dizia "O backend está rodando?", termo técnico que o jogador não entende | Baixa | Corrigido em 2026-11-24 ("Não foi possível conectar ao jogo. Tente novamente.") |
| — | CT-RF17-01 | Defeito no **roteiro**, não no jogo: ele mandava empilhar tudo numa parede, o que não derruba mais a obra, porque o eixo acompanha a base. O limite do Difícil também estava errado (2,0 em vez de 1,6 coluna) | Baixa | Roteiro corrigido em 2026-11-23, retestado ✅ |

Nenhum defeito de funcionamento no jogo; o único achado foi de texto.

**Gravidade:** *alta* impede de jogar ou perde dados (partida trava, ranking some); *média* atrapalha, mas tem como contornar; *baixa* é visual ou de texto.

## 6. Sessões com jogadores

Cinco pessoas de fora do grupo jogaram em 2026-11-23, três partidas cada, sem explicação da mecânica (#30). As anotações e os questionários estão em [`sessoes/`](sessoes/), uma por participante (P1 a P5). Dois jogam muito, dois às vezes e um nunca; todos já tinham jogado jogos de encaixar blocos, e dois estudam engenharia.

| | Resultado |
|---|---|
| **Participantes** | 5 pessoas de fora do grupo, 15 partidas |
| **Tempo até entender a estabilidade** | 2 perceberam logo no início (P1, P3); 3 depois de alguns minutos (P2, P4, P5) |
| **Colapsos por partida** | Não contados |
| **Nota média por afirmação (1 a 5)** | Entendi sem explicação: 4,2 · Entendi o colapso: 4,0 · Colapsos justos: 4,0 · Painel ajudou: 3,6 · Jogaria de novo: 3,8 |

**O que mais confundiu:** saber **qual peça** derrubou a obra. P2 e P4 deram nota 3 a quase tudo e disseram que algumas quedas "pareceram acontecer do nada" e que "não era evidente qual peça levou à queda". P3 entendeu, mas achou que o peso poderia aparecer de forma mais evidente. O painel de estabilidade teve a nota mais baixa (3,6). Como os colapsos não foram contados, não dá para dizer em que momento da partida isso aconteceu.

**O que pediram:** um tutorial rápido (P2), um aviso quando a obra está perto de cair (P4), mais tipos de peças (P1), fases com dificuldades diferentes (P3) e pontos por obras maiores (P5). O aviso, os níveis e o bônus de obra ousada já existem no jogo; se P4 e P5 não os notaram, eles estão pouco visíveis.

**Proposta de ajuste da dificuldade:** manter os valores atuais de velocidade e limite de desvio. As opiniões se dividiram (dois acharam equilibrado, um fácil, um difícil e um difícil só no começo), sem um lado claro, e quem achou difícil não reclamou da velocidade, e sim de não entender a queda. O problema apontado é de **interface**, não de número: o próximo passo seria destacar a peça que puxou o centro de massa para fora do limite no momento do colapso e deixar o aviso de risco mais visível, e não mexer no limite.

## 7. Conclusão

O jogo está pronto para a entrega: todos os casos executados passaram, e o único defeito, de texto, foi corrigido. As sessões com jogadores confirmam que a mecânica é entendida sem explicação pela maioria e não pedem mudança na dificuldade. Os riscos conhecidos são os que dependem de máquina real (o duplo clique no JAR no Windows e no macOS, e no Ubuntu, onde o atalho do Java do sistema não abre o JAR) e o colapso que nem todo jogador consegue explicar, que pede um destaque melhor da peça que derrubou a obra.
