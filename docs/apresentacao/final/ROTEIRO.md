# Roteiro da apresentação final

Slides: [`../Apresentacao_Final_Canteiro.pdf`](../Apresentacao_Final_Canteiro.pdf), 28 slides. Quem apresenta: Marcelo.

A tarefa pede de **15 a 20 minutos**: documentação (3 a 5 min), codificação (10 a 12 min), o jogo funcionando (2 a 3 min) e a opinião sobre a disciplina. Este roteiro fecha em **18 minutos**: sobram 2 de folga para nervosismo ou pergunta no meio, e ainda passa dos 15 se a fala correr.

> **O que o professor avalia:** clareza, profundidade, familiaridade com o tema, tempo, relação com a disciplina e domínio do conteúdo. Os slides já mostram o código; a fala explica **por que** ele é assim. Não leia o código linha a linha: aponte a linha que importa e diga o que ela resolve.

## Tempos

| Slide | Assunto | Tempo | Acumulado |
|---|---|---|---|
| 1 | Capa | 0:15 | 0:15 |
| 2 | O assunto | 0:30 | 0:45 |
| 3 | O que o sistema faz | 0:50 | 1:35 |
| 4 | Objetivos | 0:40 | 2:15 |
| 5 | Requisitos | 0:45 | 3:00 |
| 6 | Bibliotecas e referências | 0:40 | **3:40** |
| 7 | Parte 2 | 0:05 | 3:45 |
| 8 | Arquitetura | 0:50 | 4:35 |
| 9 | Protocolo | 0:35 | 5:10 |
| 10 | Herança | 0:40 | 5:50 |
| 11 | Polimorfismo | 0:40 | 6:30 |
| 12 | Fila e sacola | 0:40 | 7:10 |
| 13 | Pilha da reserva | 0:25 | 7:35 |
| 14 | Centro de massa O(1) | 0:50 | 8:25 |
| 15 | Teste da otimização | 0:35 | 9:00 |
| 16 | Colapso recursivo | 0:40 | 9:40 |
| 17 | Problema: física local | 0:30 | 10:10 |
| 18 | Balanço | 0:35 | 10:45 |
| 19 | Esmagamento | 0:30 | 11:15 |
| 20 | Uma thread só | 0:40 | 11:55 |
| 21 | Repetição | 0:35 | 12:30 |
| 22 | Exceções e arquivos | 0:25 | 12:55 |
| 23 | A tela | 0:25 | 13:20 |
| 24 | Outros problemas | 0:40 | 14:00 |
| 25 | Testes e números | 0:25 | **14:25** |
| 26 | Demonstração ao vivo | 2:30 | **16:55** |
| 27 | Opinião sobre a disciplina | 0:50 | 17:45 |
| 28 | Obrigado | 0:10 | **17:55** |

**Marcos para olhar o relógio:** slide 7 em **3:45**, slide 17 em **10:10**, slide 26 em **14:25**. Atrasou mais de um minuto? Pule para o próximo marco e corte os slides 13, 22 e 23 (são os mais fáceis de resumir numa frase).

---

## Parte 1: documentação (3 min 40 s)

**1. Capa (15 s).** "Somos o grupo do CANTEIRO: Marcelo, Mateus e Alisson."

**2. O assunto (30 s).** Faça a pergunta do slide e espere um segundo. Jogos de encaixe só pensam em espaço; no nosso, cada peça é um material de construção, e a pilha precisa ficar equilibrada.

**3. O que o sistema faz (50 s).** Aponte no print: o índice de estabilidade, a linha tracejada (eixo da base) e o X vermelho. Explique a regra central: a cada peça, o jogo calcula o centro de massa e compara com o eixo; passou do limite, a obra desaba. Feche com a frase da faixa.

**4. Objetivos (40 s).** Não leia os oito cartões. O objetivo era um jogo em que **cada conceito da disciplina resolve um problema real**, e todos eles vão aparecer no código daqui a pouco.

**5. Requisitos (45 s).** 22 regras, 31 funcionais (todos prontos) e 16 não funcionais. Os da tabela são o coração do jogo. Os dois pendentes são medições em máquina real, que vocês vão citar na demonstração.

**6. Bibliotecas e referências (40 s).** As regras são Java puro. O Javalin só serve HTTP e WebSocket; o Svelte só desenha. Cite as referências.

## Parte 2: codificação (10 min 45 s)

**7. Parte 2 (5 s).** Só passe: "Agora, como foi feito."

**8. Arquitetura (50 s).** O navegador manda teclas e recebe o estado. No backend: `api` → `controle` → `modelo`. O modelo, onde estão todas as regras, não conhece o servidor, e um teste reprova o build se alguém importar Javalin ou JSON nele. **Por quê:** dá para testar todas as regras sem abrir servidor nem navegador.

**9. Protocolo (35 s).** O backend manda o estado inteiro, não "a peça desceu". Parece desperdício, mas cabe em menos de 4 KB e, se uma mensagem se perder, a próxima corrige tudo.

**10. Herança (40 s).** `Peca` é abstrata e guarda o que é comum; a `PecaT` só diz o seu desenho. A peça **tem** um material, não **é** de um material. Por isso são 11 classes em vez de 28.

**11. Polimorfismo (40 s).** Aponte a linha `material.aoFixar(this, celulas)`: o tabuleiro chama sem saber qual material é. O aço responde firmando os blocos de baixo; os outros não fazem nada. Nenhum `switch` por tipo.

**12. Fila e sacola (40 s).** A sacola embaralha as 7 formas e só repete quando todas saíram, então nunca falta uma forma por muito tempo. A fila guarda as 3 próximas. A semente faz o sorteio ser reproduzível; isso volta no slide 21.

**13. Pilha da reserva (25 s).** Guardar e pegar operam sempre no último: pilha. A trava impede trocar duas vezes a mesma peça.

**14. Centro de massa (50 s).** É o slide mais importante. O jeito ingênuo varre as 200 posições a cada ciclo. O nosso guarda dois números, massa total e momento; fixar um bloco soma, remover subtrai, e o centro é a divisão. Custo constante, não importa a altura da pilha. Detalhe que mostra domínio: eliminar uma linha não muda o momento de quem desce, porque o bloco continua na mesma coluna.

**15. Teste da otimização (35 s).** Uma conta rápida errada gera colapsos que parecem injustos. O teste joga 500 peças ao acaso e, a cada uma, compara a conta rápida com a varredura completa.

**16. Colapso (40 s).** Acha a linha mais pesada; os blocos acima caem. Aponte o `return cair(t, abaixo)`: a função chama a si mesma até achar apoio, e o caso base é o chão ou um bloco. O bloco firmado pelo aço não cai: é o polimorfismo do slide 11.

**17. Problema: física local (30 s).** Conte como uma história: "Quando jogamos, o CANTEIRO parecia um encaixe comum. As linhas quase cheias puxam o centro para o meio, e quase nunca desabava." A solução foram três regras novas.

**18. Balanço (35 s).** A peça olha o que tem embaixo de cada coluna: chão, bloco ou nada. Se ela for mais densa que o apoio mais forte, a parte sem nada embaixo cai, reaproveitando o mesmo `cair` recursivo. Concreto sobre madeira perde a ponta.

**19. Esmagamento (30 s).** Cada bloco carrega o peso dos blocos colados acima. Passou do limite do material, ele é esmagado; o laço repete até ninguém mais ceder. Aponte as rachaduras no print.

**20. Uma thread só (40 s).** A tecla chega numa thread e o jogo roda em outra. Em vez de trava, uma fila concorrente: o WebSocket só enfileira, o laço esvazia a fila no começo de cada ciclo. Sem condição de corrida.

**21. Repetição (35 s).** Consequência do slide anterior: o jogo é determinístico. A repetição grava só a semente e os comandos com o ciclo; reproduzir é refazer os comandos no mesmo ciclo. O desfazer do modo treino usa o mesmo truque.

**22. Exceções e arquivos (25 s).** Não há banco: são arquivos de texto. Arquivo ausente ou estragado vira o padrão que vem dentro do JAR, e o jogo sempre abre.

**23. A tela (25 s).** O navegador não tem regra: desenha o último estado a cada quadro. Em máquina lenta, cai o desenho, nunca o ritmo do jogo.

**24. Outros problemas (40 s).** Uma frase por cartão. No bug do fim de jogo, conte a causa: a prévia tentava fixar a peça que nasce em cima da pilha, lançava exceção, e a tela nunca recebia o fim de jogo.

**25. Testes e números (25 s).** 406 testes no backend e 234 no frontend, rodando em cada PR. 60 ciclos por segundo e 13 ms da tecla à tela. Se a #27 e a #30 tiverem fechado, diga o resultado.

## Parte 3: demonstração ao vivo (2 min 30 s)

**26.** Deixe tudo pronto **antes** de começar: o JAR baixado e o navegador fechado. Se o JAR falhar, use a versão online (na rede da UFLA, o endereço alternativo do README).

1. Dois cliques no JAR: o navegador abre sozinho. (15 s)
2. Nova partida no **Normal**. Mostre o índice e o anel que mostra onde o centro de massa vai parar. (30 s)
3. Empilhe tudo de um lado: o painel avisa, aparece o X vermelho e a obra desaba. (45 s)
4. Se aparecer aço, mostre que ele firma os blocos de baixo. (15 s)
5. Termine a partida, salve no ranking e abra o relatório. (30 s)
6. Diga o que não está pronto, apontando a coluna da direita do slide. (15 s)

## Conclusão (1 min)

**27. Opinião (50 s).** É do grupo e tem que ser sincera, como o professor pediu. Combinem antes.

**28. Obrigado (10 s).** Deixe o link do jogo na tela durante as perguntas.

---

## Antes de entregar

- [ ] Preencher as caixas amarelas dos slides 6, 25 e 27 e gerar o PDF de novo (veja o [README](README.md) desta pasta)
- [ ] Ensaiar com cronômetro, pelo menos dois dias antes, conferindo os três marcos
- [ ] Testar o JAR no computador da apresentação
- [ ] Enviar o PDF no Campus Virtual até **26/11, 12:00**
