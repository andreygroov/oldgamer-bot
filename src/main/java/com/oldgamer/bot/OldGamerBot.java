package com.oldgamer.bot;

import com.oldgamer.bot.model.Game;
import com.oldgamer.bot.model.GeneratedPost;
import com.oldgamer.bot.service.ClaudeService;
import com.oldgamer.bot.service.DatabaseService;
import com.oldgamer.bot.service.IGDBService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.send.SendPhoto;
import org.telegram.telegrambots.meta.api.objects.InputFile;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class OldGamerBot extends TelegramLongPollingBot {
    private static final Logger logger = LoggerFactory.getLogger(OldGamerBot.class);

    @Value("${telegram.bot.token}")
    private String botToken;

    @Value("${telegram.bot.username}")
    private String botUsername;

    @Value("${telegram.channel.id}")
    private String channelId;

    private final DatabaseService databaseService;
    private final ClaudeService claudeService;
    private final IGDBService igdbService;

    // Состояние пользователя: ожидаем ввод для какого постдокумента
    private final Map<Long, BotState> userStates = new HashMap<>();

    public OldGamerBot(DatabaseService databaseService,
                       ClaudeService claudeService,
                       IGDBService igdbService) {
        this.databaseService = databaseService;
        this.claudeService = claudeService;
        this.igdbService = igdbService;
    }

    @Override
    public String getBotUsername() {
        return botUsername;
    }

    @Override
    public String getBotToken() {
        return botToken;
    }

    @Override
    public void onUpdateReceived(Update update) {
        try {
            if (update.hasMessage() && update.getMessage().hasText()) {
                handleMessage(update.getMessage());
            } else if (update.hasCallbackQuery()) {
                handleCallbackQuery(update.getCallbackQuery());
            }
        } catch (TelegramApiException e) {
            logger.error("Telegram API error processing update", e);
        } catch (Exception e) {
            logger.error("Error processing update", e);
        }
    }

    private void handleMessage(org.telegram.telegrambots.meta.api.objects.Message message) throws TelegramApiException {
        long chatId = message.getChatId();
        String text = message.getText();

        if (text.startsWith("/start")) {
            sendStartMenu(chatId);
        } else if (text.startsWith("/add")) {
            handleAddGame(chatId, text);
        } else if (text.startsWith("/drafts")) {
            showDrafts(chatId);
        } else if (text.startsWith("/help")) {
            sendHelpMenu(chatId);
        } else {
            // Может быть это ввод параметров игры
            BotState state = userStates.get(chatId);
            if (state != null && state.isAwaitingGameInput()) {
                handleAddGame(chatId, "/add " + text);
            }
        }
    }

    private void sendHelpMenu(long chatId) throws TelegramApiException {
        String help = """
            *📖 СПРАВКА - КАК ПОЛЬЗОВАТЬСЯ БОТОМ*

            ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

            *🎮 СОЗДАНИЕ КОНТЕНТА*

            Команда: `/add [Название] [Платформа] [новый/б/у]`

            *Пример:*
            `/add Half-Life 3 PS5 новый`

            Бот сгенерирует:
            ✓ Описание для Avito
            ✓ Пост для Telegram канала

            ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

            *📱 ПОДДЕРЖИВАЕМЫЕ ПЛАТФОРМЫ*

            • `PS5` — PlayStation 5
            • `Xbox` — Xbox Series X/S
            • `Switch` — Nintendo Switch
            • `PC` — Personal Computer

            ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

            *📦 СОСТОЯНИЕ ТОВАРА*

            • `новый` — новый запечатанный диск
            • `б/у` — бывший в использовании, хорошее состояние

            ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

            *📋 ЧЕРНОВИКИ И СТАТУСЫ*

            Команда: `/drafts`

            Показывает все созданные черновики.

            Статусы:
            • 📋 DRAFT — черновик, не опубликован
            • ✅ APPROVED — одобрен и готов
            • 🚀 PUBLISHED — опубликован в канал
            • ⏰ SCHEDULED — запланирован на позже

            ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

            *🎯 КНОПКИ ДЕЙСТВИЯ*

            При создании контента появятся кнопки:

            📝 *Avito* — получить описание для объявления
            📢 *Telegram* — опубликовать пост в канал
            🎯 *Оба* — опубликовать описание и пост одновременно
            ⏰ *Отложить* — запланировать публикацию
            ✏️ *Редактировать* — изменить черновик
            🗑️ *Удалить* — удалить черновик

            ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

            *💡 СОВЕТЫ*

            ✓ Точное название игры даст лучший результат
            ✓ Используй сокращения платформ (PS5, Xbox, Switch)
            ✓ Проверяй черновик перед публикацией
            ✓ Отложи пост если хочешь опубликовать позже

            ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

            Есть вопросы? Напиши разработчику 👨‍💻
            """;

        SendMessage msg = new SendMessage();
        msg.setChatId(chatId);
        msg.setText(help);
        msg.setParseMode("Markdown");
        execute(msg);
    }

    private void handleCallbackQuery(org.telegram.telegrambots.meta.api.objects.CallbackQuery query) throws TelegramApiException {
        String callbackData = query.getData();
        String[] parts = callbackData.split(":");
        if (parts.length < 2) {
            logger.error("Invalid callback data: {}", callbackData);
            return;
        }

        String action = parts[0];
        long postId = Long.parseLong(parts[1]);

        GeneratedPost post = databaseService.getPostById(postId);
        if (post == null) {
            sendMessage(query.getFrom().getId(), "❌ Пост не найден");
            answerCallbackQuery(query);
            return;
        }

        switch (action) {
            case "approve_avito" -> approveAvitoPost(query.getFrom().getId(), post);
            case "approve_telegram" -> approveTelegramPost(query.getFrom().getId(), post);
            case "approve_both" -> {
                approveAvitoPost(query.getFrom().getId(), post);
                approveTelegramPost(query.getFrom().getId(), post);
            }
            case "schedule" -> schedulePost(query.getFrom().getId(), postId);
            case "reject" -> rejectPost(query.getFrom().getId(), postId);
            case "edit" -> editPost(query.getFrom().getId(), postId);
        }

        answerCallbackQuery(query);
    }

    private void handleAddGame(long chatId, String input) throws TelegramApiException {
        // Парсим ввод: "Half-Life 3 PS5 новый"
        String[] parts = input.substring(4).trim().split(" ");
        if (parts.length < 3) {
            sendMessage(chatId, """
                *❌ НЕВЕРНЫЙ ФОРМАТ*

                Используй:
                `/add [Название] [Платформа] [новый/б/у]`

                Платформы: PS5, Xbox, Switch, PC
                Состояние: новый или б/у

                Примеры:
                • `/add Half-Life 3 PS5 новый`
                • `/add Elden Ring Xbox б/у`
                """);
            return;
        }

        String platform = parts[parts.length - 2];
        String condition = parts[parts.length - 1];
        String gameName = String.join(" ", java.util.Arrays.copyOf(parts, parts.length - 2));

        // Проверяем состояние
        if (!condition.equals("новый") && !condition.equals("б/у")) {
            sendMessage(chatId, "*❌ Состояние должно быть: новый или б/у*");
            return;
        }

        // Показываем статус загрузки
        SendMessage loadingMsg = new SendMessage();
        loadingMsg.setChatId(chatId);
        loadingMsg.setText(String.format("""
            *⚙️ СОЗДАНИЕ КОНТЕНТА*

            ━━━━━━━━━━━━━━━━━━━━━━━
            🔍 Ищу информацию об игре...
            Игра: `%s`
            Платформа: `%s`
            Состояние: `%s`

            Подожди секунду... ⏳
            """, gameName, platform, condition));
        loadingMsg.setParseMode("Markdown");
        org.telegram.telegrambots.meta.api.objects.Message loadMsg = execute(loadingMsg);

        Game game = igdbService.searchGame(gameName, platform);
        if (game == null) {
            sendMessage(chatId, """
                *❌ ИГРА НЕ НАЙДЕНА*

                Убедись что название верное.
                Попробуй еще раз или уточни название.
                """);
            return;
        }

        // Генерируем контент
        GeneratedPost post = claudeService.generateContent(game, condition);
        databaseService.savePost(post);

        // Показываем превью
        showPostPreview(chatId, post);
    }

    private void showPostPreview(long chatId, GeneratedPost post) throws TelegramApiException {
        String avitoPreview = truncateText(post.getAvitoDescription(), 300);
        String telegramPreview = truncateText(post.getTelegramPost(), 250);

        String preview = String.format("""
            ✨ *ЧЕРНОВИК ГОТОВ!*

            🎮 *Игра:* `%s`
            📱 *Платформа:* `%s`
            📦 *Состояние:* `%s`

            ━━━━━━━━━━━━━━━━━━━━━━━━━━━

            *📝 AVITO ОПИСАНИЕ:*

            %s

            ━━━━━━━━━━━━━━━━━━━━━━━━━━━

            *📢 TELEGRAM ПОСТ:*

            %s

            ━━━━━━━━━━━━━━━━━━━━━━━━━━━

            Выбери что публиковать 👇
            """, post.getGameName(), post.getPlatform(), post.getCondition(),
                avitoPreview, telegramPreview);

        SendMessage message = new SendMessage();
        message.setChatId(chatId);
        message.setText(preview);
        message.setParseMode("Markdown");

        InlineKeyboardMarkup markup = createPostActionsKeyboard(post.getId());
        message.setReplyMarkup(markup);

        execute(message);
    }

    private String truncateText(String text, int maxLength) {
        if (text == null) return "...";
        if (text.length() <= maxLength) return text;
        return text.substring(0, maxLength).trim() + "\n\n`... (показано сокращено)`";
    }

    private InlineKeyboardMarkup createPostActionsKeyboard(long postId) {
        InlineKeyboardMarkup markup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> keyboard = new ArrayList<>();

        // Первая строка: публикация Avito
        List<InlineKeyboardButton> row1 = new ArrayList<>();
        row1.add(createButton("📝 Avito", "approve_avito:" + postId));
        keyboard.add(row1);

        // Вторая строка: публикация Telegram
        List<InlineKeyboardButton> row2 = new ArrayList<>();
        row2.add(createButton("📢 Telegram", "approve_telegram:" + postId));
        keyboard.add(row2);

        // Третья строка: опубликовать оба
        List<InlineKeyboardButton> row3 = new ArrayList<>();
        row3.add(createButton("🎯 Опубликовать оба", "approve_both:" + postId));
        keyboard.add(row3);

        // Четвёртая строка: отложить
        List<InlineKeyboardButton> row4 = new ArrayList<>();
        row4.add(createButton("⏰ Отложить", "schedule:" + postId));
        keyboard.add(row4);

        // Пятая строка: редактировать и отклонить
        List<InlineKeyboardButton> row5 = new ArrayList<>();
        row5.add(createButton("✏️ Редактировать", "edit:" + postId));
        row5.add(createButton("🗑️ Удалить", "reject:" + postId));
        keyboard.add(row5);

        markup.setKeyboard(keyboard);
        return markup;
    }

    private InlineKeyboardButton createButton(String text, String callbackData) {
        InlineKeyboardButton button = new InlineKeyboardButton();
        button.setText(text);
        button.setCallbackData(callbackData);
        return button;
    }

    private void approveAvitoPost(long chatId, GeneratedPost post) throws TelegramApiException {
        String message = String.format("""
            *✅ AVITO ОПИСАНИЕ ГОТОВО К ПУБЛИКАЦИИ*

            ━━━━━━━━━━━━━━━━━━━━━━━━━

            %s

            ━━━━━━━━━━━━━━━━━━━━━━━━━

            *📌 Инструкция:*
            1. Скопируй текст выше
            2. Перейди на avito.ru в свой магазин
            3. Создай новое объявление
            4. Вставь описание
            5. Добавь фото игры
            6. Опубликуй объявление

            ✅ Статус: *APPROVED*
            """, post.getAvitoDescription());

        SendMessage msg = new SendMessage();
        msg.setChatId(chatId);
        msg.setText(message);
        msg.setParseMode("Markdown");
        execute(msg);

        databaseService.updatePostStatus(post.getId(), GeneratedPost.PostStatus.APPROVED);
        sendMessage(chatId, "✔️ Пост сохранен как APPROVED");
    }

    private void approveTelegramPost(long chatId, GeneratedPost post) throws TelegramApiException {
        SendMessage message = new SendMessage();
        message.setChatId(channelId);
        message.setText(post.getTelegramPost());
        message.setParseMode("Markdown");

        execute(message);

        String confirmMsg = String.format("""
            *🚀 ПОСТ ОПУБЛИКОВАН В КАНАЛ!*

            ━━━━━━━━━━━━━━━━━━━━━━━━━

            Канал: `@oldgamer_shop`
            Игра: `%s`
            Платформа: `%s`

            ✅ Статус: *PUBLISHED*
            """, post.getGameName(), post.getPlatform());

        SendMessage msg = new SendMessage();
        msg.setChatId(chatId);
        msg.setText(confirmMsg);
        msg.setParseMode("Markdown");
        execute(msg);

        databaseService.updatePostStatus(post.getId(), GeneratedPost.PostStatus.PUBLISHED);
    }

    private void schedulePost(long chatId, long postId) throws TelegramApiException {
        String message = """
            *⏰ ОТЛОЖИТЬ ПОСТ*

            На какую дату и время опубликовать?

            Форматы:
            • `2024-07-25 14:30` — точная дата и время
            • `завтра 10:00` — завтра в 10:00
            • `через 2 часа` — через 2 часа

            Напиши время 👇
            """;

        SendMessage msg = new SendMessage();
        msg.setChatId(chatId);
        msg.setText(message);
        msg.setParseMode("Markdown");
        execute(msg);

        BotState state = new BotState();
        state.setAwaitingScheduleDate(postId);
        userStates.put(chatId, state);
    }

    private void rejectPost(long chatId, long postId) throws TelegramApiException {
        String message = """
            *🗑️ ПОСТ УДАЛЕН*

            ━━━━━━━━━━━━━━━━━━━━━━━━━

            Черновик удален из очереди.
            Ты можешь создать новый контент.

            ✅ Статус: *DELETED*
            """;

        SendMessage msg = new SendMessage();
        msg.setChatId(chatId);
        msg.setText(message);
        msg.setParseMode("Markdown");
        execute(msg);

        databaseService.updatePostStatus(postId, GeneratedPost.PostStatus.DRAFT);
    }

    private void editPost(long chatId, long postId) throws TelegramApiException {
        sendMessage(chatId, """
            *✏️ РЕДАКТИРОВАНИЕ*

            К сожалению, встроенное редактирование еще не реализовано.

            Но ты можешь:
            1. Удалить этот пост 🗑️
            2. Создать новый с правильным названием
            3. Или связаться с разработчиком

            Скоро добавим быстрое редактирование! 🚀
            """);
    }

    private void showDrafts(long chatId) throws TelegramApiException {
        List<GeneratedPost> drafts = databaseService.getDraftPosts();
        if (drafts.isEmpty()) {
            String empty = """
                *📭 ЧЕРНОВИКОВ НЕТ*

                ━━━━━━━━━━━━━━━━━━━━━━━━━

                Давай создадим контент! 🎮
                Используй команду:

                `/add Название Платформа новый`

                Пример:
                `/add Half-Life 3 PS5 новый`
                """;

            SendMessage msg = new SendMessage();
            msg.setChatId(chatId);
            msg.setText(empty);
            msg.setParseMode("Markdown");
            execute(msg);
            return;
        }

        StringBuilder sb = new StringBuilder();
        sb.append("*📋 ЧЕРНОВИКИ (").append(drafts.size()).append(" шт.)*\n\n");
        sb.append("━━━━━━━━━━━━━━━━━━━━━━━━━\n\n");

        int count = 1;
        for (GeneratedPost draft : drafts) {
            sb.append(String.format("*%d️⃣ %s*\n", count, draft.getGameName()));
            sb.append(String.format("  📱 %s | %s\n", draft.getPlatform(), draft.getCondition()));
            sb.append(String.format("  🕐 %s\n", draft.getCreatedAt()));
            sb.append(String.format("  📊 Статус: `%s`\n\n", draft.getStatus()));
            count++;
        }

        sb.append("━━━━━━━━━━━━━━━━━━━━━━━━━\n");
        sb.append("Выбери пост для публикации");

        SendMessage msg = new SendMessage();
        msg.setChatId(chatId);
        msg.setText(sb.toString());
        msg.setParseMode("Markdown");
        execute(msg);
    }

    private void sendStartMenu(long chatId) throws TelegramApiException {
        String text = """
            *╔════════════════════════════╗*
            *║   🎮 OLD GAMER CONTENT BOT  ║*
            *╚════════════════════════════╝*

            Привет! 👋 Я помогу тебе создавать контент для магазина.

            *📋 КАК ИСПОЛЬЗОВАТЬ:*

            *1️⃣ Создать описание и пост:*
            `/add` *[Название] [Платформа] [новый/б/у]*

            *Примеры:*
            • `/add Half-Life 3 PS5 новый`
            • `/add Elden Ring Xbox Series X б/у`
            • `/add Zelda Nintendo Switch новый`

            *2️⃣ Посмотреть черновики:*
            `/drafts`

            *3️⃣ Доступные платформы:*
            • PS5 (PlayStation 5)
            • Xbox Series X/S
            • Nintendo Switch
            • PC

            *❔ СТАТУСЫ ПОСТОВ:*
            • 📋 DRAFT — черновик
            • ✅ APPROVED — одобрен
            • 🚀 PUBLISHED — опубликован
            • ⏰ SCHEDULED — запланирован

            ━━━━━━━━━━━━━━━━━━━━━━━━━

            Начни с создания контента 👇
            """;

        SendMessage message = new SendMessage();
        message.setChatId(chatId);
        message.setText(text);
        message.setParseMode("Markdown");

        InlineKeyboardMarkup markup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> keyboard = new ArrayList<>();

        List<InlineKeyboardButton> row1 = new ArrayList<>();
        row1.add(createButton("➕ Создать контент", "new_content"));
        keyboard.add(row1);

        List<InlineKeyboardButton> row2 = new ArrayList<>();
        row2.add(createButton("📋 Мои черновики", "show_drafts"));
        keyboard.add(row2);

        markup.setKeyboard(keyboard);
        message.setReplyMarkup(markup);

        execute(message);
    }

    private void sendMessage(long chatId, String text) throws TelegramApiException {
        SendMessage message = new SendMessage();
        message.setChatId(chatId);
        message.setText(text);
        message.setParseMode("Markdown");
        execute(message);
    }

    private void answerCallbackQuery(org.telegram.telegrambots.meta.api.objects.CallbackQuery query) throws TelegramApiException {
        org.telegram.telegrambots.meta.api.methods.AnswerCallbackQuery answer = new org.telegram.telegrambots.meta.api.methods.AnswerCallbackQuery();
        answer.setCallbackQueryId(query.getId());
        execute(answer);
    }

    private static class BotState {
        private boolean awaitingGameInput = false;
        private boolean awaitingScheduleDate = false;
        private long postIdForSchedule = 0;

        public boolean isAwaitingGameInput() { return awaitingGameInput; }
        public void setAwaitingGameInput(boolean value) { this.awaitingGameInput = value; }

        public boolean isAwaitingScheduleDate() { return awaitingScheduleDate; }
        public void setAwaitingScheduleDate(long postId) {
            this.awaitingScheduleDate = true;
            this.postIdForSchedule = postId;
        }
    }
}
