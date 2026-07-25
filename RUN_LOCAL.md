# 🚀 Локальный запуск (Windows)

## Требования

- **Java 17+** (https://www.oracle.com/java/technologies/downloads/)
- **Maven 3.6+** (https://maven.apache.org/download.cgi)
- **PostgreSQL 12+** (https://www.postgresql.org/download/)

## Быстрый запуск

### Вариант 1: Автоматический скрипт (Рекомендуется)

**PowerShell:**
```powershell
cd C:\Users\a.kurinskih\Documents\oldgamer-bot
.\run.ps1
```

**Или Batch (cmd.exe):**
```batch
cd C:\Users\a.kurinskih\Documents\oldgamer-bot
run.bat
```

Скрипт автоматически:
✅ Проверит Java
✅ Проверит PostgreSQL
✅ Создаст БД если нужно
✅ Собрает проект
✅ Запустит бот

---

### Вариант 2: Вручную (Пошагово)

#### ШАГ 1: Проверить Java
```bash
java -version
```

Должно быть Java 17+. Если нет — установи отсюда:
https://www.oracle.com/java/technologies/downloads/

#### ШАГ 2: Проверить Maven
```bash
mvn -version
```

Если нет — установи отсюда:
https://maven.apache.org/download.cgi

Или через chocolatey:
```bash
choco install maven
```

#### ШАГ 3: Проверить PostgreSQL
```bash
psql --version
```

Если PostgreSQL не установлен:
1. Скачай отсюда: https://www.postgresql.org/download/
2. Установи с паролем по умолчанию `postgres`

#### ШАГ 4: Создать БД
```bash
createdb -U postgres oldgamer_bot
```

Проверить что создалась:
```bash
psql -U postgres -d oldgamer_bot -c "SELECT 1"
```

#### ШАГ 5: Собрать проект
```bash
cd C:\Users\a.kurinskih\Documents\oldgamer-bot
mvn clean package -DskipTests
```

Первая сборка занимает 2-3 минуты (скачиваются зависимости).

#### ШАГ 6: Запустить бот
```bash
java -jar target/oldgamer-bot-1.0-SNAPSHOT.jar
```

Должно вывести:
```
Old Gamer Bot started successfully
```

---

## ✅ Если всё работает

1. **Открой Telegram**
2. **Найди бота** по username (по умолчанию `oldgamer_shop_bot`)
3. **Напиши:** `/start`
4. **Попробуй:** `/add Half-Life 3 PS5 новый`

Бот должен ответить с генерированным контентом!

---

## ⚠️ Troubleshooting

### "java: command not found"
**Проблема:** Java не установлена

**Решение:**
1. Скачай Java 17: https://www.oracle.com/java/technologies/downloads/
2. Установи
3. Перезагрузи CMD/PowerShell
4. Проверь: `java -version`

### "mvn: command not found"
**Проблема:** Maven не установлен

**Решение:**
1. Скачай Maven: https://maven.apache.org/download.cgi
2. Распакуй в папку (например `C:\maven`)
3. Добавь в PATH:
   - Win+X → System
   - Advanced system settings → Environment Variables
   - PATH → Add → `C:\maven\bin`
4. Перезагрузи CMD/PowerShell

Или установи через chocolatey:
```bash
choco install maven
```

### "psql: command not found"
**Проблема:** PostgreSQL не установлен

**Решение:**
1. Скачай: https://www.postgresql.org/download/windows/
2. Установи (запомни пароль!)
3. Перезагрузи CMD
4. Проверь: `psql --version`

### "psql: FATAL: database "oldgamer_bot" does not exist"
**Проблема:** БД не создана

**Решение:**
```bash
createdb -U postgres oldgamer_bot
```

### "Cannot connect to database"
**Проблема:** PostgreSQL не запущена

**Решение:**
```bash
# Windows Services → PostgreSQL → Start

# Или через PowerShell:
Get-Service PostgreSQL* | Start-Service
```

### Build ошибка "Unknown repository type pom"
**Проблема:** Версия Maven старая

**Решение:**
```bash
mvn -v  # Должно быть 3.6+

# Обнови Maven и переустанови
```

### "Old Gamer Bot started successfully" но не отвечает в Telegram
**Проблем:** Бот запущен, но не реагирует

**Решение:**
1. Проверь что TELEGRAM_BOT_TOKEN верный в `application.yml`
2. Проверь интернет
3. Перезагрузи бот (Ctrl+C и запусти снова)

### "Claude API error"
**Проблема:** API ключ неверный

**Решение:**
Проверь в `application.yml`:
```yaml
claude:
  api:
    key: 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
```

---

## 📝 Конфигурация

Все переменные в файле:
```
C:\Users\a.kurinskih\Documents\oldgamer-bot\src\main\resources\application.yml
```

Там уже вставлены все ключи:
- ✅ Telegram Token
- ✅ Claude API Key
- ✅ RAWG API Key
- ✅ Giant Bomb API Key

---

## 🛑 Остановить бот

Нажми **Ctrl + C** в консоли

---

## 📊 Логи

Бот выводит логи в консоль. Смотри их для отладки:
- INFO — обычные сообщения
- DEBUG — детальная информация
- ERROR — ошибки

---

Готово! Бот должен работать 🚀
