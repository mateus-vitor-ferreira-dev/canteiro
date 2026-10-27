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
        await fireEvent.click(screen.getByRole("button", { name: "Guardar na reserva" }));
        await fireEvent.click(screen.getByRole("button", { name: "Pausar" }));
        expect(aoComando.mock.calls).toEqual([
            ["ESQUERDA"],
            ["GIRAR_HORARIO"],
            ["QUEDA_INSTANTANEA"],
            ["RESERVAR"],
            ["PAUSAR"],
        ]);
    });

    it("o botão de desfazer só existe no modo treino", async () => {
        const aoComando = vi.fn();
        const { unmount } = render(ControlesToque, { aoComando, pausada: false });
        expect(screen.queryByRole("button", { name: "Desfazer a última jogada" })).toBeNull();
        unmount();

        render(ControlesToque, { aoComando, pausada: false, treino: true });
        await fireEvent.click(screen.getByRole("button", { name: "Desfazer a última jogada" }));
        expect(aoComando).toHaveBeenCalledWith("DESFAZER");
    });

    it("pausada, só o botão de continuar funciona", async () => {
        const aoComando = vi.fn();
        render(ControlesToque, { aoComando, pausada: true });
        expect(screen.getByRole("button", { name: "Girar" })).toBeDisabled();
        await fireEvent.click(screen.getByRole("button", { name: "Continuar" }));
        expect(aoComando).toHaveBeenCalledWith("RETOMAR");
    });

    it("segurar mover repete o comando até soltar; o clique do dedo não manda de novo", async () => {
        vi.useFakeTimers();
        const aoComando = vi.fn();
        render(ControlesToque, { aoComando, pausada: false });
        const esquerda = screen.getByRole("button", { name: "Mover para a esquerda" });

        await fireEvent.pointerDown(esquerda, { button: 0 });
        expect(aoComando).toHaveBeenCalledTimes(1);
        vi.advanceTimersByTime(170 + 55 * 3);
        expect(aoComando).toHaveBeenCalledTimes(4);
        await fireEvent.pointerUp(esquerda);
        await fireEvent.click(esquerda, { detail: 1 });
        vi.advanceTimersByTime(1000);
        expect(aoComando).toHaveBeenCalledTimes(4);
        vi.useRealTimers();
    });

    it("a queda instantânea ocupa duas colunas fora do treino", () => {
        render(ControlesToque, { aoComando: vi.fn(), pausada: false });
        expect(screen.getByRole("button", { name: "Queda instantânea" })).toHaveClass("largo");
    });
});
