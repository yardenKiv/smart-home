import pickle
import pymongo
import numpy
import drill



class database_manager:

    def __init__(self):

        self.mongo_client = pymongo.MongoClient("mongodb+srv://admin:admin@cluster0.wwoqh.mongodb.net/mainDB?retryWrites=true&w=majority")
        # self.mongo_client = pymongo.MongoClient('localhost', 27017)

        print("log: database ready")

        self.database = self.mongo_client["mainDB"]
        self.users_collection = self.database["users"]


    """
      add image to user from the database or create new user
     
     :param code - the code of the user to be added the image
     :param image - the image to save
    """
    def insert_image(self, code, image):
        if self.check_if_user_exist(code):
            self.add_image(code, image)

        else:
            self.add_user(code, image)

    """
     add new user to the database 
     
     :param code - the code of the new user
     :param image - the first image of the user
     
    """
    def add_user(self, code, image):
        post = {"_id": code, "image": [image.tolist()]}
        self.users_collection.insert_one(post)

    """
     add new image to the database 
     
     :param code - the code of the new user
     :param image - the first image of the user
     
    """
    def add_image(self, code, image):
        query = {'_id': code}
        post = {'$push': {'image': image.tolist()}}
        self.users_collection.update(query, post)

    """
     check if user exist in the database 
     
     :param code - the code of the user that is being checked
     
     :return if the user exist
    """
    def check_if_user_exist(self, code):
        res = self.users_collection.find({"_id": code})

        return res.count() > 0

    """
     get the images of user
     
     :param code - the code of the user 
     
     :return the images
    """
    def get_images(self, code):
        image_encoding_list = []

        results = self.users_collection.find({"_id": code})

        for result in results:
            for image_encoding in result["image"]:
                image_encoding_list.append(numpy.asarray(image_encoding))

        return image_encoding_list




