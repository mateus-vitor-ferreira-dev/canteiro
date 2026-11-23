# Relatório de testes do CANTEIRO

| Campo | Conteúdo |
|---|---|
| **Responsável** | Alisson Antônio Oliveira Santos |
| **Período** | 2026-11-23 (rodada final, antes da apresentação) |
| **Última atualização** | 2026-11-23 |

## 1. Resumo

Os sete casos com roteiro pronto e um teste exploratório foram executados no JAR da `develop` (versão 0.3.1), e todos passaram. Nenhum defeito foi encontrado no jogo. O único problema foi no roteiro do colapso (CT-RF17-01), escrito antes do eixo passar a ser o centro da base: seguido à risca, ele não derruba a obra. O roteiro foi corrigido e, com ele, o caso passou. Os testes automáticos passaram todos: 406 no backend e 234 no frontend. As sessões com jogadores de fora do grupo (#30) não foram feitas. Do ponto de vista dos testes, o jogo está pronto para a entrega.

| Casos de teste | Executados | ✅ Passaram | ❌ Falharam | ⚠️ Bloqueados | Bugs abertos | Bugs corrigidos |
|---|---|---|---|---|---|---|
| 8 (7 de roteiro e 1 exploratório) | 8 | 8 (100 %) | 0 | 0 | 0 | 0 |

## 2. Escopo

**Testado:** RF16 e RF17 (aviso e colapso), RF21 e RF22 (ranking persistente, validação do nome, dez melhores), RF30 e RF31 (pausa automática e reconexão), RNF14 (entrada inválida não derruba o jogo) e, no exploratório, o relatório, as repetições, o modo treino e as configurações.

**Fora deste relatório:** as regras conferidas pelos testes automáticos (JUnit e Vitest, que rodam a cada PR no CI); os casos da tabela do [guia](README.md#o-que-precisa-de-roteiro) que ainda não têm roteiro escrito; o duplo clique no JAR em máquina real (#133); e as sessões com jogadores (#30), que não aconteceram.

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
| — | CT-RF17-01 | Defeito no **roteiro**, não no jogo: ele mandava empilhar tudo numa parede, o que não derruba mais a obra, porque o eixo acompanha a base. O limite do Difícil também estava errado (2,0 em vez de 1,6 coluna) | Baixa | Roteiro corrigido em 2026-11-23, retestado ✅ |

Nenhum defeito no jogo.

**Gravidade:** *alta* impede de jogar ou perde dados (partida trava, ranking some); *média* atrapalha, mas tem como contornar; *baixa* é visual ou de texto.

## 6. Sessões com jogadores

**Não foram feitas.** As sessões com pessoas de fora do grupo (#30) não aconteceram até a entrega, então não há dados de jogadores nem proposta de ajuste da dificuldade tirada deles. Os valores de limite de desvio e velocidade continuam os da seção **A curva de dificuldade** do README.

## 7. Conclusão

O jogo está pronto para a entrega: todos os casos executados passaram e nenhum defeito foi encontrado. Os riscos conhecidos são os que dependem de máquina real (o duplo clique no JAR no Windows e no macOS, e no Ubuntu, onde o atalho do Java do sistema não abre o JAR) e a falta de validação com jogadores de fora do grupo, que deixa o balanceamento da dificuldade sem confirmação.
