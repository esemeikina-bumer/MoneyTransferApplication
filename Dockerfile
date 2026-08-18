# Этап 1: Сборка приложения
FROM maven:3.9.4-eclipse-temurin-21 AS build

# Устанавливаем рабочую директорию
WORKDIR /build

# Копируем pom.xml для кэширования зависимостей
COPY pom.xml .

# Загружаем зависимости (этот слой будет закэширован)
RUN mvn dependency:go-offline -B

# Копируем исходный код
COPY src ./src

# Собираем приложение, пропуская тесты
RUN mvn clean package -DskipTests

# Этап 2: Создание финального образа
FROM eclipse-temurin:21-jre-alpine

# Устанавливаем рабочую директорию
WORKDIR /app

# Копируем собранный jar файл из первого этапа
# Обратите внимание на имя jar файла: money-transfer-service-1.0.0.jar
COPY --from=build /build/target/money-transfer-service-*.jar app.jar

# Создаем директорию для логов
RUN mkdir -p /app/logs

# Открываем порт (у нас 5500, а не 8080)
EXPOSE 5500

# Запускаем приложение
ENTRYPOINT ["java", "-jar", "app.jar"]