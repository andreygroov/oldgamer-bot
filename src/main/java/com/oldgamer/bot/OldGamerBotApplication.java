package com.oldgamer.bot;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;

@SpringBootApplication
public class OldGamerBotApplication {
    private static final Logger logger = LoggerFactory.getLogger(OldGamerBotApplication.class);

    public static void main(String[] args) {
        ApplicationContext context = SpringApplication.run(OldGamerBotApplication.class, args);

        try {
            TelegramBotsApi botsApi = new TelegramBotsApi(DefaultBotSession.class);
            OldGamerBot bot = context.getBean(OldGamerBot.class);
            botsApi.registerBot(bot);
            logger.info("Old Gamer Bot started successfully");
        } catch (Exception e) {
            logger.error("Failed to start bot", e);
        }
    }
}
