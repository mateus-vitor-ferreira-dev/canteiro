import { fireEvent, render, screen } from "@testing-library/svelte";
import { describe, expect, it, vi } from "vitest";
import ControlesToque from "./ControlesToque.svelte";

describe("ControlesToque", () => {
    it("cada botão manda o seu comando", async () => {
        const aoComando = vi.fn();
        render(ControlesToque, { aoComando, pausada: false });
        await fireEvent.click(screen.getByRole("button", { name: "Mover para a esquerda" }));
        await fireEvent.click(screen.getByRole("button", { name: "Girar" }));
        await fireEvent.click(screen.getByRole("button", { name: "Queda instantânea" }));
        await fireEvent.click(screen.getByRole("button", { name: "Pausar" }));
        expect(aoComando.mock.calls).toEqual([
            ["ESQUERDA"],
            ["GIRAR_HORARIO"],
            ["QUEDA_INSTANTANEA"],
            ["PAUSAR"],
        ]);
    });

    it("pausada, só o botão de continuar funciona", async () => {
        const aoComando = vi.fn();
        render(ControlesToque, { aoComando, pausada: true });
        expect(screen.getByRole("button", { name: "Girar" })).toBeDisabled();
        await fireEvent.click(screen.getByRole("button", { name: "Continuar" }));
        expect(aoComando).toHaveBeenCalledWith("RETOMAR");
    });
});
