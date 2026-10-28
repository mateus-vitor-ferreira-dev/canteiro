# Relatório de plataforma e desvios do CANTEIRO

<!--
Entregável da seção 4.11 da especificação. Na entrega, ele é exportado para PDF.
As linhas marcadas com [a preencher] esperam os testes em máquina real (#27 e #40).
-->

| Campo | Conteúdo |
|---|---|
| **Responsável** | Mateus Vitor Ferreira |
| **Revisão** | Alisson Antônio Oliveira Santos |
| **Versão do jogo** | 0.2.1 |
| **Última atualização** | 2026-10-28 |

## 1. Resumo

O CANTEIRO foi planejado como um aplicativo desktop em Swing (especificação 1.0). Ele foi entregue como um jogo no navegador, com as regras num backend em Java e a tela num frontend em Svelte. O jogador continua precisando só do Java 17 e de um navegador, e o jogo continua sendo um JAR que abre com dois cliques.

Durante o desenvolvimento, quatro coisas mudaram em relação à especificação 2.x: o jogo ganhou uma **versão online**, passou a ser **jogável no celular**, ganhou **sete regras novas** de controle, pontuação e física (RN16 a RN22) e teve **pequenos ajustes de código** em relação ao modelo de classes. Cada mudança está abaixo, com a fonte e o porquê.

## 2. Ambiente usado

### 2.1. O que o jogador precisa

| Item | Versão |
|---|---|
| Java | 17 ou superior (JRE basta) |
| Navegador | Chrome, Firefox, Edge ou Safari atuais (RNF04) |
| Sistema | Windows, Linux ou macOS |
| Node.js | **Não precisa.** O frontend já vai compilado dentro do JAR (RNF16) |

Na versão online, o jogador precisa só do navegador.

### 2.2. Com o que o jogo foi feito

| Parte | Tecnologia | Versão |
|---|---|---|
| Backend | Java (Eclipse Temurin) | 17 |
| Servidor HTTP e WebSocket | Javalin | 7.2.3 |
| Documentação das rotas | javalin-openapi e Swagger UI | 7.2.3 |
| JSON | Jackson | 2.22.3 |
| Log | SLF4J (saída simples) | 2.0.18 |
| Testes do backend | JUnit 5 | 5.14.4 |
| Build | Maven, pelo Maven Wrapper | 3.9 ou superior |
| Frontend | Svelte 5 com TypeScript em modo `strict` | Svelte 5.57, TypeScript 6.0 |
| Empacotador do frontend | Vite | 8.3 |
| Gráfico do relatório | Chart.js | 4.5 |
| Testes do frontend | Vitest, Testing Library e jsdom | Vitest 5.0 |
| Node.js no build do JAR | baixado pelo `frontend-maven-plugin` | 24.21 (LTS) |
| Node.js para desenvolver o frontend | instalado por quem programa | 20.19 ou 22.12 em diante |
| Versão online | Docker, imagem `eclipse-temurin:17-jre`, no Render (plano grátis) | — |

As versões saem do `backend/pom.xml`, do `frontend/package.json` e do `Dockerfile`.

### 2.3. Onde o jogo foi testado

| Onde | O que foi conferido | Fonte |
|---|---|---|
| CI no Linux (Ubuntu), com Java 17 | Testes do backend, `ArquiteturaTest`, Javadoc sem aviso; lint, tipos, testes e build do frontend; o JAR completo | `.github/workflows/ci.yml`, em todo PR |
| CI no Linux, no Windows e no macOS, com Java 17 | O JAR abre, mostra a página, responde `GET /api/dificuldades` e `POST /api/partidas` em até 3 s | #63 |
| Contêiner `eclipse-temurin:17-jre`, sem Node | O JAR roda sem Node.js e, sem tela gráfica, mostra o endereço no terminal (RF29) | #63 |
| Linux, Java 21, Intel Core Ultra 9 285K, 30 GB | Dez minutos de jogo automático: 59,7 a 60,1 ciclos por segundo, ciclo médio de 0,03 ms e memória de até 72 MB (RNF01) | #83 |
| JAR, jogando de verdade | Da tecla ao estado novo: mediana de 13 ms e pior caso de 32 ms (RNF03); 60 quadros por segundo no navegador | #25 |
| Chromium (Playwright) em 375, 768, 1024 e 1440 px | Nenhuma rolagem horizontal; botões de toque de pelo menos 44 px; os quatro materiais distinguíveis em simulação de daltonismo e escala de cinza (RNF06) | #60 |
| Chromium (Playwright) emulando um Android de 360 × 760 | A partida cabe na tela sem rolar; gestos e botões funcionam | #105 |
| Versão online ([canteiro.digitalcodexa.com](https://canteiro.digitalcodexa.com)) | O mesmo JAR, publicado a cada merge na `develop` | #87 |
| Máquinas reais, com dois cliques no JAR | [a preencher: sistema, versão do Java e navegador de cada máquina] | #40 |
| Máquina de referência do RNF01 (dois núcleos e 4 GB) | [a preencher: ciclos por segundo, tempo do ciclo e memória] | #27 |
| Roteiros manuais | [a preencher: navegadores e versões usados na rodada de testes] | #27, `docs/testes/` |

## 3. Desvios em relação à especificação 1.0

A versão 1.0 previa um aplicativo desktop em Swing. A versão 2.0 dividiu o jogo em backend e frontend, antes de existir código. A tabela é a do Apêndice A.2 da documentação do projeto (especificação 2.2), que não fica no repositório.

| Item | Planejado (1.0) | Entregue | Por quê |
|---|---|---|---|
| Interface | Swing, dentro do JDK | Svelte 5 com TypeScript, no navegador | Animações, texturas e gráficos ficam bem mais simples com HTML, CSS e Canvas. E o modelo não tem como chamar a tela, porque ela está em outro processo e outra linguagem |
| Laço de atualização | `javax.swing.Timer` de ~16 ms | `ScheduledExecutorService` de ~16 ms no backend; `requestAnimationFrame` no frontend | O laço das regras deixa de depender da interface. Em máquina lenta cai a taxa de quadros, não o ritmo do jogo |
| Comunicação do modelo com a tela | Observadores no mesmo processo | Observadores no backend, que mandam o estado por WebSocket | A tela agora está em outro processo |
| Dependências | Só a biblioteca padrão do Java | Javalin, Jackson e SLF4J no backend; Svelte e Chart.js no frontend | O JDK não tem servidor WebSocket. O Javalin foi escolhido em vez do Spring Boot por ser pequeno e por deixar todo objeto ser criado com `new`, à vista |
| Pacote `canteiro.visao` | Telas em Swing | Removido. No lugar, `canteiro.api` (rotas e WebSocket) e a pasta `frontend/` | A tela passou para o frontend |
| Execução | Dois cliques no JAR abrem a janela | Dois cliques no JAR sobem o servidor em `127.0.0.1` e abrem o navegador | Mantém o entregável "executável único" |
| Plataforma | Java 17 | Java 17 e um navegador atual. Node.js só para quem desenvolve o frontend | O jogador continua precisando só do Java |
| Testes | JUnit 5 | JUnit 5 no backend; Vitest e Testing Library no frontend | Cada lado é testado com a ferramenta da sua linguagem |
| Requisitos | RF01–RF28, RNF01–RNF12 | RF01–RF31, RNF01–RNF16 | Novos requisitos de navegador, conexão, protocolo e segurança local |

**O que não mudou:** as regras de negócio RN01 a RN15, as hierarquias de peças e materiais, as estruturas de dados, o cálculo incremental do centro de massa, a regra de colapso, a máquina de estados, os arquivos persistidos e a curva de dificuldade. Herança, polimorfismo e estruturas de dados continuam todos no Java.

## 4. Desvios durante o desenvolvimento

### 4.1. Svelte no lugar de React

| | |
|---|---|
| **Planejado** | Frontend em React com TypeScript (especificação 2.0) |
| **Entregue** | Svelte 5 com TypeScript (especificação 2.1) |
| **Por quê** | O frontend é fino: recebe o estado, desenha num Canvas e troca de tela. No Svelte, um estado reativo é só `$state`, sem `useEffect` nem lista de dependências, e o pacote compilado é menor, o que ajuda no JAR único (RNF16) e em máquina fraca (RNF01) |
| **Quando** | Antes de existir código no frontend, então não houve retrabalho (#3) |

### 4.2. Versão online

| | |
|---|---|
| **Planejado** | Jogo só local. A premissa 2.9 diz que o servidor nunca fica exposto na rede, e a seção 4.15 diz que publicá-lo exigiria uma revisão de segurança própria |
| **Entregue** | Além do JAR, o mesmo jogo roda num contêiner Docker, publicado no Render a cada merge na `develop` (#87) |
| **Por quê** | Para quem avalia e para os jogadores das sessões de teste, abrir um link é bem mais fácil que instalar o Java e baixar o JAR |
| **O que mudou no código** | Nada nas regras. O comportamento muda por variáveis de ambiente: `CANTEIRO_HOST=0.0.0.0` (só dentro do contêiner), `CANTEIRO_ABRIR_NAVEGADOR=false`, `CANTEIRO_LIMITE_PARTIDAS=10` e `CANTEIRO_PASTA=/app/dados`. Sem nenhuma delas, vale o jogo local, só em `127.0.0.1` |
| **Requisito afetado** | RNF13. Ele continua valendo para o JAR, e o desvio está registrado no README |
| **Cuidados tomados** | Limite de 10 partidas ao mesmo tempo, porque cada uma tem um laço numa thread própria; partida sem conexão por 10 minutos é descartada (RF31); o contêiner roda com um usuário sem privilégios; o ranking lê a pontuação da própria partida no servidor, nunca do navegador (#92) |
| **Limites que continuam** | Não há autenticação. No plano grátis, o serviço desliga depois de 15 minutos sem acesso, as partidas abertas se perdem e o ranking online recomeça do zero a cada reinício. O ranking que vale é o do JAR |

### 4.3. Jogável no celular

| | |
|---|---|
| **Planejado** | Premissa 2.9: área mínima de 1024 × 768 e entrada da partida só pelo teclado |
| **Entregue** | Layout de uma a três colunas a partir de 375 px, com botões de toque (#60); no celular, gestos no tabuleiro, botões em duas linhas, uma versão compacta do placar e vibração (#105) |
| **Por quê** | No celular, a partida era quase injogável: os botões ficavam empilhados numa coluna só, e placar, estabilidade e botões passavam da altura da tela, então o jogador rolava a página e perdia o tabuleiro de vista (#95) |
| **Requisito afetado** | RNF05 (comandos aprendidos sem manual): na pausa, quem joga pelo toque lê como jogar com os gestos. No computador, nada mudou: teclado, legenda e telas são os mesmos |

### 4.4. Regras novas: RN16 a RN22

A especificação tem 15 regras de negócio. Sete entraram na reta final, todas no backend, com testes, e registradas no README.

| Regra | O que faz | Por quê | Fonte |
|---|---|---|---|
| RN16 | A peça que a gravidade encosta no apoio espera 500 ms antes de fixar, e mover ou girar recomeça a espera, até 15 vezes | Sem a espera, a peça fixava no mesmo ciclo em que encostava, e não dava para deslizá-la nem girá-la no último instante. Nos níveis rápidos, de 200 ms por linha, a sensação era de injustiça. O limite de 15 recomeços impede segurar a peça para sempre | #101 |
| RN17 | Fechar linha com o índice de estabilidade abaixo de 25 % vale 50 % a mais (obra ousada) | O equilíbrio só castigava. Agora arriscar também rende pontos | #103 |
| RN18 | Cada peça seguida que fecha linha soma 10 % à próxima eliminação, até 100 % | Também recompensa o risco: manter a sequência exige fechar linha com toda peça | #103 |
| RN19 | Cada nível tem uma meta curta, sorteada pela semente da partida; cumprir soma 500 × nível | Dá um objetivo além de sobreviver, sem quebrar a repetição da partida, porque o sorteio usa a mesma semente | #104 |
| RN20 | Balanço: a parte da peça sem nada embaixo se solta e cai se a peça for mais densa que o material em que se apoia | Jogando, o CANTEIRO não se diferenciava de um encaixe de blocos comum: a única física era o centro de massa da pilha inteira, e uma peça pesada apoiada pela ponta num bloco leve ficava parada no ar. Agora o material de baixo importa | #117 |
| RN21 | Esmagamento: cada material aguenta um peso máximo em cima, na mesma coluna; perto do limite o bloco racha, e passando ele vira um buraco | Obriga a construir como numa obra, pesado embaixo e leve em cima. O bloco esmagado deixa um buraco em vez de sumir com a coluna descendo, porque descer limitava a altura da pilha e deixava o jogo mais fácil | #118 |
| RN22 | O colapso confere também a parte de cima da pilha, sobre a faixa em que ela encosta na linha crítica; e cada dificuldade multiplica o limite de desvio e a resistência dos materiais | As linhas quase cheias puxavam o centro de massa para o meio, e o colapso quase nunca acontecia, nem no difícil | #119 |

Junto com elas veio a **prévia do centro de massa** (#107): o jogador vê, antes de soltar a peça, onde o centro de massa vai parar e se a jogada derruba a obra. Desde a RN20, a prévia roda a jogada inteira numa cópia da grade, e por isso também marca os blocos que se soltariam ou seriam esmagados. Ela só é recalculada quando o pouso da peça muda; o centro de massa da partida continua incremental (RNF02).

### 4.5. Ajustes de código em relação ao modelo de classes

| Planejado | Feito | Por quê | Fonte |
|---|---|---|---|
| `Comando` no pacote `controle` | `Comando` em `canteiro.modelo` | O motor recebe comandos, e o modelo não pode importar o controle (RNF08) | #50 |
| `Material.aoFixar(Tabuleiro, Peca)` | `Material.aoFixar(Tabuleiro, List<Celula>)` | A peça não sabe onde está no tabuleiro. O aço precisa das células em que ela assentou | #48 |
| A pontuação, o nível e o colapso dentro do `MotorJogo` | Saíram para `Assentamento` | O `MotorJogo` tinha passado do limite de 400 linhas (RNF09); hoje tem 389 | #101 |

## 5. O que ainda não foi feito

São os requisitos desejáveis cujo backend ainda está em andamento. As telas já existem.

| Requisito | Falta | Issue |
|---|---|---|
| RF24 (replay) | A gravação e as rotas das repetições | #35 |
| RF25 (relatório de fim de partida) | A rota `GET /api/partidas/{id}/relatorio` | #70 |
| RF26 (modo treino com desfazer) | O motor desfazer a jogada. Até lá, o backend recusa a partida de treino com um aviso | #37 |
| RF27 (configurar as teclas) | Guardar no servidor. Hoje as teclas ficam no navegador | #71 |
| RNF01 e RNF04 | A medição na máquina de referência e o teste com dois cliques em máquinas reais | #27, #40 |

[a preencher na entrega: atualizar esta tabela com o que ficou de fora e o porquê]
