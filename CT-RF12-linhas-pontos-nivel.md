# CT-RF12-01 — Linhas, pontuação e mudança de nível

**Requisitos:** RF12, RF13, RF18  
**Data:** [PREENCHER]  
**Testado por:** Alisson Antônio Oliveira Santos  
**Versão:** [ONLINE / JAR]  
**Resultado:** [✅ / ❌ / ⚠️]

## Objetivo
Verificar se completar linhas altera a pontuação, o contador de linhas e o nível.

## Pré-condição
1. Abrir o jogo.
2. Iniciar uma nova partida.
3. Observar os campos **PONTUAÇÃO**, **Nível**, **Linhas** e **Colapsos**.

## Passos
1. Jogar até completar uma linha.  
   **Resultado esperado:** a linha completa é removida.

2. Conferir **PONTUAÇÃO**.  
   **Resultado esperado:** a pontuação aumenta após a conclusão da linha.

3. Conferir **Linhas**.  
   **Resultado esperado:** o contador de linhas é atualizado.

4. Continuar jogando até atingir o critério previsto para mudança de nível.  
   **Resultado esperado:** o campo **Nível** é atualizado conforme as regras do jogo.

5. Conferir a caixa **META DO NÍVEL**.  
   **Resultado esperado:** a meta atual e seu progresso ficam visíveis durante a partida.

## Caminho errado
Deixar uma linha incompleta.  
**Resultado esperado:** ela não é removida nem contabilizada como linha concluída.

## Observações
Na tela observada aparecem:
- PONTUAÇÃO
- Nível
- Linhas
- Colapsos
- META DO NÍVEL

[PREENCHER somente se necessário]
