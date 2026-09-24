package canteiro.controle;

import canteiro.modelo.Dificuldade;
import canteiro.modelo.MotorJogo;
import canteiro.modelo.estruturas.FontePecas;

import java.security.SecureRandom;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

/**
 * As partidas em andamento, numa tabela hash indexada pelo id.
 *
 * <p>O id vem na URL do WebSocket, então a busca precisa ser O(1) e segura
 * entre threads: é um {@link ConcurrentHashMap}.</p>
 *
 * @author Mateus Vitor Ferreira
 * @version 0.1.0
 */
public final class GerenciadorPartidas {

    private static final String ALFABETO = "abcdefghijklmnopqrstuvwxyz0123456789";
    private static final int TAMANHO_ID = 8;

    private final Map<String, SessaoPartida> sessoes = new ConcurrentHashMap<>();
    private final Function<Dificuldade, FontePecas> fabricaDeFontes;
    private final SecureRandom sorteio = new SecureRandom();

    /**
     * Cria o gerenciador.
     *
     * @param fabricaDeFontes cria a fonte de peças de cada partida nova
     */
    public GerenciadorPartidas(Function<Dificuldade, FontePecas> fabricaDeFontes) {
        this.fabricaDeFontes = Objects.requireNonNull(fabricaDeFontes, "fábrica de fontes");
    }

    /**
     * Cria uma partida, ainda parada: ela começa quando o WebSocket conecta.
     *
     * @param dificuldade dificuldade escolhida
     * @return a sessão criada
     */
    public SessaoPartida criar(Dificuldade dificuldade) {
        MotorJogo motor = new MotorJogo(dificuldade, fabricaDeFontes.apply(dificuldade));
        String id;
        SessaoPartida sessao;
        do {
            id = novoId();
            sessao = new SessaoPartida(id, motor);
        } while (sessoes.putIfAbsent(id, sessao) != null);
        return sessao;
    }

    /**
     * Busca uma partida pelo id, em O(1).
     *
     * @param id id da partida
     * @return a sessão, se existir
     */
    public Optional<SessaoPartida> buscar(String id) {
        return Optional.ofNullable(sessoes.get(id));
    }

    /**
     * Encerra e esquece uma partida.
     *
     * @param id id da partida
     */
    public void remover(String id) {
        SessaoPartida sessao = sessoes.remove(id);
        if (sessao != null) {
            sessao.encerrar();
        }
    }

    /**
     * Encerra todas as partidas, ao desligar o servidor.
     */
    public void encerrarTodas() {
        sessoes.keySet().forEach(this::remover);
    }

    /**
     * Quantas partidas estão abertas.
     *
     * @return número de sessões
     */
    public int quantidade() {
        return sessoes.size();
    }

    private String novoId() {
        StringBuilder id = new StringBuilder(TAMANHO_ID);
        for (int i = 0; i < TAMANHO_ID; i++) {
            id.append(ALFABETO.charAt(sorteio.nextInt(ALFABETO.length())));
        }
        return id.toString();
    }
}
