package com.algorand.app.service.core;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.telegram.telegrambots.ApiContextInitializer;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

class TelegramBotTest {

    private Logger log = LoggerFactory.getLogger("TelegramBotTest");

    @org.junit.jupiter.api.Test
    void testTelegram() {
        sendMsg();
    }

    void sendMsg() {

        ApiContextInitializer.init();

        TelegramBotsApi botsApi = new TelegramBotsApi();

        TelegramBot telegramBot = new TelegramBot();
        try {
            log.info("registering telegramBot: " + telegramBot);
            botsApi.registerBot(telegramBot);
            log.info("successfully registered bot");
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
        log.info("sending message");
        telegramBot.sendMessage("-1001262740454", "hello world");
        log.info("successfully sent message");

        try {
            Thread.sleep(60000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}