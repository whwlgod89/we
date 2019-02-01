package kr.co.theunify.wear;

import android.app.Application;

import java.util.ArrayList;

import kr.co.theunify.wear.data.SensorInfo;
import kr.co.theunify.wear.sensor.Sensor;
import kr.co.theunify.wear.service.SensorService;
import kr.co.theunify.wear.utils.ULog;

public class WearApp extends Application {
    private static final String TAG = "[" + WearApp.class.getSimpleName() + "]";

    private SensorService mService = null;          // 서비스
    private SensorDatabase mDB = null;              // 데이터베이스
    private ArrayList<Sensor> mAllSensors = null;   // 센서 리스트
    private int mCurPosition = -1;
    private Sensor mCurSensor = null;               // 현재 센서

    private boolean mAlarmState = false;            // 알람 상태

    // 부팅하면 App은 실행되지만, 부팅시에 Service만 실행되고, User 단의 Activity 실행은 없다.
    // MainActivity가 실행되었을 때, User App이 실행된 것으로 판단하고, mAppLaunched를 TRUE로 설정.
    // 알람 이벤트 발생하면 mAppLaunched를 확인하여 App을 실행할 것인지, Intent만 전달할 것인지
    // 판단해서 테스트 해보자.
    public boolean mAppLaunched = false;            // 앱 실행 여부

    private boolean mActivityVisible = false;       // 화면 보이는지?
    private int mResumed = 0;
    private int mPaused = 0;

    @Override
    public void onCreate() {
        super.onCreate();

        ULog.i(TAG, "onCreateApp. Application Started. App=" + this  + " ...............");

        // DB 생성
        mDB = new SensorDatabase(getApplicationContext(), "HeyTong.db", null, 6);
        // 모든 센서 정보 가져오기
        loadSensors();
        if (mAllSensors.size()>0){
            setCurSensor(0);
        }

        // 서비스 가져오기
        mService = getService();
        ULog.i(TAG, "onCreateApp SensorService=" + ((mService == null) ? "NULL" : mService));

        // 테스트 센서 추가
//        addSensor("HT-AAAA_01", "Mama's Wallet", "010-1111-1111", 0, -100);
//        addSensor("HT-AAAA_02", "Papa's Wallet", "010-1111-1111", 0, -100);
//        addSensor("HT-AAAA_03", "Tomi's Wallet", "010-1111-1111", 0, -100);
//        addSensor("HT-AAAA_04", "Jack's Wallet", "010-1111-1111", 0, -100);
    }

    /**
     * 센서 추가하기
     * @param sensorId      address
     * @param sensorName    지정한 이름
     * @param wearName      지갑이름
     * @param phoneNumber   전화번호
     * @param actionMode    설정 모드
     * @param rssi          rssi 값 - 도난 모드에서
     */
    public void addSensor(String sensorId, String sensorName, String wearName, int cover, String phoneNumber, int actionMode, int rssi) {
        SensorInfo info = new SensorInfo(sensorId, sensorName, wearName, cover, phoneNumber, actionMode, rssi);
        Sensor sensor = new Sensor(info);
        insertSensor(sensor);           // 등록된 센서 목록 관리 DB에 추가
        mAllSensors.add(sensor);        // 센서 목록 관리 메모리에 추가 (처음 실행시에 DB 에서 로드)
        if (mService != null) {
            mService.addSensor(sensor);     //
        }
    }

    /**
     * 현재 센서 삭제하기
     */
    public void removeSensor() {
        mService.removeSensor(mCurSensor);
        mAllSensors.remove(mCurSensor);
        deleteSensor();

        // 현재 센서 위치 지정
        setCurSensor(0);
    }

    /**
     * 현재 선택된 인덱스 - 가져오기
     * @return
     */
    public int getCurPosition() {
        ULog.i(TAG, "getCurPosition=" + mCurPosition);
        return mCurPosition;
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

    /**
     * 센서 저장 숫자 가져오기
     * @return
     */
    public int getSensorCount() {
        return mAllSensors.size();
    }

    /**
     * 센서 목록 확인하기
     * @return
     */
    public ArrayList<Sensor> getAllSensors() {
        // ULog.i(TAG, "Get All Wallets in Memory = " + mAllSensors.size());
        return mAllSensors;
    }

    /**
     * 순번의 센서 가져오기
     * @param index
     * @return
     */
    public Sensor getSensor(int index) {
        ULog.i(TAG, "Get Sensor with List-Index = " + index);
        if(mAllSensors == null || index >= mAllSensors.size()) {
            return null;
        }
        return mAllSensors.get(index);
    }

    /**
     * 센서 이름으로 센서 가져오기
     * @param sensorName
     * @return
     */
    public Sensor getSensor(String sensorName) {
        ULog.i(TAG, "Get Sensor with Sensor-Name = " + sensorName);
        for(Sensor sensor : mAllSensors) {
            if(sensor.getSensorName().equals(sensorName)) {
                return sensor;
            }
        }
        return null;
    }


    public Sensor getSensorFromAddr(String addr) {
        ULog.i(TAG, "Get Sensor with Sensor-Id = " + addr);
        for(Sensor sensor : mAllSensors) {
            if(sensor.getSensorId().equals(addr)) {
                return sensor;
            }
        }
        return null;
    }

    /**
     * 센서 목록 가져오기
     * @return
     */
    public ArrayList<Sensor> loadSensors() {
        mAllSensors = mDB.selectAllSensors();
        ULog.i(TAG, "Get All Sensor in Database = " + mAllSensors.size());
        return mAllSensors;
    }

    /**
     * 센서 기본정보 업데이트 하기
     * @param sensor
     */
    public void updateSensor(Sensor sensor) {
        ULog.i(TAG, "Update Sensor in Database = " + sensor.toShortString());
        mDB.updateSensor(sensor.getInfo());
    }

    /**
     * 센서 위치정보 업데이트 하기
     * @param sensor
     */
    public void updateSensorLoc(Sensor sensor) {
        ULog.i(TAG, "Update Sensor in Database = " + sensor.toShortString());
        mDB.updateSensorLoc(sensor.getInfo());
    }

    /**
     * 특정 센서 삭제하기
     * @param sensorId
     */
    public void deleteSensor(String sensorId) {
        ULog.i(TAG, "Remove Sensor in Database=" + sensorId);
        mAllSensors.remove(mCurSensor);
        mDB.deleteSensor(sensorId);
        mCurSensor = null;
    }

    /**
     * 센서 추가하기
     * @param sensor
     */
    private void insertSensor(Sensor sensor) {
        ULog.i(TAG, "Insert Sensor into Database = " + sensor.getInfo().getName());
        mCurSensor = sensor;
        mDB.insertSensor(sensor.getInfo());
    }

    /**
     * 센서 삭제하기 - 현재 센서 삭제
     */
    private void deleteSensor() {
        assert(mCurSensor != null);

        ULog.i(TAG, "Remove Sensor in Database=" + mCurSensor.getSensorName());
        mDB.deleteSensor(mCurSensor.getSensorId());
        mCurSensor = null;
    }

    /**
     * 현재 센서 정보 가져오기
     * @return
     */
    public Sensor getCurSensor() {
        if (mCurSensor != null) {
            ULog.i(TAG, "Get Current Wallet in Memory = " + mCurSensor.toLongString());
        }
        return mCurSensor;
    }

    /**
     * 위치의 센서를 현재 센서로 지정하기
     * @param position
     */
    public void setCurSensor(int position) {
        if(0 <= position && position < mAllSensors.size()) {
            mCurPosition = position;
            mCurSensor = mAllSensors.get(position);
            ULog.i(TAG, "Set Current Wallet in Memory = " + mCurSensor.toLongString());
        }
        else {
            mCurPosition = -1;
            mCurSensor = null;
            ULog.i(TAG, "Clear Current Wallet in Memory. Position=" + position);
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
    public void stopAlarm2() { mService.stopAlarm2();
    }
}
