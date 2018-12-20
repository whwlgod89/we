package kr.co.theunify.wear.data;

import android.app.PendingIntent;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.UUID;

import kr.co.theunify.wear.Const;
import kr.co.theunify.wear.activity.MainActivity;
import kr.co.theunify.wear.sensor.ConnectionEvent;
import kr.co.theunify.wear.sensor.Sensor;

public class SensorInfo {

    public SensorInfo() {
    }

    public SensorInfo(String id, String name, String phone, int mode, int rssi) {
        this.id = id;
        this.name = name;
        this.phone = phone;
        this.mode = mode;
        this.rssi = rssi;
    }

    public SensorInfo(String id, String name, String phone, int mode, int rssi, double latitude, double longitude, int battery) {
        this.id = id;
        this.name = name;
        this.phone = phone;
        this.mode = mode;
        this.rssi = rssi;
        this.latitude = latitude;
        this.longitude = longitude;
        this.battery = battery;
    }

    private String id;           // Bluetooth Device Address
    private String name;         // User Defined Device Name
    private String phone;        // User Defined Phone Number
    private int mode;            // User Defined Action Mode (Prevent Loss=0, Theft=1)
    private int rssi;                  // User Defined RSSI Value (75, 85, 100)

    private int state;            // Device Connection State (8)
    private double latitude;           // Current or Last Location based on mConnectState
    private double longitude;
    private int battery;          // Battery Level;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public int getMode() {
        return mode;
    }

    public void setMode(int mode) {
        this.mode = mode;
    }

    public int getRssi() {
        return rssi;
    }

    public void setRssi(int rssi) {
        this.rssi = rssi;
    }


    public int getState() {
        return state;
    }

    public void setState(int state) {
        this.state = state;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public int getBattery() {
        return battery;
    }

    public void setBattery(int battery) {
        this.battery = battery;
    }
}
