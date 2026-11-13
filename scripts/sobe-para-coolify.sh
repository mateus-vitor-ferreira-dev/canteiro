#!/usr/bin/env bash
#
# Pede o deploy ao Coolify, espera o desfecho e, quando falha, diz de quem é a
# falha.
#
# - Espera o desfecho, e não só o aceite: o `POST /deploy` volta assim que o
#   deploy entra na fila, e sair dali daria verde com o build ainda por quebrar.
# - Separa "o Coolify não atendeu" de "o build quebrou": os dois saem vermelhos,
#   mas um pede reexecutar o job e o outro pede corrigir o código.
#
# O Coolify faz rolling update: o contêiner antigo só sai depois que o novo
# passa no healthcheck (`/api/dificuldades`). Por isso `finished` já quer dizer
# "subiu e respondeu", mas o CI ainda confere de fora, no passo seguinte, que o
# jogo atende pelo domínio.
#
# Variáveis:
#   COOLIFY_URL       painel do Coolify          (ex.: https://coolify.digitalcodexa.com)
#   COOLIFY_TOKEN     token da API do Coolify    (secret do repositório)
#   COOLIFY_APP_UUID  uuid da aplicação no Coolify
#   DEPLOY_TIMEOUT    segundos até desistir      (padrão: 840)
#   DEPLOY_INTERVALO  segundos entre consultas   (padrão: 10)

set -euo pipefail

: "${COOLIFY_URL:?COOLIFY_URL não definido}"
: "${COOLIFY_TOKEN:?COOLIFY_TOKEN não definido}"
: "${COOLIFY_APP_UUID:?COOLIFY_APP_UUID não definido}"

TIMEOUT="${DEPLOY_TIMEOUT:-840}"
INTERVALO="${DEPLOY_INTERVALO:-10}"
RESUMO="${GITHUB_STEP_SUMMARY:-/dev/null}"
API="${COOLIFY_URL%/}/api/v1"

chama() {
  curl -sS --max-time 30 -H "Authorization: Bearer $COOLIFY_TOKEN" "$@"
}

plataforma_fora() {
  echo "::error title=Coolify não atendeu::$1 O código não está em causa."
  cat >> "$RESUMO" <<EOF
### 🟡 O Coolify não aceitou o deploy

$1 **Isto não é defeito do código deste commit**: nada foi compilado, nada mudou em produção.

**O que fazer:** conferir se o painel (<${COOLIFY_URL}>) e o VPS estão no ar e se o secret \`COOLIFY_TOKEN\` continua válido; depois **reexecutar este job**.
EOF
  exit 1
}

# ── Pede o deploy ───────────────────────────────────────────────────────────
#
# O Coolify builda o topo do branch configurado na aplicação (`develop`), e não um
# SHA específico. Se dois pushes chegarem colados, ele pode subir o mais novo;
# o passo seguinte confere de fora que o jogo responde.
codigo=0
resposta="$(chama -X POST "$API/deploy?uuid=$COOLIFY_APP_UUID" -w '\n%{http_code}')" || codigo=$?
http="$(tail -n1 <<<"$resposta")"
corpo="$(sed '$d' <<<"$resposta")"

if [ "$codigo" -ne 0 ]; then
  plataforma_fora "O pedido de deploy não chegou ao Coolify (curl saiu com $codigo)."
fi
if [ "$http" != "200" ]; then
  plataforma_fora "O Coolify recusou o pedido de deploy com HTTP $http."
fi

deploy="$(jq -r '.deployments[0].deployment_uuid // empty' <<<"$corpo")"
if [ -z "$deploy" ]; then
  plataforma_fora "O Coolify respondeu sem id de deploy: $(jq -c '.deployments[0].message // .' <<<"$corpo")"
fi
echo "Deploy $deploy na fila do Coolify. Aguardando o desfecho (limite: ${TIMEOUT}s)."

# ── Espera o desfecho ───────────────────────────────────────────────────────
#
# `finished` é o único desfecho que quer dizer "está no ar". `failed` é o build,
# ou o healthcheck reprovando; `cancelled-by-user` é alguém tendo
# parado pelo painel. Nos dois o job fica vermelho.
inicio=$SECONDS
ultimo_status=""

while :; do
  # Falha de rede pontual não derruba o deploy: quem decide é o laço, que
  # ainda tem tempo de sobra no orçamento.
  json="$(chama "$API/deployments/$deploy" 2>/dev/null || echo '{}')"
  status="$(jq -r '.status // empty' <<<"$json" 2>/dev/null || true)"

  if [ -n "$status" ] && [ "$status" != "$ultimo_status" ]; then
    echo "Deploy $deploy: $status"
    ultimo_status="$status"
  fi

  case "$status" in
    finished)
      echo "Deploy $deploy concluído (commit $(jq -r '.commit // "?"' <<<"$json" | cut -c1-7))."
      exit 0
      ;;
    failed | cancelled-by-user)
      echo "::error title=Build quebrou::O Coolify aceitou o deploy e ele terminou como $status. O erro é nosso."
      # Só as linhas não ocultas: as ocultas são os comandos internos do
      # Coolify, e é nelas que variável de ambiente poderia aparecer.
      echo "Últimas linhas do log do deploy:"
      jq -r '.logs // "[]" | fromjson | map(select(.hidden == false) | .output) | .[-80:] | .[]' <<<"$json" 2>/dev/null ||
        echo "(não foi possível ler o log; veja o deploy no painel do Coolify)"
      cat >> "$RESUMO" <<EOF
### 🔴 O deploy falhou no Coolify

O Coolify aceitou o deploy \`$deploy\` e ele terminou como \`$status\`. **O erro está no código ou na configuração deste commit**: o log do build está no passo acima.

Como é rolling update, o contêiner anterior continua servindo: produção **não** recebeu esta mudança.
EOF
      exit 1
      ;;
  esac

  if [ $((SECONDS - inicio)) -ge "$TIMEOUT" ]; then
    # Falhar por tempo é melhor do que ficar pendurado: um build travado não
    # pode segurar o runner para sempre.
    echo "::error title=Deploy não terminou::O deploy $deploy não terminou em ${TIMEOUT}s (último status: ${ultimo_status:-desconhecido})."
    cat >> "$RESUMO" <<EOF
### 🟡 O deploy não terminou em ${TIMEOUT}s

O deploy pode ainda terminar. Abra o deploy \`$deploy\` no painel do Coolify antes de concluir qualquer coisa; se ele estiver travado, cancele por lá e **reexecute este job**.
EOF
    exit 1
  fi

  sleep "$INTERVALO"
done
