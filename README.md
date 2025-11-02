# Math Operations App

Консольное Java-приложение для выполнения арифметических операций с сохранением результатов в MySQL и экспортом в Excel.

## Функционал
✅ Вывод всех таблиц из MySQL  
✅ Создание таблицы `operations`  
✅ Сложение, вычитание, умножение, деление, модуль, степень  
✅ Сохранение результатов в базу данных  
✅ Экспорт всех операций в Excel (.xlsx)

## Как запустить
1. Установите [Java 25](https://jdk.java.net/) и [Maven](https://maven.apache.org/)
2. Настройте MySQL: создайте БД `math_operations_db`
3. Запустите: `mvn clean compile exec:java`

## Требования
- Java 25
- Maven 3.9+
- MySQL Server

Автор: lg71luda
