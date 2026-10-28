import { afterEach, beforeEach, describe, expect, it, vi } from "vitest";
import type { Nota } from "@/sons/receitas";
import { duracaoDoCompasso } from "@/sons/musica";
import { Musica } from "./musica.svelte";

type Agendado = { notas: readonly Nota[]; volume: number; quando: number | undefined };

/** Uma música com relógio e tocador falsos: o relógio anda quando o teste manda. */
function montar(ligado = () => true) {
    const agendados: Agendado[] = [];
    let agora = 10;
    const musica = new Musica(
        ligado,
        (notas, volume, quando) => void agendados.push({ notas, volume, quando }),
        () => agora,
        () => 0.5,
    );
    return {
        musica,
        agendados,
        /** Anda o relógio em passos de 100 ms, como o de verdade, acordando o agendador em cada um. */
        avancar(segundos: number) {
            for (let passo = 0; passo < Math.round(segundos * 10); passo++) {
                agora += 0.1;
                vi.advanceTimersByTime(100);
            }
        },
    };
}

describe("Musica", () => {
    beforeEach(() => vi.useFakeTimers());
    afterEach(() => vi.useRealTimers());

    it("emenda cada compasso no fim do anterior, no relógio do áudio", () => {
        const { musica, agendados, avancar } = montar();
        musica.tocar();
        avancar(10);

        const inicios = agendados.map((a) => a.quando ?? 0);
        expect(inicios[0]).toBeCloseTo(10.05);
        for (let k = 1; k < inicios.length; k++) {
            expect((inicios[k] ?? 0) - (inicios[k - 1] ?? 0)).toBeCloseTo(duracaoDoCompasso(0));
        }
        expect(agendados.length).toBeGreaterThan(3);
        expect(agendados.every((a) => a.volume === 0.5)).toBe(true);
        musica.parar();
    });

    it("a intensidade nova vale no próximo compasso, que fica mais curto", () => {
        const { musica, agendados, avancar } = montar();
        musica.tocar(); // o primeiro compasso sai na hora, ainda tranquilo
        musica.intensidade = 1;
        avancar(5);
        const [primeiro, segundo, terceiro] = agendados.map((a) => a.quando ?? 0);
        expect((segundo ?? 0) - (primeiro ?? 0)).toBeCloseTo(duracaoDoCompasso(0), 5);
        expect((terceiro ?? 0) - (segundo ?? 0)).toBeCloseTo(duracaoDoCompasso(1), 5);
        musica.parar();
    });

    it("com o som desligado, não agenda nada", () => {
        const { musica, agendados, avancar } = montar(() => false);
        musica.tocar();
        avancar(5);
        expect(agendados).toHaveLength(0);
        musica.parar();
    });

    it("parada, não agenda mais; de volta, recomeça a partir de agora", () => {
        const { musica, agendados, avancar } = montar();
        musica.tocar();
        avancar(1);
        musica.parar();
        expect(musica.tocando).toBe(false);
        const antes = agendados.length;
        avancar(30);
        expect(agendados).toHaveLength(antes);

        musica.tocar();
        expect(agendados.at(-1)?.quando).toBeCloseTo(41.05);
        musica.parar();
    });
});
