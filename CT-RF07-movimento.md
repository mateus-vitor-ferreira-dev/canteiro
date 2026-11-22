# CT-RF07-01 — Movimento, rotação e queda da peça

**Requisitos:** RF07, RF08, RF09  
**Data:** [PREENCHER]  
**Testado por:** Alisson Antônio Oliveira Santos  
**Versão:** [ONLINE / JAR]  
**Resultado:** [✅ / ❌ / ⚠️]

## Objetivo
Verificar se a peça pode ser movimentada, girada e derrubada corretamente, inclusive próxima às bordas do tabuleiro.

## Pré-condição
1. Abrir o jogo.
2. Iniciar uma nova partida.
3. Aguardar uma peça aparecer no tabuleiro.

## Passos
1. Pressionar a seta para a esquerda.  
   **Resultado esperado:** a peça se desloca para a esquerda.

2. Pressionar a seta para a direita.  
   **Resultado esperado:** a peça se desloca para a direita.

3. Levar a peça até a borda esquerda e pressionar novamente a seta para a esquerda.  
   **Resultado esperado:** a peça não atravessa a borda do tabuleiro.

4. Levar a peça até a borda direita e pressionar novamente a seta para a direita.  
   **Resultado esperado:** a peça não atravessa a borda do tabuleiro.

5. Pressionar a seta para cima.  
   **Resultado esperado:** a peça gira quando existe espaço válido.

6. Pressionar a seta para baixo.  
   **Resultado esperado:** a peça realiza a queda conforme o comando do jogo.

## Caminho errado
Tentar mover ou girar a peça contra uma parede ou contra blocos já posicionados.  
**Resultado esperado:** o jogo impede uma posição inválida.

## Observações
[PREENCHER somente se necessário]
