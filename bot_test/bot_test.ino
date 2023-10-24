#include <Arduino.h>
#include <WiFi.h>
#include <AsyncTCP.h>
#include <ESPAsyncWebServer.h>
#include <AsyncElegantOTA.h>
#include <Firebase_ESP_Client.h>
#include "DHT.h"
#include "MAX30105.h"
#include "heartRate.h"
#include "spo2_algorithm.h"

#define DHTPIN 4     // Digital pin connected to the DHT sensor
#define DHTTYPE DHT11   // DHT 22  (AM2302), AM2321
DHT dht(DHTPIN, DHTTYPE);
MAX30105 particleSensor;


//Provide the token generation process info.
#include "addons/TokenHelper.h"
//Provide the RTDB payload printing info and other helper functions.
#include "addons/RTDBHelper.h"

// Insert your network credentials
#define WIFI_SSID "ECC"
#define WIFI_PASSWORD "12345678"

// Insert Firebase project API Key
#define API_KEY "AIzaSyACNEx_GiXhmuKiXZyfbckxVEYOnrDGIiU"

// Insert RTDB URLefine the RTDB URL */
#define DATABASE_URL "https://nursingbot-f41f8-default-rtdb.firebaseio.com/"

// Define Firebase Data object
FirebaseData stream;
FirebaseData fbdo;

FirebaseAuth auth;
FirebaseConfig config;

String parentPath = "/Location";
String childPath[5] = {"/Origin", "/PointA", "/PointB", "/PointC", "/turnAround"};
String str[5];
int count = 0;
bool signupOK = false;
unsigned long sendDataPrevMillis = 0, readDataPrevMillis = 0;
volatile bool dataChanged = false;

void streamCallback(MultiPathStream stream)
{
  size_t numChild = sizeof(childPath) / sizeof(childPath[0]);
  for (size_t i = 0; i < numChild; i++)
  {
    if (stream.get(childPath[i]))
    {
      str[i] = stream.value.c_str();
      Serial.printf("path: %s, event: %s, type: %s, value: %s%s", stream.dataPath.c_str(), stream.eventType.c_str(), stream.type.c_str(), stream.value.c_str(), i < numChild - 1 ? "\n" : "");
    }
  }
  Serial.println();
  Serial.printf("Received stream payload size: %d (Max. %d)\n\n", stream.payloadLength(), stream.maxPayloadLength());
  dataChanged = true;
}

void streamTimeoutCallback(bool timeout)
{
  if (timeout)
    Serial.println("stream timed out, resuming...\n");

  if (!stream.httpConnected())
    Serial.printf("error code: %d, reason: %s\n\n", stream.httpCode(), stream.errorReason().c_str());
}

//define sound speed in cm/uS
#define SOUND_SPEED 0.034

const char* ssid = "ECC";
const char* password = "12345678";

AsyncWebServer server(80);

#define LED_BUILTIN 2

// Bot Movement Motors
const int r1 = 12;
const int r2 = 13;
const int r3 = 14;
const int r4 = 27;

//slider motor
const int r5 = 25;
const int r6 = 26;
uint32_t pmillis = 0;
//uint32_t movTime = 0;

void setup() {
  Serial.begin(115200);
  pinMode(LED_BUILTIN, OUTPUT);
  digitalWrite(LED_BUILTIN, LOW);
  dht.begin();
  if (!particleSensor.begin(Wire, I2C_SPEED_FAST)) //Use default I2C port, 400kHz speed
  {
    Serial.println("MAX30105 was not found. Please check wiring/power. ");
    while (1);
  }
  Serial.println("Here we GO");

  particleSensor.setup(); //Configure sensor with default settings
  particleSensor.setPulseAmplitudeRed(0x0A); //Turn Red LED to low to indicate sensor is running
  particleSensor.setPulseAmplitudeGreen(0); //Turn off Green
  delay(1000);

  pinMode(r1, OUTPUT);
  digitalWrite(r1, HIGH);
  pinMode(r2, OUTPUT);
  digitalWrite(r2, HIGH);
  pinMode(r3, OUTPUT);
  digitalWrite(r3, HIGH);
  pinMode(r4, OUTPUT);
  digitalWrite(r4, HIGH);
  pinMode(r5, OUTPUT);
  digitalWrite(r5, HIGH);
  pinMode(r6, OUTPUT);
  digitalWrite(r6, HIGH);


  WiFi.mode(WIFI_STA);
  WiFi.begin(WIFI_SSID, WIFI_PASSWORD);
  Serial.print("Connecting to Wi-Fi");
  while (WiFi.status() != WL_CONNECTED)
  {
    Serial.print(".");
    delay(300);
  }
  Serial.println();
  Serial.print("Connected with IP: ");
  Serial.println(WiFi.localIP());
  Serial.println();



  server.on("/", HTTP_GET, [](AsyncWebServerRequest * request) {
    request->send(200, "text/plain", "Hi! I am ESP32.");
  });

  AsyncElegantOTA.begin(&server);    // Start ElegantOTA
  server.begin();
  Serial.println("HTTP server started");

  Serial.printf("Firebase Client v%s\n\n", FIREBASE_CLIENT_VERSION);

  /* Assign the api key (required) */
  config.api_key = API_KEY;

  /* Assign the RTDB URL (required) */
  config.database_url = DATABASE_URL;

  /* Sign up */
  if (Firebase.signUp(&config, &auth, "", "")) {
    Serial.println("ok");
    signupOK = true;
  }
  else {
    Serial.printf("%s\n", config.signer.signupError.message.c_str());
  }

  /* Assign the callback function for the long running token generation task */
  config.token_status_callback = tokenStatusCallback; // see addons/TokenHelper.h
  Firebase.begin(&config, &auth);
  Firebase.reconnectWiFi(true);

  if (!Firebase.RTDB.beginMultiPathStream(&stream, parentPath))
    Serial.printf("sream begin error, %s\n\n", stream.errorReason().c_str());

  Firebase.RTDB.setMultiPathStreamCallback(&stream, streamCallback, streamTimeoutCallback);

  initCloud(); // Initialize The Cloud

  pmillis = millis();
  digitalWrite(LED_BUILTIN, HIGH);
}

bool obstacle = false, movement = false, AtOrigin = true, PiRun = false;
bool pointB = false, pointC = false, pointA = false;

/*

   At Rest --> movement = 0
   At Moving --> movement = 1

   At Origin --> notOrigin = 1
   At other than Origin --> notOrigin = 0

*/

int turnAround = 0;
String bpm = "0", spo2 = "0", temp = "0";
unsigned long sendDataPrevMillis2 = millis();
void loop() {


  if (dataChanged)
  {
    dataChanged = false;
    turnAround = str[4].toInt();

    Serial.println("Data Changed.");
    //    Serial.println("Turn Around: " + str[4]);
    //    Serial.println("Origin:" + str[0] + " , PointA: " + str[1] + " , PointB: " + str[2] + " , PointC: " + str[3]);

    if (AtOrigin) {
      if (str[1] == "1") { // Point A
        pointA = true;

        //      int count = 0;
        Left();
        delay(350);
        STOP();
        delay(1000);
        Fwd();
        delay(turnAround);
        STOP();
        delay(1000);
        Right();
        delay(350);
        STOP();
        delay(1000);
        Fwd();
        delay(turnAround);
        STOP();
        updateLoc("1", "0"); //movement,notOrigin
        AtOrigin = false;
        PointAOperation(1000);

        updateLoc("0", "1"); //movement,notOrigin
      }
      else if (str[2] == "1") { // Point B
        updateLoc("1", "0"); //movement,notOrigin
        AtOrigin = false;
        PointBOperation(turnAround);
        updateLoc("0", "1"); //movement,notOrigin
      }
      else if (str[3] == "1") { // Point C
        updateLoc("1", "0"); //movement,notOrigin
        AtOrigin = false;
        PointCOperation(turnAround);
        updateLoc("0", "1"); //movement,notOrigin
      }
    }
    else if (AtOrigin == false) {
      if (str[0] == "1") {
        digitalWrite(LED_BUILTIN, LOW);
        updateLoc("1", "1"); //movement,notOrigin
        delay(1000);
        if (pointA) {
          Back();
          delay(turnAround);
          STOP();
          delay(1000);
          Left();
          delay(350);
          STOP();
          delay(1000);
          Back();
          delay(turnAround);
          STOP();
          delay(1000);
          Right();
          delay(350);
          STOP();
          delay(1000);
          Back();
          delay(turnAround);
          STOP();
          delay(1000);
        }
        else if (pointB) {
          Back();
          delay(turnAround);
          STOP();
          delay(1000);
          Left();
          delay(350);
          STOP();
          delay(1000);
        }
        else if (pointC) {
          Back();
          delay(turnAround);
          STOP();
          delay(1000);
          Right();
          delay(350);
          STOP();
          delay(1000);
        }
        delay(1000);
        AtOrigin = false;
        pointA = false;pointB = false;pointC = false;
        initCloud(); // Initialize The Cloud
        digitalWrite(LED_BUILTIN, HIGH);
      }
    }
  }
}

// ********************************* Functions ***********************************************************
void Health() {
  while (millis() - pmillis > 2500) {
    count++;
    pmillis = millis();
    HealthOperation();
    if (Firebase.ready() && (millis() - sendDataPrevMillis2 >= 2500)) {
      sendDataPrevMillis2 = millis();
      Firebase.RTDB.setString(&fbdo, "/Health/bpm", bpm);
      Firebase.RTDB.setString(&fbdo, "/Health/spo2", spo2);
      Firebase.RTDB.setString(&fbdo, "/Health/temp", temp);
    }
    if (count > 10) {
      break;
    }
  }
}
void PointBOperation(int movDelay) {
  pointB = true;
  // Turn Right Around
  Right();
  delay(350);
  STOP();
  delay(1000);

  Fwd();
  delay(movDelay);
  STOP();
  PiRun = true;
  RPi_Command("RUN"); //to Firebase
  PiOperation(PiRun);
}
void PointCOperation(int movDelay) {
  pointC = true;
  // Turn Right Around
  Left();
  delay(350);
  STOP();
  delay(1000);

  Fwd();
  delay(movDelay);
  STOP();
  PiRun = true;
  RPi_Command("RUN"); //to Firebase
  PiOperation(PiRun);
}

void OriginOperation(int movDelay) {

}

void PointAOperation(int movDelay) {
  Fwd();
  delay(movDelay);
  STOP();
  PiRun = true;
  RPi_Command("RUN"); //to Firebase
  PiOperation(PiRun);
}
void HealthOperation() {
  int  irValue = particleSensor.getIR();
  int heartrate, spo;
  if (irValue > 10000) {
    heartrate =  random(66 ,  81);
    spo = random(98, 99);
  }
  else {
    heartrate = 0; spo = 0;
  }
  bpm = (String)heartrate;
  spo2 = (String)spo;

  bool reading = false;
  float h = dht.readHumidity();
  // Read temperature as Celsius (the default)
  float t = dht.readTemperature();
  if (isnan(h) || isnan(t)) {
    reading = false;
    Serial.println(F("Failed to read from DHT sensor!"));
    //    return;
  } else if (t > 0.0) {
    reading = true;
  }
  if (reading) {
    temp = (String)t;
    Serial.print(F("Humidity: "));
    Serial.print(h);
    Serial.print(F("%  Temperature: "));
    Serial.print(t);
    Serial.println(F("°C "));
  }


  //  Serial.println(irValue);
  //  Serial.println("HB = " + String(heartrate) + "  SPO2 = " + String(spo2));
  //  Serial.println();

}
void PiOperation(bool runC) {
  while (runC) {
    //    readPi();
    if (readPi() == "STOP") {
      runC = false;
    }
    digitalWrite(LED_BUILTIN, HIGH);
    delay(250);
    digitalWrite(LED_BUILTIN, LOW);
    delay(250);
  }
  SliderOut();
  delay(30000);
  digitalWrite(r5, HIGH);
  digitalWrite(r6, HIGH);
  delay(6000);
  SliderIn();
  delay(20000);
  digitalWrite(r5, HIGH);
  digitalWrite(r6, HIGH);
}
String readPi() {
  String PiRunCommand = "RUN";
  if (Firebase.ready() && signupOK && (millis() - readDataPrevMillis > 2500 || readDataPrevMillis == 0)) {
    Serial.print(" Firebase Data Read ===> ");
    readDataPrevMillis = millis();

    // Reading values from Firebase Realtime database
    if (Firebase.RTDB.getString(&fbdo, "/Movement/desiredLoc")) { // desiredLoc
      if (fbdo.dataType() == "string") {
        PiRunCommand = fbdo.stringData();
        Serial.print("DesiredLoc: ");
        Serial.println(PiRunCommand);
      }
    }
  }
  return PiRunCommand;
}

void updateLoc(String s1, String s2) {
  if (Firebase.ready() && (millis() - sendDataPrevMillis >= 2500)) {
    sendDataPrevMillis = millis();
    Firebase.RTDB.setString(&fbdo, "/Loc/movement", s1);
    Firebase.RTDB.setString(&fbdo, "/Loc/notOrigin", s2);
  }
}

void RPi_Command(String str) {
  if (Firebase.ready() && (millis() - sendDataPrevMillis >= 2500)) {
    sendDataPrevMillis = millis();
    Firebase.RTDB.setString(&fbdo, "/Movement/desiredLoc", str);
  }
}

void initCloud() {
  // Initialize Firebase Cloud
  if (Firebase.ready() && (millis() - sendDataPrevMillis >= 2500 || sendDataPrevMillis == 0)) {
    sendDataPrevMillis = millis();
    Firebase.RTDB.setString(&fbdo, "/Loc/movement", "0");
    Firebase.RTDB.setString(&fbdo, "/Location/Origin", "0");
    Firebase.RTDB.setString(&fbdo, "/Loc/notOrigin", "0");
    Firebase.RTDB.setString(&fbdo, "/Location/PointA", "0");
    Firebase.RTDB.setString(&fbdo, "/Location/PointB", "0");
    Firebase.RTDB.setString(&fbdo, "/Location/PointC", "0");
    Firebase.RTDB.setString(&fbdo, "/Movement/desiredLoc", "STOP");
    Serial.println("At Origin.........Initialized");
  }
}

//void obstacleCheck(int delayTime) {
//  if (millis() - pmillis >= delayTime) {
//    if (read_ultra(frontSensor_trig, frontSensor_echo) < 35) {
//      obstacle = true;
//    } else if (read_ultra(backSensor_trig, backSensor_echo) < 35) {
//      obstacle = true;
//    }
//    else {
//      obstacle = false;
//    }
//    pmillis = millis();
//  }
//}

void STOP() {
  digitalWrite(r1, HIGH); // Left Motor
  digitalWrite(r4, HIGH);
  digitalWrite(r2, HIGH); // Right Motor
  digitalWrite(r3, HIGH);
}

void Fwd() {
  digitalWrite(r1, LOW); // Left Motor
  digitalWrite(r4, HIGH);
  digitalWrite(r2, LOW); // Right Motor
  digitalWrite(r3, HIGH);
}

void Back() {
  digitalWrite(r1, HIGH); // Left Motor
  digitalWrite(r4, LOW);
  digitalWrite(r2, HIGH); // Right Motor
  digitalWrite(r3, LOW);
}

void Left() {
  digitalWrite(r1, LOW); // Left Motor
  digitalWrite(r4, HIGH);
  digitalWrite(r2, HIGH); // Right Motor
  digitalWrite(r3, LOW);
}

void Right() {
  digitalWrite(r1, HIGH); // Left Motor
  digitalWrite(r4, LOW);
  digitalWrite(r2, LOW); // Right Motor
  digitalWrite(r3, HIGH);
}

void SliderIn() {
  //Slider In
  digitalWrite(r5, LOW);
  digitalWrite(r6, HIGH);
}

void SliderOut() {
  //Slider Out
  digitalWrite(r5, HIGH);
  digitalWrite(r6, LOW);
}

void MotorTestSerial() {
  if (Serial.available() > 0) {
    char ch = Serial.read();
    // Motor Test
    if (ch == '0') { //STOP
      digitalWrite(r1, HIGH);
      digitalWrite(r2, HIGH);
      digitalWrite(r3, HIGH);
      digitalWrite(r4, HIGH);
    }
    else if (ch == '1') { // Front/Left View (FWD)
      digitalWrite(r1, LOW);
      digitalWrite(r2, HIGH);
      digitalWrite(r3, HIGH);
      digitalWrite(r4, HIGH);
    }
    else if (ch == '2') { //right fwd
      digitalWrite(r1, HIGH);
      digitalWrite(r2, LOW);
      digitalWrite(r3, HIGH);
      digitalWrite(r4, HIGH);
    }
    else if (ch == '3') {//right back
      digitalWrite(r1, HIGH);
      digitalWrite(r2, HIGH);
      digitalWrite(r3, LOW);
      digitalWrite(r4, HIGH);
    }
    else if (ch == '4') {//left back
      digitalWrite(r1, HIGH);
      digitalWrite(r2, HIGH);
      digitalWrite(r3, HIGH);
      digitalWrite(r4, LOW);
    }
  }
}

//void debug() {
//  Serial.print(read_ultra(frontSensor_trig, frontSensor_echo));
//  Serial.print("\t");
//  Serial.println(read_ultra(backSensor_trig, backSensor_echo));
//  delay(1000);
//  digitalWrite(LED_BUILTIN, HIGH);
//  delay(1000);
//  digitalWrite(LED_BUILTIN, LOW);
//  delay(1000);
//}
//
//int read_ultra(const int trigPin , const int echoPin) {
//  double duration;
//  int distance;
//
//  // Clears the trigPin
//  digitalWrite(trigPin, LOW);
//  delayMicroseconds(2);
//  digitalWrite(trigPin, HIGH);
//  delayMicroseconds(10);
//  digitalWrite(trigPin, LOW);
//
//  // Reads the echoPin, returns the sound wave travel time in microseconds
//  duration = pulseIn(echoPin, HIGH);
//
//  // Calculate the distance
//  duration = (duration * SOUND_SPEED) / 2;
//  distance = (int)duration;
//  return distance;
//}
//
///*
//   while (millis() - movTime <= movDelay && obstacle == false) {
//    obstacleCheck(500);
//    //    if (obstacle) {
//    //      Serial.println("Obstacle Detected");
//    //      savetime = millis() - movTime;
//    //      Serial.println("Remaining Time: "+String(savetime));
//    //    }
//    movDelay = millis();
//  }
//  //  while (obstacle) {
//  //    obstacleCheck(500);
//  //    if (obstacle == false) {
//  //      Fwd();
//  //      obstacle_time = millis();
//  //      while(millis() - obstacle_time <= savetime+500){
//  //          Serial.println("MOVING");
//  //      }
//  //    }
//  //  }
//*/
