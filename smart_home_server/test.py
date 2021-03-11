import socket
import time
import json
import RSA

HOST = '127.0.0.1'  # The server's hostname or IP address
PORT = 1235        # The port used by the server

with socket.socket(socket.AF_INET, socket.SOCK_STREAM) as s:

    client_private_key, client_public_key = RSA.create_rsa_keys()
    s.connect((HOST, PORT))


    s.send(json.dumps(client_private_key).encode())

    server_public_key = s.recv(1024).decode('utf-8')
    server_public_key = json.loads(server_public_key)

    rsa_json = {"client_private_key": client_private_key, "server_public_key": server_public_key}

    msg = "aaaaaa"
    enc = RSA.encrypt_text(server_public_key, msg)


    print("send")
    s.send(enc.encode())
    while True:
        pass







