#!/usr/bin/env python3

import datetime
import pickle
import os.path
import os
import hashlib, binascii, os
from bson import ObjectId

from googleapiclient.discovery import build
from google_auth_oauthlib.flow import InstalledAppFlow
from google.auth.transport.requests import Request
from newsapi import NewsApiClient

from flask import Flask, request, make_response, redirect
# from flask_redis import FlaskRedis
from flask_cors import CORS
from redis import Redis
from pymongo import MongoClient
import requests as Requests
import json

addr_ip = "localhost:8080"

app = Flask(__name__)
CORS(app)
# redis = FlaskRedis(app)
redis = Redis(host='redis', port=6379)
session = Requests.Session()
googleScope = ['https://www.googleapis.com/auth/calendar.readonly']
mongo = MongoClient('172.17.0.1', 27017)


services = [{
    'name' : 'intra',
    'widgets' : [{
        'name' : 'calendar',
        'description' : 'Display agenda by day/week/month',
        'params' : [{
            'name' : 'duration',
            'type' : 'string'
        }]
    }, {
        'name' : 'notes',
        'description' : 'Display notes of the module M',
        'params' : [{
            'name' : 'module',
            'type' : 'string'
        }]
    }]
}, {
    'name' : 'yammer',
    'widgets' : [{
        'name' : 'messages',
        'description' : 'Display N last messages of the channel C',
        'params' : [{
            'name' : 'limit',
            'type' : 'number'
        }, {
            'name' : 'channel',
            'type' : 'string'
        }]
    }]
}, {
    'name' : 'outlook',
    'widgets' : [{
        'name' : 'mails',
        'description' : 'Display N last mails from mail M',
        'params' : [{
            'name' : 'max',
            'type' : 'number'
        }, {
            'name' : 'from',
            'type' : 'string'
        }]
    }]
}, {
    'name' : 'calendar',
    'widgets' : [{
        'name' : 'calendar',
        'description' : 'Display N next events',
        'params' : [{
            'name' : 'max',
            'type' : 'number'
        }]
    }]
}, {
    'name' : 'news',
    'widgets' : [{
        'name' : 'theme',
        'description' : 'Display last news about query Q',
        'params' : [{
            'name' : 'query',
            'type' : 'string'
        }]
    }, {
        'name' : 'author',
        'description' : 'Display last news write by author A',
        'params' : [{
            'name' : 'authors',
            'type' : 'string'
        }]
    }]
}, {
    'name' : 'trello',
    'widgets' : [{
        'name' : 'board',
        'description' : 'Display all assigned cards in board B',
        'params' : [{
            'name' : 'board',
            'type' : 'string'
        }]
    }, {
        'name' : 'ending',
        'description' : 'Display all assigned cards ending until time T',
        'params' : [{
            'name' : 'time',
            'type' : 'string'
        }]
    }]
}]

class Yammer:
  def __init__(self):
    self.client = "X"
    self.secret = "Y"
    self.redirect = "http://localhost:5000/yammer/login"

yammer = Yammer()

class Outlook:
  def __init__(self):
    self.client = "X"
    self.secret = "Y"
    self.redirect = "http://localhost:5000/outlook/code"

outlook = Outlook()

googleNews = NewsApiClient(api_key='X')

class Trello:
  def __init__(self):
    self.client = "X"
    self.secret = "Y"
    self.redirect = "http://localhost:8080/profil"

trello = Trello()

def hash_password(password):
    salt = hashlib.sha256(os.urandom(60)).hexdigest().encode('ascii')
    pwdhash = hashlib.pbkdf2_hmac('sha512', password.encode('utf-8'),
    salt, 100000)
    pwdhash = binascii.hexlify(pwdhash)
    return (salt + pwdhash).decode('ascii')

def verify_password(stored_password, provided_password):
    salt = stored_password[:64]
    stored_password = stored_password[64:]
    pwdhash = hashlib.pbkdf2_hmac('sha512',
    provided_password.encode('utf-8'),
    salt.encode('ascii'),
    100000)
    pwdhash = binascii.hexlify(pwdhash).decode('ascii')
    return pwdhash == stored_password

class User:
    def __init__(self, user):
        self.lastName = user["lastName"]
        self.firstName = user["firstName"]
        self.mail = user["mail"]
        self.password = hash_password(user["password"])
        self.intra = {
            "autologin" : "",
            "mail" : "",
            "token" : "",
            "log" : False
        }
        self.yammer = {
            "code" : "",
            "token" : "",
            "id" : "",
            "log" : False
        }
        self.outlook = {
            "code" : "",
            "token" : "",
            "log" : False
        }
        self.calendar = {
            "file" : "",
            "log" : False
        }
        self.trello = {
            "token" : "",
            "log" : False
        }
        self.services = []

def res(status, data, types):
    response = make_response(data)
    response.status_code = status
    response.mimetype = types
    return response

def getDb():
    return mongo.db

def setUserRedis(user):
    key = hash_password(user['password'] + user['_id'])
    redis.set(key, user['_id'], 604800) #1 week expiration
    redis.save()
    return key

def getUserRedis(token):
    db = getDb()
    userDb = db['Users']
    userId = redis.get(token)
    print(userId)
    if (userId == None):
        return None
    userId = ObjectId(userId.decode("utf-8"))
    exist = userDb.find_one({"_id" : userId})
    return (exist)


@app.route('/')
def index():
    return "Welcome to Dashboard API"



@app.route('/user/signup', methods=['POST'])
def user_signup():
    req = request.json
    db = getDb()
    userDb = db['Users']
    newUser = User(req)
    user = newUser.__dict__
    exist = userDb.find_one({"mail" : user["mail"]})
    if (exist == None):
        userId = userDb.insert_one(user)
        user["_id"] = str(user["_id"])
        return res(200, {"user": user}, "application/json")
    return res(403, {"error": "UserAlreadyExist"}, "application/json")

@app.route('/user/signin', methods=['POST'])
def user_signin():
    req = request.json
    db = getDb()
    userDb = db['Users']
    user = userDb.find_one({"mail" : req["mail"]})
    if (user == None):
        return res(403, {"error": "UserDoesntExist"}, "application/json")
    if (verify_password(user["password"], req["password"]) == False):
        return res(403, {"error": "WrongPassword"}, "application/json")
    user['_id'] = str(user['_id'])
    token = setUserRedis(user)
    del user['password']
    return res(200, {"user": user, "token" : token}, "application/json")

@app.route('/user/me', methods=['GET'])
def user_me():
    token = request.headers['authorization']
    user = getUserRedis(token)
    if (user == None):
        return res(402, {"error": "UserNotLogin"}, "application/json")
    del user['password']
    user['_id'] = str(user['_id'])
    return res(200, {"user": user}, "application/json")

@app.route('/user/suscribe', methods=['POST'])
def user_suscribe():
    token = request.headers['authorization']
    user = getUserRedis(token)
    if (user == None):
        return res(402, {"error": "UserNotLogin"}, "application/json")
    req = request.json
    exist = False
    try:
        for service in services:
            if (req['name'] == service['name']):
                my_widget = req['widget']
                for widget in service['widgets']:
                    if (widget['name'] == my_widget['name']):
                        newWidget = widget
                        newWidget['service'] = service['name']
                        my_params = my_widget['params']
                        for param in widget['params']:
                            my_param = my_params[param['name']]
                            if (param['type'] == 'string' and type(my_param) is str):
                                continue
                            if (param['type'] == 'number' and type(my_param) is int):
                                continue
                            raise()
                        exist = True
                        break
                break
    except:
        exist = False
    if (exist != True):
        return res(402, {"error": "InvalidWidget"}, "application/json")

    for param in newWidget['params']:
        my_param = my_params[param['name']]
        if (param['type'] == 'string' and type(my_param) is str):
            param['value'] = my_param
        if (param['type'] == 'number' and type(my_param) is int):
            param['value'] = my_param

    db = getDb()
    userDb = db['Users']

    newWidget['refresh'] = req['refresh']
    tab = user['services']
    tab.append(newWidget)

    userDb.update_one({"_id" : user['_id']}, {"$set" : {"services" : tab}})
    user['services'] = tab

    user['_id'] = str(user['_id'])
    return res(200, {"user": user}, "application/json")

@app.route('/user/modifyWidget', methods=['POST'])
def user_modifyWidget():
    token = request.headers['authorization']
    user = getUserRedis(token)
    if (user == None):
        return res(402, {"error": "UserNotLogin"}, "application/json")
    req = request.json

    index = req['index']
    try:
        modService = user['services'][index]
    except:
        return res(402, {"error": "IndexOutOfRange"}, "application/json")
    newParams = req['params']
    refresh = req['refresh']
    exist = False
    try:
        for service in services:
            if (service['name'] == modService['service']):
                for widget in service['widgets']:
                    if (widget['name'] == modService['name']):
                        baseParams = widget['params']
                        break;
                break;
        for param in baseParams:
            my_param = newParams[param['name']]
            if (param['type'] == 'string' and type(my_param) is str):
                continue
            if (param['type'] == 'number' and type(my_param) is int):
                continue
            raise()
        exist = True
    except:
        exist = False
    if (exist != True):
        return res(402, {"error": "InvalidWidget"}, "application/json")

    db = getDb()
    userDb = db['Users']

    modService['refresh'] = refresh
    for param in modService['params']:
        my_param = newParams[param['name']]
        if (param['type'] == 'string' and type(my_param) is str):
            param['value'] = my_param
        if (param['type'] == 'number' and type(my_param) is int):
            param['value'] = my_param

    user['services'][index] = modService
    userDb.update_one({"_id" : user['_id']}, {"$set" : {"services" : user['services']}})

    user['_id'] = str(user['_id'])
    return res(200, {"user": user}, "application/json")

@app.route('/user/unsuscribe', methods=['POST'])
def user_unsuscribe():
    token = request.headers['authorization']
    user = getUserRedis(token)
    if (user == None):
        return res(402, {"error": "UserNotLogin"}, "application/json")
    req = request.json

    index = req['index']
    try:
        delService = user['services'][index]
    except:
        return res(402, {"error": "IndexOutOfRange"}, "application/json")

    db = getDb()
    userDb = db['Users']

    (user['services']).remove(delService)
    userDb.update_one({"_id" : user['_id']}, {"$set" : {"services" : user['services']}})

    user['_id'] = str(user['_id'])
    return res(200, {"user": user}, "application/json")

@app.route('/user/modify', methods=['POST'])
def user_modify():
    token = request.headers['authorization']
    user = getUserRedis(token)
    if (user == None):
        return res(402, {"error": "UserNotLogin"}, "application/json")
    req = request.json
    print(req)
    db = getDb()
    userDb = db['Users']
    userDb.update_one({"_id" : user['_id']}, {"$set" : req})
    user = userDb.find_one({"_id" : user['_id']})
    del user['password']
    user['_id'] = str(user['_id'])
    return res(200, {"user": user}, "application/json")


@app.route('/about.json')
def about():
    return res(200,
    {
        'customer' : {
            'host' : request.remote_addr
        },
        'server' : {
            'current_time' : int(datetime.datetime.timestamp(datetime.datetime.now())),
            'services' : services
        }
    }, "application/json")



@app.route('/intra/calendar', methods=['GET'])
def intra_calendar():
    auth = request.headers['authorization']
    user = getUserRedis(auth)
    if (user == None):
        return res(402, {"error": "UserNotLogin"}, "application/json")
    token = user['intra']['token']

    start = request.args.get("start")
    end = request.args.get("end")

    url = "https://intra.epitech.eu/planning/load?format=json&start=" + start + "&end=" + end
    r = Requests.get(url, cookies={'user' : token})
    calendar = r.json()
    registered = []
    for event in calendar:
        if event['event_registered'] != False:
            registered.append(event)
    return res(200, {"calendar" : registered}, "application/json")

@app.route('/intra/module', methods=['GET'])
def intra_module():
    auth = request.headers['authorization']
    user = getUserRedis(auth)
    if (user == None):
        return res(402, {"error": "UserNotLogin"}, "application/json")
    token = user['intra']['token']
    mail = user['intra']['mail']
    mod = request.args.get("module")
    url = "https://intra.epitech.eu/user/" + mail + "/notes/?format=json"
    r = Requests.get(url, cookies={'user' : token})
    if (mod != None):
        notes = r.json()["notes"]
        mod_notes = []
        for note in notes:
            if note["codemodule"] == mod:
                mod_notes.append(note)
        return res(200, {"notes" : mod_notes}, "application/json")
    else:
        return res(200, {"profil" : r.json()["modules"]}, "application/json")


@app.route('/intra/login', methods=['POST'])
def intra_login():
    req = request.json
    intra = req['url']
    print(intra)
    if (intra != None):
        r = session.get(intra)
        token = session.cookies.get_dict()['user']
        return res(200, {"token" : token}, "application/json")
    return res(400, {"error" : "No url"}, "application/json")



@app.route('/yammer/group', methods=['GET'])
def yammer_group():
    auth = request.headers['authorization']
    user = getUserRedis(auth)
    if (user == None):
        return res(402, {"error": "UserNotLogin"}, "application/json")
    token = user['yammer']['token']
    group_id = request.args.get("group")
    limit = request.args.get("limit")
    url = "https://www.yammer.com/api/v1/messages/in_group/" + group_id + ".json?threaded=true&limit=" + limit
    r = Requests.get(url, headers={'Authorization': "Bearer " + token})
    rep = r.json()
    return res(200, {"response" : rep}, "application/json")

@app.route('/yammer/groups', methods=['GET'])
def yammer_groups():
    auth = request.headers['authorization']
    user = getUserRedis(auth)
    if (user == None):
        return res(402, {"error": "UserNotLogin"}, "application/json")
    token = user['yammer']['token']
    my_id = yammer_me(token)
    url = "https://www.yammer.com/api/v1/groups/for_user/" + str(my_id) + ".json"
    r = Requests.get(url, headers={'Authorization': "Bearer " + token})
    rep = r.json()
    return res(200, {"groups" : rep}, "application/json")


def yammer_me(token):
    url = "https://www.yammer.com/api/v1/users/current.json"
    r = Requests.get(url, headers={'Authorization': "Bearer " + token})
    rep = r.json()
    yammer_id = rep["id"]
    return yammer_id

@app.route('/yammer/token', methods=['GET'])
def yammer_token():
    code = request.args.get("code")
    url = "https://www.yammer.com/oauth2/access_token?client_id=" + yammer.client + "&client_secret=" + yammer.secret + "&code=" + code + "&grant_type=authorization_code"
    r = Requests.post(url)
    token = r.json()["access_token"]["token"]
    return res(200, {"token" : token}, "application/json")

@app.route('/yammer/url', methods=['GET'])
def yammer_url():
    return (redirect("https://www.yammer.com/oauth2/authorize?client_id=" + yammer.client + "&response_type=code&redirect_uri=" + yammer.redirect))

@app.route('/yammer/login', methods=['GET'])
def yammer_login():
    code = request.args.get("code")
    if (code != None):
        #TODO add code to profile
        return redirect("http://localhost:8080/profil?code=" + code + "&service=yammer")
    return res(400, {"error" : "Code not found"}, "application/json")




@app.route('/outlook/mails', methods=['GET'])
def outlook_mails():
    auth = request.headers['authorization']
    user = getUserRedis(auth)
    if (user == None):
        return res(402, {"error": "UserNotLogin"}, "application/json")
    token = user['outlook']['token']

    maxNb = int(request.args.get("max"))
    fromMail = request.args.get("from")
    print(fromMail)
    if (maxNb != None and fromMail != None):
        url = "https://graph.microsoft.com/v1.0/me/mailfolders/inbox/messages?$top=1000&$filter=from/emailAddress/address eq '" + fromMail + "'"
        r = Requests.get(url, headers={'Authorization': "Bearer " + token})
        rep = r.json()
        try:
            return res(200, {"response" : rep['value'][::-1][:maxNb]}, "application/json")
        except:
            return res(400, {"error" : rep['error']}, "application/json")

    return res(400, {"error" : "Param not found"}, "application/json")

@app.route('/outlook/token', methods=['GET'])
def outlook_token():
    code = request.args.get("code")
    if (code != None):
        url = "https://login.microsoftonline.com/common/oauth2/v2.0/token"
        data = {
            'grant_type': 'authorization_code',
            'code' : code,
            'redirect_uri' : outlook.redirect,
            'client_id' : outlook.client,
            'client_secret' : outlook.secret
        }
        r = Requests.post(url, data)
        token = r.json()["access_token"]
        #add token to profile
        return res(200, {"token" : token}, "application/json")
    return res(400, {"error" : "Code not found"}, "application/json")

@app.route('/outlook/code', methods=['GET'])
def outlook_code():
    code = request.args.get("code")
    if (code != None):
        return redirect("http://localhost:8080/profil?code=" + code + "&service=outlook")
    return res(400, {"error" : "Code not found"}, "application/json")

@app.route('/outlook/login', methods=['GET'])
def outlook_login():
    return (redirect("https://login.microsoftonline.com/common/oauth2/v2.0/authorize?client_id=" + outlook.client + "&redirect_uri=" + outlook.redirect + "&response_type=code&scope=openid+Mail.Read"))



@app.route('/google/calendar', methods=['GET'])
def calendar():
    token = request.headers['authorization']
    user = getUserRedis(token)
    if (user == None):
        return res(402, {"error": "UserNotLogin"}, "application/json")
    my_id = str(user['_id'])
    # return (res(200, {"calendar" : []}, "application/json"))
    creds = get_creds(my_id)
    maxNb = request.args.get("max")
    if (creds == None):
        return (res(400, "Not login", "text/html"))

    service = build('calendar', 'v3', credentials=creds)

    # Call the Calendar API
    now = datetime.datetime.utcnow().isoformat() + 'Z'
    events_result = service.events().list(calendarId='primary', timeMin=now,
                                        maxResults=maxNb, singleEvents=True,
                                        orderBy='startTime').execute()
    events = events_result.get('items', [])

    return (res(200, {"calendar" : events}, "application/json"))

def get_creds(my_id):
    creds = None
    if os.path.exists('tokenGoogle' + my_id + '.pickle'):
        with open('tokenGoogle' + my_id + '.pickle', 'rb') as token:
            creds = pickle.load(token)
        if (not creds.valid):
            if creds and creds.expired and creds.refresh_token:
                creds.refresh(Request())
                with open('tokenGoogle' + my_id + '.pickle', 'wb') as token:
                    pickle.dump(creds, token)
    return creds

@app.route('/google/login', methods=['GET'])
def calendar_login():
    token = request.headers['authorization']
    user = getUserRedis(token)
    if (user == None):
        return res(402, {"error": "UserNotLogin"}, "application/json")
    my_id = str(user['_id'])
    creds = get_creds(my_id)
    if not creds:
        flow = InstalledAppFlow.from_client_secrets_file('credentials.json', googleScope)
        creds = flow.run_local_server(port=0)
        with open('tokenGoogle' + my_id + '.pickle', 'wb') as token:
            pickle.dump(creds, token)
    return (res(200, {"credentials": 'tokenGoogle' + my_id + '.pickle'}, "application/json"))

@app.route('/google/news', methods=['GET'])
def news():
    query = request.args.get("query")
    authors = request.args.get("authors")
    if (query != None):
        url = "https://newsapi.org/v2/everything?sortBy=publishedAt&q=" + query + "&apiKey=X"
        r = Requests.get(url)
        articles = r.json()["articles"][:5]
        return (res(200, {"articles" : articles}, "application/json"))
    elif (authors != None):
        url = "https://newsapi.org/v2/everything?sortBy=publishedAt&sources=" + authors + "&apiKey=X"
        r = Requests.get(url)
        articles = r.json()["articles"][:5]
        return (res(200, {"articles" : articles}, "application/json"))
    return (res(404, {"error" : "NotFound"}, "application/json"))


@app.route('/google/authors', methods=['GET'])
def authors():
    url = "https://newsapi.org/v2/sources?apiKey=X"
    r = Requests.get(url)
    return (res(200, {"authors" : r.json()}, "application/json"))

@app.route('/trello/login', methods=['GET'])
def trello_login():
    return (redirect("https://trello.com/1/authorize?expiration=30days&name=Dashboard&scope=read,account&response_type=token&key=" + trello.client + "&return_url=" + trello.redirect))

def get_trello_id(trello_token):
    url = "https://api.trello.com/1/members/me/?fields=id&key=" + trello.client + "&token=" + trello_token
    r = Requests.get(url)
    return (r.json()['id'])

@app.route('/trello/boards', methods=['GET'])
def get_boards():
    token = request.headers['authorization']
    user = getUserRedis(token)
    if (user == None):
        return res(402, {"error": "UserNotLogin"}, "application/json")
    trello_token = user['trello']['token']

    fields = ["name"]
    url = "https://api.trello.com/1/members/me/boards?fields=" + ",".join(fields) + "&key=" + trello.client + "&token=" + trello_token
    r = Requests.get(url)
    return (res(200, {"boards" : r.json()}, "application/json"))

@app.route('/trello/cards', methods=['GET'])
def get_cards_board():
    token = request.headers['authorization']
    user = getUserRedis(token)
    if (user == None):
        return res(402, {"error": "UserNotLogin"}, "application/json")
    trello_token = user['trello']['token']

    boardId = request.args.get("board")
    days = request.args.get("time")

    trello_id = get_trello_id(trello_token)

    if (boardId != None):
        url = "https://api.trello.com/1/boards/" + boardId + "/cards/?key=" + trello.client + "&token=" + trello_token
        r = Requests.get(url)
        cards = r.json()
        assigned_cards = []
        for card in cards:
            for member in card['idMembers']:
                if (member == trello_id):
                    assigned_cards.append(card)
        return (res(200, {"cards" : assigned_cards}, "application/json"))
    elif (days != None):
        days = int(days)
        boards = json.loads(get_boards().data.decode('utf-8'))['boards']
        all_cards = []
        now = datetime.datetime.timestamp(datetime.datetime.now())
        for board in boards:
            url = "https://api.trello.com/1/boards/" + board['id'] + "/cards/?key=" + trello.client + "&token=" + trello_token
            r = Requests.get(url)
            cards = r.json()
            for card in cards:
                date = card['dateLastActivity'][:-1].replace("T", " ")
                timestamp = datetime.datetime.timestamp(datetime.datetime.strptime(date, '%Y-%m-%d %H:%M:%S.%f'))
                delay = days * 86400
                if (now - delay < timestamp):
                    all_cards.append(card)
        return (res(200, {"cards" : all_cards}, "application/json"))
    return (res(400, {"error" : "No params to request"}, "application/json"))


if __name__ == '__main__':
    try:
        print(mongo.list_database_names())
    except:
        print("Mongo is down.")
    app.run(debug=True, host="0.0.0.0", port=5000)
