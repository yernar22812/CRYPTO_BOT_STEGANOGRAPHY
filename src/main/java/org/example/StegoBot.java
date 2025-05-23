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
        return "TYPE";
    }

    @Override
    public String getBotToken() {
        return "TYPE";
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

                if (text.equals("🔙 Main Menu")) {
                    userModes.remove(chatId);
                    userImages.remove(chatId);
                    showMainMenu(chatId);
                    return;
                }

                if (text.equals("🔐 Encrypt")) {
                    userModes.put(chatId, "encrypt");
                    showBackMenu(chatId, "Please send one PNG OR JPEG file as a document for encryption.");
                    return;
                }

                if (text.equals("🔓 Decrypt")) {
                    userModes.put(chatId, "decrypt");
                    showBackMenu(chatId, "Please send one PNG OR JPEG file as a document for decryption.");
                    return;
                }

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
                    doc.setCaption("✅ Done! Here is a PNG with hidden text.");
                    execute(doc);

                    userImages.remove(chatId);
                    userModes.remove(chatId);
                    showMainMenu(chatId);
                    return;
                }

                sendMessage(chatId, "❗ Please select the mode: encryption or decryption.");

            } else if (msg.hasDocument()) {
                Document doc = msg.getDocument();
                String fileName = doc.getFileName().toLowerCase();
                if (!fileName.endsWith(".png") && !fileName.endsWith(".jpg") && !fileName.endsWith(".jpeg")) {
                    sendMessage(chatId, "Please send PNG or JPEG file as a document.");
                    return;
                }
                GetFile getFile = new GetFile(doc.getFileId());
                org.telegram.telegrambots.meta.api.objects.File file = execute(getFile);
                String fileUrl = "https://api.telegram.org/file/bot" + getBotToken() + "/" + file.getFilePath();

                BufferedImage image = ImageIO.read(new URL(fileUrl));
                if (image == null) {
                    sendMessage(chatId, "Could not read image. Make sure it is a valid PNG or JPEG file.");
                    return;
                }

                File downloaded = new File("input_" + chatId + ".png");
                ImageIO.write(image, "PNG", downloaded);

                String mode = userModes.get(chatId);
                if (mode == null) {
                    sendMessage(chatId, "First, select the mode: encryption or decryption.");
                    return;
                }

                if (mode.equals("encrypt")) {
                    userImages.put(chatId, downloaded);
                    showBackMenu(chatId, "File received. Now send the text for encryption.");
                } else if (mode.equals("decrypt")) {
                    image = ImageIO.read(downloaded);
                    String extracted = StegoUtil.extractText(image, 500); // limit to 500 chars
                    sendMessage(chatId, "🔍 Hidden text:\n\n" + extracted);
                    userModes.remove(chatId);
                    showMainMenu(chatId);
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
            sendMessage(chatId, "⚠️ ERROR: " + e.getMessage());
        }
    }

    private void showMainMenu(long chatId) throws TelegramApiException {
        SendMessage msg = new SendMessage();
        msg.setChatId(chatId);
        msg.setText("Select mode:");

        ReplyKeyboardMarkup keyboard = new ReplyKeyboardMarkup();
        List<KeyboardRow> rows = new ArrayList<>();

        KeyboardRow row = new KeyboardRow();
        row.add(new KeyboardButton("🔐 Encrypt"));
        row.add(new KeyboardButton("🔓 Decrypt"));
        rows.add(row);

        keyboard.setKeyboard(rows);
        keyboard.setResizeKeyboard(true);
        msg.setReplyMarkup(keyboard);

        execute(msg);
    }

    private void showBackMenu(long chatId, String prompt) throws TelegramApiException {
        SendMessage msg = new SendMessage();
        msg.setChatId(chatId);
        msg.setText(prompt);

        ReplyKeyboardMarkup keyboard = new ReplyKeyboardMarkup();
        List<KeyboardRow> rows = new ArrayList<>();

        KeyboardRow row = new KeyboardRow();
        row.add(new KeyboardButton("🔙 Main Menu"));
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
