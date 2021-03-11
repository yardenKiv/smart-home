from threading import Lock

class lock_manager:

    def __init__(self):
        self.lock_dict = {}
        self.lock_dict_mutex = Lock()

    def add_lock(self, code, socket):
        self.lock_dict_mutex.acquire()
        self.lock_dict[code] = socket
        self.lock_dict_mutex.release()

    def get_socket(self, code):
        self.lock_dict_mutex.acquire()

        if code not in self.lock_dict.keys():
            self.lock_dict_mutex.release()
            raise Exception("invalid lock code", code)

        socket = self.lock_dict[code]
        self.lock_dict_mutex.release()

        return socket










