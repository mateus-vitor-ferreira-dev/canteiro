package canteiro.api.rotas;

import canteiro.api.dto.ConfiguracoesDto;
import canteiro.persistencia.ConfiguracoesSalvas;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Confere as configurações que chegam no {@code PUT /api/configuracoes}
 * antes de gravar (RF27, RNF12, RNF14).
 *
 * <p>As ações e as teclas padrão são as da tabela {@code TECLAS} de
 * {@code frontend/src/estado/teclado.ts}. Uma tecla repetida é procurada na
 * tabela completa, com as padrão das ações que não vieram: trocar
 * {@code ESQUERDA} para {@code KeyC} sem trocar {@code RESERVAR} deixaria a
 * reserva sem tecla, e é recusado.</p>
 *
 * @author Mateus Vitor Ferreira
 * @version 0.1.0
 */
public final class ValidacaoConfiguracoes {

    /** As ações que o jogador pode trocar, com as teclas padrão, iguais às do frontend. */
    static final Map<String, List<String>> TECLAS_PADRAO = Map.of(
            "ESQUERDA", List.of("ArrowLeft"),
            "DIREITA", List.of("ArrowRight"),
            "DESCER", List.of("ArrowDown"),
            "QUEDA_INSTANTANEA", List.of("Space"),
            "GIRAR_HORARIO", List.of("ArrowUp", "KeyX"),
            "GIRAR_ANTI_HORARIO", List.of("KeyZ"),
            "RESERVAR", List.of("KeyC"),
            "DESFAZER", List.of("KeyU"),
            "PAUSA", List.of("KeyP", "Escape"),
            "SOM", List.of("KeyM"));

    private ValidacaoConfiguracoes() {
    }

    /**
     * Procura o primeiro problema nas configurações.
     *
     * @param dto o que chegou no pedido; {@code null} se o corpo veio malformado
     * @return a mensagem para o jogador, ou vazio se estiver tudo certo
     */
    public static Optional<String> problema(ConfiguracoesDto dto) {
        if (dto == null) {
            return Optional.of("Pedido de configurações malformado.");
        }
        if (dto.volume() < ConfiguracoesSalvas.VOLUME_MINIMO || dto.volume() > ConfiguracoesSalvas.VOLUME_MAXIMO) {
            return Optional.of("O volume vai de " + ConfiguracoesSalvas.VOLUME_MINIMO + " a "
                    + ConfiguracoesSalvas.VOLUME_MAXIMO + ".");
        }
        Map<String, List<String>> teclas = dto.teclas() == null ? Map.of() : dto.teclas();
        return problemaNasAcoes(teclas).or(() -> teclaRepetida(teclas));
    }

    private static Optional<String> problemaNasAcoes(Map<String, List<String>> teclas) {
        for (Map.Entry<String, List<String>> troca : teclas.entrySet()) {
            if (!TECLAS_PADRAO.containsKey(troca.getKey())) {
                return Optional.of("A ação " + troca.getKey() + " não existe no jogo.");
            }
            List<String> codigos = troca.getValue();
            if (codigos == null || codigos.isEmpty()) {
                return Optional.of("A ação " + troca.getKey() + " ficou sem nenhuma tecla.");
            }
            if (codigos.stream().anyMatch(codigo -> codigo == null || codigo.isBlank())) {
                return Optional.of("A ação " + troca.getKey() + " tem uma tecla em branco.");
            }
        }
        return Optional.empty();
    }

    /** Monta a tabela completa, com as padrão no lugar das que não vieram, e procura um código em duas ações. */
    private static Optional<String> teclaRepetida(Map<String, List<String>> teclas) {
        Map<String, List<String>> completa = new LinkedHashMap<>(TECLAS_PADRAO);
        completa.putAll(teclas);
        Map<String, String> donoDaTecla = new HashMap<>();
        for (Map.Entry<String, List<String>> acao : completa.entrySet()) {
            for (String codigo : acao.getValue()) {
                String dono = donoDaTecla.putIfAbsent(codigo, acao.getKey());
                if (dono != null && !dono.equals(acao.getKey())) {
                    return Optional.of("A tecla " + codigo + " está em duas ações: " + dono + " e "
                            + acao.getKey() + ".");
                }
            }
        }
        return Optional.empty();
    }
}
