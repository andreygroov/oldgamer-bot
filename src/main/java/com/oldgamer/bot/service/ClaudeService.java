package com.oldgamer.bot.service;

import com.oldgamer.bot.model.Game;
import com.oldgamer.bot.model.GeneratedPost;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class ClaudeService {
    private static final Logger logger = LoggerFactory.getLogger(ClaudeService.class);

    @Value("${claude.api.key}")
    private String claudeApiKey;

    @Value("${claude.model:claude-3-5-sonnet-20241022}")
    private String claudeModel;

    public GeneratedPost generateContent(Game game, String condition) {
        GeneratedPost post = new GeneratedPost(game.getName(), game.getPlatform(), condition);

        try {
            post.setAvitoDescription(generateAvitoDescription(game, condition));
            post.setTelegramPost(generateTelegramPost(game, condition));
        } catch (Exception e) {
            logger.error("Failed to generate content for game: {}", game.getName(), e);
        }

        return post;
    }

    private String generateAvitoDescription(Game game, String condition) {
        String prompt = buildAvitoPrompt(game, condition);
        return callClaudeAPI(prompt);
    }

    private String generateTelegramPost(Game game, String condition) {
        String prompt = buildTelegramPrompt(game, condition);
        return callClaudeAPI(prompt);
    }

    private String buildAvitoPrompt(Game game, String condition) {
        return String.format("""
            Создай описание для объявления на Avito по следующим правилам:

            Правила (ВАЖНО - СЛЕДУЙ СТРОГО):
            1. Заголовок (до 50 символов): [Название] [Платформа] [Ключевое преимущество]
            2. Первая строка: жанр, главная эмоция, почему интересна
            3. Блок "Сюжет" (4-6 предложений, без спойлеров)
            4. Особенности (маркированный список по жанру)
            5. "Что вы получаете" (если %s: новый диск, идеальное состояние; если б/у: оригинальный диск, отличное состояние)
            6. Блок доверия (✔ Гарантия магазина, ✔ Только оригинальные диски, и т.д.)
            7. Адрес: OLD GAMER, ТРЦ Континент, ул. Троллейная 130А, 3 этаж, 10:00-21:00
            8. Доставка: Яндекс Доставка, СДЭК, Почта, Авито Доставка
            9. Не более одного эмодзи на строку

            Игра: %s
            Платформа: %s
            Состояние: %s
            Жанр: %s
            Описание: %s

            Сгенерируй полное описание объявления:
            """, condition, game.getName(), game.getPlatform(), condition,
                game.getGenre() != null ? game.getGenre() : "не определён",
                game.getDescription() != null ? game.getDescription() : "");
    }

    private String buildTelegramPrompt(Game game, String condition) {
        return String.format("""
            Создай пост для Telegram канала игрового магазина OLD GAMER.
            Это анонс поступления новой игры в магазин.

            Стиль: увлекательный, с эмодзи, 3-5 предложений, призыв к действию (написать в DM, зайти в магазин).

            Игра: %s
            Платформа: %s
            Состояние: %s (%s)
            Жанр: %s

            Сгенерируй пост:
            """, game.getName(), game.getPlatform(),
                "новый диск".equals(condition) ? "🆕 Новый" : "б/у диск",
                condition, game.getGenre() != null ? game.getGenre() : "");
    }

    private String callClaudeAPI(String prompt) {
        // Временная реализация - потом заменим на реальный API вызов
        // Просто возвращаем шаблонный текст для тестирования
        if (prompt.contains("Avito")) {
            return generateMockAvitoDescription();
        } else {
            return generateMockTelegramPost();
        }
    }

    private String generateMockAvitoDescription() {
        return """
            🎮 The Legend of Zelda PS5 новый диск

            Легендарная приключенческая RPG, ставшая культовой классикой!

            **СЮЖЕТ**
            Молодой герой просыпается без воспоминаний и должен спасти королевство от древней угрозы. Путешествие через живописные ландшафты, встреча с причудливыми персонажами и раскрытие тайн прошлого.

            **ОСОБЕННОСТИ**
            • Огромный открытый мир с полной свободой исследования
            • Глубокая система развития персонажа
            • Сотни побочных квестов и тайн
            • Потрясающая графика на PS5

            **ЧТО ВЫ ПОЛУЧАЕТЕ**
            ✔ Новый оригинальный запечатанный диск
            ✔ Идеальное состояние
            ✔ Русская версия/субтитры
            ✔ Гарантия магазина
            ✔ Только оригинальные диски
            ✔ Проверка перед продажей
            ✔ Trade-In

            **САМОВЫВОЗ**
            OLD GAMER
            ТРЦ Континент
            ул. Троллейная 130А
            3 этаж
            10:00-21:00

            **ДОСТАВКА**
            Яндекс Доставка, СДЭК, Почта, Авито Доставка
            """;
    }

    private String generateMockTelegramPost() {
        return """
            🎮 **НОВОЕ ПОСТУПЛЕНИЕ!**

            The Legend of Zelda на PS5 🆕

            Легендарная приключенческая RPG, которую должен пройти каждый геймер! Огромный открытый мир, сотни квестов, потрясающая графика на PS5. 🌍✨

            В наличии в магазине OLD GAMER! 📍 ТРЦ Континент, 3 этаж, ул. Троллейная 130А

            📩 Напиши нам в DM для деталей и бронирования! 👇
            """;
    }
}
