# CT-RF15-01 — Atualização da estabilidade

**Requisitos:** RF14, RF15  
**Data:** [PREENCHER]  
**Testado por:** Alisson Antônio Oliveira Santos  
**Versão:** [ONLINE / JAR]  
**Resultado:** [✅ / ❌ / ⚠️]

## Objetivo
Verificar se o indicador de ESTABILIDADE é atualizado de acordo com a distribuição das peças na estrutura.

## Pré-condição
1. Abrir o jogo.
2. Iniciar uma nova partida.
3. Localizar o painel **ESTABILIDADE**.

## Passos
1. Observar a porcentagem exibida em **ESTABILIDADE**.  
   **Resultado esperado:** o jogo mostra a estabilidade em porcentagem.

2. Observar os campos **Desvio** e **Limite**.  
   **Resultado esperado:** os dois valores aparecem abaixo da barra de estabilidade.

3. Fixar uma peça no tabuleiro.  
   **Resultado esperado:** a estabilidade é atualizada após a alteração da estrutura.

4. Concentrar peças em apenas um lado da estrutura.  
   **Resultado esperado:** o valor de estabilidade e o campo **Desvio** mudam conforme o desequilíbrio aumenta.

5. Continuar aumentando o desequilíbrio até se aproximar do limite.  
   **Resultado esperado:** o painel continua refletindo a mudança da estrutura e pode ocorrer colapso quando o limite é ultrapassado.

## Caminho errado
Empilhar várias peças apenas de um lado.  
**Resultado esperado:** a estabilidade não permanece igual; o desvio deve refletir o desequilíbrio da estrutura.

## Observações
Exemplo observado na tela:
- Estabilidade: 100 %
- Desvio: 0,0 coluna
- Limite: 2,5 colunas

[PREENCHER outras observações somente se necessário]
