import utime
from machine import Pin
import network
import socket
import RSA
import json

LOCK_PIN_NUMBER = 2

WIFI_NAME = "CTS-B54F-WIFI-2.4GHz"
WIFI_PASSWORD = "0006192CB54F"

SERVER_PORT = 11223
SERVER_IP = "34.105.163.102"

DEVICE_ID = 1


def connect_wifi(wifi_name, wifi_password):


    

    sta_if = network.WLAN(network.STA_IF)

    if not sta_if.isconnected():
        print('connecting to network...')

        sta_if.active(True)
        sta_if.connect(wifi_name, wifi_password)

        while not sta_if.isconnected():
            pass






def connect_server(ip, port):
    pin = Pin(2, Pin.OUT)
    pin.off()


    address_info = socket.getaddrinfo(ip, port)
    address_info = address_info[0][-1]

    server_socket = socket.socket()
    server_socket.connect(address_info)

    client_private_key, client_public_key = RSA.create_rsa_keys()
    print(client_private_key, client_public_key)
        
    server_socket.send(json.dumps(client_public_key).encode())
        
    server_public_key = server_socket.recv(1024).decode('utf-8')
    server_public_key = json.loads(server_public_key)
        
    keys = {"server_public_key": server_public_key, "client_private_key": client_private_key}
    print(keys)
        
    return server_socket, keys


def send_add_lock(server_socket, keys):
    msg_json = {}
        
    msg_json["data"] = 1;
    msg_json["state"] = "add_lock";
    msg_json["id"] = 1;
    
    msg = json.dumps(msg_json)
    encoed_msg = RSA.encrypt_text(keys["server_public_key"], msg).encode()
    server_socket.send(encoed_msg)

def main():
    lock_pin = Pin(LOCK_PIN_NUMBER, Pin.OUT)
    print("start")

    msg = "-"
    
    connect_wifi(WIFI_NAME, WIFI_PASSWORD)
    server_socket, keys = connect_server(SERVER_IP, SERVER_PORT)
    
    send_add_lock(server_socket, keys)
    
    while msg != "":
        msg = server_socket.recv(1024).decode('utf-8')
        msg = RSA.decrypt_text(keys["client_private_key"], msg)
        print(msg)
        
        if msg == "close":
            lock_pin.off()
        
        if msg == "open":
            lock_pin.on()
            

    print("end")

if __name__ == "__main__":
    main()








