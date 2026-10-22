# Imagem do CANTEIRO para hospedagem pública. Roda em qualquer serviço que
# aceite Docker; o jogo local continua sendo o JAR com dois cliques.
#
#   docker build -t canteiro .
#   docker run -p 7070:7070 canteiro      → http://localhost:7070

# 1. Build: o Maven compila o backend e, pelo frontend-maven-plugin, baixa um
#    Node próprio e compila o frontend para dentro do JAR (RNF16).
FROM eclipse-temurin:17-jdk AS build
WORKDIR /canteiro
COPY backend/.mvn backend/.mvn
COPY backend/mvnw backend/pom.xml backend/
COPY frontend frontend
COPY backend/src backend/src
RUN cd backend && ./mvnw -B -ntp package -DskipTests

# 2. Execução: só o Java e o JAR, com um usuário sem privilégios.
FROM eclipse-temurin:17-jre
RUN useradd --system --no-create-home canteiro \
    && mkdir -p /app/dados && chown canteiro /app/dados
COPY --from=build /canteiro/backend/target/canteiro.jar /app/canteiro.jar
USER canteiro

# Escuta em todas as interfaces do contêiner, sem tentar abrir navegador.
# A hospedagem costuma definir PORT; o limite protege a máquina, porque cada
# partida tem um laço de 60 ciclos por segundo numa thread própria. O usuário
# do contêiner não tem pasta pessoal, então os arquivos do jogador (ranking,
# configurações) ficam em /app/dados.
ENV CANTEIRO_HOST=0.0.0.0 \
    PORT=7070 \
    CANTEIRO_ABRIR_NAVEGADOR=false \
    CANTEIRO_LIMITE_PARTIDAS=10 \
    CANTEIRO_PASTA=/app/dados
EXPOSE 7070

ENTRYPOINT ["java", "-XX:MaxRAMPercentage=75", "-XX:+UseSerialGC", "-Djava.awt.headless=true", "-jar", "/app/canteiro.jar"]
