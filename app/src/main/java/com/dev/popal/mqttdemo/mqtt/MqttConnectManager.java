package com.dev.popal.mqttdemo.mqtt;

import android.content.Context;
import android.support.annotation.NonNull;
import android.util.Log;

import org.eclipse.paho.android.service.MqttAndroidClient;
import org.eclipse.paho.client.mqttv3.DisconnectedBufferOptions;
import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttCallbackExtended;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;


public class MqttConnectManager {

    private String TAG = MqttConnectManager.class.getSimpleName();

    private MqttAndroidClient mMQTTClient;

    public MqttConnectManager(Context context){
        mMQTTClient = new MqttAndroidClient(context, ServerConfig.SERVER_URL, ServerConfig.CLIENT_ID);
    }

    public void setCallback(MqttCallbackExtended callback) {
        mMQTTClient.setCallback(callback);
    }

    public void connect(){
        MqttConnectOptions mqttConnectOptions = new MqttConnectOptions();
        mqttConnectOptions.setAutomaticReconnect(true);
        mqttConnectOptions.setCleanSession(false);
        mqttConnectOptions.setUserName(ServerConfig.USERNAME);
        mqttConnectOptions.setPassword(ServerConfig.PASSWORD.toCharArray());
        try {
            mMQTTClient.connect(mqttConnectOptions, null, new IMqttActionListener() {
                @Override
                public void onSuccess(IMqttToken asyncActionToken) {

                    DisconnectedBufferOptions disconnectedBufferOptions = new DisconnectedBufferOptions();
                    disconnectedBufferOptions.setBufferEnabled(true);
                    disconnectedBufferOptions.setBufferSize(100);
                    disconnectedBufferOptions.setPersistBuffer(false);
                    disconnectedBufferOptions.setDeleteOldestMessages(false);
                    mMQTTClient.setBufferOpts(disconnectedBufferOptions);
                    /* Connect to sensor readings topic and sub topics */
                    subscribeToSensorReadings();
                }

                @Override
                public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                    Log.w(TAG, "Failed to connect to: " + ServerConfig.SERVER_URL + exception.toString());
                }
            });
        } catch (MqttException ex){
            ex.printStackTrace();
        }
    }

    private void subscribeToSensorReadings() {
        try {
            mMQTTClient.subscribe(Topics.TEMPERATURE_SENSOR_READINGS, 0, null, new IMqttActionListener() {
                @Override
                public void onSuccess(IMqttToken asyncActionToken) {
                    Log.d(TAG,"Successfully subscribed to sensor readings!");
                }

                @Override
                public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                    Log.w(TAG, "Subscribed fail on sensor readings topics!");
                    exception.printStackTrace();
                }
            });

        } catch (MqttException ex) {
            Log.e(TAG , "Exception whilst subscribing to sensor reading values");
            ex.printStackTrace();
        }
    }

    public void publishDesiredTemp(@NonNull Float desiredTemp){
        MqttMessage message = new MqttMessage();
        message.setPayload(String.valueOf(desiredTemp).getBytes());
        try {
            mMQTTClient.publish(Topics.DESIRED_TEMPERATURE , message);
        } catch (MqttException e) {
            e.printStackTrace();
        }
    }
}
