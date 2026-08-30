# Money Transfer REST Service

REST-сервис для перевода денег между банковскими картами, соответствующий спецификации OpenAPI.

## Схема приложений

### Взаимодействие компонентов

1. **Пользователь** открывает FRONT-приложение в браузере по адресу `http://localhost:3000`
2. **FRONT-приложение** (React) отправляет запросы к REST-сервису:
    - `POST /transfer` — запрос на перевод
    - `POST /confirmOperation` — подтверждение перевода
3. **REST-сервис** (Spring Boot) обрабатывает запросы:
    - Проверяет карты и баланс
    - Генерирует ID операции
    - Выполняет перевод
4. **Данные** хранятся:
    - Карты — в оперативной памяти (ConcurrentHashMap)
    - Логи транзакций — в файле `logs/transactions.log`

### Компоненты

| Приложение | Технология | Порт | Назначение |
|------------|------------|------|------------|
| **FRONT** | React | 3000 | Пользовательский интерфейс |
| **REST-сервис** | Spring Boot | 5500 | Обработка запросов |
| **Хранилище** | ConcurrentHashMap | - | Хранение карт в памяти |
| **Логи** | Logback | - | Запись транзакций |


## Стек технологий

- Java 17
- Spring Boot 3.1.5
- Docker & Docker Compose
- Maven
- OpenAPI (Swagger)

## Запуск проекта

### Требования
- Docker и Docker Compose
- Git

##  Запуск  REST-сервиса
docker-compose up -d --build

Сервис будет доступен по адресу: http://localhost:5500

## Запуск FRONT-приложения
FRONT-приложение запускается локально,  отдельно от REST-сервиса:

1. Перейдите в папку с FRONT:

cd money-transfer-frontend/card-transfer

2.Установите зависимости

npm install --legacy-peer-deps

3. Настройте .env файл:

   REACT_APP_API_URL=http://localhost:5500

4. Запустите приложение:

   npm start

   FRONT будет доступен по адресу: http://localhost:3000

##Проверка работы
curl http://localhost:5500/health

Ожидаемый ответ: Service is running


## API Endpoints

### 1. Перевод средств

**POST** `/transfer`

Request body:
```json
{
    "cardFromNumber": "9876543210987654",
    "cardFromValidTill": "03/27",
    "cardFromCVV": "321",
    "cardToNumber": "1111222233334444",
    "amount": {
        "value": 1000,
        "currency": "RUB"
    }
}
 ```
Пример успешного ответа (200 OK):
```
{
"operationId": "550e8400-e29b-41d4-a716-446655440000"
}
```
Пример ответа с ошибкой (400 Bad Request):
```
{
"message": "Insufficient funds on card. Balance: 3000.00, Required: 3030.00",
"id": 108114656
}
```
### 2. Подтверждение перевода
   POST /confirmOperation

Пример запроса:

```{
"operationId": "550e8400-e29b-41d4-a716-446655440000",
"code": "1234"
}
```
Пример успешного ответа (200 OK):

```
{
"operationId": "550e8400-e29b-41d4-a716-446655440000"
}
```
Пример ответа с ошибкой (400 Bad Request):

```
{
"message": "Invalid confirmation code",
"id": 1934688334
}
```

## 3. Проверка здоровья
   GET /health

Ответ: Service is running

### Тестовые карты
Для тестирования доступны следующие карты:

| Номер карты | Держатель | CVV | Срок действия | Баланс |
|------------|-----------|-----|---------------|---------|
| 1111222233334444 | John Doe | 123 | 12/25         | 10000.00 |
| 5555666677778888 | Jane Smith | 456 | 06/26         | 5000.00 |
| 1234567890123456 | Alice Johnson | 789 | 09/24         | 7500.00 |
| 9876543210987654 | Bob Wilson | 321 | 03/27         | 3000.00 |

Комиссия: 1% от суммы перевода.

Код подтверждения: 1234 (для всех переводов)

## Логирование
Все транзакции записываются в файл logs/transactions.log:

[2026-08-30 15:30:25] From: ****4444 To: ****8888 Amount: 1000.00 Commission: 10.00 Result: SUCCESS

Номера карт маскируются (видны только последние 4 цифры).

## Swagger UI
Документация API доступна по адресу: http://localhost:5500/swagger-ui.html

## Остановка сервиса
docker-compose down

## Примеры запросов
### Перевод через curl

curl -X POST http://localhost:5500/transfer \
-H "Content-Type: application/json" \
-d '{
"cardFromNumber": "1111222233334444",
"cardFromValidTill": "12/25",
"cardFromCVV": "123",
"cardToNumber": "5555666677778888",
"amount": {
"value": 1000,
"currency": "RUB"
}
}'
