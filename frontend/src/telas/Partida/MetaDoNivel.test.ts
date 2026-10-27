import { render, screen } from "@testing-library/svelte";
import { describe, expect, it } from "vitest";
import MetaDoNivel from "./MetaDoNivel.svelte";

const meta = {
    descricao: "Elimine 4 linhas sem a estabilidade cair abaixo de 50 %",
    progresso: 1,
    alvo: 4,
    cumprida: false,
};

describe("MetaDoNivel", () => {
    it("mostra a meta e o progresso, com a barra acessível", () => {
        render(MetaDoNivel, { meta });
        expect(screen.getByText(meta.descricao)).toBeInTheDocument();
        expect(screen.getByText("1 de 4")).toBeInTheDocument();
        expect(screen.getByRole("progressbar", { name: "Progresso da meta" })).toHaveAttribute(
            "aria-valuenow",
            "1",
        );
    });

    it("avisa quando a meta foi cumprida", () => {
        render(MetaDoNivel, { meta: { ...meta, progresso: 4, cumprida: true } });
        expect(screen.getByText("Cumprida! Bônus garantido")).toBeInTheDocument();
    });
});
