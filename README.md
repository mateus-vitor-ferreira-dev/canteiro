<div align="center">

# 🏗️ CANTEIRO

### Jogo de encaixe de blocos em que a pilha tem massa e precisa ficar de pé.

Não basta fechar linha: é preciso decidir **onde colocar carga**. Cada peça vem em madeira, alvenaria, concreto ou aço. O backend recalcula o **centro de massa** da estrutura a cada peça fixada e, se ele se afastar demais do eixo da base, **a obra desaba**.

<p>
  <img src="https://img.shields.io/badge/status-em_desenvolvimento-F59E0B?style=for-the-badge" alt="Em desenvolvimento"/>
  <img src="https://img.shields.io/badge/UFLA-Programação_Aplicada_à_Engenharia-004B87?style=for-the-badge" alt="UFLA"/>
  <a href="docs/ESPECIFICACAO.md"><img src="https://img.shields.io/badge/especificação-v2.1-1F3864?style=for-the-badge" alt="Especificação v2.1"/></a>
</p>

<p>
  <img src="https://img.shields.io/badge/Java-17_LTS-ED8B00?style=flat-square&logo=openjdk&logoColor=white" alt="Java 17"/>
  <img src="https://img.shields.io/badge/Javalin-7-0A0A0A?style=flat-square" alt="Javalin 7"/>
  <img src="https://img.shields.io/badge/Maven-build-C71A36?style=flat-square&logo=apachemaven&logoColor=white" alt="Maven"/>
  <img src="https://img.shields.io/badge/Svelte-5-FF3E00?style=flat-square&logo=svelte&logoColor=white" alt="Svelte 5"/>
  <img src="https://img.shields.io/badge/TypeScript-strict-3178C6?style=flat-square&logo=typescript&logoColor=white" alt="TypeScript"/>
  <img src="https://img.shields.io/badge/Vite-build-646CFF?style=flat-square&logo=vite&logoColor=white" alt="Vite"/>
  <img src="https://img.shields.io/badge/JUnit_5_·_Vitest-testes-25A162?style=flat-square&logo=junit5&logoColor=white" alt="JUnit 5 e Vitest"/>
</p>

<sub>🧱 <strong>7</strong> formas &nbsp;•&nbsp; 🪵 <strong>4</strong> materiais &nbsp;•&nbsp; 📐 <strong>15</strong> regras de negócio &nbsp;•&nbsp; ✅ <strong>31</strong> requisitos funcionais &nbsp;•&nbsp; ⚙️ <strong>16</strong> não funcionais &nbsp;•&nbsp; 🎯 <strong>60</strong> ciclos/s &nbsp;•&nbsp; 📦 <strong>1</strong> JAR</sub>

<br/><br/>

<img src="docs/imagens/tela-principal.png" alt="Esboço da tela principal do CANTEIRO" width="820"/>

<sub>Esboço da tela principal, tirado da especificação. O jogo ainda está sendo construído.</sub>

</div>

---

## 📑 Sumário

- [💡 O que é](#-o-que-é)
- [🎮 Como se joga](#-como-se-joga)
- [✨ Destaques de engenharia](#-destaques-de-engenharia)
- [🧬 Onde entra cada conceito da disciplina](#-onde-entra-cada-conceito-da-disciplina)
- [📋 Requisitos](#-requisitos) ← **o que já está pronto e o que falta**
- [🏛️ Arquitetura](#️-arquitetura)
- [📂 Estrutura de pastas](#-estrutura-de-pastas)
- [🔄 Ciclo de vida da partida](#-ciclo-de-vida-da-partida)
- [🛠️ Stack](#️-stack)
- [🚀 Rodando localmente](#-rodando-localmente)
- [🧪 Testes](#-testes)
- [📏 Convenções de código](#-convenções-de-código)
- [🌳 Guia de Git da equipe](#-guia-de-git-da-equipe) ← **comece por aqui se você nunca usou Git**
- [🗓️ Cronograma](#️-cronograma)
- [👥 Equipe](#-equipe)

> [!NOTE]
> **A fonte da verdade é a [especificação v2.1](docs/ESPECIFICACAO.md).** Ela traz todos os requisitos, os diagramas, o protocolo completo entre backend e frontend e o que mudou em relação à [versão 1.0](docs/CANTEIRO_Documentacao-1.pdf). Este README é o resumo prático.

---

## 💡 O que é

Os jogos de encaixe de blocos são um problema resolvido há quarenta anos. Existem milhares de implementações e todas fazem a mesma coisa: peças caem, você fecha linhas e as linhas somem. Copiar isso não seria um trabalho autoral, e também não teria nada a ver com engenharia.

O **CANTEIRO** mantém a grade discreta do gênero e acrescenta uma restrição tirada da estática: **a pilha tem massa e essa massa precisa ficar equilibrada sobre a base de apoio**. As peças são elementos construtivos. Cada uma é fabricada num material com densidade própria, e uma linha completa é um pavimento concluído. Enquanto você joga, o sistema faz a análise estrutural da obra: soma as massas, calcula o centro de massa horizontal e compara com o eixo da base. A distância entre os dois vira um **índice de estabilidade**, que fica visível o tempo todo.

Quando o desvio ultrapassa o limite do nível, as camadas acima da linha crítica **se desprendem e caem por gravidade**, coluna a coluna, e o jogador perde pontos. Uma torre de aço encostada numa parede cai antes de uma pirâmide de madeira duas vezes maior. **É esse comportamento contraintuitivo que o jogo ensina.**

<div align="center">
<img src="docs/imagens/estabilidade.png" alt="Estrutura equilibrada (desvio 0,00) versus estrutura em risco de colapso (desvio 2,04)" width="720"/>

<sub>A estrutura da direita tem <strong>menos</strong> blocos que a da esquerda, e mesmo assim é ela que cai.</sub>
</div>

**Como ele é construído.** O jogo tem duas partes que rodam **na máquina do próprio jogador**:

- o **backend em Java**, dono de todas as regras: física, colisão, estruturas de dados, pontuação e arquivos;
- o **frontend em Svelte 5 com TypeScript**, que roda no navegador, desenha o que o backend manda e envia as teclas.

Tudo sai num **único JAR**. Dois cliques nele sobem o servidor em `127.0.0.1` e abrem o jogo no navegador. Não precisa de internet, e o jogador só precisa ter o Java instalado.

> [!NOTE]
> O modelo físico é **uma simplificação deliberada**. Não simulamos esforço interno, resistência dos materiais, deformação nem tombamento com rotação: o critério de colapso olha só para o deslocamento horizontal do centro de massa. Isso basta para o jogo funcionar, mas **não é análise estrutural de engenharia**, e a própria tela do jogo vai deixar isso claro.

---

## 🎮 Como se joga

| | Regra |
|---|---|
| 🧱 **Tabuleiro** | 10 colunas × 20 linhas visíveis, mais 2 linhas ocultas no topo, onde as peças nascem |
| 🔷 **Peças** | Sempre 4 blocos, em uma das 7 formas canônicas: `I` `O` `T` `S` `Z` `J` `L` |
| 🎲 **Sorteio** | **Método da sacola**: as 7 formas são embaralhadas e todas saem antes de qualquer uma se repetir |
| 👀 **Fila** | O jogador vê as **3 próximas** peças, com forma e material |
| 📥 **Reserva** | Dá para guardar uma peça e trocar pela reservada, **uma vez por peça gerada** |
| 🧹 **Linhas** | Linha completa é eliminada. Eliminar 2, 3 ou 4 linhas de uma vez rende bônus progressivo |
| ⚖️ **Estabilidade** | Vai de 100 % com desvio nulo a 0 % quando o desvio chega ao limite do nível, variando linearmente |
| 💥 **Colapso** | Desvio acima do limite: os blocos acima da linha crítica caem, a pontuação perde o equivalente a **2 linhas** e o contador de colapsos sobe |
| 📈 **Nível** | Sobe a cada **10 linhas**. A queda acelera e o desvio tolerado diminui |
| 🏁 **Fim** | Quando uma peça nasce já colidindo, ou quando há um colapso com a pilha acima de **18 linhas** |

### Os materiais

Forma e material são **independentes**: qualquer forma pode sair em qualquer material. O material define a densidade, a cor, o bônus por linha e o efeito no momento em que a peça é assentada.

| Material | Peso | Bônus por linha | Efeito ao fixar | Aparece a partir do |
|---|---|---|---|---|
| 🪵 **Madeira** | leve | baixo (10) | nenhum | nível 1 |
| 🧱 **Alvenaria** | médio | médio | a definir | nível 1 |
| 🪨 **Concreto** | pesado | alto | a definir | nível 3 |
| 🔩 **Aço** | o mais pesado | o mais alto (40) | **consolida os blocos logo abaixo**, que passam a resistir ao desprendimento no colapso | nível 5, e fica mais frequente a partir do 9 |

> [!TIP]
> As cores dos materiais também terão **padrão de textura**, para jogadores com daltonismo (RNF06).

### A curva de dificuldade

A dificuldade cresce por dois caminhos ao mesmo tempo, a **velocidade** e o **aperto do limite**. Por isso, quem domina o encaixe continua tendo desafio.

| Nível | Intervalo de queda | Limite de desvio | Materiais |
|---|---|---|---|
| 1 – 2 | 800 ms | 3,0 colunas | madeira, alvenaria |
| 3 – 4 | 650 ms | 2,5 colunas | + concreto |
| 5 – 6 | 500 ms | 2,0 colunas | todos |
| 7 – 8 | 380 ms | 1,7 coluna | todos |
| 9 – 10 | 280 ms | 1,4 coluna | todos, com aço mais frequente |
| 11+ | 200 ms | 1,2 coluna | todos, com aço mais frequente |

<sub>São os valores iniciais. Eles vão ser ajustados nos testes com jogadores a partir da semana 8.</sub>

---

## ✨ Destaques de engenharia

**O centro de massa é recalculado em tempo constante, não varrendo o tabuleiro.** A saída ingênua seria percorrer as 200 posições a cada ciclo e refazer a média ponderada. Funciona, mas desperdiça processamento. O `AnalisadorEstrutural` mantém só dois acumuladores: a **massa total** e a **soma dos momentos** (massa × coluna de cada bloco). Fixar um bloco soma nos dois, remover subtrai, e o centro de massa é `momentoX / massaTotal`. **Custo O(1) por bloco alterado, independente da altura da pilha (RNF02).**

**Eliminar uma linha não mexe no momento dos blocos que descem.** Quando uma linha some, os blocos de cima caem, mas **continuam na mesma coluna**, e o momento horizontal só depende da coluna. Então basta descontar os blocos eliminados.

**Um teste confere a otimização contra a versão ingênua.** O ponto mais provável de falha do projeto é um acumulador de massa dessincronizado. Esse tipo de erro gera colapsos que parecem injustos, e o jogador percebe que o jogo está quebrado sem saber explicar por quê. Para pegar isso, um teste aplica **500 operações aleatórias** e compara o cálculo incremental com a varredura completa, com tolerância de `0,001`.

**Só uma thread mexe no jogo.** As teclas chegam pela thread do WebSocket, mas não tocam no motor. Viram um `Comando` e entram numa `ConcurrentLinkedQueue`. O laço da partida, que roda num `ScheduledExecutorService` a cada ~16 ms, esvazia essa fila no começo de cada ciclo. **Sem trava, sem condição de corrida**, e com o jogo **determinístico**: com a mesma semente e os mesmos comandos, a partida termina com a mesma pontuação. É isso que torna a repetição possível.

**Toda mensagem leva o estado inteiro.** O backend não manda "a peça desceu uma linha", manda o tabuleiro completo, a peça, o placar e a estabilidade, até 60 vezes por segundo. Parece desperdício, mas cabe em menos de 4 KB, e ganha-se muito: **se uma mensagem se perder, a próxima corrige tudo**, e o frontend nunca precisa juntar pedaços nem pode ficar dessincronizado.

**Em máquina lenta, perde-se o desenho, nunca a regra.** O backend simula a 60 ciclos por segundo, sempre. O frontend guarda o último estado recebido e redesenha o Canvas a cada `requestAnimationFrame`. Se o computador não aguentar, o que cai é a taxa de quadros da tela. A partida continua igual, porque um jogo que muda de comportamento conforme o hardware está quebrado.

**O modelo não sabe que existe um servidor.** Os pacotes `modelo`, `fisica`, `estruturas`, `controle`, `persistencia` e `util` **não podem importar Javalin, JSON nem nada gráfico**. Isso é garantido por um teste que reprova o build. É o que permite criar um `MotorJogo`, rodar milhares de jogadas e conferir o resultado num teste JUnit, sem subir servidor nem abrir navegador (RNF08).

**O frontend não tem regra de jogo.** Ele não calcula colisão, pontuação nem estabilidade: recebe tudo pronto e desenha. Isso mantém o conteúdo da disciplina no Java e deixa o Svelte simples para quem está começando. **A pontuação do ranking nunca vem do navegador**: o frontend manda só o id da partida e o nome, e o backend lê a pontuação da própria sessão.

**Duas hierarquias em vez de uma: 11 classes em vez de 28.** Forma e material são dimensões independentes. Juntar as duas numa hierarquia só exigiria uma classe para cada combinação (7 × 4). Separadas e ligadas por composição, ficam 7 subclasses de `Peca` e 4 de `Material`. **Criar um quinto material é escrever uma classe e registrá-la no catálogo**, sem tocar no motor.

**Um JAR, dois cliques.** Na hora de empacotar, o Maven compila o Svelte com um Node próprio, baixado só para isso, e coloca o resultado dentro do JAR. O Javalin serve esses arquivos e a API na mesma porta. **O jogador não precisa de Node, de npm nem de internet** (RNF16).

**Local de verdade.** O servidor escuta **só em `127.0.0.1`**: outro computador na mesma rede não consegue nem ver que ele existe (RNF13). Mensagens malformadas pelo WebSocket viram um evento `ERRO`, e a partida continua (RNF14).

---

## 🧬 Onde entra cada conceito da disciplina

Nenhuma estrutura de dados está aqui para cumprir o enunciado. **Cada uma existe porque o jogo precisa dela.** E todas estão **no Java**.

| Conceito | Onde aparece | Por que esta estrutura, e não outra |
|---|---|---|
| **Herança** | `Peca` → `PecaI`, `PecaO`, `PecaT`, `PecaS`, `PecaZ`, `PecaJ`, `PecaL` · `Material` → `Madeira`, `Alvenaria`, `Concreto`, `Aco` | O que é comum a todas as peças (blocos, material, rotação, giro) e a todos os materiais (nome, densidade, cor) fica fatorado na superclasse abstrata |
| **Polimorfismo** | `formas()`, `aoFixar()`, `bonusLinha()` | O motor pede a forma à peça e o efeito ao material **sem saber qual é qual**. Não há cadeia de `if` nem `switch` por tipo |
| **Interfaces** | `ObservadorPartida`, `Atualizavel` | O motor avisa que o estado mudou sem saber quem está ouvindo. É assim que o modelo fala com o WebSocket sem conhecê-lo |
| **Fila** (`ArrayDeque`) | Próximas peças | Consumo em ordem de chegada, inserção sempre no fim e remoção O(1) |
| **Fila concorrente** (`ConcurrentLinkedQueue`) | Comandos do jogador | É a fronteira entre a thread do WebSocket e a do laço, sem precisar de trava |
| **Pilha** (`Deque`) | Peça reservada · histórico de jogadas | Reserva e desfazer operam sobre o último elemento. A pilha da reserva já permite, no futuro, guardar várias peças |
| **Lista** (`ArrayList`) | Blocos de uma peça · linhas completas · blocos desprendidos | Acesso sequencial na colisão e na serialização |
| **Tabela hash** (`HashMap`) | Catálogo de materiais · ranking · sessões de partida | Material buscado pelo código a cada peça, sessão buscada pelo id que vem na URL do WebSocket, nome do jogador apontando para a melhor pontuação: tudo em O(1) |
| **Matriz** | Grade do tabuleiro | Acesso O(1) por linha e coluna. Checar uma linha completa percorre só 10 posições |
| **Vetor de acumuladores** | Massa por coluna | É o que torna o centro de massa O(1) |
| **Recursividade** | Reacomodação dos blocos no colapso | A queda por gravidade propaga coluna a coluna |
| **Exceções** | Leitura dos arquivos · validação das mensagens | Leitura defensiva com valores padrão, e mensagem inválida que vira erro em vez de derrubar a partida |

---

## 📋 Requisitos

A lista completa, com casos de uso, critérios de aceitação e rastreabilidade, está na [seção 2 da especificação](docs/ESPECIFICACAO.md#2-levantamento-de-requisitos). Aqui fica o resumo e, principalmente, **o andamento de cada requisito**.

| Status | Significado |
|---|---|
| ✅ | Pronto e testado |
| 🟡 | Começado: parte já está na `develop` |
| ⬜ | Ainda não começado |
| 🔁 | Regra de código: vale para todo PR, não tem "pronto" |

**Andamento:** 2 prontos e 4 começados, de 31 funcionais e 16 não funcionais. As regras de negócio entram junto com o modelo, a partir das semanas 3 – 4.

> [!TIP]
> **Quem termina um requisito atualiza o status aqui, no mesmo PR.** Na coluna *Onde está*, cite o número do PR (`#12`). O checklist do PR lembra disso.

### Regras de negócio

<details>
<summary><strong>RN01 – RN15</strong>: as regras do jogo, todas no backend</summary>

| Código | Regra |
|---|---|
| RN01 | O tabuleiro tem 10 colunas e 20 linhas visíveis, mais 2 linhas ocultas de geração no topo |
| RN02 | Todo elemento é composto por exatamente quatro blocos unitários conexos, em uma das sete formas canônicas (I, O, T, S, Z, J, L) |
| RN03 | O material do elemento é sorteado entre os liberados na fase e determina densidade, cor e multiplicador de pontos |
| RN04 | A sequência de elementos é gerada pelo método da sacola: as sete formas são embaralhadas e distribuídas antes que qualquer uma se repita |
| RN05 | O jogador pode reservar um elemento por vez. A troca só pode ser feita uma vez a cada elemento gerado |
| RN06 | Uma linha é eliminada quando suas 10 posições estão ocupadas. Eliminações simultâneas de 2, 3 ou 4 linhas recebem bônus progressivo |
| RN07 | A massa de um bloco é a densidade do seu material vezes o volume unitário. A massa da estrutura é a soma das massas dos blocos |
| RN08 | O centro de massa horizontal é a média das colunas dos blocos, ponderada pela massa de cada um |
| RN09 | O desvio é o módulo da diferença entre o centro de massa e o eixo central da base de apoio, em colunas |
| RN10 | O índice de estabilidade é 100 % com desvio nulo e 0 % quando o desvio atinge o limite do nível, variando linearmente |
| RN11 | Há colapso quando o desvio ultrapassa o limite do nível. Os blocos acima da linha de maior concentração de massa se desprendem e são reacomodados por gravidade, coluna a coluna |
| RN12 | Cada colapso subtrai da pontuação o equivalente a duas linhas eliminadas e incrementa o contador de colapsos |
| RN13 | A partida termina se um elemento recém-gerado colidir imediatamente com a estrutura, ou se houver colapso com a pilha ocupando mais de 18 linhas |
| RN14 | O nível avança a cada 10 linhas eliminadas. A cada nível, o intervalo de queda diminui e o limite de desvio é reduzido |
| RN15 | Só entram no ranking pontuações de partidas concluídas sem uso do desfazer |

</details>

### Requisitos funcionais

**Prior.:** E = essencial, entra na versão mínima; D = desejável, se houver prazo. **Lado:** ☕ backend, 🌐 frontend.

<details open>
<summary><strong>RF01 – RF31</strong></summary>

| Código | Descrição | Prior. | Lado | Status | Onde está |
|---|---|:-:|:-:|:-:|---|
| RF01 | Exibir um menu principal com nova partida, ranking, repetições, configurações e sair | E | 🌐 | ⬜ |  |
| RF02 | Permitir escolher entre três dificuldades, que definem a velocidade inicial de queda e o limite de desvio | E | ☕ 🌐 | 🟡 | `GET /api/dificuldades` pronto (#4); falta a tela e a criação da partida |
| RF03 | Gerar elementos continuamente enquanto a partida estiver em andamento | E | ☕ | ⬜ |  |
| RF04 | Atribuir a cada elemento um material sorteado entre os liberados na fase | E | ☕ | ⬜ |  |
| RF05 | Exibir os três próximos elementos da fila, com forma e material | E | ☕ 🌐 | ⬜ |  |
| RF06 | Descer o elemento em queda uma linha a cada intervalo definido pelo nível | E | ☕ | ⬜ |  |
| RF07 | Mover o elemento em queda para a esquerda e para a direita, respeitando as bordas e os blocos fixados | E | ☕ | ⬜ |  |
| RF08 | Girar o elemento nos dois sentidos, com deslocamento corretivo quando a rotação simples causar sobreposição | E | ☕ | ⬜ |  |
| RF09 | Permitir a queda instantânea do elemento até a primeira posição de apoio | E | ☕ | ⬜ |  |
| RF10 | Permitir reservar o elemento em queda e trocá-lo pelo reservado, uma vez por elemento | E | ☕ | ⬜ |  |
| RF11 | Fixar o elemento quando ele colidir com o fundo ou com um bloco fixado | E | ☕ | ⬜ |  |
| RF12 | Identificar e eliminar as linhas completas após a fixação, descendo as linhas de cima | E | ☕ | ⬜ |  |
| RF13 | Calcular a pontuação considerando linhas simultâneas, material predominante e nível | E | ☕ | ⬜ |  |
| RF14 | Recalcular o centro de massa sempre que a composição do tabuleiro mudar | E | ☕ | ⬜ |  |
| RF15 | Exibir continuamente o índice de estabilidade, o desvio corrente e o limite tolerado | E | ☕ 🌐 | ⬜ |  |
| RF16 | Sinalizar visualmente a aproximação do limite de desvio antes do colapso | E | 🌐 | ⬜ |  |
| RF17 | Executar o colapso quando o desvio ultrapassar o limite, reacomodando os blocos desprendidos | E | ☕ | ⬜ |  |
| RF18 | Avançar de nível a cada dez linhas, ajustando velocidade e limite de desvio | E | ☕ | ⬜ |  |
| RF19 | Permitir pausar e retomar a partida | E | ☕ 🌐 | ⬜ |  |
| RF20 | Encerrar a partida nas condições de fim de jogo e exibir a tela de resultado | E | ☕ 🌐 | ⬜ |  |
| RF21 | Registrar a pontuação no ranking persistente, com o nome informado pelo jogador | E | ☕ 🌐 | ⬜ |  |
| RF22 | Exibir o ranking com as dez melhores pontuações | E | ☕ 🌐 | ⬜ |  |
| RF23 | Registrar em pilha todas as jogadas executadas na partida | E | ☕ | ⬜ |  |
| RF24 | Reproduzir passo a passo uma partida encerrada, a partir do histórico | D | ☕ 🌐 | ⬜ |  |
| RF25 | Exibir, ao fim da partida, um relatório com a evolução do índice de estabilidade e a distribuição de materiais | D | ☕ 🌐 | ⬜ |  |
| RF26 | Oferecer, no modo treino, o desfazer da última jogada | D | ☕ | ⬜ |  |
| RF27 | Permitir configurar as teclas de comando | D | ☕ 🌐 | ⬜ |  |
| RF28 | Tocar efeitos sonoros para fixação, eliminação de linha e colapso | D | 🌐 | ⬜ |  |
| RF29 | Ao abrir o JAR, subir o servidor e abrir o jogo no navegador padrão. Se não for possível abrir o navegador, mostrar o endereço no terminal | E | ☕ | 🟡 | Sobe o servidor e abre o navegador (#4); falta o frontend dentro do JAR |
| RF30 | Pausar a partida automaticamente quando a conexão com o navegador cair ou quando a aba do jogo perder o foco | E | ☕ 🌐 | ⬜ |  |
| RF31 | Reconectar sozinho após uma queda de conexão e retomar a partida do ponto em que parou | D | ☕ 🌐 | ⬜ |  |

</details>

### Requisitos não funcionais

<details open>
<summary><strong>RNF01 – RNF16</strong></summary>

| Código | Categoria | Descrição | Status | Onde está |
|---|---|---|:-:|---|
| RNF01 | Desempenho | O frontend deve desenhar o tabuleiro a 60 quadros por segundo e o backend deve atualizar o estado 60 vezes por segundo, em máquina com processador de dois núcleos e 4 GB de memória | ⬜ |  |
| RNF02 | Desempenho | O recálculo do centro de massa deve ser incremental, em tempo constante por bloco alterado, sem percorrer o tabuleiro a cada quadro | ⬜ |  |
| RNF03 | Desempenho | O tempo entre o pressionamento de uma tecla e a resposta visual não deve passar de 50 ms, contando a ida e a volta pelo WebSocket | ⬜ |  |
| RNF04 | Portabilidade | O jogo deve rodar sem alteração de código em Windows, Linux e macOS, exigindo do jogador apenas Java 17 ou superior e um navegador atual (Chrome, Firefox, Edge ou Safari, nas duas últimas versões) | ⬜ |  |
| RNF05 | Usabilidade | Os comandos devem ser aprendidos sem manual, com legenda visível na própria tela de jogo | ⬜ |  |
| RNF06 | Usabilidade | As cores dos materiais devem ser distinguíveis também por padrão de textura, atendendo jogadores com daltonismo | ⬜ |  |
| RNF07 | Manutenibilidade | Todas as classes e métodos públicos do backend devem ter Javadoc completo, com parâmetros, retorno e exceções. Os tipos exportados do frontend devem ter comentário TSDoc | 🔁 | O build reprova Javadoc faltando (`./mvnw javadoc:javadoc`) |
| RNF08 | Manutenibilidade | As regras do jogo devem ficar inteiramente na camada de modelo do backend, sem dependência de Javalin, de JSON nem de classes gráficas, permitindo testá-las sem servidor e sem navegador | ✅ | `ArquiteturaTest` (#1, ampliado no #4) |
| RNF09 | Manutenibilidade | Nenhum método ou função com mais de 40 linhas úteis. Nenhuma classe Java com mais de 400 linhas. Nenhum componente Svelte com mais de 200 linhas | 🔁 | Conferido na revisão de cada PR |
| RNF10 | Confiabilidade | Colisão, rotação, eliminação de linhas, centro de massa e a serialização do protocolo devem ter testes automatizados | ⬜ |  |
| RNF11 | Confiabilidade | Falha na leitura dos arquivos de ranking ou de configuração não deve impedir o jogo; o sistema recorre a valores padrão | ⬜ |  |
| RNF12 | Segurança | Arquivos de dados devem ser validados na leitura; conteúdo malformado é rejeitado com registro em log, sem interromper a aplicação | ⬜ |  |
| RNF13 | Segurança | O servidor deve escutar apenas em `127.0.0.1`, nunca em todas as interfaces de rede. Em produção, só a própria origem é aceita; a origem do servidor de desenvolvimento do Vite só é liberada em modo de desenvolvimento | ✅ | Escuta só em `127.0.0.1`; `ServidorWebTest` confirma que o IP de rede é recusado (#4) |
| RNF14 | Confiabilidade | Toda mensagem recebida pela API ou pelo WebSocket deve ser validada. Mensagem malformada ou comando desconhecido gera uma resposta de erro, nunca uma exceção que derrube a partida | 🟡 | Rotas REST respondem erro em JSON sem derrubar o servidor (#4); falta o WebSocket |
| RNF15 | Manutenibilidade | O protocolo entre backend e frontend deve estar documentado (seção 3.5) e tipado dos dois lados: `record`s no Java e tipos no TypeScript. O TypeScript roda em modo `strict`, sem `any` | ⬜ |  |
| RNF16 | Portabilidade | O comando de empacotamento deve gerar um único JAR que já contenha o frontend compilado. O jogador não precisa de Node.js | 🟡 | JAR único com as dependências do backend (#4); falta embutir o frontend |

</details>

---

## 🏛️ Arquitetura

```mermaid
flowchart TB
    subgraph NAV["🌐 Navegador · Svelte + TypeScript"]
        TELAS["Telas"] --> ESTADO["estado: partida · teclado"]
        ESTADO --> CANVAS["Tabuleiro (Canvas)"]
    end

    subgraph JVM["☕ Backend · Java 17 · 127.0.0.1:7070"]
        API["<b>canteiro.api</b><br/>rotas REST · WebSocket · DTOs"]
        CTRL["<b>canteiro.controle</b><br/>sessões · fila de comandos · laço 60 Hz"]
        MOD["<b>canteiro.modelo</b> · fisica · estruturas<br/>MotorJogo · Tabuleiro · Peca · Material<br/><i>sem Javalin, sem JSON, sem gráfico</i>"]
        PERS["<b>canteiro.persistencia</b><br/>materiais · ranking · repetições"]
        API --> CTRL --> MOD
        CTRL --> PERS
        MOD -. "observador" .-> CTRL
    end

    ESTADO -- "comandos (WebSocket)" --> API
    API -- "estado + eventos (WebSocket)" --> ESTADO
    TELAS -- "ranking, materiais, repetições (REST)" --> API
    PERS <--> ARQ[("~/.canteiro/")]

    style MOD fill:#1F3864,stroke:#0f1e38,color:#fff
    style NAV fill:#E8F4FB,stroke:#61DAFB,color:#000
```

O **`MotorJogo`** é o coordenador e **não implementa regra nenhuma, só delega**: pergunta ao `Tabuleiro` se há colisão, pede ao `AnalisadorEstrutural` a verificação de equilíbrio e ao `GeradorPecas` o próximo elemento. A **`SessaoPartida`** é dona de um motor, da fila de comandos e do laço; ela se inscreve como observadora do motor e, a cada mudança, publica o estado no WebSocket.

### O protocolo, em resumo

| Canal | Direção | O que passa |
|---|---|---|
| `POST /api/partidas` | front → back | Cria uma partida com a dificuldade escolhida e devolve o `id` |
| `WS /ws/partidas/{id}` | front → back | `{ "tipo": "COMANDO", "comando": "GIRAR_HORARIO" }` |
| `WS /ws/partidas/{id}` | back → front | `ESTADO` (tabuleiro, peça, fila, reserva, placar, estabilidade) e `EVENTO` (linhas eliminadas, colapso, nível, fim) |
| `GET /api/ranking` · `POST /api/ranking` | ambos | Dez melhores; registro pelo id da partida e nome |
| `GET /api/materiais` · `/api/dificuldades` · `/api/configuracoes` · `/api/repeticoes` | ambos | Catálogo, dificuldades, teclas e repetições |

Todas as rotas, os comandos, os eventos e um exemplo completo de mensagem estão na **[seção 3.5 da especificação](docs/ESPECIFICACAO.md#35-protocolo-entre-backend-e-frontend)**.

> [!IMPORTANT]
> **O protocolo existe duas vezes, de propósito:** como `record`s Java em `canteiro.api` e como tipos TypeScript em `frontend/src/api/protocolo.ts`. **Mudou um lado, muda o outro no mesmo PR.**

### Dados persistidos

Não há banco de dados. Tudo fica em arquivos de texto na pasta `~/.canteiro/` do usuário. Os valores padrão vão dentro do JAR e são usados se o arquivo faltar ou vier corrompido.

| Arquivo | Formato | Conteúdo |
|---|---|---|
| `materiais.properties` | chave = valor | Código, nome, densidade, cor e bônus de cada material. Carregado no `HashMap` do catálogo |
| `configuracoes.properties` | chave = valor | Teclas de comando, volume e texturas para daltonismo |
| `ranking.csv` | CSV | Nome, pontuação, nível, linhas, colapsos e data. Só entram partidas **sem uso do desfazer** (RN15) |
| `repeticoes/<data>.txt` | uma jogada por linha | Semente do gerador e, para cada jogada, o ciclo e o comando: o suficiente para reconstruir a partida inteira |

---

## 📂 Estrutura de pastas

```
canteiro/
├── backend/                         O JOGO EM JAVA (projeto Maven)
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/canteiro/
│   │   │   │   ├── app/             main: sobe o servidor e abre o navegador
│   │   │   │   ├── api/             rotas REST, WebSocket e DTOs
│   │   │   │   ├── controle/        sessões, fila de comandos, laço
│   │   │   │   ├── modelo/          motor, tabuleiro, estados da partida
│   │   │   │   │   ├── pecas/       Peca + as 7 formas
│   │   │   │   │   └── materiais/   Material + os 4 materiais
│   │   │   │   ├── estruturas/      sacola, fila, pilha, histórico
│   │   │   │   ├── fisica/          centro de massa, estabilidade, colapso
│   │   │   │   ├── persistencia/    ranking, materiais, repetições
│   │   │   │   └── util/            constantes e auxiliares
│   │   │   └── resources/
│   │   │       └── dados/           valores padrão dos arquivos
│   │   └── test/
│   │       ├── java/canteiro/       testes (espelham os pacotes de main)
│   │       └── resources/arquivos/  arquivos de entrada dos testes
│   ├── .mvn/wrapper/                configuração do Maven Wrapper
│   ├── mvnw · mvnw.cmd              Maven sem precisar instalar
│   └── pom.xml                      a receita do backend
├── frontend/                        A INTERFACE (Svelte + TypeScript)
│   ├── src/
│   │   ├── api/                     cliente REST, WebSocket, tipos do protocolo
│   │   ├── telas/                   menu, partida, ranking, relatório...
│   │   ├── componentes/             Tabuleiro, PainelEstabilidade, FilaProximas...
│   │   ├── estado/                  partida, teclado, navegacao
│   │   ├── estilos/                 cores, fontes, texturas dos materiais
│   │   └── util/                    funções puras
│   ├── public/                      ícone, sons, imagens
│   ├── index.html
│   ├── package.json                 a receita do frontend
│   └── vite.config.ts
├── docs/
│   ├── ESPECIFICACAO.md             especificação v2.1
│   ├── CANTEIRO_Documentacao-1.pdf  especificação v1.0 (histórico)
│   ├── Proposta_Projeto_Canteiro.pdf  proposta do projeto
│   ├── Apresentacao_Canteiro.pdf    slides para a apresentação em vídeo
│   └── imagens/
├── .github/pull_request_template.md
├── .editorconfig · .gitattributes · .gitignore
└── README.md
```

> [!IMPORTANT]
> **A migração para a v2.0 está em andamento.** O `backend/` já roda com Javalin: o pacote `api/` tem o `ServidorWeb` e a primeira rota, `GET /api/dificuldades`. O `frontend/` ainda está vazio; o projeto Svelte entra no próximo PR. A árvore acima é o destino.

### Por que o backend está dividido assim

**A pasta diz de qual camada é o código, e a camada diz o que ele pode usar.** Para decidir onde uma classe nova vai morar, pergunte *"isso é regra do jogo, é coordenação ou é comunicação?"*. Regra vai para `modelo`, `fisica` ou `estruturas`. Coordenação da partida vai para `controle`. Rota, WebSocket e JSON vão para `api`. Arquivo vai para `persistencia`.

| Pasta | O que mora aqui | Por que separado | Javalin / JSON? |
|---|---|---|---|
| `app/` | Só a classe `Aplicacao`, com o `main` | É o único lugar que conhece **todas** as camadas e as liga. Também abre o navegador (RF29) | ✅ |
| `api/` | `ServidorWeb`, rotas REST, `CanalPartida`, DTOs (`record`s) e a conversão entre modelo e DTO | **Tudo o que sabe que existe HTTP fica aqui.** Se amanhã trocássemos o Javalin, só esta pasta mudaria | ✅ |
| `controle/` | `GerenciadorPartidas`, `SessaoPartida`, `Comando` | Coordena uma partida no tempo: recebe comandos, roda o laço, avisa quando o estado muda. Não sabe o que é JSON | ❌ |
| `modelo/` | `MotorJogo`, `Tabuleiro`, `Bloco`, estados da partida | **O coração do jogo.** Tem que dar para rodar uma partida inteira num teste, sem servidor | ❌ |
| `modelo/pecas/` | `Peca` (abstrata) e as 7 formas | Uma hierarquia inteira de herança, junta para ser fácil de achar e comparar | ❌ |
| `modelo/materiais/` | `Material` (abstrata) e os 4 materiais | A segunda hierarquia, independente da primeira. A cor é um número RGB, não `java.awt.Color` | ❌ |
| `estruturas/` | Gerador por sacola, fila de próximas, pilha de reserva, histórico | As estruturas de dados que a disciplina avalia, fáceis de mostrar e testar sozinhas | ❌ |
| `fisica/` | `AnalisadorEstrutural`: acumuladores, centro de massa, índice, colapso | **O diferencial do projeto** e o ponto mais provável de bug. Merece pacote e testes próprios | ❌ |
| `persistencia/` | Catálogo de materiais, configurações, ranking, repetições | Arquivo tem outro tipo de erro (sumiu, veio corrompido). Isolado, o tratamento defensivo fica num lugar só | ❌ |
| `util/` | Constantes (`COLUNAS = 10`, `LINHAS = 20`...) e auxiliares | Os "números mágicos" proibidos têm um lugar para morar | ❌ |

> [!IMPORTANT]
> **O ❌ não é sugestão, é teste.** O `ArquiteturaTest` lê os `import` de cada arquivo desses pacotes e **reprova o build** se encontrar Javalin, Jackson, `java.awt`, `javax.swing` ou uma camada de cima.

### Por que o frontend está dividido assim

**O frontend só desenha e envia teclas.** Se você está escrevendo uma conta de colisão, pontuação ou estabilidade no TypeScript, ela está no lugar errado: é o backend que calcula.

| Pasta | O que mora aqui | Por que separado |
|---|---|---|
| `api/` | `cliente.ts` (REST), `conexao.ts` (WebSocket com reconexão) e `protocolo.ts` (tipos) | **Único lugar que fala com o backend.** As telas não fazem `fetch` direto; chamam funções daqui |
| `telas/` | Um componente por tela: `Menu`, `NovaPartida`, `Partida`, `FimDePartida`, `Ranking`, `Repeticoes`, `Relatorio`, `Configuracoes` | Qual tela aparece é decidido por `estado/navegacao.svelte.ts`, sem biblioteca de rotas. Uma tela junta componentes e estado, e quase não tem lógica própria |
| `componentes/` | `Tabuleiro` (Canvas), `PainelEstabilidade`, `FilaProximas`, `Reserva`, `Placar`, `LegendaTeclas` | Pedaços reaproveitáveis que recebem dados por *props* (`$props()`) e desenham. Fáceis de testar sozinhos |
| `estado/` | Módulos `.svelte.ts` com runas: `partida` (conecta, guarda o último estado, envia comandos), `teclado` (tecla → comando) e `navegacao` (tela atual) | O estado que várias telas usam, fora dos componentes para eles ficarem pequenos |
| `estilos/` | Cores, fontes e as texturas de cada material | Um lugar só para a identidade visual e para o padrão de daltonismo (RNF06) |
| `util/` | Funções puras: converter cor RGB, formatar números e tempo | Sem Svelte, sem rede: as mais fáceis de testar |
| `public/` | Ícone, sons, imagens | Arquivos servidos como estão, sem passar pelo build |

### E as outras pastas

| Pasta ou arquivo | Para que serve |
|---|---|
| `backend/src/main/resources/dados/` | Os valores padrão (`materiais.properties`, `configuracoes.properties`) que vão **dentro do JAR** e entram em ação se o arquivo do usuário faltar ou vier corrompido (RNF11) |
| `backend/src/test/java/` | Os testes, **nos mesmos pacotes do código testado**: o teste de `fisica/AnalisadorEstrutural` fica em `test/.../fisica/AnalisadorEstruturalTest` |
| `backend/src/test/resources/arquivos/` | Arquivos de entrada **feitos para quebrar**: ranking vazio, linha malformada, caractere inválido (RNF12) |
| `backend/.mvn/`, `mvnw`, `mvnw.cmd` | O Maven Wrapper: todos usam **a mesma versão do Maven**, sem instalar nada |
| `docs/` | A especificação v2.1, a v1.0 em PDF, a proposta e os slides da apresentação, em PDF, e as imagens deste README |
| `.github/` | O modelo de PR: todo PR novo já abre com o checklist |
| `.gitignore` | Impede que `target/`, `node_modules/`, `dist/`, `.idea/` e arquivos do sistema entrem no repositório |
| `.gitattributes` | Resolve o problema de fim de linha entre Windows e Linux (`CRLF` × `LF`), que senão faz o Git achar que o arquivo inteiro mudou |
| `.editorconfig` | UTF-8, LF e indentação iguais em qualquer editor |
| `target/`, `node_modules/`, `dist/` | **Não versionadas.** São geradas pelo Maven e pelo npm. Pode apagar quando quiser |

> [!NOTE]
> As pastas vazias têm um arquivo `.gitkeep`. O Git não guarda pasta vazia, e ele só existe para a pasta aparecer no repositório. Quando a pasta ganhar o primeiro arquivo de verdade, o `.gitkeep` pode ser apagado.

---

## 🔄 Ciclo de vida da partida

A partida é uma **máquina de estados finita** no backend. A cada ciclo, o motor olha o estado atual e executa só as transições previstas para ele. O estado vai em toda mensagem, e **o frontend decide qual tela ou camada mostrar a partir dele**. O menu é uma tela do frontend: a partida só passa a existir quando o jogador a cria.

```mermaid
stateDiagram-v2
    [*] --> GERANDO_PECA: partida criada
    GERANDO_PECA --> PECA_CAINDO
    GERANDO_PECA --> FIM_DE_JOGO: peça nasce colidindo
    PECA_CAINDO --> PECA_CAINDO: move · gira · desce
    PECA_CAINDO --> PAUSA: PAUSAR · conexão caiu · aba sem foco
    PAUSA --> PECA_CAINDO: RETOMAR
    PECA_CAINDO --> FIXANDO: colidiu embaixo
    FIXANDO --> GERANDO_PECA: estável, sem linha
    FIXANDO --> ELIMINANDO_LINHAS: linha completa
    FIXANDO --> COLAPSO: desvio > limite
    ELIMINANDO_LINHAS --> GERANDO_PECA
    ELIMINANDO_LINHAS --> COLAPSO: desvio > limite
    COLAPSO --> GERANDO_PECA: reacomodou
    COLAPSO --> FIM_DE_JOGO: pilha > 18 linhas
    FIM_DE_JOGO --> [*]
```

### Do aperto da tecla ao desenho

```
navegador   keydown → teclado.svelte.ts traduz a tecla → envia { COMANDO } pelo WebSocket
backend     CanalPartida valida → coloca na fila de comandos da sessão
backend     próximo ciclo (≤ 16 ms):
              1. consome a fila → move/gira, sempre testando colisão antes
              2. o intervalo de queda passou? desce a peça
              3. colidiu? fixa, empilha a jogada, elimina linhas, soma pontos
              4. atualiza os acumuladores → centro de massa → índice
              5. desvio > limite? colapso e penalidade
              6. algo mudou? a sessão converte o estado em DTO e envia { ESTADO }
navegador   partida.svelte.ts guarda o estado → o Canvas redesenha no próximo quadro (≤ 16 ms)
```

Tudo isso precisa caber em **50 ms** (RNF03). Localmente, sem internet no caminho, o esperado é bem menos.

---

## 🛠️ Stack

<table>
  <tbody>
    <tr>
      <td rowspan="4"><strong>Backend</strong></td>
      <td><img src="https://img.shields.io/badge/Java_17_LTS-ED8B00?style=flat-square&logo=openjdk&logoColor=white"/>: <code>record</code>s, <code>switch</code> como expressão e classes seladas onde fizer sentido</td>
    </tr>
    <tr>
      <td><img src="https://img.shields.io/badge/Javalin_7-0A0A0A?style=flat-square"/>: servidor HTTP e WebSocket, com rotas declaradas num <code>main</code> comum</td>
    </tr>
    <tr>
      <td><img src="https://img.shields.io/badge/Jackson-JSON-2E7D32?style=flat-square"/> <img src="https://img.shields.io/badge/SLF4J-log-555555?style=flat-square"/>: JSON e log no terminal</td>
    </tr>
    <tr>
      <td><img src="https://img.shields.io/badge/Maven_3.9-C71A36?style=flat-square&logo=apachemaven&logoColor=white"/> via <strong>Maven Wrapper</strong> (<code>./mvnw</code>): ninguém precisa instalar o Maven</td>
    </tr>
    <tr>
      <td rowspan="3"><strong>Frontend</strong></td>
      <td><img src="https://img.shields.io/badge/Svelte_5-FF3E00?style=flat-square&logo=svelte&logoColor=white"/> <img src="https://img.shields.io/badge/TypeScript_strict-3178C6?style=flat-square&logo=typescript&logoColor=white"/>: componentes com runas (<code>$state</code>, <code>$props</code>), sem <code>any</code></td>
    </tr>
    <tr>
      <td><img src="https://img.shields.io/badge/Vite-646CFF?style=flat-square&logo=vite&logoColor=white"/>: servidor de desenvolvimento com recarga instantânea e build de produção</td>
    </tr>
    <tr>
      <td><img src="https://img.shields.io/badge/Chart.js-FF6384?style=flat-square&logo=chartdotjs&logoColor=white"/> para o gráfico do relatório e <code>&lt;canvas&gt;</code> 2D para o tabuleiro</td>
    </tr>
    <tr>
      <td><strong>Qualidade</strong></td>
      <td><img src="https://img.shields.io/badge/JUnit_5-25A162?style=flat-square&logo=junit5&logoColor=white"/> <img src="https://img.shields.io/badge/Vitest-6E9F18?style=flat-square&logo=vitest&logoColor=white"/> <img src="https://img.shields.io/badge/Testing_Library-E33332?style=flat-square&logo=testinglibrary&logoColor=white"/> <img src="https://img.shields.io/badge/Javadoc-1F3864?style=flat-square"/> <img src="https://img.shields.io/badge/ESLint-4B32C3?style=flat-square&logo=eslint&logoColor=white"/> <img src="https://img.shields.io/badge/Prettier-F7B93E?style=flat-square&logo=prettier&logoColor=black"/></td>
    </tr>
    <tr>
      <td><strong>Empacotamento</strong></td>
      <td><code>frontend-maven-plugin</code> compila o Svelte durante o <code>./mvnw package</code> e o coloca dentro do JAR, que sai com todas as dependências embutidas</td>
    </tr>
    <tr>
      <td><strong>Versionamento</strong></td>
      <td><img src="https://img.shields.io/badge/Git-F05032?style=flat-square&logo=git&logoColor=white"/> <img src="https://img.shields.io/badge/GitHub-181717?style=flat-square&logo=github&logoColor=white"/>: ramo por funcionalidade e PR para a <code>develop</code></td>
    </tr>
  </tbody>
</table>

### Por que Javalin, e não Spring Boot

O Spring Boot resolve problemas de sistemas grandes: injeção de dependências, acesso a banco, segurança, configuração por ambiente. O CANTEIRO precisa de **meia dúzia de rotas e um WebSocket**. Com o Spring:

- os objetos seriam criados e ligados **pelo framework, por anotações**, justamente a parte que a disciplina quer ver escrita por nós;
- a curva de aprendizado da equipe cresceria sem ganho nenhum para o jogo.

Com o Javalin, o servidor inteiro é algo assim, e **todo objeto é criado com `new`, de forma visível**:

```java
var partidas = new GerenciadorPartidas(catalogo);

Javalin.create(config -> {
    config.staticFiles.add("/publico", Location.CLASSPATH);
    config.routes.get("/api/ranking", ctx -> ctx.json(ranking.dezMelhores()));
    config.routes.post("/api/partidas", ctx -> ctx.json(partidas.criar(ctx.bodyAsClass(NovaPartidaDto.class))));
    config.routes.ws("/ws/partidas/{id}", ws -> new CanalPartida(partidas).registrar(ws));
}).start("127.0.0.1", 7070);
```

---

## 🚀 Rodando localmente

> [!IMPORTANT]
> Enquanto a [migração para a v2.0](#-estrutura-de-pastas) não termina, só o **backend** roda: `./mvnw compile exec:java` sobe o servidor e `http://127.0.0.1:7070/api/dificuldades` já responde, mas ainda não há telas. Os comandos de frontend e o JAR com o jogo completo valem a partir do próximo PR.

### 1. Pré-requisitos

| Requisito | Versão | Quem precisa | Como conferir |
|---|---|---|---|
| **JDK** | 17 ou superior | todos | `java -version` e `javac -version` |
| **Git** | recente | todos | `git --version` |
| **Node.js** | 20 ou superior | só quem mexe no **frontend** | `node -v` |

**Não precisa instalar o Maven.** O `backend/` traz o **Maven Wrapper** (`mvnw`): na primeira execução ele baixa a versão certa sozinho, e todos usam exatamente a mesma.

> [!TIP]
> **JDK 17:** [adoptium.net](https://adoptium.net/temurin/releases/?version=17), pacote **JDK**. No Ubuntu, `sudo apt install openjdk-17-jdk`. No IntelliJ, *File → Project Structure → SDK → Download JDK*.
> **Node.js:** [nodejs.org](https://nodejs.org), versão **LTS**.

### 2. Clonar

```bash
git clone https://github.com/mateus-vitor-ferreira-dev/canteiro.git
cd canteiro
```

Nunca usou Git? Leia antes o [guia de Git da equipe](#-guia-de-git-da-equipe).

### 3. Desenvolvendo: dois terminais

No dia a dia, backend e frontend rodam **separados**, cada um no seu terminal. Assim, mudar uma tela recarrega o navegador na hora, sem reiniciar o Java.

```bash
# Terminal 1 · backend em http://127.0.0.1:7070
cd backend
./mvnw compile exec:java

# Terminal 2 · frontend em http://localhost:5173 (abra este no navegador)
cd frontend
npm install        # só na primeira vez, ou quando o package.json mudar
npm run dev
```

O Vite repassa as chamadas `/api` e `/ws` para o backend, então para o navegador parece um servidor só.

> [!TIP]
> **No IntelliJ:** abra a pasta `canteiro/backend` (*File → Open*); ele reconhece o `pom.xml`. Para subir o backend, abra `Aplicacao.java` e clique no ▶ ao lado do `main`. **No VS Code:** abra a pasta `canteiro` inteira e instale o *Extension Pack for Java*, o *ESLint* e o *Prettier*.

### 4. Gerando o JAR

```bash
cd backend
./mvnw package
java -jar target/canteiro.jar     # ou dois cliques no arquivo
```

O `package` compila o frontend com um Node próprio, baixado dentro do projeto só para isso, e coloca o resultado dentro do JAR. **Quem for só jogar não precisa de Node.**

### 5. Comandos

**Backend**, de dentro de `backend/`. No **CMD ou PowerShell do Windows**, troque `./mvnw` por `mvnw.cmd`.

| Comando | O que faz |
|---|---|
| `./mvnw compile exec:java` | Sobe o backend para desenvolvimento |
| `./mvnw test` | Roda os testes do backend. **Não mexe no frontend**, por isso é rápido |
| `./mvnw verify` | Compila, testa e empacota. **Rode antes de abrir um PR** |
| `./mvnw package` | Gera `target/canteiro.jar`, com o frontend dentro |
| `./mvnw javadoc:javadoc` | Gera a documentação em `target/reports/apidocs/`. **Falha se algo público estiver sem Javadoc** (RNF07) |
| `./mvnw clean` | Apaga a pasta `target/` |

**Frontend**, de dentro de `frontend/`.

| Comando | O que faz |
|---|---|
| `npm install` | Instala as dependências (cria `node_modules/`) |
| `npm run dev` | Sobe o Vite em `http://localhost:5173`, com recarga instantânea |
| `npm test` | Roda os testes com Vitest |
| `npm run lint` | Confere o código com ESLint |
| `npm run build` | Checa os tipos e gera o build de produção em `dist/` |

### 6. Problemas comuns

| Sintoma | Causa | Solução |
|---|---|---|
| `./mvnw: No such file or directory` · `mvnw não é reconhecido` | Você está na raiz do repositório | `cd backend` |
| `./mvnw: Permission denied` | O arquivo perdeu a permissão de execução | `chmod +x mvnw` |
| `'.' não é reconhecido como um comando` (Windows) | `./mvnw` é sintaxe do Linux | No CMD ou PowerShell use `mvnw.cmd` |
| `JAVA_HOME not found` · `JAVA_HOME is not defined correctly` | O wrapper não achou o JDK | Instale o JDK 17 e aponte a variável `JAVA_HOME` para a pasta dele |
| `O CANTEIRO exige Java 17 ou superior` | O Java ativo é antigo | `java -version`. Troque o JDK padrão ou o `JAVA_HOME` |
| `A porta 7070 já está em uso` · `Address already in use` | O backend já está rodando em outro terminal, ou outro programa usa a porta | Feche o outro. Para descobrir quem usa a porta: `ss -ltnp \| grep 7070` (Linux) ou `netstat -ano \| findstr 7070` (Windows) |
| A tela abre, mas fica em "Reconectando..." | O backend não está rodando | Suba o terminal 1 |
| `npm: command not found` | Node.js não instalado | Instale o Node LTS. Só é preciso para mexer no frontend |
| `Cannot find module` depois de um `git pull` | Alguém adicionou uma dependência | `npm install` de novo |
| `ArquiteturaTest` falhou | Alguém importou Javalin, JSON, gráfico ou uma camada de cima dentro do modelo | A mensagem mostra o arquivo e a linha. Mova o código para `api` ou `app` |
| `javadoc:javadoc` falhou com `warning: no comment` | Classe ou método público sem Javadoc | Documente o que a mensagem aponta |

---

## 🧪 Testes

A maior parte do esforço de teste fica nos **testes de unidade do modelo, no backend**, que é onde moram as regras que podem errar sem ninguém perceber. Acima deles, testes da API e do WebSocket com o servidor em memória. No frontend, testes dos componentes que têm lógica de apresentação. **O teste é escrito junto com a regra, não deixado para o fim.**

```bash
cd backend && ./mvnw test      # JUnit 5
cd frontend && npm test        # Vitest
```

**Já existem:**

| Teste | O que garante |
|---|---|
| `ArquiteturaTest` | Separação de camadas: nada de Javalin, JSON ou classe gráfica fora de `app` e `api` (RNF08) |
| `ServidorWebTest` | `GET /api/dificuldades` devolve as três dificuldades em JSON (RF02) · o servidor recusa conexão pelo IP de rede da máquina (RNF13) |
| `DificuldadeTest` | Cada dificuldade cai mais rápido, tolera menos desvio e libera ao menos os materiais da anterior (RF02) |

**Previstos:**

| Alvo | O que vai ser testado | Lado |
|---|---|---|
| **Colisão** | Peça encostada em cada uma das 4 bordas · sobre um bloco fixado · em espaço livre · parcialmente acima do topo | ☕ |
| **Rotação** | As 4 rotações das 7 formas · rotação junto às paredes · peça `I` em espaço mínimo · rotação recusada | ☕ |
| **Gerador** | Em 200 peças, nenhuma forma se repete antes de a sacola esvaziar · material só entre os liberados na fase | ☕ |
| **Linhas** | 1, 2, 3 e 4 linhas simultâneas · linha que não está no topo · nenhuma linha · descida correta | ☕ |
| **Centro de massa** | Estrutura simétrica · simétrica na geometria mas não na massa · coluna única · **incremental × varredura após 500 operações** | ☕ |
| **Colapso** | Desvio logo abaixo do limite · logo acima · pilha na altura crítica · reacomodação correta | ☕ |
| **Pontuação** | Multiplicadores por linhas, material e nível · penalidade de colapso · subida de nível | ☕ |
| **Persistência** | Arquivo ausente · vazio · linha malformada · caractere inválido · nome repetido no ranking | ☕ |
| **Repetição** | Reexecutar uma partida gravada tem que dar a mesma pontuação e o mesmo tabuleiro final | ☕ |
| **API e WebSocket** | Cada rota com entrada válida e inválida · JSON malformado vira `ERRO` · fechar a conexão pausa a partida | ☕ |
| **Protocolo** | O JSON gerado bate com os exemplos fixos · mensagem de estado abaixo de 4 KB | ☕ |
| **Componentes** | Alerta de estabilidade abaixo do limiar · legenda segue as teclas configuradas · tela de reconexão | 🌐 |
| **Teclado** | Tecla configurada gera o comando certo · tecla segurada não duplica comando | 🌐 |

### Metas de desempenho

| Métrica | Meta |
|---|---|
| Quadros por segundo no navegador | ≥ 55 durante 10 minutos seguidos |
| Ciclos por segundo no backend | 60 ± 1 |
| Tempo da lógica por ciclo | ≤ 4 ms |
| Da tecla ao desenho | ≤ 50 ms |
| Mensagem de estado | ≤ 4 KB |
| Do duplo clique ao menu | ≤ 3 s |
| Memória do backend | ≤ 200 MB após 10 minutos |

---

## 📏 Convenções de código

### Vale para os dois lados

| Regra | Detalhe |
|---|---|
| 🇧🇷 **Português** | Classes, funções, variáveis e comentários em português. Só os termos da linguagem e das bibliotecas ficam em inglês (`$state`, `onclick`, `@Override`) |
| 🔢 **Sem números mágicos** | Dimensões, limites e intervalos em constantes nomeadas |
| 🔌 **Protocolo nos dois lados** | Mudou um DTO em `canteiro.api`? Muda o tipo em `frontend/src/api/protocolo.ts` **no mesmo PR** |

### Backend (Java)

| Regra | Detalhe |
|---|---|
| 🔤 **Nomes** | `PascalCase` para classes · `camelCase` para métodos e atributos · `MAIUSCULAS_COM_SUBLINHADO` para constantes |
| 🔒 **Encapsulamento** | Atributos **sempre** `private`. Acesso de fora só por métodos que preservem as invariantes |
| 📐 **Tamanho** | Nenhum método com mais de **40 linhas úteis**, nenhuma classe com mais de **400** (RNF09) |
| 🚫 **Modelo isolado** | Nada de Javalin, Jackson, `java.awt` ou `javax.swing` fora de `api` e `app` |
| 📦 **DTOs** | São `record`s e ficam só em `canteiro.api`. O modelo nunca é transformado em JSON diretamente |
| 📝 **Javadoc** | Toda classe pública: responsabilidade, `@author`, `@version`. Todo método público: `@param`, `@return`, `@throws` |

### Frontend (TypeScript)

| Regra | Detalhe |
|---|---|
| 🔤 **Nomes** | Componentes em `PascalCase`, um por arquivo (`Tabuleiro.svelte`) · módulos de estado terminam em `.svelte.ts` (`partida.svelte.ts`) · funções e variáveis em `camelCase` |
| 🧩 **Componentes** | Sintaxe do Svelte 5, com runas (`$state`, `$derived`, `$props`) e `<script lang="ts">`. No máximo **200 linhas**; passou disso, divida |
| 🛡️ **Tipos** | TypeScript `strict`, **nada de `any`**. Todo dado que vem do backend tem tipo em `protocolo.ts` |
| 🎨 **Sem regra de jogo** | Componente recebe o estado pronto e desenha. Não calcula colisão, pontos nem estabilidade |
| 🌐 **Rede só em `api/`** | Telas e componentes não chamam `fetch` nem abrem WebSocket direto |
| 📝 **TSDoc** | Tipos exportados, funções de `estado/` e *props* dos componentes com comentário `/** ... */` |

```java
/**
 * Recalcula o centro de massa horizontal da estrutura.
 *
 * <p>Cálculo incremental: usa os acumuladores de massa por coluna,
 * atualizados a cada bloco fixado ou removido (RNF02).</p>
 *
 * @return posição horizontal do centro de massa, em colunas
 * @throws IllegalStateException se a estrutura não possuir blocos
 */
public double centroDeMassa() { ... }
```

```svelte
<!-- Barra do índice de estabilidade, com alerta ao se aproximar do limite (RF15, RF16). -->
<script lang="ts">
  import type { EstabilidadeDto } from "../api/protocolo";
  import BarraProgresso from "./BarraProgresso.svelte";
  import { LIMIAR_ALERTA } from "../util/constantes";
  import { formatarColunas } from "../util/formatacao";

  /** Estabilidade da estrutura, como veio na última mensagem do backend. */
  let { estabilidade }: { estabilidade: EstabilidadeDto } = $props();

  const emAlerta = $derived(estabilidade.indice < LIMIAR_ALERTA);
</script>

<section class="painel" class:painel--alerta={emAlerta}>
  <h2>Estabilidade</h2>
  <BarraProgresso valor={estabilidade.indice} />
  <p>Desvio: {formatarColunas(estabilidade.desvio)}</p>
</section>
```

---

## 🌳 Guia de Git da equipe

> **Para quem nunca usou Git direito.** Leia uma vez com calma, do começo ao fim. Depois, no dia a dia, você só vai precisar da [colinha](#-colinha-o-dia-a-dia-em-8-comandos).

### O que é Git, em 1 minuto

O **Git** é um programa que tira **fotos do projeto** ao longo do tempo. Cada foto se chama **commit**. Com isso dá para ver o que mudou, quem mudou e quando, e voltar atrás se algo quebrar.

O **GitHub** é o site onde fica a **cópia oficial** do projeto, que todo mundo compartilha. Cada integrante tem uma **cópia local** no próprio computador, trabalha nela e depois **envia** (`push`) as mudanças para o GitHub.

Um **branch** (ramo) é uma **linha paralela de trabalho**. Você cria um ramo, mexe à vontade, e nada do que você faz ali afeta o trabalho dos outros até ser revisado e juntado (*merge*) ao ramo principal.

### Os ramos deste repositório

```mermaid
gitGraph
    commit id: "início"
    branch develop
    checkout develop
    commit id: "docs"
    branch feature/tabuleiro
    checkout feature/tabuleiro
    commit id: "cria Tabuleiro"
    commit id: "testes de colisão"
    checkout develop
    merge feature/tabuleiro id: "PR #1"
    branch feature/pecas
    checkout feature/pecas
    commit id: "hierarquia Peca"
    checkout develop
    merge feature/pecas id: "PR #2"
    checkout main
    merge develop id: "entrega 1" tag: "v0.1"
```

| Ramo | Para que serve | Quem mexe |
|---|---|---|
| `main` | Versão **estável**, a que é entregue ao professor | **Ninguém mexe direto.** Só recebe a `develop` numa entrega |
| `develop` | Onde o trabalho de todos se junta. É o **ramo padrão** | **Ninguém mexe direto.** Só recebe PRs |
| `feature/...`, `fix/...` etc. | O **seu** trabalho do momento | Você |

> [!IMPORTANT]
> **A `main` e a `develop` são protegidas.** O GitHub **recusa** push direto nelas, force push e exclusão. Todo código entra por **Pull Request (PR)**. Se você tentar dar `git push` na `develop`, vai receber o erro `GH013: Repository rule violations found`, e isso é o sistema funcionando, não um problema. Veja em [problemas comuns](#-problemas-comuns).

### Passo 0: preparar o computador (uma vez só)

**1. Instale o Git**

| Sistema | Como |
|---|---|
| **Windows** | Baixe em [git-scm.com](https://git-scm.com/download/win) e avance com as opções padrão. Depois use o **Git Bash** para os comandos deste guia |
| **Linux** (Ubuntu/Debian) | `sudo apt install git` |
| **macOS** | `xcode-select --install` ou `brew install git` |

**2. Diga ao Git quem você é.** Use **o mesmo e-mail da sua conta do GitHub**. É ele que liga o commit ao seu perfil, e é assim que o professor vê a contribuição de cada um.

```bash
git config --global user.name "Seu Nome Completo"
git config --global user.email "seu-email-do-github@exemplo.com"
git config --global init.defaultBranch main
git config --global pull.rebase false
```

**3. Faça login no GitHub pelo terminal.** O jeito mais fácil é pelo **GitHub CLI** ([cli.github.com](https://cli.github.com)):

```bash
gh auth login
# escolha: GitHub.com → HTTPS → Yes (autenticar o Git) → Login with a web browser
```

> [!WARNING]
> O GitHub **não aceita mais a sua senha** no `git push`. Se o terminal pedir senha, use o `gh auth login` acima ou um *Personal Access Token*. Detalhes em [problemas comuns](#-problemas-comuns).

**4. Aceite o convite.** O dono do repositório te adiciona como colaborador. O convite chega por e-mail e também aparece em [github.com/notifications](https://github.com/notifications). Sem aceitar, você não consegue enviar nada.

**5. Baixe o projeto:**

```bash
git clone https://github.com/mateus-vitor-ferreira-dev/canteiro.git
cd canteiro
git branch          # deve mostrar: * develop
```

### O ciclo de trabalho, passo a passo

Toda tarefa, seja ela grande ou pequena, segue **os mesmos 7 passos**.

#### 1️⃣ Atualize a `develop`

Antes de começar qualquer coisa, pegue o que os outros já juntaram:

```bash
git switch develop
git pull
```

#### 2️⃣ Crie o seu ramo a partir dela

```bash
git switch -c feature/gerador-de-pecas
```

O `-c` quer dizer *criar*. A partir daqui, tudo o que você fizer fica no seu ramo.

**Como dar nome ao ramo:** `tipo/descricao-curta`, com letras minúsculas, sem acento e com hífen no lugar do espaço.

| Prefixo | Quando usar | Exemplo |
|---|---|---|
| `feature/` | Funcionalidade nova | `feature/rotacao-com-deslocamento` |
| `fix/` | Correção de bug | `fix/colisao-borda-direita` |
| `test/` | Só testes | `test/centro-de-massa` |
| `docs/` | Documentação, README, Javadoc | `docs/javadoc-tabuleiro` |
| `refactor/` | Reorganizar código sem mudar o comportamento | `refactor/extrai-analisador` |
| `chore/` | Configuração, build, `.gitignore` | `chore/configura-maven` |

#### 3️⃣ Trabalhe e confira o que mudou

```bash
git status          # quais arquivos mudaram?
git diff            # o que exatamente mudou dentro deles?
```

Use o `git status` **o tempo todo**. Ele é o painel de controle do Git e quase sempre diz qual é o próximo comando.

#### 4️⃣ Faça o commit (tire a foto)

```bash
git add backend/src/main/java/canteiro/modelo/GeradorPecas.java   # escolhe o que vai na foto
git commit -m "feat(modelo): adiciona gerador de peças com método da sacola"
```

O `git add .` adiciona **tudo** o que mudou. É prático, mas **rode o `git status` antes** para não mandar junto algo que não devia.

**Faça commits pequenos e frequentes.** Um commit deve ser uma unidade de mudança que faz sentido sozinha. Vários commits pequenos são melhores que um commit gigante no fim do dia.

**Como escrever a mensagem.** Siga este padrão:

```
tipo(onde): o que foi feito, no presente
```

| Tipo | Uso | Exemplo |
|---|---|---|
| `feat` | Funcionalidade nova | `feat(fisica): calcula centro de massa incremental` |
| `fix` | Correção | `fix(modelo): impede peça I de atravessar a parede ao girar` |
| `test` | Testes | `test(fisica): compara incremental com varredura completa` |
| `docs` | Documentação | `docs(readme): adiciona tabela de controles` |
| `refactor` | Reorganização | `refactor(modelo): extrai verificação de linha completa` |
| `chore` | Configuração | `chore: adiciona plugin do Javadoc ao pom` |

✅ `feat(api): adiciona rota do ranking`
❌ `mudanças` · ❌ `arrumei umas coisas` · ❌ `aaaa` · ❌ `versão final agora vai`

#### 5️⃣ Envie o ramo para o GitHub

Na **primeira** vez que você envia um ramo:

```bash
git push -u origin feature/gerador-de-pecas
```

Nas próximas, basta:

```bash
git push
```

#### 6️⃣ Abra o Pull Request

O PR é o pedido: *"revisem meu ramo e juntem na `develop`"*. Tem dois jeitos de abrir.

**Pelo site:** depois do push, o GitHub mostra um botão amarelo **"Compare & pull request"**. Confira se está **`base: develop` ← `compare: seu-ramo`**. A descrição já vem com um **modelo e um checklist**. Preencha:

- **Título:** igual a uma mensagem de commit, por exemplo `feat(modelo): gerador de peças com método da sacola`
- **Descrição:** o que foi feito, como testar e qual requisito atende (por exemplo `RF03`, `RN04`)

**Pelo terminal:**

```bash
gh pr create --base develop --title "feat(modelo): gerador de peças" --body "Implementa RF03/RN04. Testes em GeradorPecasTest."
```

#### 7️⃣ Revisão, merge e limpeza

1. Um colega lê o código na aba **Files changed** do PR e comenta o que precisar.
2. Pediram ajuste? **Não abra outro PR.** Corrija no mesmo ramo, faça commit e dê `git push`, e o PR se atualiza sozinho.
3. Tudo certo: clique em **Squash and merge**. Todos os seus commits viram um só na `develop`, **com você como autor**.
4. Apague o ramo no botão **Delete branch** e limpe o seu computador:

```bash
git switch develop
git pull
git branch -d feature/gerador-de-pecas
```

**Voltou para o passo 1.** 🔁

### Quando dá conflito

Um conflito acontece quando **você e outra pessoa mudaram a mesma linha** do mesmo arquivo. O Git não sabe qual versão manter e pergunta para você. **Não é erro nem desastre**, é só uma pergunta.

Para trazer as novidades da `develop` para o seu ramo:

```bash
git switch develop
git pull
git switch feature/meu-ramo
git merge develop
```

Se der conflito, o arquivo vai ficar assim:

```java
<<<<<<< HEAD
private static final int COLUNAS = 10;       // ← a sua versão
=======
private static final int COLUNAS_TABULEIRO = 10;   // ← a versão que veio da develop
>>>>>>> develop
```

Para resolver:

1. **Edite o arquivo** e deixe só o código certo. Pode ser uma versão, a outra ou uma mistura das duas.
2. **Apague as três linhas de marcação** (`<<<<<<<`, `=======` e `>>>>>>>`).
3. Salve e finalize:

```bash
git add Tabuleiro.java
git commit -m "merge: integra develop no ramo do gerador"
git push
```

> [!TIP]
> O **IntelliJ** e o **VS Code** têm uma tela própria para conflitos, com os botões *Accept Current*, *Accept Incoming* e *Accept Both*. É bem mais fácil que editar à mão. Se não souber qual versão manter, **pergunte a quem escreveu a outra** antes de apagar o código.

**Para ter menos conflito:** faça ramos curtos, que durem **dias, não semanas**. Traga a `develop` para o seu ramo com frequência. Combine com o grupo quem mexe em qual classe.

### Desfazendo coisas

| Situação | Comando |
|---|---|
| Quero jogar fora o que mudei num arquivo (**ainda sem commit**) | `git restore Arquivo.java` |
| Dei `git add` num arquivo por engano | `git restore --staged Arquivo.java` |
| Errei a mensagem do último commit (**ainda sem push**) | `git commit --amend -m "mensagem certa"` |
| Quero desfazer o último commit mas manter as mudanças (**ainda sem push**) | `git reset --soft HEAD~1` |
| Fiz commit na `develop` sem querer (**ainda sem push**) | `git switch -c feature/nome` e depois `git switch develop` e `git reset --hard origin/develop`. O commit vai para o ramo novo e a `develop` volta a ficar limpa |
| Quero guardar mudanças por um tempo para trocar de ramo | `git stash` para guardar e `git stash pop` para trazer de volta |
| Quero ver o histórico | `git log --oneline --graph` |
| Já dei push de um commit errado | **Não reescreva o histórico.** Corrija com um commit novo ou use `git revert <id-do-commit>` |

> [!CAUTION]
> `git reset --hard` **apaga de vez** as mudanças que ainda não viraram commit. Antes de usar, rode `git status` e tenha certeza.

### 🚫 Nunca faça

- ❌ **Trabalhar direto na `develop` ou na `main`.** Sempre crie um ramo. O GitHub vai recusar o push de qualquer jeito.
- ❌ **`git push --force`** em ramo que outra pessoa também usa.
- ❌ **Commitar arquivos gerados**, como `target/`, `node_modules/`, `dist/`, `*.class`, `.idea/` ou `.vscode/`. O `.gitignore` já barra esses arquivos.
- ❌ **Fazer merge do seu próprio PR sem ninguém ter olhado**, a não ser que o grupo combine o contrário.
- ❌ **Passar dias sem dar push.** Se o computador der problema, o trabalho que não foi para o GitHub se perde.
- ❌ **Apagar o conteúdo de um conflito sem entender o que ele é.**
- ❌ **Usar uma conta do GitHub ou um e-mail que não são seus.** A contribuição fica registrada para outra pessoa.

### 🆘 Problemas comuns

| Mensagem ou sintoma | O que significa | O que fazer |
|---|---|---|
| `GH013: Repository rule violations found` · `Changes must be made through a pull request` | Você tentou dar push direto na `develop` ou na `main` | `git switch -c feature/nome` para levar o trabalho a um ramo novo, depois `git push -u origin feature/nome` e abra um PR |
| `Please tell me who you are` | O Git não sabe o seu nome e e-mail | Faça o [passo 0.2](#passo-0-preparar-o-computador-uma-vez-só) |
| `Support for password authentication was removed` | O GitHub não aceita senha | `gh auth login`, ou gere um token em *Settings → Developer settings → Personal access tokens* e use no lugar da senha |
| `Permission denied` · `403` | Você não é colaborador, ou não aceitou o convite | Aceite o convite em [github.com/notifications](https://github.com/notifications) |
| `Updates were rejected because the remote contains work that you do not have` | Alguém enviou algo para o seu ramo antes de você | `git pull` e depois `git push` |
| `Your local changes would be overwritten` | Você tem mudanças sem commit e tentou trocar de ramo ou dar pull | Faça commit, ou `git stash`, rode o comando e depois `git stash pop` |
| `CONFLICT (content): Merge conflict in ...` | Duas pessoas mudaram a mesma linha | Veja [quando dá conflito](#quando-dá-conflito) |
| `You are in 'detached HEAD' state` | Você entrou num commit antigo em vez de num ramo | `git switch develop` |
| O terminal abriu uma tela estranha (Vim) pedindo mensagem | Você rodou `git commit` sem o `-m` | Digite `:q!` e Enter para sair, e rode de novo com `-m "mensagem"` |
| O commit aparece no GitHub sem a sua foto | O e-mail do commit não bate com o da sua conta | `git config --global user.email` com o e-mail do GitHub |

### 📋 Colinha: o dia a dia em 8 comandos

```bash
git switch develop && git pull           # 1. atualiza a develop
git switch -c feature/minha-tarefa       # 2. cria o seu ramo
git status                               # 3. o que mudou?
git add .                                # 4. separa as mudanças
git commit -m "feat(onde): o que fez"    # 5. tira a foto
git push -u origin feature/minha-tarefa  # 6. envia (depois só "git push")
gh pr create --base develop              # 7. abre o PR (ou pelo site)
git switch develop && git pull           # 8. depois do merge, volta e atualiza
```

### 📖 Glossário

| Termo | Significado |
|---|---|
| **Repositório** | A pasta do projeto, com todo o histórico dele |
| **Commit** | Uma "foto" das mudanças, com autor, data e mensagem |
| **Branch / ramo** | Uma linha paralela de trabalho |
| **`origin`** | O apelido do repositório no GitHub |
| **Push** | Enviar os seus commits para o GitHub |
| **Pull** | Trazer do GitHub os commits dos outros |
| **Clone** | A primeira cópia do repositório para o seu computador |
| **Merge** | Juntar um ramo em outro |
| **Pull Request (PR)** | O pedido de revisão e merge de um ramo, feito no GitHub |
| **Conflito** | Duas mudanças na mesma linha, que precisam de decisão humana |
| **Staging** | A área onde ficam os arquivos que vão entrar no próximo commit (`git add`) |
| **HEAD** | O ponto onde você está agora no histórico |

---

## 🗓️ Cronograma

| Semanas | Etapa | Lado | Entrega |
|---|---|---|---|
| ✅ 1 – 2 | Requisitos, modelagem de classes e arquitetura | — | [Especificação 1.0](docs/CANTEIRO_Documentacao-1.pdf) |
| 🔄 3 | Revisão da arquitetura; Javalin; projeto Svelte; build único; protocolo | ☕ 🌐 | [Especificação 2.1](docs/ESPECIFICACAO.md) e esqueleto ponta a ponta |
| ⬜ 3 – 4 | Modelo: peças, materiais, tabuleiro e colisão, com testes | ☕ | Núcleo testado, sem interface |
| ⬜ 5 – 6 | Motor, rotação, linhas, pontuação; laço e WebSocket; tela de partida mínima | ☕ 🌐 | Jogável no navegador, ainda sem estabilidade |
| ⬜ 7 – 8 | Acumuladores, centro de massa, índice e colapso; painel de estabilidade | ☕ 🌐 | **Mecânica diferencial completa** |
| ⬜ 9 – 10 | Telas completas, animações, texturas, retorno visual, reconexão | 🌐 | Interface integrada |
| ⬜ 11 | Persistência, ranking, repetição, configurações e relatório | ☕ 🌐 | Requisitos desejáveis |
| ⬜ 12 | Testes com jogadores, balanceamento, Javadoc e empacotamento | ☕ 🌐 | **Entrega final** |

### Entregáveis

- 📦 **Código-fonte**: o repositório completo, com `backend/`, `frontend/`, testes e arquivos de dados de exemplo
- ☕ **Executável**: um único JAR com o frontend embutido, que abre com dois cliques
- 📚 **Javadoc**: páginas geradas a partir dos comentários do código do backend
- 📝 **Relatório de plataforma e desvios**: o ambiente usado e o que mudou em relação à especificação, com o porquê. A [seção 0.2 da especificação](docs/ESPECIFICACAO.md#02-tabela-de-desvios) já registra os desvios da v2.0
- 📄 **[Especificação 2.1](docs/ESPECIFICACAO.md)**: requisitos, diagramas, protocolo e estratégias
- 🗂️ **[Proposta do projeto](docs/Proposta_Projeto_Canteiro.pdf)**: resumo, requisitos, cronograma, riscos e divisão de responsabilidades
- 🎬 **[Apresentação](docs/Apresentacao_Canteiro.pdf)**: 15 slides em linguagem simples, para gravar a tela narrando

---

## 👥 Equipe

| Integrante | GitHub | Responsabilidades |
|---|---|---|
| Mateus Vitor Ferreira | [@mateus-vitor-ferreira-dev](https://github.com/mateus-vitor-ferreira-dev) | ☕ Arquitetura, servidor e protocolo · motor do jogo · física e colapso · 🌐 tela de partida · build e integração contínua |
| Marcelo Camillo De Paula Leite | [@WendigoAwake](https://github.com/WendigoAwake) | ☕ Estruturas de dados · persistência · 🌐 telas de apoio (menu, ranking, repetições, relatório, configurações) |
| Wanessa Kylie Silva Medeiros | _a confirmar_ | 📋 Requisitos · 🧪 plano de testes e validação com jogadores · ♿ usabilidade e acessibilidade · 📝 documentação e apresentações |

Os testes automatizados são escritos por quem programa, no mesmo PR; os roteiros manuais, os testes de aceitação e a validação com jogadores ficam com a frente de testes. A divisão completa está na [seção 4.13 da especificação](docs/ESPECIFICACAO.md#413-divisão-de-responsabilidades).

---

<div align="center">
<sub><strong>CANTEIRO</strong> · Programação Aplicada à Engenharia · Universidade Federal de Lavras · 2026</sub>
</div>
