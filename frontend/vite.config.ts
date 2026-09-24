import { svelte } from "@sveltejs/vite-plugin-svelte";
import { svelteTesting } from "@testing-library/svelte/vite";
import { defineConfig } from "vitest/config";

/** Endereço do backend: o mesmo em que o Javalin escuta (RNF13). */
const BACKEND = "http://127.0.0.1:7070";

export default defineConfig({
    // svelteTesting só age dentro do Vitest: carrega o Svelte do navegador e limpa a tela entre os testes
    plugins: [svelte(), svelteTesting()],
    // "@/" aponta para src/: import { listarDificuldades } from "@/api/cliente"
    resolve: {
        alias: { "@": "/src" },
    },
    server: {
        port: 5173,
        strictPort: true,
        // Para o navegador, frontend e backend parecem um servidor só.
        proxy: {
            "/api": BACKEND,
            "/ws": { target: BACKEND, ws: true },
        },
    },
    build: {
        outDir: "dist",
        emptyOutDir: true,
    },
    test: {
        environment: "jsdom",
        setupFiles: ["./vitest-setup.ts"],
        include: ["src/**/*.test.ts"],
    },
});
