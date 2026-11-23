# Relatório de testes do CANTEIRO

| Campo | Conteúdo |
|---|---|
| **Responsável** | Alisson Antônio Oliveira Santos |
| **Período** | de 2026-11-22 a 2026-11-23 |
| **Última atualização** | 2026-11-23 |

## 1. Resumo

Foram realizados testes manuais de jogabilidade, acessibilidade e validação com jogadores externos. As sessões mostraram boa compreensão geral dos comandos e da relação entre peso e estabilidade. Também foi identificado um ajuste de usabilidade relacionado à clareza da mensagem de erro de conexão. Os números gerais de casos executados, aprovados, falhos e bloqueados ainda precisam ser consolidados a partir das execuções do grupo.

| Casos de teste | Executados | ✅ Passaram | ❌ Falharam | ⚠️ Bloqueados | Bugs abertos | Bugs corrigidos |
|---|---|---|---|---|---|---|
| [n] | [n] | [n] ([%]) | [n] | [n] | [n] | [n] |

## 2. Escopo

**Testado:** mecânica de estabilidade e colapso; movimentação, rotação e queda das peças; pausa e retomada; uso apenas pelo teclado; distinção visual dos quatro materiais em Protanopia, Deuteranopia, Tritanopia e Achromatopsia; clareza dos comandos para jogador de primeira viagem; revisão dos textos das telas principais; sessões de validação com 5 pessoas de fora do grupo.

**Fora deste relatório:** as regras conferidas pelos testes automáticos (JUnit e Vitest, que rodam a cada PR no CI) e os casos manuais que ainda não possuem resultado consolidado nas execuções do grupo.

## 3. Ambiente

| Versão | Sistema | Navegador | Rodadas |
|---|---|---|---|
| Online / JAR do CI | Windows 11 | Google Chrome | [preencher após consolidar as execuções] |

## 4. Resultados por requisito

O estado **mais recente** de cada caso. O histórico completo está em [`execucoes/`](execucoes/).

| Caso | Requisito | O que confere | Resultado | Data | Bug |
|---|---|---|---|---|---|
| CT-RF17-01 | RF16, RF17 | A estrutura desaba com a carga de um lado só | ✅ Passou | 2026-11-22 | — |
| CT-RF21-01 | RF21, RF22 | O ranking continua depois de fechar o jogo | | | |
| CT-RF21-02 | RF21, RNF14 | Nome inválido é recusado com mensagem clara | | | |
| CT-RF21-03 | RF22 | Só as dez melhores, uma por pessoa | | | |
| CT-RF30-01 | RF30 | Pausa ao trocar de aba | | | |
| CT-RF31-01 | RF30, RF31 | Reconecta quando a internet volta | | | |
| CT-RF31-02 | RF31 | Avisa quando a partida se perdeu | | | |

## 5. Defeitos encontrados

| Bug | Caso | O que acontecia | Gravidade | Situação |
|---|---|---|---|---|
| #151 | RNF05 | A mensagem de erro de conexão utiliza o termo técnico “backend”, que pode não ser claro para jogadores sem conhecimento técnico | Baixa | Aberto |

**Gravidade:** *alta* impede de jogar ou perde dados (partida trava, ranking some); *média* atrapalha, mas tem como contornar; *baixa* é visual ou de texto.

## 6. Sessões com jogadores

Foram realizadas 5 sessões com pessoas de fora do grupo. Cada participante jogou 3 partidas, totalizando 15 partidas. De forma geral, os comandos foram bem compreendidos e a maioria percebeu a influência do peso na estabilidade da estrutura.

| | Resultado |
|---|---|
| **Participantes** | 5 pessoas de fora do grupo |
| **Tempo médio até entender a estabilidade** | 2 participantes entenderam logo no início; 3 entenderam depois de alguns minutos |
| **Colapsos por partida (média)** | 2 |
| **Nota média por afirmação (1 a 5)** | Não aplicada; as avaliações foram registradas por respostas abertas |

**O que mais confundiu:** em duas sessões, o motivo de alguns colapsos não ficou totalmente claro de imediato. Apesar disso, a maior parte dos participantes compreendeu a relação entre peso, equilíbrio e estabilidade ao longo das partidas.

**Proposta de ajuste da dificuldade:** as opiniões ficaram distribuídas entre fácil, equilibrado e difícil, sem indicar um padrão forte de dificuldade excessiva ou insuficiente. Por isso, a proposta é manter os valores atuais de velocidade e limite de desvio neste momento.

## 7. Conclusão

Os testes realizados até o momento indicam que as principais mecânicas avaliadas estão funcionando e foram compreendidas pela maioria dos jogadores. Não foi identificada necessidade imediata de alteração dos valores de dificuldade. Permanece como melhoria conhecida a simplificação da mensagem de erro de conexão registrada na issue #151, enquanto os demais resultados do grupo ainda precisam ser consolidados no relatório final.

**Proposta de ajuste da dificuldade:** [se os colapsos pareceram injustos ou fáceis demais, qual valor mudar (limite de desvio, velocidade) e por quê, com base no que foi observado].

## 7. Conclusão

[O jogo está pronto para a entrega? O que ficou de risco conhecido e o que se recomenda corrigir antes. Duas ou três frases.]
