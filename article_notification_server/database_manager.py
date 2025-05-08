import os
import psycopg2


class DataBaseManager:
    def __init__(self):
        self.connection = psycopg2.connect(user=os.environ.get('POSTGRES_USER'), password=os.environ.get(
            'POSTGRES_PASSWORD'), host=os.environ.get('POSTGRES_HOST'), port=os.environ.get('POSTGRES_PORT'), database=os.environ.get('POSTGRES_NAME'))

    def call_foo_in_db(self, foo, params=()):
        with self.connection.cursor() as cursor:
            cursor.execute(f'select * from {foo}', params)
            self.connection.commit()
            return True, cursor.fetchall()

    def get_authors_users(self):
        return self.call_foo_in_db("user_authors.authors_users_f()")

    def author_add_last_article(self, author_link, author_last_article):
        return self.call_foo_in_db("user_authors.author_add_last_article_f(%s, %s)", (author_link, author_last_article))
