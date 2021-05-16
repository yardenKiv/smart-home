class Client:
    def __init__(self, socket, rsa_data):
        self.socket = socket
        self.rsa_data = rsa_data

    def __eq__(self, other):
        return self.rsa_data == other.rsa_data and self.socket == other.socket



