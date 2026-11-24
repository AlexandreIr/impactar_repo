# syntax=docker/dockerfile:1
# =====================================================================
# impactar-api — imagem para o Render (Web Service / Runtime: Docker)
# Root Directory no Render: backend
# =====================================================================

# ---------- build ----------
FROM eclipse-temurin:21-jdk-alpine AS build
WORKDIR /app

# Dependências primeiro: camada reaproveitada enquanto o pom.xml não muda
COPY .mvn/ .mvn/
COPY mvnw pom.xml ./
RUN sed -i 's/\r$//' mvnw && chmod +x mvnw \
    && ./mvnw -B -q dependency:go-offline

COPY src/ src/
# Testes rodam no seu ambiente/CI; o build da imagem só empacota
RUN ./mvnw -B -q package -DskipTests \
    && cp target/*.jar app.jar

# ---------- runtime ----------
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

RUN addgroup -S app && adduser -S app -G app
COPY --from=build /app/app.jar app.jar
USER app

# Plano free do Render tem 512 MB: heap limitado e GC serial (menor uso de memória)
ENV JAVA_OPTS="-XX:MaxRAMPercentage=70 -XX:+UseSerialGC -XX:TieredStopAtLevel=1 -Xss512k -Djava.awt.headless=true"

EXPOSE 8080
# O Render informa a porta em $PORT
ENTRYPOINT ["sh", "-c", "exec java $JAVA_OPTS -Dserver.port=${PORT:-8080} -jar app.jar"]
