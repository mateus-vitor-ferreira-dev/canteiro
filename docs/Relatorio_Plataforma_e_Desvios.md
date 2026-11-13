# Relatório de plataforma e desvios do CANTEIRO

<!--
Entregável da seção 4.11 da especificação. Na entrega, ele é exportado para PDF.
As linhas "pendente" da seção 2.3 esperam os testes em máquina real (#133).
-->

| Campo | Conteúdo |
|---|---|
| **Responsável** | Mateus Vitor Ferreira |
| **Revisão** | Alisson Antônio Oliveira Santos |
| **Testes em máquina real e apresentação** | Marcelo Camillo De Paula Leite |
| **Versão do jogo** | 0.3.0 |
| **Especificação de referência** | 2.5 (2026-10-29) |
| **Última atualização** | 2026-11-13 |

## 1. Resumo

O CANTEIRO foi planejado como um aplicativo desktop em Swing (especificação 1.0). Ele foi entregue como um jogo no navegador, com as regras num backend em Java e a tela num frontend em Svelte. O jogador continua precisando só do Java 17 e de um navegador, e o jogo continua sendo um JAR que abre com dois cliques.

Este relatório tem três partes:

- **Seção 3:** a mudança de plataforma em relação à especificação 1.0.
- **Seção 4:** as mudanças feitas durante o desenvolvimento e já registradas na especificação 2.x, que são a versão online, a partida no celular, as regras RN16 a RN22, a música de fundo e alguns ajustes no modelo de classes.
- **Seção 5:** os pontos em que o código entregue difere do texto da especificação 2.5.

Cada mudança vem com a fonte e o porquê.

### 1.1. Equipe

| Integrante | O que fez nesta entrega |
|---|---|
| Mateus Vitor Ferreira | Toda a programação do backend e do frontend, a documentação do código em Javadoc, o empacotamento do JAR, a versão online e este relatório |
| Marcelo Camillo De Paula Leite | A apresentação do projeto, as rodadas de teste com os roteiros, o teste das funções desejáveis (configurações, relatório, modo treino e repetições) e o teste do JAR em máquinas reais, com a medição de desempenho (#27, #132, #133) |
| Alisson Antônio Oliveira Santos | Os roteiros de teste manual, as sessões com jogadores, a revisão de usabilidade e acessibilidade e a revisão deste relatório (#17, #30, #34) |

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
| Documentação do código | Javadoc, com `doclint` completo e falha a qualquer aviso | JDK 17 (também passa no JDK 21) |
| Build | Maven, pelo Maven Wrapper | 3.9 ou superior |
| Frontend | Svelte 5 com TypeScript em modo `strict` | Svelte 5.57, TypeScript 6.0 |
| Empacotador do frontend | Vite | 8.3 |
| Gráfico do relatório | Chart.js | 4.5 |
| Testes do frontend | Vitest, Testing Library e jsdom | Vitest 5.0 |
| Node.js no build do JAR | baixado pelo `frontend-maven-plugin` | 24.21 (LTS) |
| Node.js para desenvolver o frontend | instalado por quem programa | 20.19 ou 22.12 em diante |
| Versão online | Docker (imagem `eclipse-temurin:17-jre`), publicado pelo Coolify num VPS próprio | — |
| Controle de versão | Git e GitHub, com PR revisado e CI em todo merge na `develop` | — |

As versões saem do `backend/pom.xml`, do `frontend/package.json` e do `Dockerfile`.

### 2.3. Onde o jogo foi testado

| Onde | O que foi conferido | Fonte |
|---|---|---|
| CI no Linux (Ubuntu), com Java 17 | 406 testes do backend, `ArquiteturaTest` e Javadoc sem aviso. No frontend, lint, tipos, 234 testes e o build. Depois, o JAR completo | `.github/workflows/ci.yml`, em todo PR |
| CI no Linux, no Windows e no macOS, com Java 17 | O JAR abre, mostra a página e responde `GET /api/dificuldades` e `POST /api/partidas` em até 3 s | #63 |
| Contêiner `eclipse-temurin:17-jre`, sem Node | O JAR roda sem Node.js e, sem tela gráfica, mostra o endereço no terminal (RF29) | #63 |
| Linux, Java 21, Intel Core Ultra 9 285K, 30 GB | Dez minutos de jogo automático: 59,7 a 60,1 ciclos por segundo, ciclo médio de 0,03 ms e até 72 MB de memória (RNF01) | #83 |
| JAR, jogando de verdade | Da tecla ao estado novo, mediana de 13 ms e pior caso de 32 ms (RNF03). O navegador desenha a 60 quadros por segundo | #25 |
| Chromium (Playwright) em 375, 768, 1024 e 1440 px | Nenhuma rolagem horizontal e botões de toque de pelo menos 44 px. Os quatro materiais se distinguem em simulação de daltonismo e em escala de cinza (RNF06) | #60 |
| Chromium (Playwright) emulando um Android de 360 × 760 | A partida cabe na tela sem rolar, e os gestos e os botões funcionam | #105 |
| Versão online ([canteiro.digitalcodexa.com](https://canteiro.digitalcodexa.com)) | O mesmo JAR, publicado quando o CI da `develop` passa, com verificação de fora depois do deploy | #87 |
| Máquinas reais, com dois cliques no JAR | **Pendente** até a data deste relatório | #133 |
| Máquina de referência do RNF01 (dois núcleos e 4 GB) | **Pendente** até a data deste relatório | #133 |

## 3. Desvios em relação à especificação 1.0

A versão 1.0 previa um aplicativo desktop em Swing. A versão 2.0 dividiu o jogo em backend e frontend, antes de existir código. A tabela é a do Apêndice A.2 da especificação.

| Item | Planejado (1.0) | Entregue | Por quê |
|---|---|---|---|
| Interface | Swing, dentro do JDK | Svelte 5 com TypeScript, no navegador | Animações, texturas e gráficos ficam bem mais simples com HTML, CSS e Canvas. Como a tela está em outro processo e outra linguagem, o modelo também não tem como chamá-la |
| Laço de atualização | `javax.swing.Timer` de ~16 ms | `ScheduledExecutorService` de ~16 ms no backend e `requestAnimationFrame` no frontend | O laço das regras deixa de depender da interface. Em máquina lenta, cai a taxa de quadros, não o ritmo do jogo |
| Comunicação do modelo com a tela | Observadores no mesmo processo | Observadores no backend, que mandam o estado por WebSocket | A tela agora está em outro processo |
| Dependências | Só a biblioteca padrão do Java | Javalin, Jackson e SLF4J no backend; Svelte e Chart.js no frontend | O JDK não tem servidor WebSocket. O Javalin foi escolhido em vez do Spring Boot por ser pequeno e por deixar todo objeto ser criado com `new`, à vista |
| Pacote `canteiro.visao` | Telas em Swing | Removido. No lugar, `canteiro.api` (rotas e WebSocket) e a pasta `frontend/` | A tela passou para o frontend |
| Execução | Dois cliques no JAR abrem a janela | Dois cliques no JAR sobem o servidor em `127.0.0.1` e abrem o navegador | Mantém o entregável "executável único" |
| Plataforma | Java 17 | Java 17 e um navegador atual. Node.js só para quem desenvolve o frontend | O jogador continua precisando só do Java |
| Testes | JUnit 5 | JUnit 5 no backend; Vitest e Testing Library no frontend | Cada lado é testado com a ferramenta da sua linguagem |
| Requisitos | RF01–RF28, RNF01–RNF12 | RF01–RF31, RNF01–RNF16 | Novos requisitos de navegador, conexão, protocolo e segurança local |

**O que não mudou:**

- as regras de negócio RN01 a RN15;
- as hierarquias de peças e materiais e as estruturas de dados;
- o cálculo incremental do centro de massa e a regra de colapso;
- a máquina de estados, os arquivos persistidos e a curva de dificuldade.

Herança, polimorfismo e estruturas de dados continuam todos no Java.

## 4. Desvios durante o desenvolvimento

Todos os desvios desta seção já estão na especificação 2.5, no Apêndice A.3.

### 4.1. Svelte no lugar de React

| | |
|---|---|
| **Planejado** | Frontend em React com TypeScript (especificação 2.0) |
| **Entregue** | Svelte 5 com TypeScript (especificação 2.1) |
| **Por quê** | O frontend é fino: recebe o estado, desenha num Canvas e troca de tela. No Svelte, um estado reativo é só `$state`, sem `useEffect` nem lista de dependências. O pacote compilado também é menor, o que ajuda no JAR único (RNF16) e em máquina fraca (RNF01) |
| **Quando** | Antes de existir código no frontend, então não houve retrabalho (#3) |

### 4.2. Versão online

| | |
|---|---|
| **Planejado** | Jogo só local. A premissa 2.9 diz que o servidor nunca fica exposto na rede, e a seção 4.15 diz que publicá-lo exigiria uma revisão de segurança própria |
| **Entregue** | Além do JAR, o mesmo jogo roda num contêiner Docker. Ele é publicado pelo Coolify num VPS próprio quando o CI da `develop` passa, e o deploy confere de fora que o jogo responde (#87) |
| **Por quê** | Para quem avalia e para os jogadores das sessões de teste, abrir um link é bem mais fácil que instalar o Java e baixar o JAR |
| **O que mudou no código** | Nada nas regras. O comportamento muda por variáveis de ambiente: `CANTEIRO_HOST=0.0.0.0` (só dentro do contêiner), `CANTEIRO_ABRIR_NAVEGADOR=false`, `CANTEIRO_LIMITE_PARTIDAS=10` e `CANTEIRO_PASTA=/app/dados`. Sem nenhuma delas, vale o jogo local, só em `127.0.0.1` |
| **Requisito afetado** | RNF13. O JAR continua escutando só em `127.0.0.1`; o contêiner escuta em todas as interfaces por trás do proxy da hospedagem |
| **Cuidados tomados** | <ul><li>No máximo 10 partidas ao mesmo tempo, porque cada uma tem um laço numa thread própria.</li><li>A partida sem conexão por 10 minutos é descartada (RF31).</li><li>O contêiner roda com um usuário sem privilégios.</li><li>O ranking lê a pontuação da própria partida no servidor, nunca a que vem do navegador (#92).</li><li>A API e o WebSocket só atendem a própria origem (#138).</li><li>Num servidor compartilhado, a rota de configurações não existe, e cada navegador guarda as suas.</li></ul> |
| **Limites que continuam** | <ul><li>Não há autenticação.</li><li>As partidas abertas ficam em memória, então um deploy novo encerra as que estiverem em andamento.</li><li>O ranking e as repetições ficam num volume e sobrevivem ao deploy.</li><li>O ranking que vale é o do JAR, na pasta de cada jogador.</li></ul> |

### 4.3. Jogável no celular

| | |
|---|---|
| **Planejado** | Premissa 2.9 da especificação 2.2: área mínima de 1024 × 768 e entrada da partida só pelo teclado |
| **Entregue** | <ul><li>Layout de uma a três colunas a partir de 375 px, com botões de toque (#60).</li><li>No celular: gestos no tabuleiro, botões em duas linhas, uma versão compacta do placar e vibração (#105).</li></ul> |
| **Por quê** | No celular, a partida era quase injogável. Os botões ficavam empilhados numa coluna só, e o placar, a estabilidade e os botões passavam da altura da tela, então o jogador rolava a página e perdia o tabuleiro de vista (#95) |
| **Requisito afetado** | RNF05 (comandos aprendidos sem manual): na pausa, quem joga pelo toque lê como jogar com os gestos. No computador, nada mudou: o teclado, a legenda e as telas são os mesmos |

### 4.4. Regras novas: RN16 a RN22

A especificação 1.0 tinha 15 regras de negócio. Sete entraram na reta final, todas no backend e com testes.

| Regra | O que faz | Por quê | Fonte |
|---|---|---|---|
| RN16 | A peça que a gravidade encosta no apoio espera 500 ms antes de fixar. Mover ou girar recomeça a espera, até 15 vezes | Sem a espera, a peça fixava no mesmo ciclo em que encostava, e não dava para deslizá-la nem girá-la no último instante. Nos níveis rápidos, de 200 ms por linha, a sensação era de injustiça. O limite de 15 recomeços impede segurar a peça para sempre | #101 |
| RN17 | Fechar linha com o índice de estabilidade abaixo de 25 % vale 50 % a mais (obra ousada) | Antes, o equilíbrio só castigava. Agora arriscar também rende pontos | #103 |
| RN18 | Cada peça seguida que fecha linha soma 10 % à próxima eliminação, até 100 % | Também recompensa o risco: para manter a sequência, toda peça precisa fechar linha | #103 |
| RN19 | Cada nível tem uma meta curta, sorteada pela semente da partida. Cumprir soma 500 × nível | Dá um objetivo além de sobreviver. Como o sorteio usa a mesma semente, a repetição da partida continua funcionando | #104 |
| RN20 | Balanço: a parte da peça sem nada embaixo se solta e cai se a peça for mais densa que o material em que se apoia | Jogando, o CANTEIRO não se diferenciava de um encaixe de blocos comum. A única física era o centro de massa da pilha inteira, e uma peça pesada apoiada pela ponta num bloco leve ficava parada no ar. Agora o material de baixo importa | #117 |
| RN21 | Esmagamento: cada material aguenta um peso máximo em cima, na mesma coluna. Perto do limite o bloco racha e, passando dele, vira um buraco | Obriga a construir como numa obra, pesado embaixo e leve em cima. O bloco esmagado vira um buraco, em vez de a coluna descer, porque a coluna descendo limitava a altura da pilha e deixava o jogo mais fácil | #118 |
| RN22 | O colapso confere também a parte de cima da pilha, sobre a faixa em que ela encosta na linha crítica. Cada dificuldade multiplica o limite de desvio e a resistência dos materiais | As linhas quase cheias puxavam o centro de massa para o meio, e o colapso quase nunca acontecia, nem no difícil | #119 |

**Prévia do centro de massa (#107).** Junto com as regras novas veio uma prévia:

- Antes de soltar a peça, o jogador vê onde o centro de massa vai parar e se a jogada derruba a obra.
- Desde a RN20, a prévia roda a jogada inteira numa cópia da grade, e por isso também marca os blocos que se soltariam ou seriam esmagados.
- Ela só é recalculada quando o pouso da peça muda. O centro de massa da partida continua incremental (RNF02).

### 4.5. Música de fundo (RF28)

| | |
|---|---|
| **Planejado** | Só efeitos sonoros para fixação, eliminação de linha e colapso |
| **Entregue** | Os efeitos e uma música gerada no próprio navegador, que acelera e ganha camadas quando a estrutura chega perto de cair. Ela pode ser desligada sem calar os efeitos (#123) |
| **Por quê** | Reforça pelo som o aviso de risco, sem o jogador precisar olhar o indicador. Gerar a música no navegador evita arquivos de áudio no JAR |

### 4.6. Ajustes de código em relação ao modelo de classes

| Planejado | Feito | Por quê | Fonte |
|---|---|---|---|
| `Comando` no pacote `controle` | `Comando` em `canteiro.modelo` | O motor recebe comandos, e o modelo não pode importar o controle (RNF08) | #50 |
| `Material.aoFixar(Tabuleiro, Peca)` | `Material.aoFixar(Tabuleiro, List<Celula>)` | A peça não sabe onde está no tabuleiro, e o aço precisa das células em que ela assentou | #48 |
| A pontuação, o nível e o colapso dentro do `MotorJogo` | Saíram para `Assentamento` | O `MotorJogo` tinha passado do limite de 400 linhas (RNF09). Hoje tem 399 | #101 |
| Desfazer com uma pilha de cópias do estado (tabuleiro, placar, peça em queda) | O motor é refeito do começo, com a mesma semente, até a peça desfeita voltar ao topo (`Treino` e `Reprodutor`) | Copiar o estado exigiria copiar também a fila de peças, o sorteio da sacola, a reserva e a meta do nível. Como a partida já era reproduzível, refazer as jogadas devolve tudo igual, e o `MotorJogo` não cresceu. O mesmo `Reprodutor` serve ao replay | #129 |

## 5. Diferenças entre o código e a especificação 2.5

Na revisão final, cada requisito da especificação 2.5 foi conferido contra o código. Três diferenças foram corrigidas no código antes da entrega:

| Item | Antes | Agora | Fonte |
|---|---|---|---|
| RNF13: só a própria origem é aceita | O servidor escutava só em `127.0.0.1`, mas atendia pedidos e WebSocket de qualquer origem | `FiltroOrigem` confere o `Origin` contra o `Host` antes das rotas e antes de abrir o WebSocket, e responde 403 se não casarem. Pelo proxy do Vite, a origem do desenvolvimento casa sem exceção no código | #138 |
| `GET /api/materiais` (Apêndice B) | A rota não existia | Devolve código, nome, densidade, cor, bônus por linha e carga máxima de cada material, do catálogo em uso | #139 |
| Tela de pausa (4.3.1): retomar, reiniciar e sair | A camada só dizia para apertar P | A camada tem os botões **Retomar**, **Recomeçar** (volta à escolha da dificuldade) e **Sair para o menu** | #140 |

As outras diferenças ficaram como estão, pelo motivo de cada linha:

| Item da especificação | O que o código faz | Por quê |
|---|---|---|
| Telas com rota própria (4.3.1: `/nova-partida`, `/partida/:id`, `/ranking`…) | A tela aberta é um estado do frontend (`navegacao.svelte.ts`), guardado no histórico do navegador. O endereço não muda | A própria seção 3.1 da especificação dispensa a biblioteca de rotas. O botão voltar do navegador volta uma tela, que é o que as rotas dariam ao jogador |
| Repetições com reprodução passo a passo e controle de velocidade (RF24, 4.3.1) | A partida gravada é refeita pelo motor no servidor, no ritmo do jogo, e pode ser pausada e retomada | A repetição é a partida de novo, jogada pelo `Reprodutor` com a mesma semente e os mesmos comandos, no laço de 60 Hz (RN04, RNF01). Avançar uma jogada ou mudar a velocidade exigiria um segundo relógio no motor só para o replay, e o `MotorJogo` já está no limite de 400 linhas (RNF09) |
| Fim de partida com "opção de salvar a repetição" (4.3.1) | Toda partida concluída é gravada sozinha em `repeticoes/` | O jogador não perde uma partida boa por esquecer de salvar. Ficam as 50 mais recentes, e a mais antiga é apagada |
| `configuracoes.properties` com volume, música e texturas (4.2) | O arquivo guarda as teclas e o volume. A música ligada ou desligada fica no navegador (`localStorage`), e as texturas para daltonismo ficam sempre ligadas | A música é só do frontend (RF28), e o jogo online não tem como gravar a preferência de cada jogador num arquivo do servidor. A textura fica por cima da cor sem escondê-la, então serve a quem enxerga as cores e a quem não enxerga (RNF06, #60), e a opção de desligar foi dispensada |
| `materiais.properties` com código, nome, densidade, cor e bônus (4.2) | O arquivo tem densidade e cor. O código, o nome e o bônus estão nas subclasses de `Material`, e `GET /api/materiais` mostra todos | O bônus e a carga máxima são o comportamento de cada material (Template Method, seção 3.4.1). Deixar o bônus editável num arquivo permitiria inflar a pontuação do ranking |
| Rotação "uma linha acima" como último deslocamento corretivo (3.5) | A peça I tenta subir duas linhas | A especificação define os deslocamentos por peça (`deslocamentosDeRotacao()`). A I deitada no chão só fica em pé subindo duas linhas, então a última tentativa dela sobe duas (#51) |
| RN12: o colapso desconta "o equivalente a duas linhas eliminadas" | Desconta o valor de uma eliminação dupla, 300 × nível, sem bônus de material | É a leitura adotada para a regra desde o primeiro colapso (#53). O texto não diz se são duas linhas simples (200 × nível) ou uma eliminação dupla. O Javadoc de `Pontuacao.penalidadeColapso` registra a escolha |
| Apêndice B: erros com status `400`, `404` ou `409` | O ranking responde `410` quando a partida já saiu da memória do servidor | O 410 indica que a partida existiu, mas já expirou, o que é diferente de um id que nunca existiu (404). A mensagem explica ao jogador que a partida expira 10 minutos depois de fechada |
| Apêndice B: campo `reserva` no estado | O campo se chama `reservada` | O nome descreve a peça guardada, e os dois lados usam o mesmo nome: o `EstadoDto` no Java e o tipo do TypeScript (RNF15) |
| RNF09: nenhuma função com mais de 40 linhas úteis | `conectarPartida` (`frontend/src/api/conexao.ts`) tem 55 linhas, contando três funções internas | Cada função interna tem menos de 40 linhas. A externa só monta o estado da conexão e devolve o objeto com `enviar` e `fechar` |
| Pasta `~/.canteiro/` criada na primeira execução (4.2) | A pasta e cada arquivo são criados na primeira gravação | Enquanto o jogador não muda nada, o jogo usa os padrões de dentro do JAR (RNF11) e não deixa uma pasta vazia na pasta pessoal |

## 6. O que ainda não foi feito

Todos os requisitos funcionais, essenciais e desejáveis, estão implementados. Falta só o que depende de máquina real.

| Requisito | Falta | Issue |
|---|---|---|
| RNF01 | A medição na máquina de referência, de dois núcleos e 4 GB. Numa máquina mais forte, o backend mantém 59,7 a 60,1 ciclos por segundo, com ciclo médio de 0,03 ms | #133 |
| RNF04 e RF29 | Os dois cliques no JAR em Windows, Linux e macOS, com cada navegador. No CI, o JAR já abre nos três sistemas, mas sem navegador de verdade | #133 |
