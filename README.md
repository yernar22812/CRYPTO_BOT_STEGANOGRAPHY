# 🕵️‍♂️ Steganography Telegram Bot By Yernar

This Telegram bot lets you **hide text inside an image** (encoding) and **extract hidden text from an image** (decoding) using basic steganography.

> ⚠️ **Note**: All images are resized to a **fixed size** during processing. If you send a large image, it will be **cropped or resized**, because currently the bot does not support varying image dimensions. This is necessary to avoid errors in image encoding/decoding.  
> ✅ Support for arbitrary image sizes is planned in future versions.

---
## 🔍 What is Steganography?

**Steganography** is the art and science of hiding information within other, seemingly harmless media (e.g., images, audio, or video). Unlike cryptography, which protects the content of a message, steganography hides the *existence* of the message itself.

> Example: You send a picture that looks normal, but it secretly contains a hidden message.

---

## 🧠 Steganography Method Used: LSB (Least Significant Bit)

This bot uses the **LSB steganography technique**, where each bit of the secret message is encoded into the least significant bits of image pixels. This method alters the image so slightly that the human eye cannot detect any difference.

---

## ⚙️ Features

- 🔐 **Encrypt mode** – Hide text inside an image
- 🔓 **Decrypt mode** – Extract hidden text from an image
- 🖼️ **Image formats supported** – PNG and JPEG (output saved as PNG)
- 🤖 **Telegram-friendly UI** – With inline keyboard and main menu navigation

---

## 🛠 Technologies Used

- **Java 17+**
- **Telegram Bot API** – via [TelegramBots](https://github.com/rubenlagus/TelegramBots)
- **Image manipulation** – `BufferedImage`, `ImageIO`
- **Encoding** – UTF-8 to binary, then embedded into pixel bits

---

## 🚀 How to Use

1. Start the bot with `/start`
2. Choose an operation:
   - 🔐 **Encrypt** to hide text in an image
   - 🔓 **Decrypt** to extract hidden text from an image
3. Follow the prompts:
   - For encryption: send an image as a **file**, then send the text
   - For decryption: send a stego image as a **file**
4. Receive:
   - A new image with the hidden message (PNG)
   - Or the extracted message (text)

---

## 🧪 Example Workflow

### Input:
- Original image (PNG)
- Message: `The password is swordfish`

### Output:
- Image with hidden message embedded
- Extracted message: `The password is swordfish`

 ## 📁 Project Structure

src/
└── org.example/
├── StegoBot.java ← Main Telegram bot class
└── StegoUtil.java ← Steganography logic (encode/decode)

## In class StegoBot
getBotUsername() -> type your @BotName
getBotToken() -> type your BotToken
