import face_recognition
import cv2
import numpy as np
from imutils.object_detection import non_max_suppression
import pytesseract
import platform
import firebase_admin
from firebase_admin import db
import time

cred_obj = firebase_admin.credentials.Certificate('nursingbot-f41f8-firebase-adminsdk.json')
default_app = firebase_admin.initialize_app(cred_obj, {
        'databaseURL': 'https://nursingbot-f41f8-default-rtdb.firebaseio.com/'})
refStart = db.reference("/Movement")
print(refStart.child("desiredLoc").get())

if platform.system()== 'Windows':
    pytesseract.pytesseract.tesseract_cmd = 'C:\\Program Files\\Tesseract-OCR\\tesseract.exe'

# Load a sample picture and learn how to recognize it.
obama_image = face_recognition.load_image_file("dataset/Omar.jpeg")
obama_face_encoding = face_recognition.face_encodings(obama_image)[0]

# Load a second sample picture and learn how to recognize it.
biden_image = face_recognition.load_image_file("dataset/Uzair.jpeg")
biden_face_encoding = face_recognition.face_encodings(biden_image)[0]

# Create arrays of known face encodings and their names
known_face_encodings = [
    obama_face_encoding,
    biden_face_encoding
]
known_face_names = [
    "Omar",
    "Uzair"
]

# Initialize some variables
face_locations = []
face_encodings = []
face_names = []
count_frames = 0
face_detected=0
ocr_Confident = 0.5
padding = 0.05

def detect_faces(frame):
    global name
    # Resize frame of video to 1/4 size for faster face recognition processing
    # frame = cv2.resize(frame, (0, 0), fx=0.5, fy=0.5)

    # Convert the image from BGR color (which OpenCV uses) to RGB color (which face_recognition uses)
    rgb_small_frame = frame[:, :, ::-1]

    # Find all the faces and face encodings in the current frame of video
    face_locations = face_recognition.face_locations(rgb_small_frame)
    face_encodings = face_recognition.face_encodings(rgb_small_frame, face_locations)

    face_names = []
    for face_encoding in face_encodings:
        # See if the face is a match for the known face(s)
        matches = face_recognition.compare_faces(known_face_encodings, face_encoding)
        name = "Unknown"

        # Or instead, use the known face with the smallest distance to the new face
        face_distances = face_recognition.face_distance(known_face_encodings, face_encoding)
        best_match_index = np.argmin(face_distances)
        if matches[best_match_index]:
            name = known_face_names[best_match_index]

        face_names.append(name)

    # Display the results
    for (top, right, bottom, left), name in zip(face_locations, face_names):
        # Draw a box around the face
        cv2.rectangle(frame, (left - 20, top - 20), (right + 20, bottom + 20), (255, 0, 0), 2)

        # Draw a label with a name below the face
        cv2.rectangle(frame, (left - 20, bottom - 15), (right + 20, bottom + 20), (255, 0, 0), cv2.FILLED)
        font = cv2.FONT_HERSHEY_DUPLEX
        cv2.putText(frame, name, (left - 20, bottom + 15), font, 1.0, (255, 255, 255), 2)
        print("Name: ", name)

    return frame,name

def decode_predictions(scores, geometry):
	# grab the number of rows and columns from the scores volume, then
	# initialize our set of bounding box rectangles and corresponding
	# confidence scores
	(numRows, numCols) = scores.shape[2:4]
	rects = []
	confidences = []

	# loop over the number of rows
	for y in range(0, numRows):
		# extract the scores (probabilities), followed by the
		# geometrical data used to derive potential bounding box
		# coordinates that surround text
		scoresData = scores[0, 0, y]
		xData0 = geometry[0, 0, y]
		xData1 = geometry[0, 1, y]
		xData2 = geometry[0, 2, y]
		xData3 = geometry[0, 3, y]
		anglesData = geometry[0, 4, y]

		# loop over the number of columns
		for x in range(0, numCols):
			# if our score does not have sufficient probability,
			# ignore it
			if scoresData[x] < ocr_Confident:
				continue

			# compute the offset factor as our resulting feature
			# maps will be 4x smaller than the input image
			(offsetX, offsetY) = (x * 4.0, y * 4.0)

			# extract the rotation angle for the prediction and
			# then compute the sin and cosine
			angle = anglesData[x]
			cos = np.cos(angle)
			sin = np.sin(angle)

			# use the geometry volume to derive the width and height
			# of the bounding box
			h = xData0[x] + xData2[x]
			w = xData1[x] + xData3[x]

			# compute both the starting and ending (x, y)-coordinates
			# for the text prediction bounding box
			endX = int(offsetX + (cos * xData1[x]) + (sin * xData2[x]))
			endY = int(offsetY - (sin * xData1[x]) + (cos * xData2[x]))
			startX = int(endX - w)
			startY = int(endY - h)

			# add the bounding box coordinates and probability score
			# to our respective lists
			rects.append((startX, startY, endX, endY))
			confidences.append(scoresData[x])

	# return a tuple of the bounding boxes and associated confidences
	return (rects, confidences)

layerNames = [
	"feature_fusion/Conv_7/Sigmoid",
	"feature_fusion/concat_3"]

print("[INFO] loading EAST text detector...")
net = cv2.dnn.readNet('frozen_east_text_detection.pb')
config = ("-l eng --oem 1 --psm 7")
padding = 50

def Text_detection(image,ww,hh):
    global text
    orig = image.copy()
    (H, W) = image.shape[:2]
    (origH, origW) = image.shape[:2]
    print("%d  , %d" % (W, H))
    (newW, newH) = (ww, hh)

    rW = W / float(newW)
    rH = H / float(newH)
    image = cv2.resize(image, (newW, newH))

    (H, W) = image.shape[:2]
    # cv2.imshow("Original ", image)
    blob = cv2.dnn.blobFromImage(image, 1.0, (W, H),
                                 (123.68, 116.78, 103.94), swapRB=True, crop=False)
    # cv2.imshow('blob',blob)
    # cv2.waitKey(0)
    net.setInput(blob)
    (scores, geometry) = net.forward(layerNames)
    (rects, confidences) = decode_predictions(scores, geometry)
    boxes = non_max_suppression(np.array(rects), probs=confidences)
    # frame = imutils.resize(frame, width=400)
    results = []
    text = ' '
    for (startX, startY, endX, endY) in boxes:
        # scale the bounding box coordinates based on the respective
        # ratios
        startX = int(startX * rW)
        startY = int(startY * rH)
        endX = int(endX * rW)
        endY = int(endY * rH)

        # width, height = orig.shape[1], orig.shape[0]
        # print("W: %d , H:  %d" % (width, height))

        # in order to obtain a better OCR of the text we can potentially
        # apply a bit of padding surrounding the bounding box -- here we
        # are computing the deltas in both the x and y directions
        # dX = int((endX - startX) * padding)
        # dY = int((endY - startY) * padding)

        # apply padding to each side of the bounding box, respectively
        # startX = max(0, startX - dX)
        # startY = max(0, startY - dY)
        # endX = min(origW, endX + (dX * 2))
        # endY = min(origH, endY + (dY * 2))

        # extract the actual padded ROI
        # roi = orig[startY:endY, startX:endX]

        # width, height = roi.shape[1], roi.shape[0]
        # print("W: %d , H:  %d" % (width, height))


        # draw the bounding box on the image
        cv2.rectangle(orig, (startX, startY), (endX, endY), (0, 255, 0), 2)
        text = pytesseract.image_to_string(orig, config=config)
        # print("Text1: ",text)

    return orig,text

name = " "

# Get a reference to webcam #0 (the default one)
video_capture = cv2.VideoCapture(1)
# url = 'http://192.168.0.112:8080/video'
# video_capture = cv2.VideoCapture(url)

# Declare the width and height in variables
width, height = 640, 480

# Set the width and height
# video_capture.set(cv2.CAP_PROP_FRAME_WIDTH, width)
# video_capture.set(cv2.CAP_PROP_FRAME_HEIGHT, height)
text2 = ' '
tag1 , tag2= '1234','5678'
finish_FR = False
run = False

while (run == False):
    command = refStart.child("desiredLoc").get()
    print(command)
    time.sleep(1)
    if(command == "RUN"):
        finish_FR = False
        run = True

while run:
    # Grab a single frame of video
    ret, frame = video_capture.read()
    # frame = cv2.resize(frame,(width,height))
    count_frames = count_frames + 1

    # Only process every other frame of video to save time
    if count_frames < 3:
        if(finish_FR == False):
            frame , name = detect_faces(frame)
            if(name == "Omar" or name == "Uzair"):
                face_detected = face_detected + 1
                print("Face Detected: ",face_detected)
        elif(finish_FR):
            frame , text2 = Text_detection(frame,width,height)
            # text2 = '1234]'
            print("Text=", text2)
            print(len(text2))
            print(type(text2))
            # if(text2 == " 1234" or text2 == " 1234\n" or text2 == " 1234]" or text2 == " 5678"):
            if(tag1 in text2) or (tag2 in text2):
                print("Text Required: ",text2)
                print("Breaking the Program")
                run = False

    elif(count_frames > 16):
        count_frames = 0
        if (face_detected >= 6):
            finish_FR = True

    # Display the resulting image
    cv2.imshow('Video', frame)

    # Hit 'q' on the keyboard to quit!
    if cv2.waitKey(1) & 0xFF == ord('q'):
        break

# Release handle to the webcam
video_capture.release()
cv2.destroyAllWindows()

if(finish_FR == True and run == False):
    print('Slider')
    refStart.update({"desiredLoc": "STOP"})
    time.sleep(1)
    #refStart.update({"Slider": "YES"})
    time.sleep(6)
    #refStart.update({"Slider": "NO"})
    print('Slider Operation Completed')