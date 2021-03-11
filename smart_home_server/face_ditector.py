import face_recognition
import cv2
import numpy as np

PICTURE_WIDTH = 800
PICTURE_HEIGHT = 1200
ACCURACY_LEVEL = 0.4

"""
 the function resize the image to the constant size

 :param image - the image to resize  

 :return the resized image
"""
def resize_image(image):
    new_size = (PICTURE_WIDTH, PICTURE_HEIGHT)
    img = image.resize(new_size)
    open_cv_image = np.array(img)
    rgb_image = open_cv_image[:, :, ::-1]

    return rgb_image

"""
 encode the image in order to compare or save it
 
 :param image - the image to encode 
 :param 
 
 :return the encoded image
"""
def encode_image(image):
    image_encoding = None
    image_encodings = face_recognition.face_encodings(image)

    if len(image_encodings) > 0:
        image_encoding = image_encodings[0]
    else:
        raise Exception("no faces found")

    return image_encoding

"""
 compare list of images encoding in order to find if the images contain the same face 

 :param encoding_image - one image to compare to the list
 :param encoding_images_array - list of images to be compared

 :return if the person in the single picture is in one of the other pictures 
"""
def check_face(encoding_image, encoding_images_array):

    # go over all the images
    for curr_image_encoding in encoding_images_array:

        # compare each face to the single face
        result = face_recognition.compare_faces([encoding_image], curr_image_encoding, ACCURACY_LEVEL)

        # if face is found quit
        if True in result:
            return True

    return False























