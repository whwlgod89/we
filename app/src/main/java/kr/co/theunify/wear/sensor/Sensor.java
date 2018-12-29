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

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.UUID;

import kr.co.theunify.wear.Const;
import kr.co.theunify.wear.activity.MainActivity;
import kr.co.theunify.wear.data.SensorInfo;
import kr.co.theunify.wear.utils.ULog;

public class Sensor implements Serializable {
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

    private SensorInfo mSensor;

    public boolean mScanned;           // 09.08: 서비스에서 스캔하는 것으로 변경 과정에서 추가

    private boolean mForceDisconnect = false;
    private int mRetryCount = 0;        // 일단 살려서 해본다... 30번까지
    private int mQueryCount = 0;      // 일단 막는다.. 의미가 없어 보인다.

    private Context mContext = null;
    private BluetoothAdapter mBluetoothAdapter;
    private BluetoothGatt mBluetoothGatt;
    private ConnectionEvent ce;

    private Handler mAutoHandler = null;
    private Handler mQueryHandler = null;
    private Handler mReconnHandler = null;
    private Handler mServiceDiscoveredHandler = null;

    private Handler mFindingHandler = null;
    private boolean mFindingSensor = false;

    public Sensor() {

    }

    // AddSensor 에서 센서 추가
    public Sensor(Context context, String sensorId, String sensorName, String phoneNumber, int actionMode, int rssi) {
        this.mContext = context;
        final BluetoothManager bluetoothManager =
                (BluetoothManager) mContext.getSystemService(Context.BLUETOOTH_SERVICE);
        mBluetoothAdapter = bluetoothManager.getAdapter();
        if (mBluetoothAdapter != null) {
            mAutoHandler = new Handler();
            mQueryHandler = new Handler();
            mReconnHandler = new Handler();
            mServiceDiscoveredHandler = new Handler();

            mFindingHandler = new Handler();
        }
        mSensor = new SensorInfo();
        mSensor.setId(sensorId);
        mSensor.setName(sensorName);
        mSensor.setPhone(phoneNumber);
        mSensor.setMode(actionMode);
        mSensor.setRssi(rssi);

        mSensor.setState(CONNECT_STATE.INITIALIZED.ordinal());    // 찾아서 등록하니까 연결상태로 한다.
        mSensor.setLatitude(37.5759369);     // 광화문
        mSensor.setLongitude(126.9768157);
        mSensor.setBattery(100);

        mScanned = false;
    }

    // DB 에서 읽어서 센서 추가
    public Sensor(SensorInfo info) {
        mSensor = info;
        mSensor.setState(CONNECT_STATE.INITIALIZED.ordinal());    // 찾아서 등록하니까 연결상태로 한다.
        mScanned = false;
    }

    public Sensor(String sensorId, String sensorName, String phoneNumber, int actionMode, double latitude, double longitude, int batLevel) {
        mSensor = new SensorInfo();
        mSensor.setId(sensorId);
        mSensor.setName(sensorName);
        mSensor.setPhone(phoneNumber);
        mSensor.setMode(actionMode);

        mSensor.setState(CONNECT_STATE.INITIALIZED.ordinal());    // 찾아서 등록하니까 연결상태로 한다.
        mSensor.setLatitude(latitude);     // 광화문
        mSensor.setLongitude(longitude);
        mSensor.setBattery(batLevel);

        mScanned = false;
    }

    // Get Member Values
    public SensorInfo getInfo() {return mSensor; }
    public String getSensorId() { return mSensor.getId(); }
    public String getSensorName() { return mSensor.getName(); }
    public String getPhoneNumber() { return mSensor.getPhone(); }
    public int getActionMode() { return mSensor.getMode(); }
    public int getConnectState() { return mSensor.getState(); }
    public boolean isConnected() { return (mSensor.getState() == CONNECT_STATE.CONNECTED.ordinal()); }
    public double getLatitude() { return mSensor.getLatitude(); }
    public double getLongitude() { return mSensor.getLongitude(); }
    public int getBatteryLevel() { return mSensor.getBattery(); }

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
        ULog.w(TAG, "rxHandler: ID= "+ mSensor.getId() + ", CMD=" + cmd + ", DATA=" + data);

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
                ULog.w(TAG, "Start Alarm of Phone: ID= "+ mSensor.getId() + ", Battery=" + data);
                //updateUI(Const.ACTION_SENSOR_FIND_PHONE_START);
                actionUI(Const.ACTION_SENSOR_FIND_PHONE_START);
                break;
            case 0xEE:      // 센서에서 휴대폰 찾기 알람 종료 (data = 배터리 잔량)
                ULog.w(TAG, "Stop Alarm of Phone: ID= "+ mSensor.getId() + ", Battery=" + data);
                //updateUI(Const.ACTION_SENSOR_FIND_PHONE_STOP);
                actionUI(Const.ACTION_SENSOR_FIND_PHONE_STOP);
                break;
            case 0xF0:      // 센서 단말기의 FW 버전 정보 (data = 버전 정보)
                ULog.w(TAG, "Response of Firmware: ID= "+ mSensor.getId() + ", Version=" + data);
                break;
            case 0xF1:      // 센서에서 주기적으로 발신하는 배터리 잔량 (data = 배터리 잔량)
                ULog.w(TAG, "Report of Battery Level: ID= "+ mSensor.getId() + ", Battery=" + data);
                mSensor.setBattery(data);
                updateUI(Const.ACTION_SENSOR_BATTERY);
                //battery = true;
                break;
            case 0xFF:      // 센서 단말기의 전원 OFF 알림 (data = 배터리 잔량)
                ULog.w(TAG, "rxHandler(): Report of Sensor OFF: ID= "+ mSensor.getId() + ", Battery=" + data);
                //disconnected = true;
                //mActiveState = ACTIVE_STATE.POWER_OFF;
                disconnect(true);
                break;
            default:
                ULog.w(TAG, "Unknown Packet");
                break;
        }
    }

    // Implements callback methods for GATT events that the app cares about.  For example,
    // connection change and services discovered.
    private final BluetoothGattCallback mGattCallback = new BluetoothGattCallback() {
        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            ULog.i(TAG,"ID: "+ mSensor.getId() + " onConnectionStateChange(), newState=" + newState + ", mConnectState=" + mSensor.getState());

            if (newState == BluetoothProfile.STATE_CONNECTED) {
                ULog.i(TAG,"ID: "+ mSensor.getId() + " Connected to GATT server.");
                if(mBluetoothGatt != null) {
                    mBluetoothGatt.discoverServices();
                }
                //6초 이내에   discoverServices 가 응답하지 않으면 failed 처리
                //mAutoHandler.postDelayed(connectFailed, 1000*6);
                mServiceDiscoveredHandler.postDelayed(serviceDiscoveredFailed, 1000 * 5);
            }
            // 연결이 끊어진 상태에서도 DISCONNECTED가 오는 경우가 있다. 현재 상태 체크를 추가한다.
            else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                ULog.i(TAG,"ID: " + mSensor.getId() + " Disconnected from GATT server.");

                if(mSensor.getState() == CONNECT_STATE.CONNECTED.ordinal()) {
                    SimpleDateFormat dateFormat = new SimpleDateFormat("MM-dd HH:mm:ss");      // Debug
                    mDisconnectedDateTime = dateFormat.format(Calendar.getInstance().getTime());

                    actionUI(Const.ACTION_GATT_DISCONNECTED);    //BO: Launch Activity with Intent
                    mSensor.setState(CONNECT_STATE.INITIALIZED.ordinal());          // 시작시와 같게 맞추기 위해 INITIALIZED로 설정
                }

                //updateUI(Const.ACTION_GATT_DISCONNECTED);  //BO: Broadcast Message
                if(mForceDisconnect) {
                    //Bo: 다 막혀있는데... 필요없는 것인가? =============================
                    //mDisconnectHandler.removeCallbacks(force_disconnect_handler);
                    //updateUI(IntentConstants.ACTION_GATT_DISCONNECTED);
                }else {
                    // 연결이 끊어졌거나, 초기 시작할 때 연결 시도가 실패한 경우에만 재연결 시도를 위해 Reconnect 호출
                    if(mBluetoothGatt != null && mSensor.getState() == CONNECT_STATE.INITIALIZED.ordinal()) {
                        mAutoHandler.postDelayed(reconnect, 100);
                        //mConnectState = CONNECT_STATE.CONNECTING;
                    }
                }
            }
        }

        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                ULog.w(TAG, "mBluetoothGatt.onServicesDiscovered() - ID: " + mSensor.getId() + ", mConnectState=" + mSensor.getState());

                SimpleDateFormat dateFormat = new SimpleDateFormat("MM-dd HH:mm:ss");
                mConnectedDateTime = dateFormat.format(Calendar.getInstance().getTime());

                mSensor.setState(CONNECT_STATE.CONNECTED.ordinal());
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
                //ULog.w(TAG, "ID: "+ mPosition + " onServicesDiscovered received: " + status);
                ULog.w(TAG, "ID: "+ mSensor.getId() + " onServicesDiscovered received: " + status);
            }
        }

        @Override
        public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                rxHandler(characteristic);
            }
        }

        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
            rxHandler(characteristic);
        }

        @Override
        public void onReadRemoteRssi(BluetoothGatt gatt, int rssi, int status) {
            ULog.w(TAG, "onReadRemoteRssi: Name=" + mSensor.getName() + ", RSSI=" + rssi + ", LOCAL=" + mSensor.getRssi() + ", STATUS=" + status);
            if(status == BluetoothGatt.GATT_SUCCESS) {
                if(mSensor.getMode() == Const.ACTION_MODE_THEFT && rssi < mSensor.getRssi()) {
                    ULog.e(TAG, "Hit Low RSSI in Theft Mode");
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
            ULog.w(TAG, "readRemoteRssi: Sensor=" + mSensor.getName());
            mBluetoothGatt.readRemoteRssi();
        }
    }

    private Runnable rxSensorInit = new Runnable() {
        public void run() {
            if(mQueryCount > 3) {
                mQueryCount = 0;      //BO: 3번까지만 유지되게 해본다........
            }else {
                ULog.w(TAG, "rxSensorInit(): ID=" + mSensor.getId() + ", ConnectState=" + mSensor.getState());
                sensor_init();
            }
        }
    };

    // 센서 초기화?... 센서에 연결 알림으로 사용하면 될까?.................
    public void sensor_init() {
        ULog.w(TAG, "sensor_init(): ID=" + mSensor.getId() + ", ConnectState=" + mSensor.getState());
        if(mSensor.getState() != CONNECT_STATE.CONNECTED.ordinal()) {
            return;
        }

        byte[] packet = new byte[2];
        packet[0] = (byte) 0x01;    // 휴대폰은 단말기와 연결되면 10초 이내에 발신해야 한다.
        packet[1] = (byte) 0x00;    // Dummy
        writeRXCharacteristic(packet);
        ULog.w(TAG, "SENT Initial Code...");
        mQueryCount++;
        mQueryHandler.postDelayed(rxSensorInit, 3000);
        //updateUI(Const.ACTION_SENSOR_INITIALIZE);
    }

    public void findSensor() {
        ULog.w(TAG, "findSensor(): ID=" + mSensor.getId() + ", ConnectState=" + mSensor.getState()+ ", FindState=" + mFindingSensor);
        if(mSensor.getState() != CONNECT_STATE.CONNECTED.ordinal()) {
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
//        ULog.w(TAG, "sensor_poweroff(): ID=" + mSensorId + ", ConnectState=" + mConnectState);
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
        ULog.w(TAG, "ID: "+ mSensor.getId() +" connect() called. mConnectState=" + mSensor.getState());

        if (mBluetoothAdapter == null || mSensor.getId() == null) {
            //Log.w(TAG, "ID: "+ mPosition + " BluetoothAdapter not initialized or unspecified address.");
            ULog.w(TAG, "BluetoothAdapter not initialized or unspecified address.");
            return false;
        }
        if(mSensor.getState() == CONNECT_STATE.CONNECTED.ordinal()) {
            //ULog.w(TAG, "ID: " + mPosition +" Already connected.");
            ULog.w(TAG, "ID: " + mSensor.getId() +" Already connected.");
            return false;
        }

        mForceDisconnect = false;

        // Previously connected device.  Try to reconnect.
        if (mBluetoothGatt != null) {
            ULog.w(TAG, "connect().mBluetoothGatt.connect() - ID: "+ mSensor.getId() + ", mConnectState=" + mSensor.getState());
            if (mBluetoothGatt.connect()) {
                // 연결이 안되었는데, OK가 떨어지네 (센서 끊긴 상태에서 서비스 시작할 때의 Connect()에서 발견)
                // Android 문서에서는 연결 시도 초기화가 성공되면 true로 적혀 있고, 범위 밖에 있어도 재시도를 한다고 적혀 있다~!!!.
                ULog.w(TAG, "ID: "+ mSensor.getId() + " Connected OK!");
                return true;
            } else {
                ULog.w(TAG, "ID: "+ mSensor.getId() + " Connect FAILED!");
                return false;
            }
        }

        final BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(mSensor.getId());
        if (device == null) {
            //ULog.w(TAG, "ID:" + mPosition+ " Device not found.  Unable to connect.");
            ULog.w(TAG, "ID:" + mSensor.getId() + " Device not found.  Unable to connect.");
            return false;
        }

        // We want to directly connect to the device, so we are setting the autoConnect
        // parameter to false.
        ULog.w(TAG, "connect().device.connectGatt() - ID: "+ mSensor.getId() + ", mConnectState=" + mSensor.getState());
        mBluetoothGatt = device.connectGatt(mContext, false, mGattCallback);
        if(mBluetoothGatt != null) {
            ULog.w(TAG, "ID: "+ mBluetoothGatt.getDevice().getAddress() + " connectGatt() OK!");
        }
        else {
            ULog.w(TAG, "ID: "+ mSensor.getId() + " connectGatt() FAILED");
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
            mSensor.setState(CONNECT_STATE.FORCE_DISCONNECTED.ordinal());
        else
            mSensor.setState(CONNECT_STATE.DISCONNECTED.ordinal());
        if (mBluetoothAdapter == null || mBluetoothGatt == null) {
            //ULog.w(TAG, "ID: "+ mPosition+ " BluetoothAdapter not initialized");
            ULog.w(TAG, "ID: "+ mSensor.getId() + " BluetoothAdapter not initialized");
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
        ULog.w(TAG, "disconnect(): Sensor OFF ID= "+ mSensor.getId() );
        disconnect();
    }

    private void cancel_connect() {
        //mBluetoothAdapter.stopLeScan(mLeScanCallback);
        mAutoHandler.removeCallbacks(reconnect);
        //mAutoHandler.removeCallbacks(connectFailed);
        //mReconnHandler.removeCallbacks(connectStableCheck);
        mServiceDiscoveredHandler.removeCallbacks(serviceDiscoveredFailed);
        if (mBluetoothAdapter == null || mBluetoothGatt == null) {
            //ULog.w(TAG, "ID: "+ mPosition+" BluetoothAdapter not initialized");
            ULog.w(TAG, "ID: "+ mSensor.getId() +" BluetoothAdapter not initialized");
            return;
        }

        mBluetoothGatt.disconnect();
        mBluetoothGatt.close();
        mBluetoothGatt = null;
    }


    private Runnable reconnect = new Runnable() {
        public void run() {
            ULog.w(TAG, "ID:" + mSensor.getId() + " reconnect()");
            if(mBluetoothGatt != null && mSensor.getState() != CONNECT_STATE.CONNECTED.ordinal()) {
                //mBluetoothGatt.disconnect();
                //mBluetoothGatt.close();
                //mBluetoothGatt = null;
                //connect();
                if(mBluetoothGatt.connect()) {
                    ULog.w(TAG, "ID:" + mSensor.getId() + " reconnect() - mBluetoothGatt.connect() OK!");
                }
                else {
                    ULog.w(TAG, "ID:" + mSensor.getId() + " reconnect() - mBluetoothGatt.connect() FAILED!");
                }

                mSensor.setState(CONNECT_STATE.CONNECTING.ordinal());
            }else {
                ULog.w(TAG, "ID:" + mSensor.getId() + " reconnect() - mBluetoothGatt == NULL !!!!!!!");
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


    private Runnable serviceDiscoveredFailed = new Runnable() {
        @Override
        public void run() {
            ULog.w(TAG, "ID: "+ mSensor.getId() + " serviceDiscoveredFailed()");
            if(mSensor.getState() != CONNECT_STATE.CONNECTED.ordinal()) {
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
        //ULog.w(TAG, "ID: "+ mPosition+ " mBluetoothGatt closed");
        ULog.w(TAG, "ID: "+ mSensor.getId() + " mBluetoothGatt closed");
        mSensor.setId("");          //FIXME
        mBluetoothGatt.close();
        mBluetoothGatt = null;
        //mCurConnState = mPreConnState = CONN_STATE.INITIALIZED;
        mSensor.setState(CONNECT_STATE.INITIALIZED.ordinal());
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
            ULog.w(TAG, "writeRXCharacteristic() - BluetoothGatt = NULL.. ID=" + mSensor.getId());
            return;
        }
        ULog.w(TAG, "writeRXCharacteristic() - BluetoothGatt=" + mBluetoothGatt + ", ID=" + mSensor.getId());

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
        //ULog.w(TAG, "ID: " + mPosition +  " write TXchar - status=" + status);
        ULog.w(TAG, "ID: " + mSensor.getId() +  " write TXchar - status=" + status);
    }

    /**
     * 단말기와 연결이 끊겼을 때 경고화면을 띄운다.
     */
    public void actionUI(String action) {
        //ULog.w(KeyManager.TAG, "Draw Link-loss Activity");
        //if(!mLinked) {/*if this sensor removed from service.*/
        //    ULog.w(TAG, "Can't invoke UI due to be removed from service.");
        //    return;
        //}
        //ULog.w(TAG, "ID: "+ mPosition+ " actionUI() called.");
        ULog.w(TAG, "ID: "+ mSensor.getId() + " actionUI() called.");

        Bundle bun = new Bundle();
        bun.putString(Const.EXTRA_ACTION_TYPE, action);
        bun.putString(Const.EXTRA_ACTION_SENSOR_ID, mSensor.getName());  //BO: add for alert dialog
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
            Log.w(TAG, "ID:" + mSensor.getId() + " Activity Pending Intent Canceled");
        }
    }

    // App 에서 UI 갱신이 필요할 때, 이렇게 전달했네. Activity에 Broadcast Receiver가 있어야 하겠네.
    public void updateUI(String action) {
        if(Const.DEBUG) {
            ULog.w(TAG, "updateUI() called. Action=" + action);
        }

        final Intent intent = new Intent(action);
        intent.putExtra(Const.EXTRA_ACTION_TYPE, action);
        //intent.putExtra(Const.EXTRA_ACTION_POSITION, mPosition);
        intent.putExtra(Const.EXTRA_ACTION_SENSOR_ID, mSensor.getId());
        LocalBroadcastManager.getInstance(mContext).sendBroadcast(intent);
    }

    // 1. 아래의 initialize()에서 호출
    // 2. App에서 센서를 추가했을 때(addSensor) 호출
    public void initialize() {
        //ULog.w(TAG, "ID:" + mPosition+ " initialize() called.");
        ULog.w(TAG, "ID:" + mSensor.getId() + " initialize() called.");

        //setPreConnState(CONN_STATE.INITIALIZED);
        mSensor.setState(CONNECT_STATE.INITIALIZED.ordinal());
        //setCurDetectState(DETECTION_STATE.NOT_DETECTED);
        //setActiveState(ACTIVE_STATE.INITIALIZED);
        //mDisconnectedTime = System.currentTimeMillis();
        mSensor.setBattery(80);
    }

    // SensorService를 시작할 때(onCreate) 센서들을 초기화시키고 연결하기 위해 호출
    // To Do.  2개의 initialize()를 하나로 통합해도 되겠다. ***********************
    public void initialize(Context context/*,ConnectionEvent CE, SensorDatabase db*/) {
        //initialize(); 위의 initialize() 통합
        mSensor.setState(CONNECT_STATE.INITIALIZED.ordinal());
        mSensor.setBattery(80);

        this.mContext = context;
        final BluetoothManager bluetoothManager = (BluetoothManager) mContext.getSystemService(Context.BLUETOOTH_SERVICE);
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
        return "Sensor{" + "Name=" + mSensor.getName() + ", ID=" + mSensor.getId() + ", ConnectState=" + mSensor.getState() + ", HashCode="  + this.hashCode() + "}";
    }

    public String toLongString() {
        return "Sensor{" + "Name=" + mSensor.getName() + ", ID=" + mSensor.getId() + ", Mode="  + mSensor.getMode() + ", ConnectState="  + mSensor.getState()
                + ", Lat=" + mSensor.getLatitude() + ", Lon=" + mSensor.getLongitude() + ", Battery=" + mSensor.getBattery() + ", HashCode="  + this.hashCode() + "}";
    }
}
