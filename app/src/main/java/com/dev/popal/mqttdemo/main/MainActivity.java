package com.dev.popal.mqttdemo.main;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.SeekBar;

import com.dev.popal.mqttdemo.R;
import com.dev.popal.mqttdemo.mqtt.MqttConnectManager;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;

import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallbackExtended;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements MqttCallbackExtended{

    private String TAG = MainActivity.class.getSimpleName();

    private MqttConnectManager mqttConnectManager;

    private CoordinatorLayout mCoordinatorLayout;

    private BarChart mBarChart;
    private BarData mBarData;
    private SeekBar mSeekbarDesiredTemp;

    private BarEntry mInteriorTempBar;
    private BarEntry mExteriorTempBar;
    private BarEntry mInteriorHumidityBar;
    private BarEntry mExteriorHumidityBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mBarChart = findViewById(R.id.bar_chart);
        mSeekbarDesiredTemp = findViewById(R.id.seekBar_adjust_temp);
        mCoordinatorLayout = findViewById(R.id.coordinatorLayout);

        initEntries();
        initTempAdjustment();

        mBarData.setBarWidth(0.6f); // set custom bar width
        mBarChart.setData(mBarData);
        mBarChart.setFitBars(true); // make the x-axis fit exactly all bars
        mBarChart.invalidate(); // refresh
        startMqttConnection();
    }

    private void initEntries(){
        List<BarEntry> entries = new ArrayList<>();
        mExteriorTempBar = new BarEntry(0f, 50f);
        entries.add(mExteriorTempBar);
        mInteriorTempBar = new BarEntry(1f, 50f);
        entries.add(mInteriorTempBar);
        mInteriorHumidityBar = new BarEntry(2f, 50f);
        entries.add(mInteriorHumidityBar);
        mExteriorHumidityBar = new BarEntry(3f, 50f);
        entries.add(mExteriorHumidityBar);

        BarDataSet sensorReadingsBarData = new BarDataSet(entries, "Sensor Readings");

        mBarData = new BarData(sensorReadingsBarData);
    }

    private void initTempAdjustment() {
        mSeekbarDesiredTemp.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean b) {
                   Log.d(TAG , "Temperature adjusted for value -> " + progress);
                   sendTemperatureAdjustmentToBroker(progress);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) { }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) { }
        });
    }

    private void startMqttConnection(){
        mqttConnectManager = new MqttConnectManager(getApplicationContext());
        mqttConnectManager.setCallback(this);
        mqttConnectManager.connect();
    }

    private void sendTemperatureAdjustmentToBroker(int progress) {
        mqttConnectManager.publishDesiredTemp((float) progress);
    }

    private void refreshChart(){
        mBarData.notifyDataChanged();
        mBarChart.notifyDataSetChanged();
        mBarChart.invalidate(); // refresh chart data
    }

    private void showSnackBarMessage(@NonNull String setTemperatures) {
        Snackbar snackbar = Snackbar
                .make(mCoordinatorLayout, "Temperature has been set to : " + setTemperatures + " Degrees", Snackbar.LENGTH_SHORT);
        snackbar.show();
    }

    @Override
    public void connectComplete(boolean reconnect, String serverURI) {
        Log.w(TAG,"Connected to sensor readings broker on server -> " + serverURI);
    }

    @Override
    public void connectionLost(Throwable cause) {
        Log.w(TAG,"Lost connection to mqtt broker");
        cause.printStackTrace();
    }

    @Override
    public void messageArrived(String topic, MqttMessage message) throws Exception {
        Float temp = Float.valueOf(message.toString());
        Log.w(TAG, "Received msg for topic : " + topic + " with contents -> " + temp);
        switch (topic) {
            case "ambient_readings/inside_temp":
                mInteriorTempBar.setY(temp);
                refreshChart();
                break;
            case "ambient_readings/outside_temp":
                mExteriorTempBar.setY(temp);
                refreshChart();
                break;
            case "ambient_readings/inside_humidity":
                mInteriorHumidityBar.setY(temp);
                refreshChart();
                break;
            case "ambient_readings/outside_humidity":
                mExteriorHumidityBar.setY(temp);
                refreshChart();
                break;
        }
    }

    @Override
    public void deliveryComplete(IMqttDeliveryToken token) {
        try {
            String setTemp = token.getMessage().toString();
            Log.d(TAG , "Successfully published a value of" + setTemp + " for the desired temperature");
            showSnackBarMessage(setTemp);
        } catch (MqttException e) {
            e.printStackTrace();
        }
    }
}
