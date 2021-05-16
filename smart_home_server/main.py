from server import server
import RSA
import json
import Handler
import pymongo
from database_manager import database_manager


def func(event, server):

    json_data = json.loads(event.data)
    Handler.handel_event(json_data, event.client, server)



def main():



    ser = server("", 11223, func)
    ser.run(5)



    while True:
        pass

if __name__ == '__main__':
    main()









