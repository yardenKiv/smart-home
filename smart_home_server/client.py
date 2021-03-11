import socket
import json
import RSA

class client:
    def __init__(self, ip, port):
        self.client_socket = socket.socket()

        self.ip = ip
        self.port = port

        self.rsa_keys = {}

    def connect(self):
        client_private_key, client_public_key = RSA.create_rsa_keys()

        self.client_socket.connect((self.ip, self.port))

        # send the public key to the server
        self.client_socket.send(json.dumps(client_public_key).encode())

        # get the public key of the server
        server_public_key = self.client_socket.recv(1024).decode('utf-8')
        server_public_key = json.loads(server_public_key)

        self.rsa_keys["client_private_key"] = client_private_key
        self.rsa_keys["server_public_key"] = server_public_key




    def send(self, msg):
        enc = RSA.encrypt_text(self.rsa_keys["server_public_key"], msg)
        self.client_socket.send(enc.encode())

    def read(self):
        encrypted_msg = self.client_socket.recv(1024).decode('utf-8')
        print(self.rsa_keys["client_private_key"], encrypted_msg)
        return RSA.decrypt_text(self.rsa_keys["client_private_key"], encrypted_msg)


c = client("127.0.0.1", 11223)
c.connect()

c.send("ping")
print(c.read())

while True:
    pass


















