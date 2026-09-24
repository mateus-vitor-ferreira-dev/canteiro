# CANTEIRO — Frontend

A interface do jogo em **Svelte 5 + TypeScript**, rodando no navegador. Ela só desenha o estado que o backend manda e envia as teclas do jogador: **nenhuma regra de jogo mora aqui**.

## Rodando

Precisa do Node.js 20.19+ ou 22.12+ e do backend rodando em `http://127.0.0.1:7070` (`cd ../backend && ./mvnw compile exec:java`).

```bash
npm install     # só na primeira vez, ou quando o package.json mudar
npm run dev     # abre em http://localhost:5173
```

O Vite repassa `/api` e `/ws` para o backend, então para o navegador parece um servidor só.

| Comando          | O que faz                              |
| ---------------- | -------------------------------------- |
| `npm test`       | Testes com Vitest e Testing Library    |
| `npm run check`  | Confere os tipos (TypeScript `strict`) |
| `npm run lint`   | ESLint e Prettier                      |
| `npm run format` | Formata tudo com Prettier              |
| `npm run build`  | Confere os tipos e gera `dist/`        |

**Antes de abrir um PR:** `npm run lint && npm run check && npm test`.

## Onde fica cada coisa

| Pasta              | O que mora aqui                                                                                                              |
| ------------------ | ---------------------------------------------------------------------------------------------------------------------------- |
| `src/api/`         | `cliente.ts` (REST) e `protocolo.ts` (tipos espelhados dos DTOs do backend). **Único lugar que fala com o backend**          |
| `src/telas/`       | Uma pasta por tela, com o componente, o teste e os componentes que só ela usa. Hoje: `NovaPartida/` (escolha da dificuldade) |
| `src/componentes/` | Só o que mais de uma tela usa, como o tabuleiro e o painel de estabilidade                                                   |
| `src/estado/`      | Estado compartilhado em módulos `.svelte.ts`: partida, teclado e navegação                                                   |
| `src/estilos/`     | Cores, fontes e texturas dos materiais                                                                                       |
| `src/util/`        | Funções puras, como formatação de números e conversão de cor                                                                 |

Os testes ficam ao lado do arquivo testado, com o sufixo `.test.ts`. Sons e imagens vão em `public/sons/` e `public/imagens/`.

**Imports:** `@/` aponta para `src/` (`import { listarDificuldades } from "@/api/cliente"`). Caminho relativo só para arquivos da mesma pasta. As convenções completas estão no [README principal](../README.md#-convenções-de-código).
