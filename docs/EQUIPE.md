# Manual da equipe do CANTEIRO

Como uma tarefa sai do board e chega à `develop`. Os detalhes de Git estão no [🌳 Guia de Git do README](../README.md#-guia-de-git-da-equipe); aqui fica o processo.

## 1. Em uma tela

1. Toda tarefa é uma **issue** no [board](https://github.com/users/mateus-vitor-ferreira-dev/projects/5), feita com um dos modelos. O board não puxa a issue sozinho: coloque-a lá e preencha Área, Estimativa e Prioridade.
2. Pegou uma? **Atribua-se** e mova o cartão para **🔨 Em andamento**. No máximo 2 por pessoa.
3. Crie um **ramo** a partir da `develop`: `feature/…`, `fix/…`, `docs/…`.
4. Abra o **PR para a `develop`** com `Closes #número` na descrição e mova o cartão para **👀 Em revisão**.
5. Mudou o que o jogador vê? O Alisson passa o **roteiro manual** (**🧪 Em teste**).
6. **CI verde** → **Squash and merge** → a issue fecha; mova o cartão para **✅ Concluído**.
7. Terminou um requisito? Atualize o status dele na seção [📋 Requisitos](../README.md#-requisitos), no mesmo PR.

Nunca trabalhe direto na `develop` nem na `main`: o GitHub recusa o push.

## 2. Quem responde por cada área

A área é o campo **Área** do board.

| Área | Responsável | O que cobre |
|---|---|---|
| **Back** | Mateus e Marcelo | Mateus: arquitetura, servidor e protocolo, motor, física, estruturas de dados, medições. Marcelo: persistência e as rotas de ranking, configurações, relatório, replay e modo treino |
| **Front** | Mateus | A tela de partida e as telas de apoio |
| **Requisitos** | Mateus | A tabela de requisitos do README e a revisão no fim de cada sprint (#19) |
| **Teste** | Alisson | Roteiros manuais, execução a cada entrega, issues de bug, sessões com jogadores, usabilidade e acessibilidade |
| **Documentação** | Mateus e Alisson | Mateus: este manual e o relatório de plataforma e desvios. Alisson: a apresentação e a revisão dos documentos |

**É de todo mundo:**

- escrever os **testes automatizados** do que programou, no mesmo PR;
- manter o **README** em dia com o que o seu PR mudou, inclusive o status do requisito;
- **revisar o PR** de um colega quando ele pedir uma segunda opinião.

A tabela que vale é a da seção [👥 Equipe](../README.md#-equipe) do README.

## 3. Ritmo

Sprints de **2 semanas**, no campo **Sprint** do board, alinhadas ao [cronograma](../README.md#️-cronograma).

| Sprint | Datas | Foco |
|---|---|---|
| 1 | 07/09 a 20/09 | Planejamento |
| 2 | 21/09 a 04/10 | Base do jogo |
| 3 | 05/10 a 18/10 | Primeira versão jogável |
| 4 | 19/10 a 01/11 | Equilíbrio e colapso |
| 5 | 02/11 a 15/11 | Telas e acabamento |
| 6 | 16/11 a 29/11 | Extras e entrega |

**No começo da sprint:** as issues escolhidas recebem a sprint no campo **Sprint** e vão para **🎯 Sprint**. Só entra issue que passa na [Definition of Ready](#5-definition-of-ready).

**No fim da sprint:** a tabela de requisitos do README é conferida contra os PRs mergeados, e o resumo vai como comentário na #19. O que não terminou passa para a sprint seguinte.

## 4. Fluxo de uma tarefa

| Passo | O que fazer | Comando ou lugar | Cartão |
|---|---|---|---|
| 1 | Escolha uma issue da sua área em **🎯 Sprint** e atribua-se | Board → **Assignees** | 🔨 Em andamento |
| 2 | Atualize a `develop` | `git switch develop && git pull` | |
| 3 | Crie o ramo | `git switch -c feature/nome-curto` | |
| 4 | Trabalhe com commits pequenos | `git status`, `git add`, `git commit -m "tipo(onde): o que fez"` | |
| 5 | Envie o ramo | `git push -u origin feature/nome-curto` | |
| 6 | Abra o PR para a `develop`, preencha o modelo e escreva `Closes #número` | `gh pr create --base develop` ou o botão **Compare & pull request** | 👀 Em revisão |
| 7 | Se mexe no que o jogador vê, avise o Alisson para passar o roteiro | No PR ou no grupo | 🧪 Em teste |
| 8 | CI quebrou? Corrija **no mesmo ramo** e dê `git push`. Não abra outro PR | | |
| 9 | CI verde: **Squash and merge** e **Delete branch** | Botões no fim do PR | ✅ Concluído |
| 10 | Limpe o computador | `git switch develop && git pull && git branch -d feature/nome-curto` | |

Não tem Git no computador? Dá para fazer tudo pelo site: **Add file → Create new file** (ou o lápis ✏️), depois **Commit changes… → Create a new branch for this commit and start a pull request**.

Desde a #88, **o merge não depende de aprovação**: basta o CI verde (backend, frontend e JAR completo). Pedir uma segunda opinião no PR continua possível e é bem-vindo em mudança grande.

## 5. Definition of Ready

Uma issue só entra na sprint se todas as respostas forem **sim**:

- [ ] Foi aberta com um dos modelos (funcionalidade, tarefa técnica ou bug)?
- [ ] Tem **critérios de aceite**, cada um dando para responder sim ou não?
- [ ] Diz o **requisito** que atende (RF, RNF ou RN), quando é funcionalidade?
- [ ] Tem **Área**, **Estimativa** e **Prioridade** preenchidas no board?
- [ ] Tem **responsável**?
- [ ] A estimativa é **P, M ou G**? Se for **GG**, ela é grande demais e precisa ser quebrada antes.

**Estimativa:** P = até meio dia · M = 1 a 2 dias · G = 3 dias ou mais · GG = precisa ser quebrada.

## 6. Definition of Done

A tarefa acabou quando todas as respostas forem **sim**:

- [ ] O PR aponta para a `develop` e tem `Closes #número`?
- [ ] O **CI está verde**: testes do backend e do frontend, `ArquiteturaTest`, Javadoc e o JAR completo?
- [ ] O que é novo tem **teste automatizado** no mesmo PR?
- [ ] O **checklist do modelo de PR** foi conferido (Javadoc, nada de Swing no modelo, `@OpenApi` nas rotas, métodos até 40 linhas e classes até 400)?
- [ ] Se a tarefa muda o que o jogador vê, o **roteiro manual** foi executado?
- [ ] O **status do requisito** foi atualizado na seção 📋 Requisitos do README?
- [ ] O PR foi mergeado com **Squash and merge** e o ramo apagado?

## 7. Como o board funciona

| Coluna | O que significa | Quem move o cartão para ela |
|---|---|---|
| **📥 Backlog** | Issue aberta, ainda sem sprint | Quem abriu a issue, ao colocá-la no board e preencher Área, Estimativa e Prioridade |
| **🎯 Sprint** | Escolhida para a sprint atual, pronta para ser pega | O Mateus, no começo da sprint |
| **🔨 Em andamento** | Alguém está trabalhando nela | Quem pegou a tarefa, ao se atribuir |
| **👀 Em revisão** | O PR está aberto, esperando o CI (e a segunda opinião, se foi pedida) | Quem abriu o PR |
| **🧪 Em teste** | A mudança está sendo conferida com o roteiro manual | Quem abriu o PR, ao avisar o Alisson |
| **✅ Concluído** | PR mergeado e issue fechada | Quem fez o merge. O board não move o cartão sozinho: o `Closes #número` só fecha a issue |
| **Cancelado** | Não vai ser feita, com o motivo num comentário | Quem decidiu, depois de combinar com o grupo |

## 8. Convenções

### Nomes de ramo

`tipo/descricao-curta`, em minúsculas, sem acento e com hífen no lugar do espaço.

| Prefixo | Quando usar | Exemplo |
|---|---|---|
| `feature/` | Funcionalidade nova | `feature/rotacao-com-deslocamento` |
| `fix/` | Correção de bug | `fix/colisao-borda-direita` |
| `test/` | Só testes | `test/centro-de-massa` |
| `docs/` | Documentação, README, Javadoc | `docs/manual-da-equipe` |
| `refactor/` | Reorganizar sem mudar o comportamento | `refactor/extrai-analisador` |
| `chore/` | Configuração, build | `chore/configura-maven` |

### Mensagens de commit e títulos de PR

`tipo(onde): o que foi feito, no presente`, com os mesmos tipos do ramo (`feat`, `fix`, `test`, `docs`, `refactor`, `chore`).

✅ `feat(api): adiciona rota do ranking` · ❌ `mudanças` · ❌ `versão final agora vai`

No Squash and merge, o título do PR vira a mensagem do commit na `develop`, então ele segue o mesmo padrão.

### Títulos de issue

Uma frase curta que diz **o resultado**, sem o prefixo de commit: `Rota do relatório de fim de partida`, `Tempo de ajuste antes de fixar a peça encostada`. O tipo vem do modelo usado, como etiqueta (`tipo:feature`, `tipo:técnica`, `tipo:bug`).

### Como escrever uma issue boa

Siga o modelo: **qual o problema** (sem falar de solução ainda), **o que fazer** (onde a solução mora), **critérios de aceite** (sim ou não) e **requisitos**.

**Exemplo: a [#9](https://github.com/mateus-vitor-ferreira-dev/canteiro/issues/9), "Materiais: hierarquia Material e catálogo em tabela hash".**

> **Qual o problema?** O jogo ainda não sabe o que é um material. Sem isso, nenhuma peça tem peso, e a mecânica central não existe.
>
> **O que fazer:** criar a classe abstrata `Material` em `canteiro.modelo.materiais` [...] e as subclasses `Madeira`, `Alvenaria`, `Concreto` e `Aco`. Criar o catálogo que guarda os materiais num `HashMap` pelo código.
>
> **Critérios de aceite:** as quatro subclasses existem e sobrescrevem só o que muda entre elas · o catálogo devolve o material pelo código em O(1) · a massa de um bloco é a densidade do material (RN07) · testes JUnit para o catálogo e para cada material · `ArquiteturaTest` continua passando.
>
> **Requisitos:** RN03, RN07 · RF04 (parcial).

Por que ela é boa:

- **O problema diz por que importa** (sem material, não existe a mecânica central), não só o que falta.
- **O "o que fazer" diz onde mexer**: o pacote e o nome das classes. Quem pega não precisa perguntar.
- **Cada critério dá para conferir com sim ou não**, e um deles pede os testes.
- **Os requisitos dizem o que ela atende**, e o "parcial" avisa que o RF04 não termina aqui.
- **Cabe numa tarefa só.** Não mistura materiais com peças ou com o tabuleiro, que vieram em issues próprias.
