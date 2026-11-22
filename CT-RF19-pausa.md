# CT-RF19-01 — Pausar e retomar a partida

**Requisitos:** RF19, RNF05  
**Data:** [PREENCHER]  
**Testado por:** Alisson Antônio Oliveira Santos  
**Versão:** [ONLINE / JAR]  
**Resultado:** [✅ / ❌ / ⚠️]

## Objetivo
Verificar se a partida pode ser pausada e retomada corretamente.

## Pré-condição
1. Abrir o jogo.
2. Iniciar uma partida.
3. Confirmar que a partida está em andamento.

## Passos
1. Pressionar `P`.  
   **Resultado esperado:** aparece a tela **Pausado**.

2. Conferir a mensagem exibida.  
   **Resultado esperado:** aparece **“Aperte p para continuar.”**

3. Conferir as opções da tela.  
   **Resultado esperado:** aparecem **Retomar**, **Recomeçar** e **Sair para o menu**.

4. Selecionar **Retomar** ou pressionar `P`.  
   **Resultado esperado:** a partida continua.

5. Pausar novamente e selecionar **Recomeçar**.  
   **Resultado esperado:** o jogo inicia novamente a partida.

6. Pausar novamente e selecionar **Sair para o menu**.  
   **Resultado esperado:** o jogo retorna ao menu.

## Caminho errado
Tentar continuar movimentando normalmente a peça enquanto a tela **Pausado** estiver aberta.  
**Resultado esperado:** a partida permanece pausada.

## Observações
[PREENCHER somente se necessário]
