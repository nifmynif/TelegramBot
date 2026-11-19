package com.ExProgs.telegrambot.models;

import java.text.Normalizer;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Нормализация идентичности показа для поиска в БД.
 *
 * Проблема: показ идентифицировался точным совпадением сырых строк
 * "название + дата/время". Любое изменение на сайте (лишний пробел,
 * регистр, другой разделитель) порождало "новый" показ и ложную рассылку.
 *
 * Решение: сырые строки хранятся для отображения, а поиск и уникальность
 * строятся на нормализованных значениях.
 *
 * Название: NFKC, lower case, схлопывание пробелов.
 * Дата/время: извлекаются только группы цифр. Это устойчиво к пунктуации,
 * пробелам, регистру и дню недели ("Сб, 21.11.2026 19:00" и
 * "Суббота 21.11.2026, 19.00" дают один ключ). Формат самой даты
 * (цифры против названия месяца) должен быть стабилен на сайте.
 */
public class SessionNormalizer {
    private static final Pattern DIGITS = Pattern.compile("\\d+");
    private static final Pattern WHITESPACE = Pattern.compile("\\s+");

    private SessionNormalizer() {
    }

    public static String normalizeTitle(String title) {
        if (title == null) return "";
        String s = Normalizer.normalize(title, Normalizer.Form.NFKC);
        s = s.toLowerCase(Locale.ROOT).trim();
        return WHITESPACE.matcher(s).replaceAll(" ");
    }

    public static String normalizeDateTime(String dateTime) {
        if (dateTime == null) return "";
        String s = Normalizer.normalize(dateTime, Normalizer.Form.NFKC)
                .toLowerCase(Locale.ROOT);
        Matcher matcher = DIGITS.matcher(s);
        StringBuilder key = new StringBuilder();
        while (matcher.find()) {
            if (key.length() > 0) key.append('|');
            key.append(matcher.group());
        }
        if (key.length() == 0) {
            return WHITESPACE.matcher(s.trim()).replaceAll(" ");
        }
        return key.toString();
    }
}
