from flask import Flask
from database_manager import *
from channel_cheker import *

import os

app = Flask(__name__)


@app.route('/get_messages')
def get_telegram_messages():
    db = DataBaseManager()
    authors_users = db.get_authors_users()[1]
    authors_with_last_article = dict()
    result = []
    for author_user in authors_users:
        author = author_user[1]
        last_article = author_user[2]
        authors_with_last_article[author] = AuthorCheker.check_author(
            author, last_article)
        if (authors_with_last_article[author][0]):
            result.append(
                {'chatId': author_user[0],
                 'message': f'https://habr.com{authors_with_last_article[author][1]}'
                 }
            )
            db.author_add_last_article(
                author, authors_with_last_article[author][1])

    return {'result': result}


if __name__ == '__main__':
    app.run(host='0.0.0.0', port=os.environ.get('ARTICLE_NOTIFICATION_PORT'))
