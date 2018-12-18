package kr.co.theunify.wear.sensor;

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

public class Sensor {
    private static final String TAG = "[" + Sensor.class.getSimpleName() + "]";

    public String mConnectedDateTime;       // for Debug
    public String mDisconnectedDateTime;

    public static final UUID TX_POWER_UUID = UUID.fromString("00001804-0000-1000-8000-00805f9b34fb");
    public static final UUID TX_POWER_LEVEL_UUID = UUID.fromString("00002a07-0000-1000-8000-00805f9b34fb");
    public static final UUID CCCD = UUID.fromString("00002902-0000-1000-8000-00805f9b34fb");
    public static final UUID FIRMWARE_REVISON_UUID = UUID.fromString("00002a26-0000-1000-8000-00805f9b34fb");
    public static final UUID DIS_UUID = UUID.fromString("0000180a-0000-1000-8000-00805f9b34fb");
    public static final UUID UART_SERVICE_UUID = UUID.fromString("6e400001-b5a3-f393-e0a9-e50e24dcca9e");
    public static final UUID UART_RX_CHAR_UUID = UUID.fromString("6e400002-b5a3-f393-e0a9-e50e24dcca9e");
    public static final UUID UART_TX_CHAR_UUID = UUID.fromString("6e400003-b5a3-f393-e0a9-e50e24dcca9e");

    enum CONNECT_STATE {
        POWER_OFF,
        INITIALIZED,
        CONNECTING,
        CONNECTED,
        DISCONNECTING,
        DISCONNECTED,
        FORCE_DISCONNECTED,
        FAILED,
    };

//    enum ACTIVE_STATE {
//        INITIALIZED,
//        POWER_OFF,
//        ACTIVATED,
//    }

    private String mSensorId;           // Bluetooth Device Address
    private String mSensorName;         // User Defined Device Name
    private String mPhoneNumber;        // User Defined Phone Number
    private int mActionMode;            // User Defined Action Mode (Prevent Loss=0, Theft=1)
    private CONNECT_STATE mConnectState;   // Device Connection State (8)
    private double mLatitude;           // Current or Last Location based on mConnectState
    private double mLongitude;
    private int mBatteryLevel;          // Battery Level;
    //private int mPosition;              // Index in SensorList ArrayList
    public boolean mScanned;           // 09.08: 서비스에서 스캔하는 것으로 변경 과정에서 추가

    //private ACTIVE_STATE mActiveState;  // Device Active State (3)
    //private boolean mLinked = false;    // 불안정한 Disconnect 경우, 일정 Reconnect 시도까지는 논리적 Connect 상태 유지라는데...
    private boolean mForceDisconnect = false;
    private int mRetryCount = 0;        // 일단 살려서 해본다... 30번까지
    private int mQueryCount = 0;      // 일단 막는다.. 의미가 없어 보인다.

    private Context mContext = null;
    private BluetoothAdapter mBluetoothAdapter;
    private BluetoothGatt mBluetoothGatt;
//    private SensorDatabase mDB;
    private ConnectionEvent ce;

    private Handler mAutoHandler = null;
    private Handler mQueryHandler = null;
    private Handler mReconnHandler = null;
    //private Handler mDisconnectHandler = null;    //BO: Not Used
    private Handler mServiceDiscoveredHandler = null;

    private Handler mFindingHandler = null;
    private boolean mFindingSensor = false;

    public Sensor() {

    }

    // AddSensor 에서 센서 추가
    // public Sensor(Context context, ConnectionEvent CE, SensorDatabase db) {
    public Sensor(Context context, /*ConnectionEvent CE, SensorDatabase db,*/ String sensorId, String sensorName, String phoneNumber, int actionMode) {
        this.mContext = context;
        final BluetoothManager bluetoothManager =
                (BluetoothManager) mContext.getSystemService(Context.BLUETOOTH_SERVICE);
        mBluetoothAdapter = bluetoothManager.getAdapter();
        if (mBluetoothAdapter != null) {
            mAutoHandler = new Handler();
            mQueryHandler = new Handler();
            mReconnHandler = new Handler();
            //mDisconnectHandler = new Handler();
            mServiceDiscoveredHandler = new Handler();

            mFindingHandler = new Handler();
        }
//        mDB = db;
        //ce = CE;

        mSensorId = sensorId;
        mSensorName = sensorName;
        mPhoneNumber = phoneNumber;
        mActionMode = actionMode;
        mConnectState = CONNECT_STATE.INITIALIZED;    // 찾아서 등록하니까 연결상태로 한다.
        mLatitude = 37.5759369;     // 광화문
        mLongitude = 126.9768157;
        mBatteryLevel = 100;

        mScanned = false;
    }

//    public Sensor(String sensorId) {
//        mSensorId = sensorId;
//        mSensorName = "";
//        mPhoneNumber = "";
//        mActionMode = 0;
//        mConnectState = CONNECT_STATE.POWER_OFF;
//        mLatitude = 37.5759369;     // 광화문
//        mLongitude = 126.9768157;
//        mBatteryLevel = 100;
//    }

//    public Sensor(String sensorId, String sensorName, String phoneNumber, int actionMode) {
//        mSensorId = sensorId;
//        mSensorName = sensorName;
//        mPhoneNumber = phoneNumber;
//        mActionMode = actionMode;
//        mConnectState = CONNECT_STATE.POWER_OFF;
//        mLatitude = 37.5759369;     // 광화문
//        mLongitude = 126.9768157;
//        mBatteryLevel = 100;
//    }

//    public Sensor(String sensorId, String sensorName, String phoneNumber, int actionMode, int connState, double latitude, double longitude) {
//        mSensorId = sensorId;
//        mSensorName = sensorName;
//        mPhoneNumber = phoneNumber;
//        mActionMode = actionMode;
//        mConnectState = (CONNECT_STATE) connState;
//        mLatitude = latitude;
//        mLongitude = longitude;
//        mBatteryLevel = 100;
//    }
//
    // DB 에서 읽어서 센서 추가
    public Sensor(String sensorId, String sensorName, String phoneNumber, int actionMode, double latitude, double longitude, int batLevel) {
        mSensorId = sensorId;
        mSensorName = sensorName;
        mPhoneNumber = phoneNumber;
        mActionMode = actionMode;
        mConnectState = CONNECT_STATE.INITIALIZED;    // 찾아서 등록하니까 연결상태로 한다.
        mLatitude = latitude;
        mLongitude = longitude;
        mBatteryLevel = batLevel;

        mScanned = false;
    }

    // Get Member Values
    public String getSensorId() { return mSensorId; }
    public String getSensorName() { return mSensorName; }
    public String getPhoneNumber() { return mPhoneNumber; }
    public int getActionMode() { return mActionMode; }
    public CONNECT_STATE getConnectState() { return mConnectState; }
    public boolean isConnected() { return (mConnectState == CONNECT_STATE.CONNECTED); }
    public double getLatitude() { return mLatitude; }
    public double getLongitude() { return mLongitude; }
    public int getBatteryLevel() { return mBatteryLevel; }

    // Set Member Values
    public void setSensorId(String sensorId) { mSensorId = sensorId; }
    public void setSensorName(String sensorName) { mSensorName = sensorName; }
    public void setPhoneNumber(String phoneNumber) { mPhoneNumber = phoneNumber; }
    public void setActionMode(int actionMode) { mActionMode = actionMode; }
    public void setConnectState(CONNECT_STATE connectState) { mConnectState = connectState; }
    public void setLatitude(double latitude) { mLatitude = latitude; }
    public void setLongitude(double longitude) { mLongitude = longitude; }
    public void setLocation(double latitude, double longitude) { mLatitude = latitude; mLongitude = longitude; }
    public void setBatteryLevel(int batteryLevel) { mBatteryLevel = batteryLevel; }

    // BLE Rx Data Handler ???
    // 이 부분은 수정해야 하겠다.
    // HeyTong 에서는
    //      1. 연결 상태(RSSI, Battery 포함) (요청 발신, 응답 수신)
    //      2. 센서 찾기 (요청 발신, 응답 수신)
    //      3. 전화기 찾기 (요청 수신, 응답 발신)
    private final void rxHandler(BluetoothGattCharacteristic characteristic) {
        //boolean detected = false;     // 소변 아니니까 불필요
        //boolean initialized = false;    // HeyTong 에는 정의된 바가 없네...
        //boolean battery = false;        // 배터리 잔량 Packet
        //boolean disconnected = false;   // 센서 단말기 OFF 알림 Packet

        final byte[] rxBuff = characteristic.getValue();
        final int cmd = (int) (rxBuff[0] & 0xFF);
        final int data = (int) (rxBuff[1] & 0xFF);
        Log.w(TAG, "rxHandler: ID= "+ mSensorId + ", CMD=" + cmd + ", DATA=" + data);

        // CareBell 에서는 Rx 하면 5 바이트를 받네, HeyTong 에서는 2바이트
        //int[] convBuf = new int[5];
        //for(int i=0; i< 5; i++) {
        //    convBuf[i] = (int) (rxBuff[i] & 0xFF);  //unsignedToByte(rxBuff[i]);
        //    if(convBuf[i] == 0xFF)
        //        convBuf[i] = 0x00;
        //}

        // CareBell 에서는 5바이트를 받아서
        //      [0]:STX - 0xAA
        //      [1]:CMD - 0x81:???, 0x82:Detect, 0x83:Battery, 0x84:Initialized, 0x85:Disconnected
        //      [2]:BAT - Battery Level
        //      [3]:??? -
        //      [4]:ETX - 0x55
        switch(cmd) {
            case 0xDD:      // 센서에서 휴대폰 찾기 알람 시작 (data = 배터리 잔량)
                Log.w(TAG, "Start Alarm of Phone: ID= "+ mSensorId + ", Battery=" + data);
                //updateUI(Const.ACTION_SENSOR_FIND_PHONE_START);
                actionUI(Const.ACTION_SENSOR_FIND_PHONE_START);
                break;
            case 0xEE:      // 센서에서 휴대폰 찾기 알람 종료 (data = 배터리 잔량)
                Log.w(TAG, "Stop Alarm of Phone: ID= "+ mSensorId + ", Battery=" + data);
                //updateUI(Const.ACTION_SENSOR_FIND_PHONE_STOP);
                actionUI(Const.ACTION_SENSOR_FIND_PHONE_STOP);
                break;
            case 0xF0:      // 센서 단말기의 FW 버전 정보 (data = 버전 정보)
                Log.w(TAG, "Response of Firmware: ID= "+ mSensorId + ", Version=" + data);
                break;
            case 0xF1:      // 센서에서 주기적으로 발신하는 배터리 잔량 (data = 배터리 잔량)
                Log.w(TAG, "Report of Battery Level: ID= "+ mSensorId + ", Battery=" + data);
                setBatteryLevel(data);
                updateUI(Const.ACTION_SENSOR_BATTERY);
                //battery = true;
                break;
            case 0xFF:      // 센서 단말기의 전원 OFF 알림 (data = 배터리 잔량)
                Log.w(TAG, "rxHandler(): Report of Sensor OFF: ID= "+ mSensorId + ", Battery=" + data);
                //disconnected = true;
                //mActiveState = ACTIVE_STATE.POWER_OFF;
                disconnect(true);
                break;
            default:
                Log.w(TAG, "Unknown Packet");
                break;
        }
//        if(convBuf[0] == 0xAA && convBuf[4] == 0x55) {
//            switch(convBuf[1]) {
//                case 0x81:
//                    break;
//                case 0x82:   //sensored....
//                    detected = true;
//                    break;
//                case 0x83:
//                    battery = true;
//                    break;
//                case 0x84:
//                    initialized = true;
//                    break;
//                case 0x85:
//                    disconnected = true;
//                    break;
//            }
// 소변 센서가 아니니까 필요없고....
//            if(detected) {
//                setBatteryLevel(convBuf[2]);
//                //reply
//                byte[] packet = new byte[5];
//                packet[0] = (byte)0xAA;
//                packet[1] = (byte)0x02;     //BO: Detected 수신에 대한 응답인가?
//                packet[2] = (byte)0x00;
//                packet[3] = (byte)0x00;
//                packet[4] = (byte)0x55;
//                writeRXCharacteristic(packet);
//
//                //update sensor status..
//                //Bo: 검출 시간 보정 및 기존 insertDB() 유지를 위해 insertDBDetected() 추가하여 적용
//                //    DB_STATE_DETECTED 외에는 기존대로 inserDB(STATUS) 사용 유지.
//                //insertDB(DB_STATE_DETECTED);
//                insertDBDetected(convBuf[3]);
//
//                //mPreDetectState = mCurDetectState;
//                mCurDetectState = DETECTION_STATE.DETECTED;
//                mDetectedTime = System.currentTimeMillis();
//                ce.onDetect(this);
//                //actionUI(IntentConstants.ACTION_DATA_AVAILABLE);
//                //updateUI(IntentConstants.ACTION_DATA_AVAILABLE);
//            }

        // Report of Sensor Initialized... HeyTong 에는 없네...
//        if(initialized) {
//            setBatteryLevel(convBuf[2]);
//            mQueryHandler.removeCallbacks(rxSensorInit);
//            //mCurDetectState = DETECTION_STATE.NOT_DETECTED;
//            updateUI(Const.ACTION_SENSOR_INITIALIZE);
//            mQueryHandler.postDelayed(rxDetectMode,3 *1000);
//            //mConnectTime = System.currentTimeMillis();
//            //insertDB(DB_STATE_DETECT_STARTED);
//        }

        // Report of Battery Level...
//        if(battery) {
//            setBatteryLevel(data);
//            updateUI(Const.ACTION_SENSOR_BATTERY);
//        }

        // Report of Sensor OFF
//        if(disconnected) {
//            mActiveState = ACTIVE_STATE.POWER_OFF;
//            disconnect(true);
//        }
    }

    // Implements callback methods for GATT events that the app cares about.  For example,
    // connection change and services discovered.
    private final BluetoothGattCallback mGattCallback = new BluetoothGattCallback() {
        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            Log.i(TAG,"ID: "+ mSensorId + " onConnectionStateChange(), newState=" + newState + ", mConnectState=" + mConnectState.toString());

            if (newState == BluetoothProfile.STATE_CONNECTED) {
                Log.i(TAG,"ID: "+ mSensorId + " Connected to GATT server.");
                if(mBluetoothGatt != null)
                    mBluetoothGatt.discoverServices();
                //6초 이내에   discoverServices 가 응답하지 않으면 failed 처리
                //mAutoHandler.postDelayed(connectFailed, 1000*6);
                mServiceDiscoveredHandler.postDelayed(serviceDiscoveredFailed, 1000 * 5);
            }
            // 연결이 끊어진 상태에서도 DISCONNECTED가 오는 경우가 있다. 현재 상태 체크를 추가한다.
            else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                Log.i(TAG,"ID: " + mSensorId + " Disconnected from GATT server.");

                if(mConnectState == CONNECT_STATE.CONNECTED) {
                    SimpleDateFormat dateFormat = new SimpleDateFormat("MM-dd HH:mm:ss");      // Debug
                    mDisconnectedDateTime = dateFormat.format(Calendar.getInstance().getTime());

                    actionUI(Const.ACTION_GATT_DISCONNECTED);    //BO: Launch Activity with Intent
                    mConnectState = CONNECT_STATE.INITIALIZED;          // 시작시와 같게 맞추기 위해 INITIALIZED로 설정
                }

                //updateUI(Const.ACTION_GATT_DISCONNECTED);  //BO: Broadcast Message
                if(mForceDisconnect) {
                    //Bo: 다 막혀있는데... 필요없는 것인가? =============================
                    //mDisconnectHandler.removeCallbacks(force_disconnect_handler);
                    //updateUI(IntentConstants.ACTION_GATT_DISCONNECTED);
                }else {
                    // 연결이 끊어졌거나, 초기 시작할 때 연결 시도가 실패한 경우에만 재연결 시도를 위해 Reconnect 호출
                    if(mBluetoothGatt != null && mConnectState == CONNECT_STATE.INITIALIZED) {
                        mAutoHandler.postDelayed(reconnect, 100);
                        //mConnectState = CONNECT_STATE.CONNECTING;
                    }
                }
            }
        }

        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                Log.w(TAG, "mBluetoothGatt.onServicesDiscovered() - ID: " + mSensorId + ", mConnectState=" + mConnectState);

                SimpleDateFormat dateFormat = new SimpleDateFormat("MM-dd HH:mm:ss");
                mConnectedDateTime = dateFormat.format(Calendar.getInstance().getTime());

                mConnectState = CONNECT_STATE.CONNECTED;
                //mActiveState = ACTIVE_STATE.ACTIVATED;

                enableTXNotification();
                //insertDB(DB_STATE_CONNECTED);
                mRetryCount = 0;
                //mConnectTime = System.currentTimeMillis();
                mAutoHandler.removeCallbacks(reconnect);
                //mAutoHandler.removeCallbacks(connectFailed);
                //mReconnHandler.removeCallbacks(connectStableCheck);
                mServiceDiscoveredHandler.removeCallbacks(serviceDiscoveredFailed);
                //ce.onStatusChanged(mCurConnState);    // ===================================

                updateUI(Const.ACTION_GATT_CONNECTED);

                //BO: 추가...  연결이 되었으니, 10초 이내에 0x01 Packet을 보내야 한다.
                sensor_init();
            } else {
                //Log.w(TAG, "ID: "+ mPosition + " onServicesDiscovered received: " + status);
                Log.w(TAG, "ID: "+ mSensorId + " onServicesDiscovered received: " + status);
            }
        }

        @Override
        public void onCharacteristicRead(BluetoothGatt gatt,
                                         BluetoothGattCharacteristic characteristic,
                                         int status) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                rxHandler(characteristic);
            }
        }

        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt,
                                            BluetoothGattCharacteristic characteristic) {
            rxHandler(characteristic);
        }

        @Override
        public void onReadRemoteRssi(BluetoothGatt gatt, int rssi, int status) {
            Log.d(TAG, "onReadRemoteRssi: Sensor=" + mSensorName + ", RSSI=" + rssi + ", STATUS=" + status);
            if(status == BluetoothGatt.GATT_SUCCESS) {
                if(mActionMode == Const.ACTION_MODE_THEFT && rssi < Const.ACTION_THEFT_ALARM_RSSI) {
                    Log.d(TAG, "Hit Low RSSI in Theft Mode");
                    // 알람을 줘야 한다....
                    actionUI(Const.ACTION_SENSOR_WARN_THEFT);
                }
            }
        }
    };

    // RSSI 읽기를 시도하면, 위의 BluetoothGATTCallback::onReadRemoteRssi()로 응답이 온다.
    // 이전 읽기 시도에 아직 응답이 없는데, 또-계속 읽기 시도를 하면 어찌 될라나?
    // 한번만 할까?... 일단 그냥 해보고...
    public void readRemoteRSSI() {
        if(mBluetoothGatt != null) {
            Log.d(TAG, "readRemoteRssi: Sensor=" + mSensorName);
            mBluetoothGatt.readRemoteRssi();
        }
    }

//    //Bo: 소변 Detect 필요없는데... 그럼 필요없겠지??? ================
//    private Runnable rxDetectMode = new Runnable() {
//        public void run() {
//            updateUI(IntentConstants.ACTION_SENSOR_INITIALIZE);
//            mQueryHandler.removeCallbacks(rxDetectMode);
//        }
//    };

    private Runnable rxSensorInit = new Runnable() {
        public void run() {
            if(mQueryCount > 3) {
                mQueryCount = 0;      //BO: 3번까지만 유지되게 해본다........
                //updateUI(Const.ACTION_SENSOR_INITIALIZE);
            }else {
                Log.d(TAG, "rxSensorInit(): ID=" + mSensorId + ", ConnectState=" + mConnectState);
                sensor_init();
            }
        }
    };

    // 센서 초기화?... 센서에 연결 알림으로 사용하면 될까?.................
    public void sensor_init() {
        Log.d(TAG, "sensor_init(): ID=" + mSensorId + ", ConnectState=" + mConnectState);
        if(mConnectState != CONNECT_STATE.CONNECTED) {
            return;
        }

        byte[] packet = new byte[2];
        packet[0] = (byte) 0x01;    // 휴대폰은 단말기와 연결되면 10초 이내에 발신해야 한다.
        packet[1] = (byte) 0x00;    // Dummy
        writeRXCharacteristic(packet);
        Log.d(TAG, "SENT Initial Code...");
        mQueryCount++;
        mQueryHandler.postDelayed(rxSensorInit, 3000);
        //updateUI(Const.ACTION_SENSOR_INITIALIZE);
    }

    public void findSensor() {
        Log.d(TAG, "findSensor(): ID=" + mSensorId + ", ConnectState=" + mConnectState+ ", FindState=" + mFindingSensor);
        if(mConnectState != CONNECT_STATE.CONNECTED) {
            mFindingSensor = false;
            return;
        }

        byte[] packet = new byte[2];
        if(mFindingSensor) {
            // Stop Alarm Command 작성
            packet[0] = (byte) 0xEE;    // Stop Alarm
            packet[1] = (byte) 0x00;    // Dummy
            writeRXCharacteristic(packet);
            mFindingSensor = false;
            mFindingHandler.removeCallbacks(finishFindingSensor);
        }
        else {
            // Start Alarm Command 작성
            packet[0] = (byte) 0xDD;    // Start Alarm
            packet[1] = (byte) 0x00;    // Dummy
            writeRXCharacteristic(packet);
            mFindingSensor = true;
            mFindingHandler.postDelayed(finishFindingSensor, 30 * 1000);
        }
    }

    private Runnable finishFindingSensor = new Runnable() {
        @Override
        public void run() {
            if(mFindingSensor = true) {
                mFindingSensor = false;
                mFindingHandler.removeCallbacks(finishFindingSensor);
            }
        }
    };

    public boolean isFindingSensor() {
        return mFindingSensor;
    }

// 센서 전원 OFF 명령... 그런데 HeyTong App UI 에는 센서 OFF 제어가 없다.
//    public void sensor_poweroff() {
//        Log.d(TAG, "sensor_poweroff(): ID=" + mSensorId + ", ConnectState=" + mConnectState);
//        if(mConnectState != CONNECT_STATE.CONNECTED) {
//            return;
//        }
//        mForceDisconnect = true;
//
//        byte[] packet = new byte[2];
//        packet[0] = (byte) 0xFF;    // Command to Sensor to turn off power.
//        packet[1] = (byte) 0x00;    // Dummy
//        writeRXCharacteristic(packet);
//
//        //mQueryCount++;
//        //mQueryHandler.postDelayed(rxSensorInit, 3000);
//        //updateUI(IntentConstants.ACTION_SENSOR_INITIALIZE);
//    }

    public boolean connect() {
        Log.d(TAG, "ID: "+ mSensorId +" connect() called. mConnectState=" + mConnectState);

        if (mBluetoothAdapter == null || mSensorId == null) {
            //Log.w(TAG, "ID: "+ mPosition + " BluetoothAdapter not initialized or unspecified address.");
            Log.w(TAG, "BluetoothAdapter not initialized or unspecified address.");
            return false;
        }
        if(mConnectState == CONNECT_STATE.CONNECTED) {
            //Log.w(TAG, "ID: " + mPosition +" Already connected.");
            Log.w(TAG, "ID: " + mSensorId +" Already connected.");
            return false;
        }

        mForceDisconnect = false;
        //mConnectState = CONNECT_STATE.CONNECTING;
        //mActiveState = ACTIVE_STATE.INITIALIZED;
        //BO: HeyTong에서는 CONNECTING 단계는 필요 없다
        //updateUI(Const.ACTION_GATT_CONNECTING);

        // Previously connected device.  Try to reconnect.
        if (mBluetoothGatt != null) {
            Log.w(TAG, "connect().mBluetoothGatt.connect() - ID: "+ mSensorId + ", mConnectState=" + mConnectState);
            if (mBluetoothGatt.connect()) {
                // 연결이 안되었는데, OK가 떨어지네 (센서 끊긴 상태에서 서비스 시작할 때의 Connect()에서 발견)
                // Android 문서에서는 연결 시도 초기화가 성공되면 true로 적혀 있고, 범위 밖에 있어도 재시도를 한다고 적혀 있다~!!!.
                Log.w(TAG, "ID: "+ mSensorId + " Connected OK!");
                return true;
            } else {
                Log.w(TAG, "ID: "+ mSensorId + " Connect FAILED!");
                return false;
            }
        }

        final BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(mSensorId);
        if (device == null) {
            //Log.w(TAG, "ID:" + mPosition+ " Device not found.  Unable to connect.");
            Log.w(TAG, "ID:" + mSensorId + " Device not found.  Unable to connect.");
            return false;
        }

        // We want to directly connect to the device, so we are setting the autoConnect
        // parameter to false.
        Log.d(TAG, "connect().device.connectGatt() - ID: "+ mSensorId + ", mConnectState=" + mConnectState);
        mBluetoothGatt = device.connectGatt(mContext, false, mGattCallback);
        if(mBluetoothGatt != null) {
            Log.w(TAG, "ID: "+ mBluetoothGatt.getDevice().getAddress() + " connectGatt() OK!");
        }
        else {
            Log.w(TAG, "ID: "+ mSensorId + " connectGatt() FAILED");
        }
        return true;
    }

    /**
     * Disconnects an existing connection or cancel a pending connection. The disconnection result
     * is reported asynchronously through the
     * {@code BluetoothGattCallback#onConnectionStateChange(android.bluetooth.BluetoothGatt, int, int)}
     * callback.
     */
    public void disconnect() {
        if(mForceDisconnect)
            mConnectState = CONNECT_STATE.FORCE_DISCONNECTED;
        else
            mConnectState = CONNECT_STATE.DISCONNECTED;
        if (mBluetoothAdapter == null || mBluetoothGatt == null) {
            //Log.w(TAG, "ID: "+ mPosition+ " BluetoothAdapter not initialized");
            Log.w(TAG, "ID: "+ mSensorId + " BluetoothAdapter not initialized");
            return;
        }

        mBluetoothGatt.disconnect();
        mBluetoothGatt.close();
        mBluetoothGatt = null;
        updateUI(Const.ACTION_GATT_DISCONNECTED);    //TO DO: ************************
    }

    public void disconnect(boolean force) {
        mForceDisconnect =  force;
        //mBluetoothAdapter.stopLeScan(mLeScanCallback);
        mAutoHandler.removeCallbacks(reconnect);
        //mAutoHandler.removeCallbacks(connectFailed);
        //mReconnHandler.removeCallbacks(connectStableCheck);
        mServiceDiscoveredHandler.removeCallbacks(serviceDiscoveredFailed);

        //mAutoHandler.postDelayed(force_disconnect_handler, 10*1000);
        Log.w(TAG, "disconnect(): Sensor OFF ID= "+ mSensorId );
        disconnect();
    }

    private void cancel_connect() {
        //mBluetoothAdapter.stopLeScan(mLeScanCallback);
        mAutoHandler.removeCallbacks(reconnect);
        //mAutoHandler.removeCallbacks(connectFailed);
        //mReconnHandler.removeCallbacks(connectStableCheck);
        mServiceDiscoveredHandler.removeCallbacks(serviceDiscoveredFailed);
        if (mBluetoothAdapter == null || mBluetoothGatt == null) {
            //Log.w(TAG, "ID: "+ mPosition+" BluetoothAdapter not initialized");
            Log.w(TAG, "ID: "+ mSensorId +" BluetoothAdapter not initialized");
            return;
        }

        mBluetoothGatt.disconnect();
        mBluetoothGatt.close();
        mBluetoothGatt = null;
    }

//    private Runnable connectFailed = new Runnable() {
//        public void run() {
//            //mPreConnState = mCurConnState;
//            //mBluetoothGatt.disconnect();
//            //Log.w(TAG, "ID: " + mPosition+ " mBluetoothGatt connectFailed");
//            Log.w(TAG, "ID: " + mSensorId + " mBluetoothGatt connectFailed");
//            mBluetoothAdapter.stopLeScan(mLeScanCallback);
//
//            // 일단 이것을 살려서 해본다...
////            if(mRetryCount < 30) {
////                mRetryCount++;
//                mAutoHandler.postDelayed(reconnect, 1000);
//                mConnectState = CONNECT_STATE.CONNECTING;
////                updateUI(Const.ACTION_GATT_CONNECTING);
////            }else {
////                mRetryCount = 0;
////                mConnectState = CONNECT_STATE.FAILED;
////                //mAutoHandler.removeCallbacks(reconnect);
////                //mAutoHandler.removeCallbacks(connectFailed);
////                //mReconnHandler.removeCallbacks(connectStableCheck);
////                Log.w(TAG, "connectFailed(): Sensor OFF ID= "+ mSensorId + ", RetryCount=30");
////                disconnect(true);
////                updateUI(Const.ACTION_GATT_FAILED);
////            }
//
////            {
////                mRetryCount++;
////                mAutoHandler.postDelayed(reconnect, 1000);
////                mConnectState = CONNECT_STATE.CONNECTING;
////                updateUI(Const.ACTION_GATT_CONNECTING);
////            }
//        }
//    };

//    private Runnable reconnect = new Runnable() {
//        public void run() {
//            Log.w(TAG, "ID:" + mSensorId + " reconnect()");
//            if(mBluetoothGatt != null) {
//                mBluetoothGatt.disconnect();
//                //mBluetoothGatt.close();
//                //mBluetoothGatt = null;
//                //connect();
//                if(mBluetoothGatt.connect()) {
//                    Log.w(TAG, "ID:" + mSensorId + " reconnect() - mBluetoothGatt.connect() OK!");
//                }
//                else {
//                    Log.w(TAG, "ID:" + mSensorId + " reconnect() - mBluetoothGatt.connect() FAILED!");
//                }
//            }else {
//                Log.w(TAG, "ID:" + mSensorId + " reconnect() - mBluetoothGatt == NULL !!!!!!!");
//                //connect();
//            }
//
//            //mBluetoothAdapter.startLeScan(mLeScanCallback);
//            mAutoHandler.postDelayed(connectFailed, 1000 * 3);
//            //updateUI(IntentConstants.ACTION_GATT_FAILED);
//        }
//    };

    private Runnable reconnect = new Runnable() {
        public void run() {
            Log.w(TAG, "ID:" + mSensorId + " reconnect()");
            if(mBluetoothGatt != null && mConnectState != CONNECT_STATE.CONNECTED) {
                //mBluetoothGatt.disconnect();
                //mBluetoothGatt.close();
                //mBluetoothGatt = null;
                //connect();
                if(mBluetoothGatt.connect()) {
                    Log.w(TAG, "ID:" + mSensorId + " reconnect() - mBluetoothGatt.connect() OK!");
                }
                else {
                    Log.w(TAG, "ID:" + mSensorId + " reconnect() - mBluetoothGatt.connect() FAILED!");
                }

                mConnectState = CONNECT_STATE.CONNECTING;
            }else {
                Log.w(TAG, "ID:" + mSensorId + " reconnect() - mBluetoothGatt == NULL !!!!!!!");
                //connect();
            }

            //mBluetoothAdapter.startLeScan(mLeScanCallback);
            //mAutoHandler.postDelayed(connectFailed, 1000 * 2);
            // 혹시, 어떤 단말(API 버전)에서 BluetoothGatt 스스로 Reconnection 시도를 안할지도 모르니 방어코드로 30초에 한번은 시도.
            // To Do: BluetoothGatt 스스로 Reconnect 시도의 배터리 소모량과 강제 Disconnect 하고 일정 주기로 Connect 하는 방안의 배터리 소모량 확인
            mAutoHandler.postDelayed(reconnect, 1000 * 30);
            //updateUI(IntentConstants.ACTION_GATT_FAILED);
        }
    };

//    private Runnable tryNewConnect = new Runnable() {
//        public void run() {
//            Log.w(TAG, "ID:" + mSensorId + " tryNewConnect()");
//            mAutoHandler.removeCallbacks(tryNewConnect);
//            connect();
//        }
//    };

//    private BluetoothAdapter.LeScanCallback mLeScanCallback = new BluetoothAdapter.LeScanCallback() {
//        @Override
//        public void onLeScan(final BluetoothDevice device, final int rssi, byte[] scanRecord) {
//            if (mSensorId.equals(device.getAddress())) {
//                Log.w(TAG, "ID:" + mSensorId + " mLeScanCallback Found device" + device.getAddress());
//                //disconnect(true);
//                cancel_connect();
//                mAutoHandler.postDelayed(tryNewConnect, 100);
//            }else {
//                Log.w(TAG, "ID: "+ mSensorId + " mLeScanCallback my Name:" + mSensorName + "  device:" + device.getAddress());
//            }
//        }
//    };

//    private Runnable connectStableCheck = new Runnable() {
//        public void run() {
//            if(mBluetoothGatt != null) {
//                if(mConnectState != CONNECT_STATE.CONNECTED) {
//                    actionUI(Const.ACTION_GATT_DISCONNECTED);
//                }
//            }
//            Log.w(TAG, "connectStableCheck start..");
//        }
//    };

    private Runnable serviceDiscoveredFailed = new Runnable() {
        @Override
        public void run() {
            Log.w(TAG, "ID: "+ mSensorId + " serviceDiscoveredFailed()");
            if(mConnectState != CONNECT_STATE.CONNECTED) {
                //disconnect(true);
                mAutoHandler.postDelayed(reconnect, 100);
            }
        }
    };

    /**
     * After using a given BLE device, the app must call this method to ensure resources are
     * released properly.
     */
    public void close() {
        if (mBluetoothGatt == null) {
            return;
        }
        //Log.w(TAG, "ID: "+ mPosition+ " mBluetoothGatt closed");
        Log.w(TAG, "ID: "+ mSensorId + " mBluetoothGatt closed");
        mSensorId = null;
        mBluetoothGatt.close();
        mBluetoothGatt = null;
        //mCurConnState = mPreConnState = CONN_STATE.INITIALIZED;
        mConnectState = CONNECT_STATE.INITIALIZED;
        mAutoHandler.removeCallbacks(reconnect);
        //mAutoHandler.removeCallbacks(connectFailed);
    }

    public void enableTXNotification()
    {
    	/*
    	if (mBluetoothGatt == null) {
    		showMessage("mBluetoothGatt null" + mBluetoothGatt);
    		broadcastUpdate(DEVICE_DOES_NOT_SUPPORT_UART);
    		return;
    	}
    	*/
        BluetoothGattService RxService = mBluetoothGatt.getService(UART_SERVICE_UUID);
        if (RxService == null) {
            //showMessage("Rx service not found!");
            //broadcastUpdate(DEVICE_DOES_NOT_SUPPORT_UART);
            return;
        }
        BluetoothGattCharacteristic TxChar = RxService.getCharacteristic(UART_TX_CHAR_UUID);
        if (TxChar == null) {
            //showMessage("Tx characteristic not found!");
            //broadcastUpdate(DEVICE_DOES_NOT_SUPPORT_UART);
            return;
        }
        mBluetoothGatt.setCharacteristicNotification(TxChar,true);
        BluetoothGattDescriptor descriptor = TxChar.getDescriptor(CCCD);
        descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
        mBluetoothGatt.writeDescriptor(descriptor);

    }

    public void writeRXCharacteristic(byte[] value)
    {
        if (mBluetoothGatt == null) {
            Log.d(TAG, "writeRXCharacteristic() - BluetoothGatt = NULL.. ID=" + mSensorId);
            return;
        }
        Log.d(TAG, "writeRXCharacteristic() - BluetoothGatt=" + mBluetoothGatt + ", ID=" + mSensorId);

        BluetoothGattService RxService = mBluetoothGatt.getService(UART_SERVICE_UUID);
        //showMessage("mBluetoothGatt null"+ mBluetoothGatt);
        if (RxService == null) {
            //showMessage("Rx service not found!");
            //broadcastUpdate(DEVICE_DOES_NOT_SUPPORT_UART);
            return;
        }
        BluetoothGattCharacteristic RxChar = RxService.getCharacteristic(UART_RX_CHAR_UUID);
        if (RxChar == null) {
            //showMessage("Rx characteristic not found!");
            //broadcastUpdate(DEVICE_DOES_NOT_SUPPORT_UART);
            return;
        }
        RxChar.setValue(value);
        boolean status = mBluetoothGatt.writeCharacteristic(RxChar);
        //Log.d(TAG, "ID: " + mPosition +  " write TXchar - status=" + status);
        Log.d(TAG, "ID: " + mSensorId +  " write TXchar - status=" + status);
    }

    /**
     * 단말기와 연결이 끊겼을 때 경고화면을 띄운다.
     */
    public void actionUI(String action) {
        //Log.w(KeyManager.TAG, "Draw Link-loss Activity");
        //if(!mLinked) {/*if this sensor removed from service.*/
        //    Log.d(TAG, "Can't invoke UI due to be removed from service.");
        //    return;
        //}
        //Log.d(TAG, "ID: "+ mPosition+ " actionUI() called.");
        Log.d(TAG, "ID: "+ mSensorId + " actionUI() called.");

        Bundle bun = new Bundle();
        bun.putString(Const.EXTRA_ACTION_TYPE, action);
        bun.putString(Const.EXTRA_ACTION_SENSOR_ID, mSensorName);  //BO: add for alert dialog
        //bun.putInt(Const.EXTRA_ACTION_POSITION, mPosition);

        Intent popupIntent = new Intent(mContext, MainActivity.class);
        //popupIntent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);    //BO: CareBell에서는 Detect에서만 있으므로 onDetect() 참조
        popupIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP|Intent.FLAG_ACTIVITY_SINGLE_TOP);
        popupIntent.putExtras(bun);
        PendingIntent pie= PendingIntent.getActivity(mContext, 1, popupIntent, PendingIntent.FLAG_ONE_SHOT);
        try {
            pie.send();
        }catch (PendingIntent.CanceledException e) {
            //Log.w(TAG, "ID:" + mPosition+ " Activity Pending Intent Canceled");
            Log.w(TAG, "ID:" + mSensorId + " Activity Pending Intent Canceled");
        }
    }

    // App 에서 UI 갱신이 필요할 때, 이렇게 전달했네. Activity에 Broadcast Receiver가 있어야 하겠네.
    public void updateUI(String action) {
        if(Const.DEBUG) {
            Log.d(TAG, "updateUI() called. Action=" + action);
        }

        final Intent intent = new Intent(action);
        intent.putExtra(Const.EXTRA_ACTION_TYPE, action);
        //intent.putExtra(Const.EXTRA_ACTION_POSITION, mPosition);
        intent.putExtra(Const.EXTRA_ACTION_SENSOR_ID, mSensorId);
        LocalBroadcastManager.getInstance(mContext).sendBroadcast(intent);
    }

    // 1. 아래의 initialize()에서 호출
    // 2. App에서 센서를 추가했을 때(addSensor) 호출
    public void initialize() {
        //Log.d(TAG, "ID:" + mPosition+ " initialize() called.");
        Log.d(TAG, "ID:" + mSensorId + " initialize() called.");

        //setPreConnState(CONN_STATE.INITIALIZED);
        setConnectState(CONNECT_STATE.INITIALIZED);
        //setCurDetectState(DETECTION_STATE.NOT_DETECTED);
        //setActiveState(ACTIVE_STATE.INITIALIZED);
        //mDisconnectedTime = System.currentTimeMillis();
        setBatteryLevel(80);
    }

    // SensorService를 시작할 때(onCreate) 센서들을 초기화시키고 연결하기 위해 호출
    // To Do.  2개의 initialize()를 하나로 통합해도 되겠다. ***********************
    public void initialize(Context context/*,ConnectionEvent CE, SensorDatabase db*/) {
        //initialize(); 위의 initialize() 통합
        setConnectState(CONNECT_STATE.INITIALIZED);
        setBatteryLevel(80);

        this.mContext = context;
        final BluetoothManager bluetoothManager =
                (BluetoothManager) mContext.getSystemService(Context.BLUETOOTH_SERVICE);
        mBluetoothAdapter = bluetoothManager.getAdapter();
        if (mBluetoothAdapter != null) {
            if(mAutoHandler == null)
                mAutoHandler = new Handler();
            if(mQueryHandler == null)
                mQueryHandler = new Handler();
            if(mReconnHandler == null)
                mReconnHandler = new Handler();
            if(mServiceDiscoveredHandler == null)
                mServiceDiscoveredHandler = new Handler();

            if(mFindingHandler == null)
                mFindingHandler = new Handler();
        }
    }

    public String toShortString() {
        return "Sensor{" + "Name=" + mSensorName + ", ID=" + mSensorId + ", ConnectState=" + mConnectState + ", HashCode="  + this.hashCode() + "}";
    }

    public String toLongString() {
        return "Sensor{" + "Name=" + mSensorName + ", ID=" + mSensorId + ", Mode="  + mActionMode + ", ConnectState="  + mConnectState
                + ", Lat=" + mLatitude + ", Lon=" + mLongitude + ", Battery=" + mBatteryLevel + ", HashCode="  + this.hashCode() + "}";
    }
}
