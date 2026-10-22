# Relatório de testes do CANTEIRO

<!--
Este é o documento final dos testes manuais. Atualize no fim de cada rodada: os números saem das
execuções (pasta execucoes/) e os bugs, das issues. Na entrega, ele é exportado para PDF.
Tudo entre colchetes é para trocar; apague estes comentários quando o relatório estiver pronto.
-->

| Campo | Conteúdo |
|---|---|
| **Responsáveis** | Alisson Antônio Oliveira Santos · Wanessa Kylie Silva Medeiros |
| **Período** | [de AAAA-MM-DD a AAAA-MM-DD] |
| **Última atualização** | [AAAA-MM-DD] |

## 1. Resumo

[Três a cinco frases para quem não vai ler o resto: quantos casos foram testados, quantos passaram, quantos bugs foram achados e quantos foram corrigidos, e se o jogo está pronto para a entrega.]

| Casos de teste | Executados | ✅ Passaram | ❌ Falharam | ⚠️ Bloqueados | Bugs abertos | Bugs corrigidos |
|---|---|---|---|---|---|---|
| [n] | [n] | [n] ([%]) | [n] | [n] | [n] | [n] |

## 2. Escopo

**Testado:** [os requisitos cobertos pelos roteiros, por exemplo RF07–RF31 e os não funcionais conferidos à mão].

**Fora deste relatório:** as regras conferidas pelos testes automáticos (JUnit e Vitest, que rodam a cada PR no CI) e [o que não deu para testar, com o motivo].

## 3. Ambiente

| Versão | Sistema | Navegador | Rodadas |
|---|---|---|---|
| [Online / JAR do CI de AAAA-MM-DD] | [Windows 11] | [Chrome 130] | [2] |

## 4. Resultados por requisito

O estado **mais recente** de cada caso. O histórico completo está em [`execucoes/`](execucoes/).

| Caso | Requisito | O que confere | Resultado | Data | Bug |
|---|---|---|---|---|---|
| CT-RF17-01 | RF16, RF17 | A estrutura desaba com a carga de um lado só | | | |
| CT-RF21-01 | RF21, RF22 | O ranking continua depois de fechar o jogo | | | |
| CT-RF21-02 | RF21, RNF14 | Nome inválido é recusado com mensagem clara | | | |
| CT-RF21-03 | RF22 | Só as dez melhores, uma por pessoa | | | |
| CT-RF30-01 | RF30 | Pausa ao trocar de aba | | | |
| CT-RF31-01 | RF30, RF31 | Reconecta quando a internet volta | | | |
| CT-RF31-02 | RF31 | Avisa quando a partida se perdeu | | | |

## 5. Defeitos encontrados

| Bug | Caso | O que acontecia | Gravidade | Situação |
|---|---|---|---|---|
| [#00](https://github.com/mateus-vitor-ferreira-dev/canteiro/issues) | [CT-RFxx-01] | [Uma frase] | [Alta · média · baixa] | [Aberto · corrigido em AAAA-MM-DD, retestado ✅] |

**Gravidade:** *alta* impede de jogar ou perde dados (partida trava, ranking some); *média* atrapalha, mas tem como contornar; *baixa* é visual ou de texto.

## 6. Sessões com jogadores

[Resumo das sessões da pasta [`sessoes/`](sessoes/) (#30).]

| | Resultado |
|---|---|
| **Participantes** | [n pessoas de fora do grupo] |
| **Tempo médio até entender a estabilidade** | [x min] |
| **Colapsos por partida (média)** | [x] |
| **Nota média por afirmação (1 a 5)** | Entendi sem explicação: [x] · Entendi o colapso: [x] · Colapsos justos: [x] · Painel ajudou: [x] · Jogaria de novo: [x] |

**O que mais confundiu:** [os pontos que se repetiram entre os participantes].

**Proposta de ajuste da dificuldade:** [se os colapsos pareceram injustos ou fáceis demais, qual valor mudar (limite de desvio, velocidade) e por quê, com base no que foi observado].

## 7. Conclusão

[O jogo está pronto para a entrega? O que ficou de risco conhecido e o que se recomenda corrigir antes. Duas ou três frases.]
