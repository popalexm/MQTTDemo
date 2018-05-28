
import json
import paho.mqtt.client as mqtt
import random
import time
import threading
import sys

def pubOutsideTemp():
    mqttc.publish("ambient_readings/inside_temp", payload=random.normalvariate(50, 0.5), qos=0)
    threading.Timer(2, pubOutsideTemp).start()

def pubInsideTemp():
    mqttc.publish("ambient_readings/outside_temp", payload=random.normalvariate(50, 0.5), qos=0)
    threading.Timer(2, pubInsideTemp).start()

def pubOutsideHumidity():
    mqttc.publish("ambient_readings/inside_humidity", payload=random.normalvariate(50, 0.5), qos=0)
    threading.Timer(2, pubOutsideHumidity).start()

def pubInsideHumidity():
    mqttc.publish("ambient_readings/outside_humidity", payload=random.normalvariate(50, 0.5), qos=0)
    threading.Timer(2, pubInsideHumidity).start()

def listenToTemperatureNotifications():
    mqttc.subscribe("desired_temperature/", 0)

def startPunishingData():
    pubInsideHumidity()
    pubOutsideHumidity
    pubInsideTemp()
    pubOutsideTemp()
    listenToTemperatureNotifications()

# The callback for when the client receives a CONNACK response from the server.
def on_connect(client, userdata, flags, rc):
    print("Connected with result code "+str(rc))
 
    # The callback for when a PUBLISH message is received from the server.
def on_message(client, userdata, msg):
    print("Received message for topic " + msg.topic+ " with payload " +str(msg.payload))

        # The callback for when a PUBLISH message is received from the server.
def on_publish(client,userdata,result):           
          print("Published new sensor data -> " + str(result))

mqttc = mqtt.Client("sensor_client", clean_session=False)
mqttc.username_pw_set("sensor", "sensor")
mqttc.on_connect = on_connect
mqttc.on_message = on_message
mqttc.on_publish = on_publish
mqttc.connect("m12.cloudmqtt.com", 10204, 60)
startPunishingData()
mqttc.loop_forever()






