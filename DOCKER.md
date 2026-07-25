# 🐳 Запуск на Docker

## Требования

- Docker (https://www.docker.com/products/docker-desktop)
- Docker Compose (обычно идёт с Docker Desktop)

## Быстрый старт

### 1. Запустить всё в один клик

```bash
cd C:\Users\a.kurinskih\Documents\oldgamer-bot
docker-compose up -d
```

Это запустит:
- ✅ PostgreSQL (порт 5432)
- ✅ Old Gamer Bot (порт 8080)

### 2. Проверить логи

```bash
docker-compose logs -f bot
```

Должно вывести:
```
Old Gamer Bot started successfully
```

### 3. Тестировать бот в Telegram

1. Открой Telegram
2. Напиши своему боту: `/start`
3. Попробуй: `/add Half-Life 3 PS5 новый`

### 4. Остановить

```bash
docker-compose down
```

---

## Полезные команды

### Просмотр статуса контейнеров
```bash
docker-compose ps
```

### Просмотр логов
```bash
# Все логи
docker-compose logs

# Только бот
docker-compose logs bot

# Только БД
docker-compose logs postgres

# Последние 100 строк в реальном времени
docker-compose logs -f --tail=100 bot
```

### Перезагрузить бот
```bash
docker-compose restart bot
```

### Пересобрать образ (если менял код)
```bash
docker-compose up -d --build
```

### Полное удаление (включая БД)
```bash
docker-compose down -v
```

### Зайти в контейнер
```bash
docker-compose exec bot bash
docker-compose exec postgres psql -U postgres -d oldgamer_bot
```

---

## Структура Docker Compose

```
┌─────────────────────────────────────────┐
│         docker-compose.yml              │
├─────────────────────────────────────────┤
│                                         │
│  ┌──────────────┐  ┌──────────────┐    │
│  │  PostgreSQL  │  │  Old Gamer   │    │
│  │   :5432      │  │     Bot      │    │
│  │              │  │   :8080      │    │
│  └──────────────┘  └──────────────┘    │
│       ▲                    │            │
│       └────────────────────┘            │
│    (зависит от постгреса)              │
│                                         │
└─────────────────────────────────────────┘
```

---

## Конфигурация

Все переменные окружения в `docker-compose.yml`:

```yaml
environment:
  # БД
  DB_URL: jdbc:postgresql://postgres:5432/oldgamer_bot
  DB_USER: postgres
  DB_PASSWORD: postgres

  # Telegram
  TELEGRAM_BOT_TOKEN: твой_токен
  TELEGRAM_BOT_USERNAME: твое_имя_бота
  TELEGRAM_CHANNEL_ID: айди_канала

  # API ключи
  CLAUDE_API_KEY: твой_ключ
  RAWG_API_KEY: твой_ключ
  GIANTBOMB_API_KEY: твой_ключ
```

Если хочешь изменить — отредактируй `docker-compose.yml` и пересоберись.

---

## Troubleshooting

### "docker-compose: command not found"
Установи Docker Desktop (включает docker-compose)

### "Port 5432 already in use"
Уже запущен постгрес на машине:
```bash
# Измени порт в docker-compose.yml
ports:
  - "5433:5432"  # Вместо 5432
```

### "Bot fails to start"
Смотри логи:
```bash
docker-compose logs bot
```

Частые ошибки:
- **Cannot connect to database** — БД не поднялась, подожди 30 сек
- **Telegram API error** — неверный TOKEN
- **Claude API error** — неверный ключ

### "Cannot build image"
Убедись что Docker запущен:
```bash
docker --version
```

Если ошибка в pom.xml:
```bash
docker-compose down
docker system prune -a
docker-compose up -d --build
```

---

## Где хранятся данные

- **База данных**: `postgres_data` volume (автоматически создаётся)
- **Логи**: выводятся в консоль через `docker-compose logs`
- **Черновики**: в БД

При `docker-compose down -v` все данные удалятся!

---

## Production vs Development

### Development (текущая конфигурация)
```bash
docker-compose up
```
- Все логи видны в консоли
- Автоматический перезапуск
- Удобно для тестирования

### Production (если нужно)
```bash
docker-compose up -d
```
- Запуск в фоне
- Можно отключить сессию
- Контейнеры перезагружаются автоматически

---

## Резервная копия БД

```bash
# Экспорт
docker-compose exec postgres pg_dump -U postgres oldgamer_bot > backup.sql

# Импорт
docker-compose exec -T postgres psql -U postgres oldgamer_bot < backup.sql
```

---

Готово! Теперь всё на Docker 🚀
