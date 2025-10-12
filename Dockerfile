# ==== Runtime (simples) ====
# JRE 17 enxuto
FROM eclipse-temurin:17-jre-jammy

# pasta de trabalho
WORKDIR /app

# copia o jar gerado pelo Maven
# (assume que você já rodou mvn package)
COPY target/*.jar app.jar

# porta interna padronizada dos serviços
# (deixe seu Spring com server.port=8080)
EXPOSE 8080

# permite passar flags de JVM ou propriedades do Spring via ENV
ENV JAVA_OPTS=""

# sobe o app
ENTRYPOINT ["sh","-c","java $JAVA_OPTS -jar app.jar"]
