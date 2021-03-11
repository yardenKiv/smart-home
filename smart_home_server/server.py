from threading import Thread, Lock, Condition
import RSA
from _thread import *
import socket
import time
import json
import XOR

JSON_START = 123
JSON_START_CHAR = "{"
RECOVER_DATA_BYTES_SIZE = 1000000
DISCONNECT_MSG = """{"state": "delete_user", "data": "-", "id": "-"}"""

class server:

    def __init__(self, ip, port, event_handler_function):
        self.connected_clients = {}
        self.events = list()
        self.text_list = list()

        self.ip = ip
        self.port = port

        self.server_socket = None
        self.create_socket()

        self.connected_clients_mutex = Lock()
        self.events_mutex = Lock()

        self.lock = Lock()
        self.thread_pool_mutex = Condition(self.lock)

        self.event_handler_function = event_handler_function
        self.curr_event = None

        self.skip_sockets = []
        self.skip_sockets_mutex = Lock()

    """
     create the socket of the server
    """
    def create_socket(self):
        self.server_socket = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
        self.server_socket.bind((self.ip, self.port))
        self.server_socket.listen(5)

    """
     open the threads of the functions of the server
     
     :param number_of_threads - the number of threads that handel events 
    """
    def run(self, number_of_threads):

        start_new_thread(self.listen_for_clients, ())
        start_new_thread(self.listen_for_events, ())

        for i in range(number_of_threads):
            start_new_thread(self.thread_function, (i, ))

    print("log: server running")

    """
     listen for new clients and add them to the server data
    """
    def listen_for_clients(self):

        user_device = ""

        while True:

            # wait for new sockets
            new_socket, addr = self.server_socket.accept()
            print("got client")

            # fix bug with socket on android studio
            start = new_socket.recv(2)

            # find witch device the user is using
            if start[0] == JSON_START:
                user_device = "esp"
            else:
                user_device = "android"

            # get client key
            client_public_key = new_socket.recv(1024)
            client_public_key = client_public_key.decode('utf-8')

            # fix bug with socket on android studio
            if user_device == "esp":
                client_public_key = start.decode('utf-8') + client_public_key

            # get the public key of the client
            client_public_key = json.loads(client_public_key)

            # create the keys of the server
            server_private_key, server_public_key = RSA.create_rsa_keys()

            # send public key of the server back
            new_socket.send(json.dumps(server_public_key).encode())

            # remove the blocking of the user socket in order to use it in a thread pool server
            new_socket.setblocking(0)

            # save the data of the rsa encryption
            rsa_json = {"client_public_key": client_public_key,
                        "server_private_key": server_private_key}

            # add new socket
            self.add_connected_clients(new_socket, rsa_json)

            print('keys ', rsa_json)
            print('log: Connected to :', addr[0], ':', addr[1])


    """
    wait for new events from the other clients
    """
    def listen_for_events(self):

        user_device = ""

        while True:


            # get the current connect clients
            copy_connected_clients = self.copy_connected_clients()

            # go over all the connected sockets
            for curr_socket in copy_connected_clients.keys():

                try:
                    if curr_socket in self.copy_skip_socket():
                        continue

                    # read socket
                    data = curr_socket.recv(1024)

                    # get the data of the connection
                    connection_data = copy_connected_clients[curr_socket]
                    curr_socket.setblocking(1)

                    # if data was found stop the socket so the client will be able to use it
                    self.add_skip_socket(curr_socket)

                    # handel the decode even if the client send invalid data
                    try:

                        # create event
                        new_event = (data.decode('utf-8'), curr_socket, connection_data)
                    except UnicodeDecodeError:

                        # fix bug with socket on android studio
                        data = data[2:]

                        # create event
                        new_event = (data.decode('utf-8'), curr_socket, connection_data)
                        print("log: new event", new_event)

                    # add event to be handheld
                    self.push_event(new_event)

                    # notify one of the event handler functions
                    with self.thread_pool_mutex:
                        self.thread_pool_mutex.notify()

                # catch if the socket didn't get any data
                except BlockingIOError as e:
                    time.sleep(0.001)

    """
     warp the function that handel event that was given to the server
    """
    def thread_function(self, thread_id):

        user_device = ""

        while True:

            # wait for new event to be added
            with self.thread_pool_mutex:
                self.thread_pool_mutex.wait()

            # get the event
            print("log: start event")
            (msg, client_socket, connection_data) = self.pop_event()

            # decrypt the data of the event
            print("log: decrypt data")
            text = self.decrypt_msg(msg, connection_data)
            print("log: event data", text)

            # check witch device the user is using
            if text[0] == JSON_START_CHAR:
                user_device = "esp"
            else:
                user_device = "android"

            if user_device == "android":
                text = text[2:]

            # run given function the handel the event
            print("log: start main function")
            self.event_handler_function((text, client_socket), self)
            print("log: end main function")

            # if the user send disconnect msg remove him from the server
            if not msg or msg == DISCONNECT_MSG:
                self.remove_connected_clients(client_socket)

            # remove blocking of the socket
            client_socket.setblocking(0)

            # return the socket to be checked
            self.remove_skip_socket(client_socket)
            print("log: end event")



    """
     send msg to the client
     
     :param soc - the socket to send the msg to
     :param msg - the msg to send
    """
    def send(self, soc, msg):
        print("log: ", self.connected_clients)
        rsa_data = self.get_data_connected_clients(soc)

        text = RSA.encrypt_text(rsa_data["client_public_key"], msg)
        soc.send(text.encode())

    """
     read the given socket
     
     :param socket - the socket to read
     :param length - the number of character to read
     
     :return the data that was red from the socket
    """
    @staticmethod
    def read_socket(socket, length):
        image_length = int(length)

        msg = bytearray()

        while True:
            data = socket.recv(RECOVER_DATA_BYTES_SIZE)
            msg = msg + data

            if len(msg) == image_length:
                break

        print("got image")

        return msg


    # functions to get to shard resources with mutexes
    def add_skip_socket(self, socket):
        self.skip_sockets_mutex.acquire()
        self.skip_sockets.append(socket)
        self.skip_sockets_mutex.release()

    def remove_skip_socket(self, socket):
        self.skip_sockets_mutex.acquire()

        try:
            self.skip_sockets.pop(socket)
        except Exception:
            pass

        self.skip_sockets_mutex.release()

    def copy_skip_socket(self):
        self.skip_sockets_mutex.acquire()
        copy = self.skip_sockets
        self.skip_sockets_mutex.release()

        return copy

    def get_data_connected_clients(self, socket):

        self.connected_clients_mutex.acquire()
        rsa_data = self.connected_clients[socket]
        self.connected_clients_mutex.release()

        return rsa_data

    def add_connected_clients(self, socket, data):
        self.connected_clients_mutex.acquire()
        print("con1:", self.connected_clients)
        self.connected_clients[socket] = data
        print("con2:", self.connected_clients)
        self.connected_clients_mutex.release()

    def copy_connected_clients(self):
        self.connected_clients_mutex.acquire()
        connected_clients_copy = self.connected_clients.copy()
        self.connected_clients_mutex.release()

        return connected_clients_copy

    def remove_connected_clients(self, socket):
        self.connected_clients_mutex.acquire()

        try:
            self.connected_clients.pop(socket)
        except Exception:
            pass

        self.connected_clients_mutex.release()

    def pop_event(self):
        self.events_mutex.acquire()
        event = self.events.pop(0)
        self.events_mutex.release()

        return event

    def push_event(self, event):
        self.events_mutex.acquire()
        self.events.append(event)
        self.events_mutex.release()


    """
     take message and decrypt it
     
     :param msg - the msg to decrypt
     :param rsa_data - the rsa keys
     
     :return the decrypted msg
    """
    def decrypt_msg(self, msg, rsa_data):
        text = ""

        if msg:
            text = RSA.decrypt_text(rsa_data["server_private_key"], msg)
            print("log: data msg", text)

        # return disconnect msg
        else:
            text = DISCONNECT_MSG
        return text









