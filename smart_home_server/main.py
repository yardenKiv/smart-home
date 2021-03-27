from server import server
import RSA
import json
import Handler
import pymongo
from database_manager import database_manager


def func(event, server):

    json_data = json.loads(event[0])
    Handler.handel_event(json_data, event[1], server)



def main():



    ser = server("", 11223, func)
    ser.run(5)



    while True:
        pass

if __name__ == '__main__':
    main()









