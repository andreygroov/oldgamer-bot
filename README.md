# OLD GAMER Bot

Telegram бот для генерации контента (описания Avito + посты Telegram) для магазина видеоигр.

## Требования

- Java 17+
- Maven 3.6+
- PostgreSQL 12+
- Telegram Bot Token ✅ (уже есть)
- Claude API ключ ✅ (уже есть)
- RAWG.io API ключ (бесплатно, без 2FA)
- Giant Bomb API ключ (бесплатно, без 2FA)

## Установка

### 1. PostgreSQL

Создай БД для проекта:
```bash
createdb oldgamer_bot
psql -U postgres oldgamer_bot < schema.sql
```

### 2. Ключи (у тебя почти всё есть!)

**✅ Telegram Bot:**
```
TOKEN: <YOUR_TELEGRAM_BOT_TOKEN>
```

**✅ Claude API:**
```
KEY: <YOUR_CLAUDE_API_KEY>
```

**⏳ RAWG.io API (бесплатно, без 2FA):**
1. Зайди на https://rawg.io/apidocs
2. Нажми "Get API Key"
3. Заполни форму (email, пароль)
4. Скопируй **API Key**
5. Вставь в конфиг

**⏳ Giant Bomb API (бесплатно, без 2FA):**
1. Зайди на https://www.giantbomb.com/api/
2. Scroll down до "Register"
3. Создай аккаунт на Giant Bomb
4. Скопируй **API Key**
5. Вставь в конфиг

### 3. Конфигурация

Отредактируй `src/main/resources/application.yml`:

```yaml
telegram:
  bot:
    token: <YOUR_TELEGRAM_BOT_TOKEN>
    username: oldgamer_shop_bot
  channel:
    id: -1001234567890

claude:
  api:
    key: <YOUR_CLAUDE_API_KEY>
  model: claude-3-5-sonnet-20241022

rawg:
  api:
    key: ТВОЙ_RAWG_API_KEY

giantbomb:
  api:
    key: ТВОЙ_GIANTBOMB_API_KEY

db:
  url: jdbc:postgresql://localhost:5432/oldgamer_bot
  user: postgres
  password: postgres
```

### 4. Запуск

**Вариант 1: Через IntelliJ IDEA**
1. Открой проект: `File → Open → oldgamer-bot`
2. Maven автоматически загрузит зависимости
3. Нажми на `Run → Run 'OldGamerBotApplication'`

**Вариант 2: Через консоль**
```bash
cd C:\Users\a.kurinskih\Documents\oldgamer-bot
mvn spring-boot:run
```

**Вариант 3: Собрать JAR**
```bash
mvn clean package
java -jar target/oldgamer-bot-1.0-SNAPSHOT.jar
```

Если всё правильно, увидишь:
```
Old Gamer Bot started successfully
```

## Использование

### Команды бота

**Создать контент:**
```
/add Half-Life 3 PS5 новый
/add Elden Ring Xbox Series X б/у
```

**Показать черновики:**
```
/drafts
```

### Процесс работы

1. Пишешь боту: `/add [Название] [Платформа] [новый/б/у]`
2. Бот ищет игру в IGDB
3. Claude генерирует:
   - Описание для Avito (по "Библии")
   - Пост для Telegram канала
4. Показывает превью с кнопками:
   - 📝 Avito — скопировать описание
   - 📢 Telegram — опубликовать пост
   - 🎯 Оба — опубликовать оба
   - ⏰ Отложить — расписать на дату/время
   - ✏️ Редактировать — изменить текст
   - ❌ Отклонить — удалить черновик

## Архитектура

```
OldGamerBot (Telegram)
├── ClaudeService (генерация текстов)
├── IGDBService (поиск информации об играх)
└── DatabaseService (хранение черновиков)
    └── PostgreSQL
```

## Планы развития

- [ ] Интеграция с Shazoo парсингом
- [ ] Steam API для скидок
- [ ] Редактирование черновиков в боте
- [ ] История публикаций
- [ ] Планировщик автопостов
- [ ] Аналитика постов

## Troubleshooting

**Ошибка подключения к БД:**
```
Проверь что PostgreSQL запущен и ключи верные в application.yml
```

**Бот не отвечает:**
```
Убедись что Telegram токен правильный и интернет работает
```

**Ошибки Claude API:**
```
Проверь API ключ и лимит использования на console.anthropic.com
```
