# 🚀 Быстрый старт

## ШАГ 1: Подготовка (5 минут)

### 1.1 PostgreSQL
```bash
createdb oldgamer_bot
psql -U postgres oldgamer_bot < C:\Users\a.kurinskih\Documents\oldgamer-bot\schema.sql
```

### 1.2 Получи API ключи (два сервиса)

**RAWG.io API (основной источник):**
1. Зайди: https://rawg.io/apidocs
2. Нажми "Get API Key"
3. Заполни email и пароль
4. Скопируй **API Key**

**Giant Bomb API (резервный источник):**
1. Зайди: https://www.giantbomb.com/api/
2. Нажми "Register" (если нет аккаунта)
3. Создай аккаунт
4. Скопируй **API Key** со своего профиля

### 1.3 Вставь ключи в конфиг
Отредактируй: `C:\Users\a.kurinskih\Documents\oldgamer-bot\src\main\resources\application.yml`

Найди строки и вставь свои ключи:
```yaml
rawg:
  api:
    key: ТВОЙ_RAWG_API_KEY

giantbomb:
  api:
    key: ТВОЙ_GIANTBOMB_API_KEY
```

---

## ШАГ 2: Запуск (2 минуты)

### Через IntelliJ IDEA:
1. `File → Open → C:\Users\a.kurinskih\Documents\oldgamer-bot`
2. Дождись загрузки Maven
3. `Run → Run 'OldGamerBotApplication'`

### Через консоль:
```bash
cd C:\Users\a.kurinskih\Documents\oldgamer-bot
mvn spring-boot:run
```

Должно вывести:
```
Old Gamer Bot started successfully
```

---

## ШАГ 3: Тестирование (2 минуты)

1. Открой Telegram
2. Найди свой бот (username из конфига)
3. Напиши: `/start`
4. Попробуй: `/add Half-Life 3 PS5 новый`

Бот должен:
- ✅ Показать "Ищу информацию об игре..."
- ✅ Сгенерировать описание Avito
- ✅ Сгенерировать пост Telegram
- ✅ Показать кнопки действия

---

## ⚠️ Если не работает

### "Cannot connect to database"
```bash
# Проверь что PostgreSQL запущен и БД создана
psql -U postgres -d oldgamer_bot -c "SELECT 1"
```

### "Bot did not respond"
- Проверь **TELEGRAM_BOT_TOKEN** в конфиге
- Убедись что интернет есть
- Перезагрузи бот

### "RAWG API error"
- Проверь что **RAWG_API_KEY** верный
- Убедись что аккаунт активирован (проверь email)
- Проверь лимит запросов на https://rawg.io/apidocs

### "Giant Bomb API error"
- Проверь что **GIANTBOMB_API_KEY** верный
- Убедись что аккаунт на Giant Bomb активирован
- Если одна API не работает, вторая будет использоваться автоматически

### "Claude API error"  
- Проверь что **CLAUDE_API_KEY** верный
- Проверь баланс на https://console.anthropic.com

---

## 🎮 Первый контент

Когда бот запущен, напиши:

```
/add Half-Life 3 PS5 новый
```

Выбери действие:
- **📝 Avito** — получить описание для объявления
- **📢 Telegram** — опубликовать пост в канал
- **🎯 Оба** — опубликовать и то, и то
- **⏰ Отложить** — запланировать на позже

---

## 📋 ID канала @oldgamer_shop

Когда будешь публиковать в Telegram, нужен ID канала.

Как получить:
1. Напиши боту @username_to_id_bot: `/start`
2. Перешли ему сообщение из канала
3. Бот вернёт ID
4. Вставь в конфиг:
```yaml
telegram:
  channel:
    id: -1001234567890
```

Или просто используй заглушку - обнови позже когда будешь готов публиковать

---

## 🎯 Следующие шаги

- Настроить автопубликацию
- Интегрировать Shazoo парсинг
- Добавить Steam API для скидок
- Реализовать редактирование в боте

---

Готово! Бот работает 🚀
