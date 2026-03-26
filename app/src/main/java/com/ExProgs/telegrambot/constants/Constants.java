package com.ExProgs.telegrambot.constants;

import com.ExProgs.telegrambot.BuildConfig;

public class Constants {
    public static final String TB_TOKEN = BuildConfig.TELEGRAM_BOT_TOKEN;
    public static final String STICKER_PACK = BuildConfig.STICKER_PACK;
    public static final String START = "Здравствуйте, это бот магазина MDCShop" +
            "\nвы можете посмотреть весь товар тут:" +
            "\nhttps://t.me/addstickers/" + STICKER_PACK +
            "\nесли вас что-то заинтересует то просто отправьте мне его и я вас проконсультирую";
    public static final String DEFAULT = "я вас не понимаю(" +
            "\n попробуйте отправить мне стикер из нашего магазина" +
            "\nhttps://t.me/addstickers/" + STICKER_PACK;
    public static final String FILE_API = "https://api.telegram.org/file/bot";
    public static final String NO_STUFF = "такого товара еще нет, посмотрите позже";
    public static final String CHANNEL_ID = "i never asked for this";
    public static final String NO = "нет";
    public static final String NULL_USERNAME = "пожалуйста, перед тем как что-то делать в магазине" +
            " создайте себе уникальное имя в настройках аккаунта телеграм";
}
