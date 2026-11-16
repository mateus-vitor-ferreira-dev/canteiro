# Roteiro da apresentação final

Slides: [`../Apresentacao_Final_Canteiro.pdf`](../Apresentacao_Final_Canteiro.pdf), 30 slides. Quem apresenta: Marcelo.

A tarefa pede de **15 a 20 minutos**: documentação (3 a 5 min), codificação (10 a 12 min), o jogo funcionando (2 a 3 min) e a opinião sobre a disciplina. Este roteiro fecha em **17 min 45 s**: sobram 2 minutos de folga, e ainda passa dos 15 se a fala correr.

> **O que o professor avalia:** clareza, profundidade, familiaridade com o tema, tempo, relação com a disciplina e domínio do conteúdo.
>
> **Como apresentar os slides de código (8 a 26).** Todos têm o mesmo formato: à esquerda, o **print do arquivo de verdade**, com os números de linha reais; à direita, **o requisito** e **como foi feito**. As linhas com fundo amarelo são as que você aponta. A fala tem sempre três tempos:
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
| 3 | O que o sistema faz | 0:45 | 1:25 |
| 4 | Objetivos | 0:35 | 2:00 |
| 5 | Mapa dos requisitos | 0:50 | 2:50 |
| 6 | Bibliotecas e referências | 0:35 | **3:25** |
| 7 | Parte 2 | 0:05 | 3:30 |
| 8 | RNF08 · arquitetura | 0:40 | 4:10 |
| 9 | RN04 · sacola e fila | 0:35 | 4:45 |
| 10 | RN05 · reserva em pilha | 0:25 | 5:10 |
| 11 | RF08 · rotação | 0:30 | 5:40 |
| 12 | RF11 · polimorfismo | 0:40 | 6:20 |
| 13 | RF13 · pontuação | 0:30 | 6:50 |
| 14 | RF14 · centro de massa | 0:45 | 7:35 |
| 15 | RN09–RN10 · estabilidade | 0:35 | 8:10 |
| 16 | RNF10 · teste | 0:30 | 8:40 |
| 17 | RN11 · colapso | 0:40 | 9:20 |
| 18 | Problema: física local | 0:30 | 9:50 |
| 19 | RN20 · balanço | 0:30 | 10:20 |
| 20 | RN21 · esmagamento | 0:30 | 10:50 |
| 21 | Bug do fim de jogo | 0:35 | 11:25 |
| 22 | RNF01 · uma thread só | 0:35 | 12:00 |
| 23 | RF24 · repetição | 0:30 | 12:30 |
| 24 | RF21 · ranking | 0:30 | 13:00 |
| 25 | RNF11 · arquivos | 0:25 | 13:25 |
| 26 | RF15–RF16 · painel | 0:30 | 13:55 |
| 27 | Testes e números | 0:20 | **14:15** |
| 28 | Demonstração ao vivo | 2:30 | **16:45** |
| 29 | Opinião sobre a disciplina | 0:50 | 17:35 |
| 30 | Obrigado | 0:10 | **17:45** |

**Marcos para olhar o relógio:** slide 7 em **3:30**, slide 18 em **9:50** e slide 28 em **14:15**. Se estiver mais de um minuto atrasado, resuma numa frase só os slides 10, 13, 25 e 26 e passe direto.

---

## Parte 1: documentação (3 min 25 s)

**1. Capa (15 s).** "Somos o grupo do CANTEIRO: Marcelo, Mateus e Alisson."

**2. O assunto (25 s).** Faça a pergunta do slide e espere um segundo. Jogos de encaixe só pensam em espaço; no nosso, cada peça é um material de construção.

**3. O que o sistema faz (45 s).** Aponte no print do jogo: o índice de estabilidade, a linha tracejada (o eixo da base) e o X vermelho. A cada peça, o jogo calcula o centro de massa e compara com o eixo; passou do limite, a obra desaba.

**4. Objetivos (35 s).** Não leia os oito cartões. O objetivo era um jogo em que **cada conceito da disciplina resolve um problema real**.

**5. Mapa dos requisitos (50 s).** São 22 regras, 31 funcionais (todos prontos) e 16 não funcionais. "Nos próximos slides, cada requisito desta tabela aparece no código, com o número do slide ao lado." Esse slide é a ponte entre a documentação e o código: deixe claro que vocês vão **provar** cada linha da tabela.

**6. Bibliotecas e referências (35 s).** As regras são Java puro. O Javalin só serve HTTP e WebSocket; o Svelte só desenha. Cite as referências.

## Parte 2: codificação (10 min 50 s)

**7. Parte 2 (5 s).** "Agora, como cada requisito foi implementado."

**8. RNF08, arquitetura (40 s).** O navegador manda teclas e recebe o estado; todas as regras ficam no modelo Java. O print é o `ArquiteturaTest`, que lê os `import` de cada pacote e reprova o build se o modelo importar o servidor ou o JSON. **Por quê:** dá para testar as regras sem abrir servidor nem navegador.

**9. RN04, sacola e fila (35 s).** Linha 103: quando a sacola esvazia, entram as 7 formas embaralhadas. Linha 106: tira da frente. Assim nenhuma forma some por muito tempo. As próximas peças ficam numa fila (`ArrayDeque`): a primeira que entra é a primeira que sai.

**10. RN05, reserva (25 s).** É uma pilha: guarda e pega sempre no topo (linhas 50 e 52). A trava da linha 49 impede trocar duas vezes a mesma peça.

**11. RF08, rotação (30 s).** Gira; se colidiu, a linha 85 tenta cada deslocamento da tabela da peça. Se nenhum serve, a linha 90 desfaz o giro.

**12. RF11, polimorfismo (40 s).** Linha 89: o tabuleiro chama `material.aoFixar(...)` sem saber o material. Embaixo, o `Aco` sobrescreve o método e firma os blocos de baixo. É polimorfismo: nenhum `switch` por tipo. Forma e material em duas hierarquias dão 11 classes em vez de 28.

**13. RF13, pontuação (30 s).** Linha 47: base por linhas eliminadas de uma vez. Linha 87: multiplica o bônus do material, o da obra ousada e o da sequência.

**14. RF14 e RNF02, centro de massa (45 s).** É o slide mais importante. O jeito ingênuo varre as 200 posições a cada ciclo. Aqui são dois números: linha 59, fixar soma massa × coluna; linha 71, remover subtrai; linha 107, o centro é uma divisão. Custo constante. Detalhe que mostra domínio: eliminar uma linha não muda o momento de quem desce, porque o bloco continua na mesma coluna.

**15. RN09 e RN10, estabilidade (35 s).** Linha 176: o desvio. Linha 177: o índice cai em linha reta até zero no limite. Linha 138: mede a pilha inteira e a parte de cima, e vale a pior.

**16. RNF10, teste (30 s).** Uma conta rápida errada gera colapso injusto. O teste joga 500 peças e, a cada uma, compara com a varredura completa (linha 47).

**17. RN11, colapso (40 s).** Linha 36: acha a linha mais pesada. Linha 41: bloco firmado pelo aço não cai (o polimorfismo do slide 12). Linha 90: `cair` chama a si mesma até achar apoio, e o caso base é o chão ou um bloco.

**18. Problema: física local (30 s).** Conte como história: "Jogando, o CANTEIRO parecia um encaixe comum. As linhas quase cheias puxam o centro para o meio, e quase nunca desabava." A solução foram três regras novas.

**19. RN20, balanço (30 s).** Linha 53: coluna sem nada embaixo está em balanço. Linha 56: se a peça não for mais densa que o apoio, segura; senão cai, usando o mesmo `cair` do colapso.

**20. RN21, esmagamento (30 s).** Linha 128: a carga é a massa dos blocos colados acima. Linha 85: esmaga um, recalcula, até ninguém mais ceder.

**21. Bug do fim de jogo (35 s).** A pilha chegava ao topo e a tela congelava. Explique a causa (a prévia tentava fixar a peça em cima da pilha e lançava exceção) e aponte a correção na linha 349.

**22. RNF01, uma thread só (35 s).** Linha 47: uma fila concorrente. Linha 143: a thread do WebSocket só enfileira. Linha 198: o laço, 60 vezes por segundo, esvazia a fila. Sem trava e sem condição de corrida.

**23. RF23 e RF24, repetição (30 s).** Como só uma thread mexe no motor, o jogo é determinístico. Linha 87: reproduzir é aplicar cada comando no mesmo ciclo em que chegou.

**24. RF21 e RN15, ranking (30 s).** O navegador manda só o id e o nome. Linha 149: treino é recusado. Linha 162: a pontuação é lida da sessão, no servidor.

**25. RNF11 e RNF12, arquivos (25 s).** Arquivo ausente ou estragado vira o padrão do JAR, com aviso no log (linha 54), e o jogo sempre abre.

**26. RF15 e RF16, painel (30 s).** É o lado do navegador, em Svelte. Ele não calcula nada: recebe a estabilidade e a prévia prontas e decide o aviso.

**27. Testes e números (20 s).** 406 testes no backend e 234 no frontend, em cada PR. 60 ciclos por segundo e 13 ms da tecla à tela.

## Parte 3: demonstração ao vivo (2 min 30 s)

**28.** Deixe tudo pronto **antes** de começar: o JAR baixado e o navegador fechado. Se o JAR falhar, use a versão online (na rede da UFLA, o endereço alternativo do README).

1. Dois cliques no JAR: o navegador abre sozinho. (15 s)
2. Nova partida no **Normal**: mostre o índice e o anel da prévia. (30 s)
3. Empilhe tudo de um lado: o painel avisa, aparece o X vermelho e a obra desaba. (45 s)
4. Se aparecer aço, mostre que ele firma os blocos de baixo. (15 s)
5. Termine a partida, salve no ranking e abra o relatório. (30 s)
6. Diga o que não está pronto, apontando a coluna da direita. (15 s)

## Conclusão (1 min)

**29. Opinião (50 s).** É do grupo e tem que ser sincera, como o professor pediu. Combinem antes.

**30. Obrigado (10 s).** Deixe o link do jogo na tela durante as perguntas.

---

## Antes de entregar

- [ ] Preencher as caixas amarelas dos slides 6, 27 e 29 e gerar o PDF de novo (veja o [README](README.md) desta pasta)
- [ ] Ensaiar com cronômetro, pelo menos dois dias antes, conferindo os três marcos
- [ ] Testar o JAR no computador da apresentação
- [ ] Enviar o PDF no Campus Virtual até **26/11, 12:00**
