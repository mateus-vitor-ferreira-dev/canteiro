# Medição de desempenho e navegadores (RNF01, RNF03 e RNF04)

Medição feita em 2026-11-16 com o JAR da versão 0.3.0, para os dois requisitos não funcionais que ainda esperavam medição (#133).

## Resumo

| Requisito | Resultado |
|---|---|
| **RNF01**: 60 quadros/s na tela e 60 atualizações/s no backend, com 2 núcleos e 4 GB | **Atende**, com a máquina de referência **emulada**: 60,0 quadros/s em todas as janelas e 59,3 a 60,1 ciclos/s, inclusive com os 2 núcleos limitados a metade da capacidade |
| **RNF03**: da tecla à tela em até 50 ms | Mediana de **14 ms** na mesma emulação (a medição anterior, sem limites, deu 13 ms) |
| **RNF04**: navegadores atuais | **Chrome 157, Chromium 156 e Firefox 158 no Linux:** a partida abre, responde ao teclado e não há erro no console |
| **RNF04**: dois cliques no JAR | **Não abre nesta máquina** (Ubuntu 26.04.1). Ver abaixo |
| **RNF04**: Windows e macOS | Não testado aqui. O CI abre o JAR nos três sistemas (#63), mas falta o duplo clique e os navegadores em máquinas reais |

## RNF01 e RNF03: como foi medido

Não havia uma máquina de 2 núcleos e 4 GB à mão. Ela foi emulada numa máquina de 24 núcleos (Intel Core Ultra 9 285K, Ubuntu 26.04.1):

- **Backend:** o JAR num contêiner `eclipse-temurin:17-jre` (Java 17), preso a **2 núcleos** (`--cpuset-cpus=22,23`, dois núcleos de eficiência, os mais lentos do processador) e **4 GB** (`--memory=4g`, sem swap).
- **Navegador:** Google Chrome 157 sem tela, preso aos **mesmos 2 núcleos** (`taskset -c 22,23`) e limitado a **4 GB** (`systemd-run --scope -p MemoryMax=4G`).
- **Jogador:** o script [`medir.cjs`](medir.cjs) joga pelo teclado, peça por peça, espalhando as peças pelas colunas, e começa uma partida nova a cada fim de jogo. Ele conta os quadros desenhados (`requestAnimationFrame`) e o tempo entre mandar um comando e chegar o estado seguinte pelo WebSocket. Os quadros do menu, entre uma partida e outra, são descartados.
- **Backend:** o próprio laço da partida escreve no log, a cada 10 s, os ciclos por segundo, o tempo do ciclo e a memória (#78). Só escreve com partida em jogo.

Dois cenários:

- **A:** 2 núcleos inteiros, 10 minutos.
- **B:** os mesmos 2 núcleos limitados a 50 % (`--cpus=1` no contêiner e `CPUQuota=100%` no navegador), 5 minutos. É o pior caso: um núcleo desta máquina é bem mais rápido que o de uma máquina barata, e cortar a capacidade pela metade compensa parte disso.

## RNF01 e RNF03: resultados

| Métrica | Meta | A: 2 núcleos, 10 min | B: 2 núcleos a 50 %, 5 min |
|---|---|---|---|
| Quadros por segundo na tela | 60 | 60,0 em todas as janelas de 10 s; nenhum quadro acima de 33 ms | 60,0 em todas; nenhum quadro acima de 33 ms |
| Ciclos por segundo no backend | 60 ± 1 | 59,3 a 60,0 (média 59,99), em 50 janelas | 59,3 a 60,1 (média 59,98), em 25 janelas |
| Tempo médio do ciclo | até 4 ms | 0,06 ms (pior janela: 0,26 ms) | 0,08 ms (pior janela: 0,23 ms) |
| Memória do backend | até 200 MB | 21 a 76 MB | 25 a 68 MB |
| Tecla → estado novo (mediana) | até 50 ms | 14 ms (4.366 amostras) | 14 ms (2.155 amostras) |
| Memória do navegador | — | até 1,35 GB | até 1,33 GB |
| Partidas e peças jogadas | — | 16 partidas, 401 peças | 9 partidas, 199 peças |

**O que olhar com cuidado:**

- **Picos isolados no ciclo.** O ciclo mais lento de uma janela passou de 4 ms em 5 das 50 janelas do cenário A e em 6 das 25 do B, com máximo de 37 ms. O maior pico de cada cenário (33 ms no A e 37 ms no B) foi na primeira janela da medição, com a JVM recém-aberta; depois do primeiro minuto, nenhum passou de 7 ms no A nem de 9 ms no B. A meta do RNF01 é para o tempo **médio**, que ficou em décimos de milissegundo, e os picos não derrubaram a taxa de ciclos.
- **O percentil 95 da tecla (191 ms) não vale como medida.** O script às vezes empurra a peça contra a parede. Esse comando não muda nada, e o backend só manda estado quando algo muda, então o script espera até a próxima queda da peça. Por isso só a mediana é usada.
- **Memória.** O contêiner do jogo inteiro (Java e JVM) ficou em 190 MB. Somado ao navegador, fica abaixo dos 4 GB.

**Limites:** é uma emulação. Os 2 núcleos são de um processador de 2025, e a memória é limitada por cgroup, não por hardware. O navegador rodou sem tela, então desenhou o Canvas no processador, sem placa de vídeo; isso pesa mais, não menos. Uma máquina antiga de verdade ainda é o teste definitivo (#133).

Os dados brutos estão em [`resultados/`](resultados/): `navegador-A.json` e `navegador-B.json` têm cada janela de 10 s do navegador; `backend-A.txt` e `backend-B.txt` têm as linhas do log do laço.

## RNF04: navegadores

O script [`navegadores.cjs`](navegadores.cjs) abre o jogo, começa uma partida no Normal, joga 30 teclas e confere a partida na tela, o tabuleiro mudando, a taxa de quadros e os erros no console.

| Navegador | Partida abre | Responde ao teclado | Quadros/s | Erros no console |
|---|---|---|---|---|
| Google Chrome 157 | sim | sim | 60 | nenhum |
| Chromium 156 | sim | sim | 60 | nenhum |
| Firefox 158 | sim | sim | 56 | nenhum |

O Firefox sem tela não sincroniza o desenho com uma tela de verdade; os 56 quadros/s dele não medem o RNF01, que foi medido no Chrome. Edge e Safari não foram testados.

## RNF04: dois cliques

**Nesta máquina, o duplo clique no JAR não abre o jogo.** No Ubuntu 26.04.1, o pacote `openjdk-17-jre` associa os arquivos `.jar` a um atalho que roda `cautious-launcher %f /usr/bin/java -jar`. O `cautious-launcher` não está instalado e não aparece no `apt`. Com o atalho inválido, o sistema abre o JAR no gerenciador de arquivos, como se fosse uma pasta compactada.

O jogo abre normalmente com `java -jar canteiro.jar`. Faltam:

- confirmar se isso acontece em outra instalação do Ubuntu;
- testar o duplo clique no Windows e no macOS (#133).

## Como repetir

```bash
# uma vez: o puppeteer-core numa pasta qualquer
mkdir -p /tmp/pptr && (cd /tmp/pptr && npm install puppeteer-core)
export NODE_PATH=/tmp/pptr/node_modules CHROME=/usr/bin/google-chrome

# o jogo num contêiner com 2 núcleos e 4 GB, na porta 7071
docker run -d --name canteiro-rnf01 --cpuset-cpus=22,23 --memory=4g --memory-swap=4g \
  -p 127.0.0.1:7071:7071 -e PORT=7071 -e CANTEIRO_HOST=0.0.0.0 -e CANTEIRO_ABRIR_NAVEGADOR=false \
  -e CANTEIRO_PASTA=/tmp/dados -v "$PWD/backend/target/canteiro.jar:/app/canteiro.jar:ro" \
  eclipse-temurin:17-jre java -jar /app/canteiro.jar

# o navegador nos mesmos 2 núcleos, com 4 GB, jogando por 10 minutos
systemd-run --user --scope -p MemoryMax=4G -p MemorySwapMax=0 \
  taskset -c 22,23 node docs/testes/desempenho/medir.cjs http://127.0.0.1:7071 600 navegador.json

# o que o backend mediu
docker logs canteiro-rnf01 2>&1 | grep 'ciclos/s'
```

Para o cenário B, acrescente `--cpus=1` ao `docker run` e `-p CPUQuota=100%` ao `systemd-run`. Escolha os núcleos com `lscpu -e=CPU,MAXMHZ`, de preferência os de menor frequência.
