package kr.co.theunify.wear;

import android.app.Application;
import android.util.Log;

import java.util.ArrayList;

import kr.co.theunify.wear.data.SensorInfo;
import kr.co.theunify.wear.sensor.Sensor;
import kr.co.theunify.wear.service.SensorService;

public class WearApp extends Application {
    private static final String TAG = "[" + WearApp.class.getSimpleName() + "]";

    private SensorService mService = null;
    private SensorDatabase mDB = null;
    private ArrayList<Sensor> mAllSensors = null;
    private Sensor mCurSensor = null;

    private boolean mAlarmState = false;

    // 부팅하면 App은 실행되지만, 부팅시에 Service만 실행되고, User 단의 Activity 실행은 없다.
    // MainActivity가 실행되었을 때, User App이 실행된 것으로 판단하고, mAppLaunched를 TRUE로 설정.
    // 알람 이벤트 발생하면 mAppLaunched를 확인하여 App을 실행할 것인지, Intent만 전달할 것인지
    // 판단해서 테스트 해보자.
    public boolean mAppLaunched = false;

    private boolean mActivityVisible = false;
    private int mResumed = 0;
    private int mPaused = 0;

    @Override
    public void onCreate() {
        super.onCreate();

        Log.d(TAG, "onCreateApp. Application Started. App=" + this  + " ...............");
        mDB = new SensorDatabase(getApplicationContext(), "HeyTong.db", null, 5);
        mAllSensors = mDB.selectAllSensors();

        mService = getService();
        Log.d(TAG, "onCreateApp SensorService=" + ((mService == null) ? "NULL" : mService));

//        // 테스트 센서 추가
//        Sensor sensor;
//        sensor = new Sensor("HT-AAAA_01", "Mama's Wallet", "010-1111-1111", 0);
//        mDB.insertSensor(sensor);
//        sensor = new Sensor("HT-BBBB_02", "Papa's Wallet", "010-2222-2222", 0);
//        mDB.insertSensor(sensor);
//        sensor = new Sensor("HT-CCCC_03", "Tomi's Wallet", "010-3333-3333", 0);
//        mDB.insertSensor(sensor);
    }

    public void addSensor(String sensorId, String sensorName, String phoneNumber, int actionMode, int rssi) {
        SensorInfo info = new SensorInfo(sensorId, sensorName, phoneNumber, actionMode, rssi);
        Sensor sensor = new Sensor(info);
        insertSensor(sensor);           // 등록된 센서 목록 관리 DB에 추가
        mAllSensors.add(sensor);        // 센서 목록 관리 메모리에 추가 (처음 실행시에 DB 에서 로드)
        mService.addSensor(sensor);     //
    }

    public void removeSensor() {
        mService.removeSensor(mCurSensor);
        mAllSensors.remove(mCurSensor);
        deleteSensor();
    }

    public void removeSensor(int index) {
        mCurSensor = mAllSensors.get(index);
        removeSensor();
    }

    // App에 서비스 설정
    public void setService(SensorService service) {
        mService = service;
    }
    public SensorService getService() {
        return mService;
    }

    // 센서 데이터베이스 관리
    public SensorDatabase getSensorDB() { return mDB; }

    public ArrayList<Sensor> loadSensors() {
        mAllSensors = mDB.selectAllSensors();
        Log.d(TAG, "Get All Sensor in Database = " + mAllSensors.size());
        return mAllSensors;
    }

    private void insertSensor(Sensor sensor) {
        Log.d(TAG, "Insert Sensor into Database = " + sensor.getInfo().getName());
        mCurSensor = sensor;
        mDB.insertSensor(sensor.getInfo());
    }

    public void updateSensor(Sensor sensor) {
        Log.d(TAG, "Update Sensor in Database = " + sensor.toShortString());
        //mCurSensor = sensor;
        mDB.updateSensor(sensor.getInfo());
    }

//    public void deleteSensor(String sensorId) {
//        Log.d(TAG, "Remove Sensor in Database=" + sensorId);
//        mAllSensors.remove(mCurSensor);
//        mDB.deleteSensor(sensorId);
//        mCurSensor = null;
//    }

    private void deleteSensor() {
        assert(mCurSensor != null);

        Log.d(TAG, "Remove Sensor in Database=" + mCurSensor.getSensorName());
        mDB.deleteSensor(mCurSensor.getSensorId());
        mCurSensor = null;
    }

    //
    public int getSensorCount() {
        return mAllSensors.size();
    }

    public ArrayList<Sensor> getAllSensors() {
        Log.d(TAG, "Get All Wallets in Memory = " + mAllSensors.size());
        return mAllSensors;
    }

    public Sensor getSensor(int index) {
        Log.d(TAG, "Get Sensor with List-Index = " + index);
        if(mAllSensors == null || index >= mAllSensors.size()) {
            return null;
        }

        return mAllSensors.get(index);
    }

    public Sensor getSensor(String sensorName) {
        Log.d(TAG, "Get Sensor with Sensor-Name = " + sensorName);
        for(Sensor sensor : mAllSensors) {
            if(sensor.getSensorName().equals(sensorName)) {
                return sensor;
            }
        }

        return null;
    }

    public Sensor getCurSensor() {
        Log.d(TAG, "Get Current Wallet in Memory = " + mCurSensor.toLongString());
        return mCurSensor;
    }

    public void setCurSensor(int position) {
        if(0 <= position && position < mAllSensors.size()) {
            mCurSensor = mAllSensors.get(position);
            Log.d(TAG, "Set Current Wallet in Memory = " + mCurSensor.toLongString());
        }
        else {
            mCurSensor = null;
            Log.d(TAG, "Clear Current Wallet in Memory. Position=" + position);
        }
    }

    public boolean isActivityVisible() {
        //return activityVisible;
        if( mPaused >= mResumed && mActivityVisible ) {
            mActivityVisible = false;
        }
        else if( mResumed > mPaused && !mActivityVisible ) {
            mActivityVisible = true;
        }
        return mActivityVisible;
    }

    public void activityResumed() {
        //activityVisible = true;
        ++mResumed;
        if( !mActivityVisible )
        {
            // Don't check for foreground or background right away
            // finishing an activity and starting a new one will trigger to many
            // foreground <---> background switches
            //
            // In half a second call foregroundOrBackground
        }

    }
    public void activityPaused() {
        //activityVisible = false;
        ++mPaused;
        if( mActivityVisible )
        {
            // Don't check for foreground or background right away
            // finishing an activity and starting a new one will trigger to many
            // foreground <---> background switches
            //
            // In half a second call foregroundOrBackground
        }
    }

    public void setAlarmState(boolean state) {
        mAlarmState = state;
    }
    public boolean isAlarmEnabled() {
        return mAlarmState;
    }
    public void startAlarm() {
        mService.startAlarm();
    }
    public void stopAlarm() {
        mService.stopAlarm();
    }
    public void startAlarm2(boolean detected) {
        mService.startAlarm2(detected);
    }
    public void stopAlarm2() {
        mService.stopAlarm2();
    }
}
