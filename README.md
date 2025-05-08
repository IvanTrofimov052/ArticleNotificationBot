# Бот нотификации об новых статьях

## Доступные команды
- команда /add author_link; это команда добавляет автора в отслеживаемые авторы. Пример использования для https://habr.com/ru/users/yadro_team/publications/articles/ -
/add yadro_team
- команда /delete author_link; это команда убирает автора из отслеживаемых авторов. Пример использования для https://habr.com/ru/users/yadro_team/publications/articles/ -
  /delete yadro_team
- команда /list; это команда выводит список всех отслеживаемых авторов юзера

## Запуск
1. Заполнить .env
2. создать image docker для питона
```
docker build -t flask-app article_notification_server
```
3. создать image docker для scala
```
sbt docker:stage
sbt docker:publishLocal
```
4. выполнить docker compose up
```
docker compose up
```