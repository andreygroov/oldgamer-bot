# Stage 1: Build
FROM maven:3.9-eclipse-temurin-17 AS builder
WORKDIR /build

# Копируем pom.xml
COPY pom.xml .

# Скачиваем зависимости
RUN mvn dependency:resolve dependency:resolve-plugins

# Копируем весь проект
COPY . .

# Собираем JAR
RUN mvn clean package -DskipTests -q

# Stage 2: Runtime
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app

# Копируем собранный JAR
COPY --from=builder /build/target/oldgamer-bot-1.0-SNAPSHOT.jar app.jar

# Expose port
EXPOSE 8080

# Запуск
ENTRYPOINT ["java", "-jar", "app.jar"]
