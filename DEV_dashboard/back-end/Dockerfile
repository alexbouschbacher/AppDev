FROM python:3

COPY . /back-end
WORKDIR /back-end
RUN pip3 install flask-redis
RUN pip3 install Flask
RUN pip3 install google-api-python-client
RUN pip3 install flask-cors
RUN pip3 install bson
RUN pip3 install google-auth-oauthlib
RUN pip3 install newsapi-python
RUN pip3 install pymongo
RUN pip3 install redis

EXPOSE 5000 27017 6379

CMD python3 /back-end/server.py
