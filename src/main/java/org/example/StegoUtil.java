package org.example;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;
import java.nio.charset.StandardCharsets;

public class StegoUtil {

    public static void hideText(BufferedImage image, String text, File outputFile) throws Exception {
        byte[] data = (text + "\0").getBytes(StandardCharsets.UTF_8);
        if (data.length * 8 > image.getWidth() * image.getHeight()) {
            throw new Exception("Текст слишком длинный для этой картинки!");
        }

        int bitIndex = 0;
        outer:
        for (int y = 0; y < image.getHeight(); y++) {
            for (int x = 0; x < image.getWidth(); x++) {
                int rgb = image.getRGB(x, y);
                int blue = rgb & 0xFF;

                if (bitIndex < data.length * 8) {
                    int bit = (data[bitIndex / 8] >> (7 - (bitIndex % 8))) & 1;
                    blue = (blue & 0xFE) | bit;
                    bitIndex++;
                }

                int newRGB = (rgb & 0xFFFFFF00) | blue;
                image.setRGB(x, y, newRGB);

                if (bitIndex >= data.length * 8) break outer;
            }
        }

        ImageIO.write(image, "PNG", outputFile);
    }

    public static String extractText(BufferedImage image, int maxLength) throws Exception {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        int currentByte = 0;
        int bitIndex = 0;

        for (int y = 0; y < image.getHeight(); y++) {
            for (int x = 0; x < image.getWidth(); x++) {
                int rgb = image.getRGB(x, y);
                int blue = rgb & 0xFF;
                int bit = blue & 1;
                currentByte = (currentByte << 1) | bit;
                bitIndex++;

                if (bitIndex == 8) {
                    if (currentByte == 0) {
                        return baos.toString(StandardCharsets.UTF_8);
                    }
                    baos.write(currentByte);
                    bitIndex = 0;
                    currentByte = 0;

                    if (baos.size() >= maxLength) {
                        break;
                    }
                }
            }
        }

        return baos.toString(StandardCharsets.UTF_8);
    }
}
