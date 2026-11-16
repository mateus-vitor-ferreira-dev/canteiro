# Roteiro da apresentação final

Slides: [`../Apresentacao_Final_Canteiro.pdf`](../Apresentacao_Final_Canteiro.pdf), 32 slides. Quem apresenta: Marcelo.

A tarefa pede de **15 a 20 minutos**: documentação (3 a 5 min), codificação (10 a 12 min), o jogo funcionando (2 a 3 min) e a opinião sobre a disciplina. Este roteiro fecha em **18 min 25 s**: sobram 2 minutos de folga, e ainda passa dos 15 se a fala correr.

> **O que o professor avalia:** clareza, profundidade, familiaridade com o tema, tempo, relação com a disciplina e domínio do conteúdo.
>
> **Como apresentar os slides de código (10 a 28).** Todos têm o mesmo formato: à esquerda, o **print do arquivo de verdade**, com os números de linha reais; à direita, **o requisito** e **como foi feito**. As linhas com fundo amarelo são as que você aponta. A fala tem sempre três tempos:
> 1. **O requisito:** "o RN04 pede que...".
> 2. **A linha:** "aqui, na linha 103, ...".
> 3. **O porquê:** por que essa estrutura, e não outra.
>
> Não leia o código linha a linha.

## Tempos

| Slide | Assunto | Tempo | Acumulado |
|---|---|---|---|
| 1 | Capa | 0:15 | 0:15 |
| 2 | O assunto | 0:25 | 0:40 |
| 3 | O que o sistema faz | 0:40 | 1:20 |
| 4 | Objetivos | 0:25 | 1:45 |
| 5 | Regras de negócio | 0:45 | 2:30 |
| 6 | Requisitos funcionais | 0:40 | 3:10 |
| 7 | Requisitos não funcionais | 0:25 | 3:35 |
| 8 | Bibliotecas e referências | 0:30 | **4:05** |
| 9 | Parte 2 | 0:05 | 4:10 |
| 10 | RNF08 · arquitetura | 0:40 | 4:50 |
| 11 | RN04 · sacola e fila | 0:35 | 5:25 |
| 12 | RN05 · reserva em pilha | 0:25 | 5:50 |
| 13 | RF08 · rotação | 0:30 | 6:20 |
| 14 | RF11 · polimorfismo | 0:40 | 7:00 |
| 15 | RF13 · pontuação | 0:30 | 7:30 |
| 16 | RF14 · centro de massa | 0:45 | 8:15 |
| 17 | RN09–RN10 · estabilidade | 0:35 | 8:50 |
| 18 | RNF10 · teste | 0:30 | 9:20 |
| 19 | RN11 · colapso | 0:40 | 10:00 |
| 20 | Problema: física local | 0:30 | 10:30 |
| 21 | RN20 · balanço | 0:30 | 11:00 |
| 22 | RN21 · esmagamento | 0:30 | 11:30 |
| 23 | Bug do fim de jogo | 0:35 | 12:05 |
| 24 | RNF01 · uma thread só | 0:35 | 12:40 |
| 25 | RF24 · repetição | 0:30 | 13:10 |
| 26 | RF21 · ranking | 0:30 | 13:40 |
| 27 | RNF11 · arquivos | 0:25 | 14:05 |
| 28 | RF15–RF16 · painel | 0:30 | 14:35 |
| 29 | Testes e números | 0:20 | **14:55** |
| 30 | Demonstração ao vivo | 2:30 | **17:25** |
| 31 | Opinião sobre a disciplina | 0:50 | 18:15 |
| 32 | Obrigado | 0:10 | **18:25** |

**Marcos para olhar o relógio:** slide 9 começando em **4:05**, slide 20 em **10:00** e slide 30 em **14:55**. Se estiver mais de um minuto atrasado, resuma numa frase só os slides 12, 15, 27 e 28 e passe direto.

---

## Parte 1: documentação (4 min 5 s)

**1. Capa (15 s).** "Somos o grupo do CANTEIRO: Marcelo, Mateus e Alisson."

**2. O assunto (25 s).** Faça a pergunta do slide e espere um segundo. Jogos de encaixe só pensam em espaço; no nosso, cada peça é um material de construção.

**3. O que o sistema faz (40 s).** Aponte no print do jogo: o índice de estabilidade, a linha tracejada (o eixo da base) e o X vermelho. A cada peça, o jogo calcula o centro de massa e compara com o eixo; passou do limite, a obra desaba.

**4. Objetivos (25 s).** Não leia os oito cartões. O objetivo era um jogo em que **cada conceito da disciplina resolve um problema real**.

**5. Regras de negócio (45 s).** São 22 regras; aqui estão as 15 que vão aparecer no código. Não leia linha por linha: leia as do equilíbrio (RN07 a RN11) e diga que a coluna **Slide** é onde cada uma vai ser mostrada no código. As RN20 a RN22 entraram no meio do projeto; o slide 20 conta por quê.

**6. Requisitos funcionais (40 s).** Os 31 estão prontos; estes 14 aparecem no código. Destaque o RF14 (recalcular o centro de massa a cada mudança) e o RF17 (o colapso), que são o coração do jogo.

**7. Requisitos não funcionais (25 s).** São de qualidade, e não de função. Destaque o RNF02 (centro de massa em tempo constante) e o RNF08 (regras só no modelo). O RNF01 (60 quadros por segundo com 2 núcleos e 4 GB) foi medido; os números estão no slide 29.

**8. Bibliotecas e referências (30 s).** As regras são Java puro. O Javalin só serve HTTP e WebSocket; o Svelte só desenha. Cite as referências.

## Parte 2: codificação (10 min 50 s)

**9. Parte 2 (5 s).** "Agora, como cada requisito foi implementado."

**10. RNF08, arquitetura (40 s).** O navegador manda teclas e recebe o estado; todas as regras ficam no modelo Java. O print é o `ArquiteturaTest`, que lê os `import` de cada pacote e reprova o build se o modelo importar o servidor ou o JSON. **Por quê:** dá para testar as regras sem abrir servidor nem navegador.

**11. RN04, sacola e fila (35 s).** Linha 103: quando a sacola esvazia, entram as 7 formas embaralhadas. Linha 106: tira da frente. Assim nenhuma forma some por muito tempo. As próximas peças ficam numa fila (`ArrayDeque`): a primeira que entra é a primeira que sai.

**12. RN05, reserva (25 s).** É uma pilha: guarda e pega sempre no topo (linhas 50 e 52). A trava da linha 49 impede trocar duas vezes a mesma peça.

**13. RF08, rotação (30 s).** Gira; se colidiu, a linha 85 tenta cada deslocamento da tabela da peça. Se nenhum serve, a linha 90 desfaz o giro.

**14. RF11, polimorfismo (40 s).** Linha 89: o tabuleiro chama `material.aoFixar(...)` sem saber o material. Embaixo, o `Aco` sobrescreve o método e firma os blocos de baixo. É polimorfismo: nenhum `switch` por tipo. Forma e material em duas hierarquias dão 11 classes em vez de 28.

**15. RF13, pontuação (30 s).** Linha 47: base por linhas eliminadas de uma vez. Linha 87: multiplica o bônus do material, o da obra ousada e o da sequência.

**16. RF14 e RNF02, centro de massa (45 s).** É o slide mais importante. O jeito ingênuo varre as 200 posições a cada ciclo. Aqui são dois números: linha 59, fixar soma massa × coluna; linha 71, remover subtrai; linha 107, o centro é uma divisão. Custo constante. Detalhe que mostra domínio: eliminar uma linha não muda o momento de quem desce, porque o bloco continua na mesma coluna.

**17. RN09 e RN10, estabilidade (35 s).** Linha 176: o desvio. Linha 177: o índice cai em linha reta até zero no limite. Linha 138: mede a pilha inteira e a parte de cima, e vale a pior.

**18. RNF10, teste (30 s).** Uma conta rápida errada gera colapso injusto. O teste joga 500 peças e, a cada uma, compara com a varredura completa (linha 47).

**19. RN11, colapso (40 s).** Linha 36: acha a linha mais pesada. Linha 41: bloco firmado pelo aço não cai (o polimorfismo do slide 14). Linha 90: `cair` chama a si mesma até achar apoio, e o caso base é o chão ou um bloco.

**20. Problema: física local (30 s).** Conte como história: "Jogando, o CANTEIRO parecia um encaixe comum. As linhas quase cheias puxam o centro para o meio, e quase nunca desabava." A solução foram três regras novas.

**21. RN20, balanço (30 s).** Linha 53: coluna sem nada embaixo está em balanço. Linha 56: se a peça não for mais densa que o apoio, segura; senão cai, usando o mesmo `cair` do colapso.

**22. RN21, esmagamento (30 s).** Linha 128: a carga é a massa dos blocos colados acima. Linha 85: esmaga um, recalcula, até ninguém mais ceder.

**23. Bug do fim de jogo (35 s).** A pilha chegava ao topo e a tela congelava. Explique a causa (a prévia tentava fixar a peça em cima da pilha e lançava exceção) e aponte a correção na linha 349.

**24. RNF01, uma thread só (35 s).** Linha 47: uma fila concorrente. Linha 143: a thread do WebSocket só enfileira. Linha 198: o laço, 60 vezes por segundo, esvazia a fila. Sem trava e sem condição de corrida.

**25. RF23 e RF24, repetição (30 s).** Como só uma thread mexe no motor, o jogo é determinístico. Linha 87: reproduzir é aplicar cada comando no mesmo ciclo em que chegou.

**26. RF21 e RN15, ranking (30 s).** O navegador manda só o id e o nome. Linha 149: treino é recusado. Linha 162: a pontuação é lida da sessão, no servidor.

**27. RNF11 e RNF12, arquivos (25 s).** Arquivo ausente ou estragado vira o padrão do JAR, com aviso no log (linha 54), e o jogo sempre abre.

**28. RF15 e RF16, painel (30 s).** É o lado do navegador, em Svelte. Ele não calcula nada: recebe a estabilidade e a prévia prontas e decide o aviso.

**29. Testes e números (20 s).** 406 testes no backend e 234 no frontend, em cada PR. O RNF01 foi medido com o jogo e o navegador presos a 2 núcleos e 4 GB, durante 15 minutos de jogo: 60 quadros por segundo na tela, 60 ciclos no backend e 14 ms da tecla à tela. Diga que foi uma **emulação**, não uma máquina antiga de verdade; os detalhes estão em `docs/testes/desempenho/`.

## Parte 3: demonstração ao vivo (2 min 30 s)

**30.** Deixe tudo pronto **antes** de começar: o JAR baixado e o navegador fechado. Se o JAR falhar, use a versão online (na rede da UFLA, o endereço alternativo do README).

1. Abra o JAR: o navegador abre sozinho. (15 s) No Windows, tente o duplo clique. **No Ubuntu, o duplo clique não abre o JAR** (o atalho do Java está quebrado no sistema): abra pelo terminal com `java -jar canteiro.jar`. Teste antes, no computador da apresentação.
2. Nova partida no **Normal**: mostre o índice e o anel da prévia. (30 s)
3. Empilhe tudo de um lado: o painel avisa, aparece o X vermelho e a obra desaba. (45 s)
4. Se aparecer aço, mostre que ele firma os blocos de baixo. (15 s)
5. Termine a partida, salve no ranking e abra o relatório. (30 s)
6. Diga o que não está pronto, apontando a coluna da direita: o duplo clique no Ubuntu, o duplo clique no Windows e no macOS em máquina real e o desempenho numa máquina antiga de verdade. (15 s)

## Conclusão (1 min)

**31. Opinião (50 s).** É do grupo e tem que ser sincera, como o professor pediu. Combinem antes.

**32. Obrigado (10 s).** Convide a turma a jogar: "quem quiser, aponte a câmera para o QR code". Deixe esse slide na tela durante as perguntas. O QR code grande abre o endereço principal; o pequeno abre o endereço alternativo, para quem estiver no Wi-Fi da UFLA, que bloqueia o principal.

---

## Antes de entregar

- [ ] Preencher as caixas amarelas dos slides 8, 29 e 31 e gerar o PDF de novo (veja o [README](README.md) desta pasta)
- [ ] Ensaiar com cronômetro, pelo menos dois dias antes, conferindo os três marcos
- [ ] Testar o JAR no computador da apresentação
- [ ] Enviar o PDF no Campus Virtual até **26/11, 12:00**
