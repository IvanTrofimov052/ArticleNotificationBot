import requests
from bs4 import BeautifulSoup


class AuthorCheker:
    url = 'https://habr.com/ru/users/{author_link}/publications/articles/'

    @classmethod
    def check_author(cls, author_link, db_last_article):
        response = requests.get(cls.url.format(author_link=author_link))
        soup = BeautifulSoup(response.text, 'html.parser')
        last_article = soup.find('a', href=True, class_="tm-title__link")
        if (last_article != None):
            last_article = last_article['href']
        return (last_article != db_last_article), last_article
