import json
import io
from server import server
import face_ditector
from PIL import Image
from database_manager import database_manager
from lock_manager import lock_manager

database = database_manager()
lock_managar = lock_manager()


""" 
 called each time event has accrued and handel it

 :param data - json with the data of the event 
 :param socket - the socket of the user
 :param server - instance of the server in order to use it functions

"""
def handel_event(data, socket, server):
    res = None

    state = data["state"]
    value = data["data"]
    code = data["id"]

    if state == "check_image":
        image = get_image_event(socket, value)
        res = check_image_event(image, code)
        lock_socket = lock_managar.get_socket(code)

        if res:
            print("log: checking result", res)
            server.send(socket, "open")
            server.send(lock_socket, "open")

    elif state == "insert_image":
        image = get_image_event(socket, value)
        insert_image_event(image, code)
        print("log: image added")

    elif state == "add_lock":
        lock_managar.add_lock(code, socket)
        print("log: lock added")

    elif state == "close_lock":
        server.send(socket, "close")
        lock_socket = lock_managar.get_socket(code)
        server.send(lock_socket, "close")
        print("log: lock closed")


""" 
 handel event of user checking if face is in the database 

 :param image - the image to check
 :param code - the code of the user

 :return the result of the comparison

"""
def check_image_event(image, code):

    # resize the image in order to make it same as the others
    image = face_ditector.resize_image(image)

    # encode the image in order to compare it
    encoded_image = face_ditector.encode_image(image)

    # get the images of the users from the database to check from them
    encoding_images_array = database.get_images(code)

    # compare the image list with the given image
    res = face_ditector.check_face(encoded_image, encoding_images_array)

    return res

""" 
 handel event of user adding face to the database 

 :param image - the image to check
 :param code - the code of the user

"""
def insert_image_event(image, code):

    # resize the image in order to make it same as the others
    image = face_ditector.resize_image(image)

    # encode the image in order to save it
    encoded_image = face_ditector.encode_image(image)

    # add the image to the database
    database.insert_image(code, encoded_image)

""" 
 handel event of getting image from the user

 :param socket - the socket of the user
 :param image_length - the length of the image

 :return the image from the user

"""
def get_image_event(socket, image_length):

    # get the image
    msg = server.read_socket(socket, image_length)

    # convert the image bits to png
    image1 = io.BytesIO(msg)
    image = Image.open(image1).convert("RGB")

    return image




















