package com.algorand.app.service.core;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.telegram.telegrambots.bots.TelegramLongPollingBot;

import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.exceptions.TelegramApiRequestException;

import java.util.List;

//https://api.telegram.org/bot887649384:AAF5qPfrX7zNOorWgLkKWMoE37Ksv5V3Bxc/getUpdates

public class TelegramBot extends TelegramLongPollingBot {

    private Logger log = LoggerFactory.getLogger("TelegramBot");

    /**
     * Method for creating a message and sending it.
     * @param accountAddress chat id
     * @param transactionEnvelopeId The String that you want to send as a message.
     */
    public synchronized void sendMessage(String accountAddress, String transactionEnvelopeId ) {

        SendMessage sendMessage = new SendMessage();
        sendMessage.enableMarkdown(false);
        sendMessage.setChatId(accountAddress);
        sendMessage.setText(transactionEnvelopeId);

        try {
            System.out.println("sending message: " + sendMessage);
            Message response = execute(sendMessage);
            log.info("response: " + response);
        } catch (TelegramApiRequestException e) {
            log.error("TelegramApiRequestException: ", e.getApiResponse());
        } catch (TelegramApiException e) {
            log.error("TelegramApiException: ", e);
        }
    }

    /**
     * Method for receiving messages.
     * @param update Contains a message from the user.
     */
    @Override
    public void onUpdateReceived(Update update) {

        String message = update.getMessage().getText();
        System.out.println("onUpdateReceived message: " + message);

        // We check if the update has a message and the message has text
        if (update.hasMessage() && update.getMessage().hasText()) {



            String response = "echo: " + update.getMessage().getText();
            System.out.println("sending response " + response);
            SendMessage message2 = new SendMessage() // Create a SendMessage object with mandatory fields
                    .setChatId(update.getMessage().getChatId())
                    .setText(response);
            try {
                execute(message2); // Call method to send the message
            } catch (TelegramApiException e) {
                e.printStackTrace();
            }
         }
    }

    @Override
    public void onUpdatesReceived(List<Update> updates) {

        for (Update update :  updates)
             {

                 System.out.println("onUpdatesReceived message: " + update);
                 SendMessage sendMessage = new SendMessage();
                 sendMessage.enableMarkdown(false);
                 sendMessage.setChatId(update.getChannelPost().getChatId());
                 sendMessage.setText("echo " + update.getChannelPost().getText());

                 try {
                     System.out.println("sending message: " + sendMessage);
                     Message response = execute(sendMessage);
                     log.info("response: " + response);
                 } catch (TelegramApiRequestException e) {
                     log.error("TelegramApiRequestException: ", e.getApiResponse());
                 } catch (TelegramApiException e) {
                     log.error("TelegramApiException: ", e);
                 }
             }

    }

    /**
     * This method returns the bot's name, which was specified during registration.
     * @return bot name
     */
    @Override
    public String getBotUsername() {
        return "trxgatewaybot";
    }

    /**
     * This method returns the bot's token for communicating with the Telegram server
     * @return the bot's token
     */
    @Override
    public String getBotToken() {
        return "887649384:AAF5qPfrX7zNOorWgLkKWMoE37Ksv5V3Bxc";
    }

}
