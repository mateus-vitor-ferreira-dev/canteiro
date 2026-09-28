import type { Nota } from "./receitas";

/** Tempo para o som chegar ao pico: curto, para não estalar. */
const ATAQUE = 0.005;
/** O Web Audio não aceita rampa exponencial até zero: este é o "quase silêncio". */
const SILENCIO = 0.0001;
/** Folga depois do fim da nota antes de desligar a fonte. */
const FOLGA = 0.05;

/**
 * Toca as notas no contexto de áudio, a partir de agora. Cada nota ganha um
 * envelope (sobe rápido, some aos poucos); o ruído passa por um filtro cujo
 * corte desliza, para soar como entulho em vez de chiado de rádio.
 *
 * @param contexto contexto de áudio do navegador
 * @param destino para onde vai o som (o controle de volume)
 * @param notas o que tocar
 */
export function tocarNotas(
    contexto: BaseAudioContext,
    destino: AudioNode,
    notas: readonly Nota[],
): void {
    const agora = contexto.currentTime;
    for (const nota of notas) {
        const inicio = agora + nota.inicio;
        const fim = inicio + nota.duracao;
        const envelope = contexto.createGain();
        envelope.gain.setValueAtTime(SILENCIO, inicio);
        envelope.gain.exponentialRampToValueAtTime(nota.ganho, inicio + ATAQUE);
        envelope.gain.exponentialRampToValueAtTime(SILENCIO, fim);
        envelope.connect(destino);

        const fonte =
            nota.onda === "ruido"
                ? ruido(contexto, nota, envelope, inicio, fim)
                : tom(contexto, nota, envelope, inicio, fim);
        fonte.start(inicio);
        fonte.stop(fim + FOLGA);
    }
}

function tom(
    contexto: BaseAudioContext,
    nota: Nota,
    saida: AudioNode,
    inicio: number,
    fim: number,
): AudioScheduledSourceNode {
    const oscilador = contexto.createOscillator();
    oscilador.type = nota.onda === "ruido" ? "sine" : nota.onda;
    deslizar(oscilador.frequency, nota, inicio, fim);
    oscilador.connect(saida);
    return oscilador;
}

function ruido(
    contexto: BaseAudioContext,
    nota: Nota,
    saida: AudioNode,
    inicio: number,
    fim: number,
): AudioScheduledSourceNode {
    const amostras = Math.ceil(contexto.sampleRate * (nota.duracao + FOLGA));
    const buffer = contexto.createBuffer(1, amostras, contexto.sampleRate);
    const dados = buffer.getChannelData(0);
    for (let i = 0; i < amostras; i++) {
        dados[i] = Math.random() * 2 - 1;
    }
    const fonte = contexto.createBufferSource();
    fonte.buffer = buffer;
    const filtro = contexto.createBiquadFilter();
    filtro.type = "lowpass";
    deslizar(filtro.frequency, nota, inicio, fim);
    fonte.connect(filtro);
    filtro.connect(saida);
    return fonte;
}

function deslizar(parametro: AudioParam, nota: Nota, inicio: number, fim: number): void {
    parametro.setValueAtTime(nota.frequencia, inicio);
    if (nota.frequenciaFinal !== undefined) {
        parametro.exponentialRampToValueAtTime(nota.frequenciaFinal, fim);
    }
}
