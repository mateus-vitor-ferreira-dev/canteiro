import { describe, expect, it, vi } from "vitest";
import { tocarNotas } from "./sintetizador";

/** Um contexto de áudio falso, que só conta o que foi criado e iniciado. */
function contextoFalso() {
    const parametro = () => ({
        setValueAtTime: vi.fn(),
        exponentialRampToValueAtTime: vi.fn(),
    });
    const no = () => ({ connect: vi.fn() });
    const fonte = () => ({ ...no(), start: vi.fn(), stop: vi.fn() });
    const criados = {
        osciladores: [] as ReturnType<typeof fonte>[],
        ruidos: [] as ReturnType<typeof fonte>[],
    };
    const contexto = {
        currentTime: 10,
        sampleRate: 8000,
        createGain: () => ({ ...no(), gain: parametro() }),
        createOscillator: () => {
            const o = { ...fonte(), type: "", frequency: parametro() };
            criados.osciladores.push(o);
            return o;
        },
        createBufferSource: () => {
            const r = { ...fonte(), buffer: null };
            criados.ruidos.push(r);
            return r;
        },
        createBuffer: (_canais: number, tamanho: number) => {
            const dados = new Float32Array(tamanho);
            return { getChannelData: () => dados };
        },
        createBiquadFilter: () => ({ ...no(), type: "", frequency: parametro() }),
    };
    return { contexto: contexto as unknown as BaseAudioContext, criados };
}

describe("tocarNotas", () => {
    it("cria um oscilador por tom e uma fonte por ruído, começando a partir de agora", () => {
        const { contexto, criados } = contextoFalso();
        tocarNotas(contexto, { connect: vi.fn() } as unknown as AudioNode, [
            { onda: "triangle", inicio: 0, duracao: 0.1, frequencia: 100, ganho: 0.5 },
            {
                onda: "ruido",
                inicio: 0.2,
                duracao: 0.1,
                frequencia: 1000,
                frequenciaFinal: 200,
                ganho: 0.3,
            },
        ]);
        expect(criados.osciladores).toHaveLength(1);
        expect(criados.ruidos).toHaveLength(1);
        expect(criados.osciladores[0]?.start).toHaveBeenCalledWith(10);
        expect(criados.ruidos[0]?.start).toHaveBeenCalledWith(10.2);
    });
});
