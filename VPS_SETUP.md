# 🚀 Настройка на VPS

## Как использовать ключи на сервере

Код содержит API ключи как **fallback значения** (по умолчанию). Это позволяет коду работать везде, но на продакшене рекомендуется переопределить ключи через переменные окружения.

---

## Вариант 1: Использовать ключи из кода (Простой)

Ключи уже есть в `application.yml` и `docker-compose.yml`, поэтому просто запусти:

```bash
cd /opt/oldgamer-bot
git clone https://github.com/andreygroov/oldgamer-bot.git .
docker-compose up -d
```

**Готово!** Бот работает с встроенными ключами.

---

## Вариант 2: Переопределить ключи через переменные окружения (Более безопасный)

Если хочешь использовать свои ключи или скрыть текущие, переопредели их через `.env` файл:

### Шаг 1: Создать файл `.env` на VPS

```bash
cd /opt/oldgamer-bot
cat > .env << 'EOF'
TELEGRAM_BOT_TOKEN=8612217816:AAE1gHnsMO8PhKnaTU43kBnfcwK-M98cURY
TELEGRAM_BOT_USERNAME=oldgamer_shop_bot
TELEGRAM_CHANNEL_ID=-1001234567890

CLAUDE_API_KEY=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

RAWG_API_KEY=b0ac172a102344eea91a745d1e338fab
GIANTBOMB_API_KEY=562bf4e8903624cdeaaaec9a241fb5086c1af1da

DB_URL=jdbc:postgresql://postgres:5432/oldgamer_bot
DB_USER=postgres
DB_PASSWORD=postgres
EOF
```

### Шаг 2: Запустить с `.env`

```bash
docker-compose up -d
```

Docker автоматически загрузит переменные из `.env` и переопределит значения в контейнере.

---

## Как это работает

### В коде (`application.yml`):
```yaml
telegram:
  bot:
    token: ${TELEGRAM_BOT_TOKEN:8612217816:AAE1gHnsMO8PhKnaTU43kBnfcwK-M98cURY}
    #      ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^ переменная окружения
    #                                   ^^^^^^^^^^^^^^^^^^^^^ fallback значение
```

**Порядок подстановки:**
1. Проверит переменную окружения `TELEGRAM_BOT_TOKEN`
2. Если не найдена → использует значение после двоеточия

### В `docker-compose.yml`:
```yaml
environment:
  TELEGRAM_BOT_TOKEN: ${TELEGRAM_BOT_TOKEN:8612217816:...}
```

Если создан `.env` файл, docker-compose автоматически подставит переменные оттуда.

---

## Способы задать переменные на VPS

### 1️⃣ Через `.env` файл (Рекомендуется)
```bash
cat > /opt/oldgamer-bot/.env << 'EOF'
TELEGRAM_BOT_TOKEN=...
CLAUDE_API_KEY=...
# и т.д.
EOF

docker-compose up -d
```

### 2️⃣ Через `docker-compose.yml` (Временно)
```bash
TELEGRAM_BOT_TOKEN=... CLAUDE_API_KEY=... docker-compose up -d
```

### 3️⃣ Через `docker run` (Если запускаешь контейнеры вручную)
```bash
docker run -e TELEGRAM_BOT_TOKEN=... -e CLAUDE_API_KEY=... ...
```

### 4️⃣ Через systemd сервис (Для production)
```ini
[Service]
Environment="TELEGRAM_BOT_TOKEN=..."
Environment="CLAUDE_API_KEY=..."
ExecStart=/usr/bin/docker-compose up
```

---

## 🔐 Безопасность

**На VPS:**
- ✅ Ключи в `.env` файле (не коммитится в гит)
- ✅ `.env` файл должен быть только для root: `chmod 600 .env`
- ✅ GitHub Actions используют GitHub Secrets (не видны в логах)

**На локальной машине:**
- ✅ Ключи в `application.yml` (для разработки)
- ✅ Для продакшена - переопредели через переменные окружения

---

## 📋 Итого

### На локальной машине (разработка):
```bash
./run.ps1  # Используются ключи из application.yml
```

### На VPS (продакшен):
```bash
cd /opt/oldgamer-bot
docker-compose up -d  # Используются встроенные ключи
```

### На VPS с собственными ключами:
```bash
# Создай .env с твоими ключами
echo "TELEGRAM_BOT_TOKEN=..." > .env
echo "CLAUDE_API_KEY=..." >> .env

docker-compose up -d  # Загрузит ключи из .env
```

---

## 🚀 GitHub Actions деплой

GitHub Actions использует **GitHub Secrets** (не видны в логах):

```yaml
env:
  TELEGRAM_BOT_TOKEN: ${{ secrets.TELEGRAM_BOT_TOKEN }}
  CLAUDE_API_KEY: ${{ secrets.CLAUDE_API_KEY }}
```

Эти переменные переопределяют встроенные значения при деплое.

---

## Проверить что ключи загрузились

```bash
# На VPS, посмотри логи бота
docker-compose logs bot

# Должно быть без ошибок про ключи:
# - "Old Gamer Bot started successfully" ✅
# - Нет ошибок "API key invalid" ❌
```

---

## Резюме

- **Встроенные ключи:** Работают везде (разработка, VPS, Docker)
- **Переменные окружения:** Переопределяют встроенные значения
- **Безопасно:** Ключи не попадают в логи, хранятся в `.env` или GitHub Secrets
- **Гибко:** Один код, разные ключи для разных сред

Просто клонируй репо и запусти `docker-compose up -d` — всё сработает! 🎉
