package org.example;

import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.GetFile;
import org.telegram.telegrambots.meta.api.methods.send.*;
import org.telegram.telegrambots.meta.api.objects.*;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.*;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.io.File;
import java.net.URL;
import java.util.*;
import java.util.List;

public class StegoBot extends TelegramLongPollingBot {

    private final Map<Long, File> userImages = new HashMap<>();
    private final Map<Long, String> userModes = new HashMap<>();

    @Override
    public String getBotUsername() {
        return "BOT_NAME";
    }

    @Override
    public String getBotToken() {
        return "TOKEN";
    }

    @Override
    public void clearWebhook() {
        // отключаем удаление webhook
    }

    @Override
    public void onUpdateReceived(Update update) {
        if (!update.hasMessage()) return;

        Message msg = update.getMessage();
        long chatId = msg.getChatId();

        try {
            if (msg.hasText()) {
                String text = msg.getText();

                if (text.equalsIgnoreCase("/start")) {
                    showMainMenu(chatId);
                    return;
                }

                if (text.equals("🔐 Шифровать")) {
                    userModes.put(chatId, "encrypt");
                    sendMessage(chatId, "Отправьте одинчё PNG-файл как документ для шифрования.");
                    return;
                }

                if (text.equals("🔓 Расшифровать")) {
                    userModes.put(chatId, "decrypt");
                    sendMessage(chatId, "Отправьте один PNG-файл как документ для расшифровки.");
                    return;
                }

                // пользователь отправил текст
                if (userModes.getOrDefault(chatId, "").equals("encrypt") && userImages.containsKey(chatId)) {
                    String hiddenText = text;
                    File inputFile = userImages.get(chatId);

                    BufferedImage original = ImageIO.read(inputFile);
                    BufferedImage resized = resizeImage(original, 512, 512);

                    File outFile = new File("encoded_" + chatId + ".png");
                    StegoUtil.hideText(resized, hiddenText, outFile);

                    SendDocument doc = new SendDocument();
                    doc.setChatId(chatId);
                    doc.setDocument(new InputFile(outFile));
                    doc.setCaption("✅ Готово! Вот PNG с текстом.");
                    execute(doc);

                    userImages.remove(chatId);
                    userModes.remove(chatId);
                    return;
                }

                sendMessage(chatId, "❗ Пожалуйста, выберите режим: шифрование или расшифровка.");

            } else if (msg.hasDocument()) {
                Document doc = msg.getDocument();
                if (!doc.getFileName().toLowerCase().endsWith(".png")) {
                    sendMessage(chatId, "Пожалуйста, отправьте PNG-файл как документ.");
                    return;
                }

                GetFile getFile = new GetFile(doc.getFileId());
                org.telegram.telegrambots.meta.api.objects.File file = execute(getFile);
                String fileUrl = "https://api.telegram.org/file/bot" + getBotToken() + "/" + file.getFilePath();

                File downloaded = new File("input_" + chatId + ".png");
                try (InputStream in = new URL(fileUrl).openStream(); FileOutputStream out = new FileOutputStream(downloaded)) {
                    in.transferTo(out);
                }

                String mode = userModes.get(chatId);
                if (mode == null) {
                    sendMessage(chatId, "Сначала выберите режим: шифрование или расшифровка.");
                    return;
                }

                if (mode.equals("encrypt")) {
                    userImages.put(chatId, downloaded);
                    sendMessage(chatId, "Файл получен. Теперь отправьте текст для шифрования.");
                } else if (mode.equals("decrypt")) {
                    BufferedImage image = ImageIO.read(downloaded);
                    String extracted = StegoUtil.extractText(image, 500); // 500 символов макс.
                    sendMessage(chatId, "🔍 Извлечённый текст:\n\n" + extracted);
                    userModes.remove(chatId);
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
            sendMessage(chatId, "⚠️ Ошибка: " + e.getMessage());
        }
    }

    private void showMainMenu(long chatId) throws TelegramApiException {
        SendMessage msg = new SendMessage();
        msg.setChatId(chatId);
        msg.setText("Выберите режим:");

        ReplyKeyboardMarkup keyboard = new ReplyKeyboardMarkup();
        List<KeyboardRow> rows = new ArrayList<>();
        KeyboardRow row = new KeyboardRow();
        row.add(new KeyboardButton("🔐 Шифровать"));
        row.add(new KeyboardButton("🔓 Расшифровать"));
        rows.add(row);

        keyboard.setKeyboard(rows);
        keyboard.setResizeKeyboard(true);
        msg.setReplyMarkup(keyboard);

        execute(msg);
    }

    private void sendMessage(long chatId, String text) {
        try {
            execute(new SendMessage(String.valueOf(chatId), text));
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    private BufferedImage resizeImage(BufferedImage originalImage, int targetWidth, int targetHeight) {
        BufferedImage resizedImage = new BufferedImage(targetWidth, targetHeight, BufferedImage.TYPE_INT_ARGB);
        Graphics2D graphics2D = resizedImage.createGraphics();
        graphics2D.drawImage(originalImage, 0, 0, targetWidth, targetHeight, null);
        graphics2D.dispose();
        return resizedImage;
    }
}