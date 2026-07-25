# 🚀 Развертывание на VPS через GitHub Actions

## Шаг 1: Создать GitHub репо

1. Зайди на https://github.com/new
2. Заполни:
   - **Repository name:** `oldgamer-bot`
   - **Description:** Telegram bot for game content generation
   - **Visibility:** Private (или Public на выбор)
3. Нажми **Create repository**

Скопируй ссылку репо (например: `https://github.com/твой_юзер/oldgamer-bot.git`)

---

## Шаг 2: Инициализировать гит локально

```bash
cd C:\Users\a.kurinskih\Documents\oldgamer-bot

# Уже сделано, но если нужно переделать:
git init
git config user.name "Your Name"
git config user.email "your.email@gmail.com"
```

---

## Шаг 3: Закоммитить весь код

```bash
cd C:\Users\a.kurinskih\Documents\oldgamer-bot

# Добавить все файлы
git add .

# Проверить что добавлено
git status

# Первый коммит
git commit -m "Initial commit: Old Gamer Bot with Docker setup"
```

---

## Шаг 4: Добавить GitHub как remote и пушить

```bash
# Замени YOUR_USERNAME и YOUR_REPO на свои значения!
git remote add origin https://github.com/YOUR_USERNAME/oldgamer-bot.git

# Переименовать ветку на main (если нужно)
git branch -M main

# Пушить код на GitHub
git push -u origin main
```

**Потребуется ввести GitHub credentials:**
- Username: твой GitHub username
- Password: GitHub Personal Access Token (или пароль, зависит от настроек)

Как получить Personal Access Token:
1. GitHub → Settings → Developer settings → Personal access tokens
2. Нажми "Generate new token"
3. Выбери: `repo`, `workflow`, `write:packages`
4. Скопируй токен и используй как пароль

---

## Шаг 5: Добавить GitHub Secrets

В GitHub репо:
1. Перейди: **Settings → Secrets and variables → Actions**
2. Нажми **New repository secret**

Добавь эти 4 секрета:

| Имя | Значение |
|-----|----------|
| `SSH_HOST` | `147.45.134.129` |
| `SSH_USER` | `root` |
| `SSH_PASSWORD` | `uTgpGgk7NX3C+@` |
| `SSH_PORT` | `22` |

**Важно:** GitHub Secrets видны только GitHub Actions, не видны в логах.

---

## Шаг 6: Первоначальное развертывание на VPS (вручную)

Подключись к VPS и выполни эту команду (замени GITHUB_REPO на свой репо):

```bash
ssh root@147.45.134.129

# На VPS выполни:
export GITHUB_REPO="https://github.com/YOUR_USERNAME/oldgamer-bot.git"

mkdir -p /opt/oldgamer-bot
cd /opt/oldgamer-bot

git clone $GITHUB_REPO .

docker-compose down 2>/dev/null || true
docker-compose pull
docker-compose up -d

# Проверить статус
docker-compose ps
```

Или скопируй содержимое `setup-vps.sh`, отредактируй `GITHUB_REPO` и запусти на VPS.

---

## Шаг 7: Тестировать GitHub Actions

1. Измени какой-нибудь файл (например, DESIGN.md)
2. Закоммить: `git add . && git commit -m "Test deployment"`
3. Пушить: `git push`
4. Перейди на GitHub репо → **Actions**
5. Смотри выполнение workflow

Если всё зелено ✅ — значит deployment выполнился успешно!

---

## Шаг 8: Проверить что бот работает на VPS

```bash
ssh root@147.45.134.129

cd /opt/oldgamer-bot

# Смотреть статус контейнеров
docker-compose ps

# Смотреть логи бота
docker-compose logs -f bot

# Смотреть логи БД
docker-compose logs -f postgres
```

Бот должен вывести:
```
Old Gamer Bot started successfully
```

---

## 🔄 Теперь всё просто!

Когда GitHub Actions настроен:

**Для обновления кода:**
```bash
git add .
git commit -m "Update: ..."
git push
```

**GitHub Actions автоматически:**
1. ✅ Подключится к VPS
2. ✅ Обновит код (git pull)
3. ✅ Обновит Docker образы
4. ✅ Перезапустит контейнеры
5. ✅ Проверит статус

Все обновления будут на VPS за 1-2 минуты! 🚀

---

## 🛠️ Troubleshooting

### GitHub Actions fail с "Permission denied"
**Причина:** Пароль неверный

**Решение:**
- Проверь `SSH_PASSWORD` в Secrets
- SSH на VPS вручную и проверь пароль

### "Repository not found"
**Причина:** Неверная ссылка на репо

**Решение:**
- Проверь что репо публичный ИЛИ добавил deploy key
- Для приватного репо может потребоваться Personal Access Token вместо пароля

### Контейнеры не запускаются на VPS
**Проверить:**
```bash
ssh root@147.45.134.129
cd /opt/oldgamer-bot
docker-compose logs bot
docker-compose logs postgres
```

### "docker-compose: command not found"
**Решение:** На VPS не установлен Docker Compose

```bash
ssh root@147.45.134.129
docker-compose --version  # Если нет, установи
```

---

## 📚 Дополнительно

### Как откатить версию?
```bash
git revert HEAD
git push
# GitHub Actions автоматически откатит на VPS
```

### Как остановить контейнеры?
```bash
ssh root@147.45.134.129
cd /opt/oldgamer-bot
docker-compose down
```

### Как посмотреть историю деплоев?
GitHub репо → **Actions** → смотри историю выполнений

---

Готово! 🎉 Теперь у тебя есть полный CI/CD pipeline: GitHub → Actions → VPS
