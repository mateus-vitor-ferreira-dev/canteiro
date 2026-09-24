import { render, screen } from "@testing-library/svelte";
import { describe, expect, it } from "vitest";
import type { EstabilidadeDto } from "@/api/protocolo";
import PainelEstabilidade from "./PainelEstabilidade.svelte";

function estabilidade(parcial: Partial<EstabilidadeDto> = {}): EstabilidadeDto {
    return {
        indice: 0.68,
        desvio: 0.64,
        limite: 2,
        centroDeMassa: 4.36,
        eixo: 5,
        alerta: false,
        ...parcial,
    };
}

describe("PainelEstabilidade", () => {
    it("mostra o índice em porcentagem, com o medidor acessível", () => {
        render(PainelEstabilidade, { estabilidade: estabilidade() });
        expect(screen.getByText("68 %")).toBeInTheDocument();
        expect(screen.getByRole("meter", { name: "Índice de estabilidade" })).toHaveAttribute(
            "aria-valuenow",
            "68",
        );
        expect(screen.getByText("2,0 colunas")).toBeInTheDocument();
    });

    it("sem alerta, não avisa nada", () => {
        render(PainelEstabilidade, { estabilidade: estabilidade() });
        expect(screen.queryByRole("alert")).toBeNull();
    });

    it("no alerta, diz de que lado está a carga", () => {
        render(PainelEstabilidade, {
            estabilidade: estabilidade({ indice: 0.2, centroDeMassa: 3.4, alerta: true }),
        });
        expect(screen.getByRole("alert")).toHaveTextContent("carga demais à esquerda");
    });

    it("com a carga à direita, avisa a direita", () => {
        render(PainelEstabilidade, {
            estabilidade: estabilidade({ indice: 0.1, centroDeMassa: 6.8, alerta: true }),
        });
        expect(screen.getByRole("alert")).toHaveTextContent("à direita");
    });

    it("o ponteiro fica no meio com o centro no eixo e para no canto além do limite", () => {
        const { container, rerender } = render(PainelEstabilidade, {
            estabilidade: estabilidade({ centroDeMassa: 5 }),
        });
        const ponteiro = () => container.querySelector<HTMLElement>(".ponteiro");
        expect(ponteiro()?.style.left).toBe("50%");

        rerender({ estabilidade: estabilidade({ centroDeMassa: 1, alerta: true, indice: 0 }) });

        expect(ponteiro()?.style.left).toBe("0%");
    });

    it("o índice muda assim que chega o estado novo", () => {
        const { rerender } = render(PainelEstabilidade, {
            estabilidade: estabilidade({ indice: 0.9 }),
        });
        rerender({ estabilidade: estabilidade({ indice: 0.4 }) });
        expect(screen.getByText("40 %")).toBeInTheDocument();
    });
});
