"""Sobe o canteiro.jar, mede quanto tempo ele leva para responder e confere
a tela e a API. Roda igual no Linux, no Windows e no macOS (RF29, RNF04).

Uso: python testar_jar.py caminho/do/canteiro.jar
"""

import json
import subprocess
import sys
import time
import urllib.request

ENDERECO = "http://127.0.0.1:7070"
LIMITE_SEGUNDOS = 3.0
ESPERA_MAXIMA = 30.0


def pedir(caminho, dados=None):
    corpo = None if dados is None else json.dumps(dados).encode()
    pedido = urllib.request.Request(ENDERECO + caminho, data=corpo, headers={"Content-Type": "application/json"})
    with urllib.request.urlopen(pedido, timeout=2) as resposta:
        return resposta.read().decode("utf-8")


def main():
    jar = sys.argv[1]
    inicio = time.monotonic()
    processo = subprocess.Popen(["java", "-Djava.awt.headless=true", "-jar", jar])
    try:
        while True:
            if processo.poll() is not None:
                sys.exit(f"o JAR fechou sozinho, com código {processo.returncode}")
            try:
                pagina = pedir("/")
                break
            except OSError:
                if time.monotonic() - inicio > ESPERA_MAXIMA:
                    sys.exit(f"o JAR não respondeu em {ESPERA_MAXIMA:.0f} s")
                time.sleep(0.05)
        tempo = time.monotonic() - inicio
        print(f"respondeu em {tempo * 1000:.0f} ms")

        assert "<title>CANTEIRO" in pagina, "a página inicial não é a do jogo"
        dificuldades = json.loads(pedir("/api/dificuldades"))
        assert [d["codigo"] for d in dificuldades] == ["FACIL", "NORMAL", "DIFICIL"], dificuldades
        partida = json.loads(pedir("/api/partidas", {"dificuldade": "NORMAL"}))
        assert len(partida["id"]) == 8, partida
        print("tela, dificuldades e criação de partida ok")

        if tempo > LIMITE_SEGUNDOS:
            sys.exit(f"abriu em {tempo:.1f} s; o limite é {LIMITE_SEGUNDOS:.0f} s")
    finally:
        processo.terminate()
        processo.wait(timeout=10)


if __name__ == "__main__":
    main()
