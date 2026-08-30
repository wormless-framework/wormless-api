# Estágio 1: Build (Compilação)
FROM maven:3.9-eclipse-temurin-21-alpine AS builder

WORKDIR /build

# 1. Copia o pom.xml primeiro para aproveitar o cache de camadas do Docker
COPY pom.xml .

# 2. Baixa as dependências offline. 
# Se o pom.xml não mudar, o Docker usa o cache dessa camada, acelerando muito o build.
RUN mvn dependency:go-offline -B

# 3. Copia o restante do código-fonte
COPY src ./src

# 4. Compila o projeto pulando os testes (os testes JUnit devem rodar na esteira de CI/CD, antes de gerar a imagem)
RUN mvn clean package -DskipTests

# Estágio 2: Runtime (Execução)
FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

# 5. Segurança: Cria um grupo e um usuário não-root
RUN addgroup -S wormlessgroup && adduser -S wormlessuser -G wormlessgroup

# Muda a propriedade do diretório de trabalho
RUN chown -R wormlessuser:wormlessgroup /app

# Define o usuário não-root para rodar o processo
USER wormlessuser

# 6. Copia apenas o JAR gerado no estágio de build (deixa para trás todo o Maven e código-fonte)
COPY --from=builder /build/target/*.jar app.jar

# 7. Expõe a porta que o Spring Boot vai rodar (padrão 8080)
EXPOSE 8080

# 8. Entrypoint otimizado com flags da JVM voltadas para containers
# -XX:MaxRAMPercentage=75.0 -> Garante que o Java entenda os limites de memória do Docker
# -XX:+UseG1GC -> Coletor de lixo eficiente para aplicações web
ENTRYPOINT ["java", "-XX:MaxRAMPercentage=75.0", "-XX:+UseG1GC", "-jar", "app.jar"]