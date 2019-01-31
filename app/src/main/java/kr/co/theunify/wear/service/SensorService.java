package kr.co.theunify.wear.service;

import android.app.PendingIntent;
import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Binder;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Vibrator;
import android.preference.PreferenceManager;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import kr.co.theunify.wear.Const;
import kr.co.theunify.wear.WearApp;
import kr.co.theunify.wear.activity.MainActivity;
import kr.co.theunify.wear.sensor.Sensor;
import kr.co.theunify.wear.utils.ULog;

public class SensorService extends Service {
    private final static String TAG = "[" + SensorService.class.getSimpleName() + "]";
    private WearApp mApp;
    //private SensorDatabase mDB;

    public ArrayList<Sensor> mSensorList;
    private BluetoothAdapter mBluetoothAdapter;

    private boolean mServiceStarted = false;
    //private boolean mConnectNotification = false;

    private static final int TIMER_PERIOD = 1000 * 60 * 5;
    private int mAlarmHowTo=0;
    private int mAlarmHowLong=0;
    private int mAlarmHowOften=0;
    private int mSoundLoopCount=0;
    private int mVibeLoopCount=0;
    private int mSoundFinishCount=0;
    private int mVibeFinishCount=0;

    private Handler hRingTone;
    private Handler hVibe;
    private Vibrator mVibe;
    private MediaPlayer mediaPlayer;
    private RingtoneManager mRingtoneMgr;
    private AudioManager audioManager;
    private int beforeAudioVolume;        //이전 오디오 볼륨

    //BO: Alarm (Sound + Vibrate)
    private boolean mAlarmVibrate = false;
    private boolean mAlarmSound = false;
    private int mAlarmLoopCount = 0;
    private int mAlarmPlayCount = 0;
    private Uri mAlarmUri;
    private Handler hAlarm;

    @Override
    public void onCreate() {
        super.onCreate();

        mApp = (WearApp)getApplication();
        ULog.i(TAG, "onCreateService. Service Started. App=" + ((mApp == null) ? "NULL" : mApp));
        ULog.i(TAG, "onCreateService SensorService=" + this);

        final BluetoothManager bluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        mBluetoothAdapter = bluetoothManager.getAdapter();

        //mNotificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

        IntentFilter filter = new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED);
        registerReceiver(mReceiver, filter);

        mApp = (WearApp)getApplication();
        mApp.setService(this);
        //mDB = mApp.getDB();

        mSensorList = mApp.getAllSensors();
        scanSensors();
//        for(int i=0; i<mSensorList.size();i++ ) {
//            Sensor sensor = mSensorList.get(i);
//            sensor.initialize(getApplicationContext());
//            sensor.connect();
//        }

        mRingtoneMgr = new RingtoneManager(getApplicationContext());
        mediaPlayer = MediaPlayer.create(this,RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE));
        audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);

        mVibe = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        //Uri notification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        if(hVibe == null) {
            hVibe = new Handler();
        }
        if(hRingTone == null) {
            hRingTone = new Handler();
        }

        //BO:
        if(hAlarm == null) {
            hAlarm = new Handler();
        }

        // 도난 방지 모드를 지원하기 위해 신호 세기를 계속 읽어야 한다.
        mReadRssiHandler.sendEmptyMessageDelayed(0, 1000);

        ULog.i(TAG, "Service Create restart  " + mServiceStarted);
        mServiceStarted = true;
    }

    @Override
    public boolean stopService(Intent name) {
        mBtAdapter.stopLeScan(mLeScanCallback);
        return super.stopService(name);
    }

    private BluetoothManager mBtManager = null;
    private BluetoothAdapter mBtAdapter = null;
    private int mScannedSensorCount = 0;
    private boolean mScanStarted = false;
    //private Handler mScanStopHandler = null;

    public boolean scanSensors() {
        if(mScanStarted) {
            return false;
        }

        mBtManager = (BluetoothManager)getSystemService(BLUETOOTH_SERVICE);
        mBtAdapter = mBtManager.getAdapter();

        mScannedSensorCount = mSensorList.size();
        for (Sensor sensor: mSensorList) {
            sensor.mScanned = false;
        }
        mBtAdapter.startLeScan(mLeScanCallback);
        mScanStarted = true;

        // 만약 모두 연결되지 않았어도 일정 시간(30초?) 후에 스캔을 멈추려면 핸들러를 추가해야겠다.
        //mScanStopHandler = new Handler();
        //mScanStopHandler.postDelayed(mScanStopCallback, 30 * 1000);

        return true;
    }

    //private Runnable mScanStopCallback = new Runnable() {
    //    @Override
    //    public void run() {
    //        mBtAdapter.stopLeScan(mLeScanCallback);
    //        mScanStopHandler.removeCallbacks(mScanStopCallback);
    //    }
    //};

    private BluetoothAdapter.LeScanCallback mLeScanCallback = new BluetoothAdapter.LeScanCallback() {
        @Override
        public void onLeScan(final BluetoothDevice device, final int rssi, byte[] scanRecord) {
            for(int i=0; i<mSensorList.size();i++ ) {
                Sensor sensor = mSensorList.get(i);
                if (sensor.mScanned == false && sensor.getSensorId().equals(device.getAddress())) {
                    Log.w(TAG, "mLeScanCallback.onLeScan() - ID:" + sensor.getSensorId() + " Device Found");
                    sensor.mScanned = true;
                    mScannedSensorCount++;
                    sensor.initialize(getApplicationContext());
                    sensor.connect();

                    if(mScannedSensorCount == mSensorList.size()) {
                        mBtAdapter.stopLeScan(mLeScanCallback);
                        //mScanStopHandler.removeCallbacks(mScanStopCallback);
                    }
                }
            }
        }
    };

    @Override
    public void onDestroy() {
        super.onDestroy();

        if (mBtAdapter != null) {
            mBtAdapter.stopLeScan(mLeScanCallback);
        }

        for(int i=0; i<mSensorList.size();i++ ) {
            Sensor sensor = mSensorList.get(i);
            Log.w(TAG, "onDestroy(): Sensor OFF ID= "+ sensor.getSensorId() + ", Index=" + i);
            sensor.disconnect(true);
        }

        unregisterReceiver(mReceiver);
        mServiceStarted = false;
        ULog.i(TAG, "Service Destroyed");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        //서비스가 실행 중에 앱이 서비스를 다시 호출하는 경우 실행된다.
        ULog.i(TAG, "Service onStartCommand...");
        return super.onStartCommand(intent, flags, startId);

/*
		// 상단에 Notification 띄우기 및 App-List에서 종료시에 Service 종료 안되도록 하는 테스트 코드
        Intent notificationIntent = new Intent(getApplicationContext(), MyActivity.class);
        notificationIntent.setAction(C.ACTION_MAIN);  // A string containing the action name
        notificationIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent contentPendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, 0);

        Bitmap icon = BitmapFactory.decodeResource(getResources(), R.drawable.my_icon);

        Notification notification = new NotificationCompat.Builder(this)
                .setContentTitle(getResources().getString(R.string.app_name))
                .setTicker(getResources().getString(R.string.app_name))
                .setContentText(getResources().getString(R.string.my_string))
                .setSmallIcon(R.drawable.my_icon)
                .setLargeIcon(Bitmap.createScaledBitmap(icon, 128, 128, false))
                .setContentIntent(contentPendingIntent)
                .setOngoing(true)
//                .setDeleteIntent(contentPendingIntent)  // if needed
                .build();
        notification.flags = notification.flags | Notification.FLAG_NO_CLEAR;     // NO_CLEAR makes the notification stay when the user performs a "delete all" command
        startForeground(NOTIFICATION_ID, notification);

        return START_STICKY;
*/
    }

    private final IBinder mBinder = new LocalBinder();

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    // 팝업? Application Context 이면 어떤 Activity 에서도 받을 수 있는 것인가 확인 ============
    private void infoUI(String action) {
        Bundle bun = new Bundle();
        bun.putString(Const.EXTRA_ACTION_TYPE, action);

        Intent popupIntent = new Intent(getApplicationContext(), MainActivity.class);
        popupIntent.putExtras(bun);
        PendingIntent pie= PendingIntent.getActivity(getApplicationContext(), 1, popupIntent, PendingIntent.FLAG_ONE_SHOT);
        try {
            pie.send();
        }catch (PendingIntent.CanceledException e) {
            Log.w(TAG, "Activity Pending Intent Canceled");
        }
    }

    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            if (action.equals(BluetoothAdapter.ACTION_STATE_CHANGED)) {
                final int state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE,BluetoothAdapter.ERROR);
                switch (state) {
                    case BluetoothAdapter.STATE_OFF:
                        for(int i=0; i<mSensorList.size();i++ ) {
                            Sensor sensor = mSensorList.get(i);
                            Log.w(TAG, "BroadcastReceiver::onReceive(): Sensor OFF ID= "+ sensor.getSensorId() + ", Index=" + i);
                            sensor.disconnect(true);
                        }
                        infoUI(Const.ACTION_GATT_STATUS);
                        //stopSelf();
                        break;
                    case BluetoothAdapter.STATE_TURNING_OFF:
                        //setButtonText("Turning Bluetooth off...");
                        //actionUI(IntentConstants.ACTION_GATT_STATUS);
                        break;
                    case BluetoothAdapter.STATE_ON:
                        //setButtonText("Bluetooth on");
                        //for(int i=0; i<mSensorList.size();i++ ) {
                        //    Sensor sensor = (Sensor) mSensorList.get(i);
                        //    sensor.connect();
                        //}
                        break;
                    case BluetoothAdapter.STATE_TURNING_ON:
                        //setButtonText("Turning Bluetooth on...");
                        break;
                }
            }
            //final Intent newIntent = new Intent(IntentConstants.ACTION_GATT_STATUS);
            //LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(newIntent);
            Log.w(TAG, "Boradcast receiver " + action);
        }
    };

    public class LocalBinder extends Binder {
       public SensorService getService() {
            return SensorService.this;
        }
    }

    public boolean initialize() {
        return true;
    }

    public ArrayList<Sensor> getSensorList() {
        return mSensorList;
    }

    // 앱의 센서 등록으로부터 호출
    public Sensor addSensor(Sensor sensor) {
        sensor.initialize(getApplicationContext());
        sensor.connect();
        return sensor;
    }

    public void removeSensor(Sensor sensor) {
        if (sensor != null) {
            //FIXME: we should wait for disconnected event...
            sensor.disconnect();
            ULog.i(TAG, "removeSensor(): Sensor disconnected and removed in the Service= " + sensor.getSensorId());
        }
    }

//    public void renameSensor(Sensor sensor, String name) {
//        sensor.setSensorName(name);
//        //serialize();
//        mApp.updateSensor(sensor);
//        ULog.i(TAG, "Service name changed: " + sensor.getSensorName());
//    }

    public void connectSensor(Sensor sensor) {
        if (sensor != null) {
            sensor.connect();
        }
    }
    public void disConnectSensor(Sensor sensor) {
        if (sensor != null) {
            Log.w(TAG, "disConnectSensor(): Sensor OFF: ID= " + sensor.getSensorId());
            sensor.disconnect(true);
        }
    }
    public void sensorInit(Sensor sensor) {
        ULog.i(TAG, "sensorInit(): ID=" + sensor.getSensorId() + ", ConnectState=" + sensor.getConnectState());
        if(sensor != null)
            sensor.sensor_init();
    }

    public Sensor findSensor(String sensorId) {
        for(Sensor sensor : mSensorList) {
            if(sensor.getSensorId().equals(sensorId)) {
                return sensor;
            }
        }

        return null;
    }

    public boolean hasSensor() {
        return (mSensorList.size() > 0) ? true : false;
    }

    public void startAlarm2(boolean disconnected) {
        ULog.i(TAG,"startAlarm2()");

        if(mApp.isAlarmEnabled()) {
            ULog.i(TAG,"Alarm is already running!!!");
            return;
        }

        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(this);
        mAlarmVibrate = settings.getBoolean("pref_key_alarm_vibrate", false);
        String strDuration = settings.getString("pref_key_alarm_duration", "10");
        int alarmDuration = Integer.parseInt(strDuration);
        mAlarmLoopCount = alarmDuration / 10;
        mAlarmPlayCount = 0;

        String strAlarm;
        if(disconnected) {
            strAlarm = settings.getString("pref_key_alarm_disconnected", "FAIL");   // "Silent");
        }
        else {
            strAlarm = settings.getString("pref_key_alarm_find_phone", "FAIL");     // "Silent");
        }

        ULog.i(TAG, "Alarm Sound=" + strAlarm);

        // 알람이 None이 아닌지 확인하여 진행한다.
        if(strAlarm == null || strAlarm.equals("")) {
            mAlarmSound = false;
        }
        else {
            mAlarmSound = true;

            if(strAlarm.equals("FAIL")) {
                mAlarmUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM);
            }
            else {
                mAlarmUri = Uri.parse(strAlarm);
            }
            mediaPlayer = MediaPlayer.create(this, mAlarmUri);
        }
        ULog.i(TAG, "Alarm=" + mAlarmSound + ", Sound=" + strAlarm + ", Vibrate=" + mAlarmVibrate + ", Count=" + mAlarmLoopCount + "/" + strDuration);

        if(mAlarmSound || mAlarmVibrate) {
            if(mAlarmSound) {
                beforeAudioVolume = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
                audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC),0);
                mediaPlayer.setLooping(true);
                mediaPlayer.start();
            }
            mApp.setAlarmState(true);
            mAlarmTimer.start();
        }
    }

    public void stopAlarm2() {
        ULog.i(TAG,"stopAlarm2()");

        mAlarmTimer.cancel();
        mApp.setAlarmState(false);
        //hAlarm.removeCallbacks(iAlarm);
        audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, beforeAudioVolume,0);

        if(mAlarmSound && mediaPlayer != null) {
            mediaPlayer.stop();
            mediaPlayer.release();
            mediaPlayer = null;
        }

        if(mAlarmVibrate) {
            mVibe.cancel();
        }
    }

    public void startAlarm() {
        boolean bVibeEn = false;
        boolean bSoundEn = false;
        boolean noneDetected = true;

        if(mApp.isAlarmEnabled()) {
            ULog.i(TAG,"Alarm is running already!!!");
            return;
        }

        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(this);
        String strJson = settings.getString("KEY_ALARM_SETTING","{\"howto\":0,\"howlong\":0,\"howoften\":0}");

        JSONObject jsonData = null;
        try {
            jsonData = new JSONObject(strJson);
            mAlarmHowTo = jsonData.getInt("howto");
            mAlarmHowLong = jsonData.getInt("howlong");
            mAlarmHowOften = jsonData.getInt("howoften");

        } catch (JSONException e) {
            e.printStackTrace();
        }

        //
        mAlarmHowTo = 1;    //=================================================
        mAlarmHowLong = 10;
        mAlarmHowOften = 3;
        //switch(Integer.valueOf(settings.getString("KEY_PHONE_ALARM","1"))) {
        switch(mAlarmHowTo) {
            case 0:
                bSoundEn = true;
                bVibeEn = false;
                break;
            case 1:
                bSoundEn = true;
                bVibeEn = true;
                break;
            case 2:
                bSoundEn = false;
                bVibeEn = true;
                break;
            case 3:
                bSoundEn = false;
                bVibeEn = false;
                break;
            default:
                bSoundEn = true;
                bVibeEn = true;
                break;
        }

        //=======================
        Uri defaultRingtoneUri = RingtoneManager.getActualDefaultRingtoneUri(getApplicationContext(), RingtoneManager.TYPE_RINGTONE);
        //Ringtone defaultRingtone = RingtoneManager.getRingtone(getApplicationContext(), defaultRingtoneUri);
        //=========================
        //String ring = settings.getString("KEY_RINGTONE_ALARM", "");
        //if(ring != null && !ring.isEmpty()) {
            //Uri ringToneUri = Uri.parse(ring);
            //ULog.i(TAG,"RING TONE IS ...." + RingtoneManager.EXTRA_RINGTONE_PICKED_URI);
        ULog.i(TAG,"RING TONE IS ...." + RingtoneManager.EXTRA_RINGTONE_DEFAULT_URI);
            //if(RingtoneManager.EXTRA_RINGTONE_PICKED_URI == null) {
            if(RingtoneManager.EXTRA_RINGTONE_DEFAULT_URI == null) {
                bSoundEn = false;
                ULog.i(TAG,"RING TONE IS NULL....");
            }else {
                //if(ringToneUri != null && mRingtoneMgr != null) {
                //    mediaPlayer = MediaPlayer.create(this,ringToneUri);
                if(defaultRingtoneUri != null && mRingtoneMgr != null) {
                    mediaPlayer = MediaPlayer.create(this, defaultRingtoneUri);
                    if(mediaPlayer == null) {
                        ULog.i(TAG,"MEDIA PLAYER IS NULL....");
                        bSoundEn = false;
                    }else {
                        beforeAudioVolume = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
                        audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC),0);
                    }
                }else {
                    ULog.i(TAG,"RING TONE or RingtoneMgr IS NULL....");
                    bSoundEn = false;
                }
            }
        //}else {
        //    ULog.i(TAG,"RING TONE PICKER IS NULL....");
        //    bSoundEn = false;
        //}
        AlarmSound(bSoundEn);
        AlarmVibration(bVibeEn);
        if(bSoundEn || bVibeEn) {
            mApp.setAlarmState(true);
        }
    }

    public void stopAlarm() {
        mApp.setAlarmState(false);
        AlarmSound(false);
        AlarmVibration(false);
    }

    private void AlarmSound(boolean enable) {
        if(enable) {
            hRingTone.postDelayed(iRingTone, 100);
        }else {
            hRingTone.removeCallbacks(iRingTone);
            mRingtoneTimer.cancel();
            if(mediaPlayer != null) {
                mediaPlayer.stop();
                mediaPlayer.release();
                mediaPlayer = null;
            }
        }
    }

    private void AlarmVibration(boolean enable) {
        if(enable) {
            hVibe.postDelayed(iVibe, 100);
        }else {
            hVibe.removeCallbacks(iVibe);
            mVibeTimer.cancel();
            mVibe.cancel();
        }
    }

    // 알람 타이머. 30초를 기본으로 알람 시간만큼 반복 (30초 1번, 1분 2번, 3분 6번), 진동은 1초 울리고, 1초 쉬고.
    private CountDownTimer mAlarmTimer = new CountDownTimer(1000 * 10, 2000) {
        @Override
        public void onTick(long millisUntilFinished) {
            if(mAlarmVibrate) {
                mVibe.vibrate(1000);
            }
        }

        // 30초면 한번에 끝나게 되고, 1분이면 2번, 3분이면 6번 30초 카운터를 반복한다.
        @Override
        public void onFinish() {
            if(++mAlarmPlayCount >= mAlarmLoopCount) {
                // 종료
                ULog.i(TAG,"Alarm Timer Finished...." + mAlarmPlayCount + " of " + mAlarmLoopCount);

                mApp.setAlarmState(false);

                if(mAlarmSound) {
                    mediaPlayer.stop();
                    mediaPlayer.release();
                    mediaPlayer = null;
                }

                if(mAlarmVibrate) {
                    mVibe.cancel();
                }
            }
            else {
                ULog.i(TAG,"Restart Alarm Timer...." + mAlarmPlayCount + " of " + mAlarmLoopCount);
                this.start();
            }
        }
    };

    private CountDownTimer mVibeTimer = new CountDownTimer(1000*30, 3000) {
        public void onTick(long millisUntilFinished) {
            mVibe.vibrate(1000);
        }
        public void onFinish() {
            mVibeFinishCount++;
            if(mVibeLoopCount<= mVibeFinishCount) {
                mVibeFinishCount=0;
                mVibe.cancel();
                switch(mAlarmHowOften) {
                    case 0: /*one time*/
                        break;
                    case 1: /*5 minute*/
                        hVibe.postDelayed(iVibe, 5*1000*60);
                        break;
                    case 2: /*10 minutes*/
                        hVibe.postDelayed(iVibe, 10*1000*60);
                        break;
                    case 3: /*20 minutes*/
                        hVibe.postDelayed(iVibe, 20*1000*60);
                        break;
                    case 4: /*30 minutes*/
                        hVibe.postDelayed(iVibe, 30*1000*60);
                        break;
                    default:
                        break;
                }
            }else {
                this.start();
            }
        }
    };

    private CountDownTimer mRingtoneTimer = new CountDownTimer(1000*30, 2000) {
        public void onTick(long millisUntilFinished) {
            if(mediaPlayer != null && !mediaPlayer.isPlaying()) {
                Log.w(TAG, "Alarm Sound play...");
                mediaPlayer.start();
            }
        }
        public void onFinish() {
            mSoundFinishCount++;
            if(mSoundLoopCount<= mSoundFinishCount) {
                mSoundFinishCount= 0;

                if (mediaPlayer != null) {
                    Log.w(TAG, "Alarm Sound stop...");
                    mediaPlayer.pause();
                    //mediaPlayer.stop();
                    //mediaPlayer.release();
                }
                switch(mAlarmHowOften) {
                    case 0: /*one time*/
                        break;
                    case 1: /*5 minute*/
                        hRingTone.postDelayed(iRingTone, 5*1000*60);
                        break;
                    case 2: /*10 minutes*/
                        hRingTone.postDelayed(iRingTone, 10*1000*60);
                        break;
                    case 3: /*20 minutes*/
                        hRingTone.postDelayed(iRingTone, 20*1000*60);
                        break;
                    case 4: /*30 minutes*/
                        hRingTone.postDelayed(iRingTone, 30*1000*60);
                        break;
                    default:
                        break;
                }
            }else{
                this.start();
            }
        }
    };

    Runnable iAlarm = new Runnable() {
        @Override
        public void run() {

        }
    };

    Runnable iVibe = new Runnable() {
        public void run() {
            //hVibe.postDelayed(iVibe, TIMER_PERIOD);
            mVibeFinishCount=0;
            switch(mAlarmHowLong){
                case 0:
                    mVibeLoopCount=0;
                    break;
                case 1:
                    mVibeLoopCount=2;
                    break;
                case 2:
                    mVibeLoopCount=6;
                    break;
                default:
                    mVibeLoopCount=0;
                    break;
            }
            if(mVibe != null) {
                mVibeTimer.start();
            }
        }
    };

    Runnable iRingTone = new Runnable() {
        public void run() {
            //hRingTone.postDelayed(iRingTone, TIMER_PERIOD);
            //mRingtoneTimer.
            mSoundFinishCount=0;
            switch(mAlarmHowLong){
                case 0:
                    mSoundLoopCount=0;
                    break;
                case 1:
                    mSoundLoopCount=2;
                    break;
                case 2:
                    mSoundLoopCount=6;
                    break;
                default:
                    mSoundLoopCount=0;
                    break;
            }
            mRingtoneTimer.start();
        }
    };

    ////////////////////////////////////////////////////////////////////
    // 서비스 시작되면서 읽기 시작해서 1초 간격으로 읽는다.
	// To Do: Android Studio에서 static으로 작성하라는 경고가 뜬다.... 나중에라도 확인해 보자....!!!
    private Handler mReadRssiHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            for(Sensor sensor : mApp.getAllSensors()) {
                if(sensor.isConnected() && sensor.getActionMode() == Const.ACTION_MODE_THEFT) {
                    ULog.i(TAG, "ReadRssiHandler: Sensor=" + sensor.getSensorName());
                    sensor.readRemoteRSSI();
                }
            }

            // 메세지를 처리하고 또다시 핸들러에 메세지 전달 (1000ms 지연)
            mReadRssiHandler.sendEmptyMessageDelayed(0,1000);
        }
    };
    ///////////////////////////////////////////////////////////////////////////////////
}
