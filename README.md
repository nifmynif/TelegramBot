# TelegramBot

Android-приложение, которое запускает Telegram-бота в `ForegroundService` и хранит товары и заказы в локальной SQLite.

## Что нужно для запуска

- JDK 17 или новее. Проект обновлен до build tooling, совместимого с Java 21.
- Android SDK и стандартная Android Studio/Gradle среда.
- Значения конфигурации через переменные окружения или Gradle properties.

## Обязательные параметры

Можно задать в `~/.gradle/gradle.properties`:

```properties
TELEGRAM_BOT_TOKEN=your_bot_token
STICKER_PACK=your_sticker_pack
ADMIN_USERNAMES=admin_one,admin_two
```

Можно использовать и переменные окружения с теми же именами.

## Важно

- `TELEGRAM_BOT_TOKEN` больше не хранится в исходниках.
- Если `ADMIN_USERNAMES` не задан, в приложении не будет администраторов.
- Если `TELEGRAM_BOT_TOKEN` пустой, сервис запишет ошибку в лог и остановится.
