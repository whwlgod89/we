package kr.co.theunify.wear.activity;

import android.Manifest;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Calendar;
import java.util.Date;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import kr.co.theunify.wear.Const;
import kr.co.theunify.wear.R;
import kr.co.theunify.wear.WearApp;
import kr.co.theunify.wear.adapter.MainPagerAdapter;
import kr.co.theunify.wear.dialog.CommonDialog;
import kr.co.theunify.wear.sensor.Sensor;
import kr.co.theunify.wear.service.SensorService;
import kr.co.theunify.wear.utils.ULog;
import kr.co.theunify.wear.utils.Utils;
import kr.co.theunify.wear.view.TitlebarView;

public class MainActivity extends BaseActivity {

    private String TAG = MainActivity.class.getSimpleName();


    //********************************************************************************
    //  Layout Member Variable
    //********************************************************************************

    @BindView(R.id.v_titlebar)      TitlebarView v_titlebar;        // 타이틀 바

    @BindView(R.id.layout_wallet)   LinearLayout layout_wallet;     // 지갑 레이아웃
    @BindView(R.id.txt_name)        TextView txt_name;              // 지갑 이름

    @BindView(R.id.pager_main)      ViewPager pager_main;           // 지갑 View pager
    @BindView(R.id.img_battery)     ImageView img_battery;          // 배터리
    @BindView(R.id.txt_page)        TextView txt_page;              // 페이지 번호
    @BindView(R.id.layout_page)     LinearLayout layout_page;       // 페이지 Dot

    @BindView(R.id.move_left)     ImageView move_left;              // 왼쪽으로 이동
    @BindView(R.id.move_right)     ImageView move_right;            // 오른쪽으로 이동

    @BindView(R.id.layout_empty)   LinearLayout layout_empty;       // 하나도 없을 시 레이아웃
    @BindView(R.id.btn_add)         TextView btn_add;               // 추가 버튼

    //********************************************************************************
    //  Member Variable
    //********************************************************************************

    private Context mContext;

    private SensorService mService = null;
    private WearApp mApp = null;

    private String mActionType = null;
    private boolean mServiceShutdown = false;    // 강제 종료 여부 (App 재시작, App 완전 종료)

    private String mSenserNameForPermission = "";

    private MainPagerAdapter mainPagerAdapter;
    private Sensor mSensor;
    private CommonDialog mDlgFindPhone;           // 센서에서 폰 찾기 다이얼로그
    private boolean mDlgFindPhoneOn = false;     // 센서에서 폰 찾기 다이얼로그 표시 여부 (알람 종료 후 종료?)
    private boolean mDlgConnected;
    private CommonDialog mDlgDisconnected;        // 센서 연결 끊김 다이얼로그
    private boolean mDlgDisconnectedOn = false;  // 센서 연결 끊김 다이얼로그 표시 여부 (알람 종료 후 유지, 사용자 확인 종료)

    private LocationManager mLocManager = null;     // 연결이 끊겼을 때 위치 확보.
    private LocationListener mLocListener = null;

    //********************************************************************************
    //  LifeCycle Functions
    //********************************************************************************

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.a_main);
        mContext = this;
        ButterKnife.bind(this);

        mApp = (WearApp) getApplication();
        mService = mApp.getService();

        initView();

        ULog.i(TAG, "onCreate() - mAppLaunched=" + mApp.mAppLaunched);
        mApp.mAppLaunched = true;
        ULog.i(TAG, "onCreate() - mAppLaunched=" + mApp.mAppLaunched);


        service_init();
        Intent intent = getIntent();
        CheckIntent(intent);
        if(mActionType != null) {
            Bundle extras = intent.getExtras();
            String sensorName = extras.getString(Const.EXTRA_ACTION_SENSOR_ID);
            onCreateByIntent(sensorName);
        }
    }


    @Override
    public void onResume(){
        super.onResume();
    }


    @Override
    public void onPause(){
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        unbindService(mServiceConnection);

        //BO: 아래 코드는 Setting 설정을 고려하여 서비스를 함께 종료할지를 판단하고 있지만,
        //    App 완전 종료 or App 다시 시작 자체가 서비스까지 종료하는 것을 전제로 하니까 필요 없다.
        //    세팅에서 App 완전 종료 or App 다시 시작을 선택하면 mServiceShutdown Flag를 이용한다.
//        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(MainActivity.this);
//        boolean enable = settings.getBoolean("KEY_SERVICE_ON", false);
//        if(/*enable == false || */mServiceShutdown) {

        // 위치 서비스도 해제 해줘야 할까?

//        if(mServiceShutdown) {
//            Intent bindIntent = new Intent(this, SensorService.class);
//            unbindService(this);
//            stopService(bindIntent);
//        }
        LocalBroadcastManager.getInstance(this).unregisterReceiver(SensorStatusChangeReceiver);
        ULog.i(TAG, "Main Activity Stopped, with service=" + mServiceShutdown);
    }


    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    //********************************************************************************
    //  Override Functions
    //********************************************************************************

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        CheckIntent(intent);
        if(mActionType != null)  {
            Bundle extras = intent.getExtras();
            String sensorName = extras.getString(Const.EXTRA_ACTION_SENSOR_ID);
            onCreateByIntent(sensorName);
        }
    }

    private void CheckIntent(Intent intent) {
        if (null != intent) {
            Bundle extras = intent.getExtras();
            if(extras != null) {
                mActionType = extras.getString(Const.EXTRA_ACTION_TYPE);
                Log.e(TAG, "Created by intent() .........." +  mActionType);
            }else{
                mActionType = null;
                Log.e(TAG, "Created by user..........");
            }
        }
    }

    private void onCreateByIntent(final String sensorName)  {

        ULog.i(TAG, "onCreateByIntent(): Sensor=" + sensorName + ", ActionType=" + mActionType);


        if(mActionType.equals(Const.ACTION_GATT_CONNECTED)) {
            ULog.i(TAG, "onCreateByIntent() - ACTION_GATT_CONNECTED");
        }
        else if(mActionType.equals(Const.ACTION_GATT_DISCONNECTED)) {
            ULog.i(TAG, "onCreateByIntent() - ACTION_GATT_DISCONNECTED");

            getWindow().addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED
                    | WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD
                    | WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
                    | WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON);

            mDlgDisconnected = Utils.showPopupDlg(this, "", "[" + sensorName + "] " + getString(R.string.alert_msg_lost_sensor),
                    getString(R.string.ok), new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {

                        }
                    },
                    "", null, null);
            mDlgDisconnected.setOnDismissListener(new DialogInterface.OnDismissListener() {
                @Override
                public void onDismiss(DialogInterface dialog) {
                    mDlgDisconnected.dismiss();
                    mDlgDisconnectedOn = false;
                    mApp.stopAlarm2();
                }
            });
            mDlgDisconnectedOn = true;
            mApp.startAlarm2(true);
            mainPagerAdapter.notifyDataSetChanged();

            //============================================================
            // 연결이 끊어졌을 때의 위치를 확인한다.
            if (ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED ||
                    ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                mLocManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
                boolean isGpsEnabled = mLocManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
                boolean isNetEnabled = mLocManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
                ULog.i(TAG, "onCreateByIntent(): LocationManager=" + mLocManager + ", GPS=" + isGpsEnabled + ", NET=" + isNetEnabled);

                Date locTime = Calendar.getInstance().getTime();
                Date curTime = Calendar.getInstance().getTime();

                if(isGpsEnabled || isNetEnabled) {
//                    if (isGpsEnabled) {
//                        Location lastLoc = mLocManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
//                        if(lastLoc != null) {
//                            locTime.setTime(lastLoc.getTime());
//                            ULog.i(TAG, "onCreateByIntent() - LAST Location Location from GPS = "
//                                    + lastLoc.getLatitude() + ", " + lastLoc.getLongitude() + ", L:" + locTime.toString() + ", C:" + curTime.toString());
//                            mApp.getSensor(sensorName).setLocation(lastLoc.getLatitude(), lastLoc.getLongitude());
//                            return;
//                        }
//                        else
//                            ULog.i(TAG, "onCreateByIntent() - LAST Location Location from GPS = NULL");
//                    }
//
//                    if (isNetEnabled) {
//                        Location lastLoc = mLocManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
//                        if(lastLoc != null) {
//                            locTime.setTime(lastLoc.getTime());
//                            ULog.i(TAG, "onCreateByIntent() - LAST Location Location from NET = "
//                                    + lastLoc.getLatitude() + ", " + lastLoc.getLongitude() + ", L:" + locTime.toString() + ", C:" + curTime.toString());
//                            mApp.getSensor(sensorName).setLocation(lastLoc.getLatitude(), lastLoc.getLongitude());
//                            return;
//                        }
//                        else
//                            ULog.i(TAG, "onCreateByIntent() - LAST Location Location from NET = NULL");
//                    }
                }
                else {
                    ULog.i(TAG, "onCreateByIntent() - Location Providers are NOT Enabled");
                    showAlertPopup("", "위치 제공자 비활성화", getResources().getString(R.string.ok), null, "");
                    return;
                }

                // 위치 정보 제공은 활성화되어 있지만 Last Location 정보를 얻지 못해서 실시간 정보 읽기로 취득
                mLocListener = new LocationListener() {
                    @Override
                    public void onLocationChanged(Location location) {
                        ULog.i(TAG, "onLocationChanged(): Lat=" + location.getLatitude() + ", Lon=" + location.getLongitude() + ", Provider=" + location.getProvider());
                        Sensor sensor = mApp.getSensor(sensorName);
                        if (sensor.getLatitude() == 0 && sensor.getLongitude() == 0) {
                            mApp.getSensor(sensorName).getInfo().setLocation(location.getLatitude(), location.getLongitude());
                            mApp.updateSensorLoc(mApp.getSensor(sensorName));
                        }
                        mLocManager.removeUpdates(mLocListener);
                    }

                    @Override
                    public void onStatusChanged(String provider, int status, Bundle extras) {
                        ULog.i(TAG, "Status of Provider [" + provider + "] changed to " + status);
                    }

                    @Override
                    public void onProviderEnabled(String provider) {
                        ULog.i(TAG, "Provider [" + provider + "] enabled");
                    }

                    @Override
                    public void onProviderDisabled(String provider) {
                        ULog.i(TAG, "Provider [" + provider + "] disabled");
                    }
                };

                if (isGpsEnabled) {
                    mLocManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 5000, 1, mLocListener);
                }
                if (isNetEnabled) {
                    mLocManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 5000, 1, mLocListener);
                }
            }
            else {
                // 위치 정보에 대한 권한이 없으므로, 위치를 초기화하고 그냥 나간다.
                Sensor sensor = mApp.getSensor(sensorName);
                sensor.getInfo().setLocation(0, 0);

                ULog.i(TAG, "onCreateByIntent() - Location Permission is not Granted");
                showAlertPopup("", "위치 정보 권한 없음", getResources().getString(R.string.ok), null, "");
            }
            // 연결이 끊어졌을 때의 위치를 확인한다.
            //============================================================
        }
        else if(mActionType.equals(Const.ACTION_DATA_AVAILABLE)) {
            ULog.i(TAG, "onCreateByIntent() - ACTION_DATA_AVAILABLE");
        }
        else if(mActionType.equals(Const.ACTION_SENSOR_FIND_PHONE_START)) {
            ULog.i(TAG, "onCreateByIntent() - ACTION_SENSOR_FIND_PHONE_START");

            getWindow().addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED
                    | WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD
                    | WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
                    | WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON);

            mDlgFindPhone = Utils.showPopupDlg(this, "", "[" + sensorName + "] " + getString(R.string.alert_msg_find_sensor),
                    getString(R.string.ok), null, "", null, null);
            mDlgFindPhone.setOnDismissListener(new DialogInterface.OnDismissListener() {
                @Override
                public void onDismiss(DialogInterface dialog) {
                    mDlgFindPhoneOn = false;
                    mApp.stopAlarm2();
                }
            });
            mDlgFindPhoneOn = true;
            mApp.startAlarm2(false);
        }
        else if(mActionType.equals(Const.ACTION_SENSOR_FIND_PHONE_STOP)) {
            ULog.i(TAG, "onCreateByIntent() - ACTION_SENSOR_FIND_PHONE_STOP");
            if(mDlgFindPhoneOn) {
                mDlgFindPhone.dismiss();
                mDlgFindPhoneOn = false;
                mApp.stopAlarm2();
            }
        }
        else if(mActionType.equals(Const.ACTION_SENSOR_WARN_THEFT)) {
            ULog.i(TAG, "onCreateByIntent() - ACTION_SENSOR_WARN_THEFT");

            if (mDlgDisconnected != null && mDlgDisconnected.isShowing()) {
                return;
            }

            getWindow().addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED
                    | WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD
                    | WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
                    | WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON);

            mDlgDisconnected = Utils.showPopupDlg(this, "도난 경보", "[" + sensorName + "] 웨어와\n연결이 약해졌습니다.",
                    getString(R.string.ok), null, "", null, null);
            mDlgDisconnected.setOnDismissListener(new DialogInterface.OnDismissListener() {
                @Override
                public void onDismiss(DialogInterface dialog) {
                    mDlgDisconnected.dismiss();
                    mDlgDisconnectedOn = false;
                    mApp.stopAlarm2();
                }
            });
            mDlgDisconnectedOn = true;
            mApp.startAlarm2(true);
            mainPagerAdapter.notifyDataSetChanged();
        }
        else if(mActionType.equals(Const.ACTION_GATT_STATUS)) {
            Handler h;//핸들러 선언
            h= new Handler(); //딜래이를 주기 위해 핸들러 생성
            h.postDelayed(needRestart, 100); // 딜레이 ( 런어블 객체는 mrun, 시간 2초)
            return;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        ULog.i(TAG, "onRequestPermissionsResult(): requestCode=" + requestCode + ", grantResult[0]=" + grantResults[0]);
        if(requestCode == Const.REQUEST_CODE_OF_READ_EXTERNAL_STORAGE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                ULog.i(TAG, "onRequestPermissionsResult(): GRANTED");
                onCreateByIntent(mSenserNameForPermission);
            }
            else {
                ULog.i(TAG, "onRequestPermissionsResult(): DENIED");
                return;
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        ULog.i(TAG, "onActivityResult - Request=" + requestCode + ", Result=" + resultCode + ".............");

        if(requestCode == Const.REQUEST_CODE_OF_ADD_SENSOR) {
            if (resultCode == Const.RESULT_CODE_OF_SENSOR_ADDED) {
                //WearApp mApp

                String sensorId = data.getStringExtra(Const.SENSOR_ID);
                String sensorName = data.getStringExtra(Const.SENSOR_NAME);
                String walletName = data.getStringExtra(Const.WEAR_NAME);
                int cover = data.getIntExtra(Const.WEAR_COVER, R.drawable.purse_01);
                String phoneNumber = data.getStringExtra(Const.PHONE_NUMBER);
                int actionMode = data.getIntExtra(Const.ACTION_MODE, Const.ACTION_MODE_LOSS);
                int rssi = data.getIntExtra(Const.RSSI, 100);

                mApp.addSensor(sensorId, sensorName, walletName, cover, phoneNumber,actionMode, rssi);  // 센서 추가 (App에서 DB & 목록에 추가, 서비스 연결)

                // 센서의 위치를 추가한 것으확인한다.
                mApp.setCurSensor(mApp.getSensorCount()-1);

                initView();

                if(Const.DEBUG) {
                    int index = mApp.getAllSensors().size()-1;
                    //Sensor sensor = mApp.getSensorItem(index);
                    Sensor sensor = mApp.getSensor(index);
                    String strRegister = "ID=[" + sensor.getSensorId() + "], Name=[" + sensor.getSensorName() + "], Phone=[" + sensor.getPhoneNumber() + "], Mode=[" + sensor.getActionMode() + "]";
                    Toast.makeText(MainActivity.this, strRegister, Toast.LENGTH_SHORT).show();
                    mApp.updateSensor(mSensor);
                }
            }
        }
        else if(requestCode == Const.REQUEST_CODE_OF_MODIFY_SENSOR) {
            // 센서 설정 변경 (이름, 폰번, 모드) - ModifySensor 모듈로부터 전달
            if(resultCode == RESULT_OK) {
                Toast.makeText(MainActivity.this, getString(R.string.msg_sensor_modified), Toast.LENGTH_SHORT).show();
                initView();
            }
        }
         if(requestCode == Const.REQUEST_CODE_OF_APP_SETTINGS) {
            // 재시작
            if(resultCode == Const.RESULT_CODE_OF_RESTART_APP) {
                Toast.makeText(MainActivity.this, getString(R.string.pref_app_control_restart), Toast.LENGTH_SHORT).show();
                restartApp(2000);
            } else if(resultCode == Const.RESULT_CODE_OF_FINISH_APP) {
                // 종료
                Toast.makeText(MainActivity.this, getString(R.string.pref_app_control_finish), Toast.LENGTH_SHORT).show();
                finishApp();
            } else {
                SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(this);
                String strAlarm1 = settings.getString("pref_key_alarm_disconnected", "FAIL");
                String strAlarm2 = settings.getString("pref_key_alarm_find_phone", "FAIL");
                ULog.i(TAG, "Conn=" + strAlarm1 + ", Find=" + strAlarm2);
            }
        }
    }

    //********************************************************************************
    //  Override Event Functions
    //********************************************************************************


    @OnClick(R.id.btn_add)
    public void onClickBtnAdd() {
        if(mApp.getSensorCount() >= Const.MAX_SENSORS) {
//            showAddSensor(false);       // Show/Hide Widgets for Add Sensor
        } else {
            Intent i = new Intent();
            i.setClass(this, WearListActivity.class);
            startActivityForResult(i, Const.REQUEST_CODE_OF_ADD_SENSOR);
        }
    }

    @OnClick(R.id.img_setting)
    public void onClickImgSetting() {
        Intent intent = new Intent();
        intent.setClass(mContext, SettingActivity.class);
        ((MainActivity) mContext).startActivityForResult(intent, Const.REQUEST_CODE_OF_APP_SETTINGS);

    }

    @OnClick({R.id.btn_find, R.id.btn_location, R.id.btn_setting,R.id.btn_instruction} )
    public void onClickOption(View v) {

        switch (v.getId())
        {
            case R.id.btn_find: {
                mApp.getCurSensor().findSensor();
                break;
            }
            case R.id.btn_location: {
                // 지도 화면으로 이동한다. - 구글 지도
                Intent i = new Intent();
                i.setClass(this, MapActivity.class);
                startActivity(i);
                break;
            }
            case  R.id.btn_setting: {
                Intent i = new Intent();
                i.setClass(this, ModifyActivity.class);
                startActivityForResult(i, Const.REQUEST_CODE_OF_MODIFY_SENSOR);
                break;
            }
            case R.id.btn_instruction: {
                Intent i = new Intent();
                i.setClass(this, InstructionActivity.class);
                startActivity(i);
                break;
            }
                // remove 를 지우고 사용방법을 추가
        }


    }

    @OnClick({R.id.move_left, R.id.move_right} )
    public void onClickMove(View v) {
        if (v.getId() == R.id.move_left) {
            if (mApp.getCurPosition() != 0) {
                pager_main.setCurrentItem(mApp.getCurPosition()-1);
//                mApp.setCurSensor(mApp.getCurPosition()-1);
//                updatePage(mApp.getCurPosition());
            }
        } else if (v.getId() == R.id.move_right) {
            if (mApp.getCurPosition() < mApp.getSensorCount()-1) {
                pager_main.setCurrentItem(mApp.getCurPosition()+1);
//                mApp.setCurSensor(mApp.getCurPosition()+1);
//                updatePage(mApp.getCurPosition());
            }
        }
    }

    //********************************************************************************
    //  User Define Functions
    //********************************************************************************

    /**
     * 사용자 뷰 초기화
     */
    private void initView() {
        initTitle();

        initViewPager();

        if (mApp.getSensorCount()>0) {
            layout_empty.setVisibility(View.GONE);
            layout_wallet.setVisibility(View.VISIBLE);
            updatePage(mApp.getCurPosition());
        } else {
            layout_empty.setVisibility(View.VISIBLE);
            layout_wallet.setVisibility(View.GONE);
        }

        updateAddSensor();
    }

    /**
     * 타이틀 생성
     */
    private void initTitle() {
        v_titlebar.setLogoVisible(View.VISIBLE);
        v_titlebar.setSettingVisible(View.VISIBLE);
    }


    /**
     * 뷰 페이저 생성하기
     */
    private void initViewPager() {

        mainPagerAdapter = new MainPagerAdapter(getSupportFragmentManager(), mApp.getAllSensors());
        pager_main.setAdapter(mainPagerAdapter);

        pager_main.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            }

            @Override
            public void onPageSelected(int position) {
                ULog.i(TAG, "onPageSelected: position=" + position);
                mApp.setCurSensor(position);
                updatePage(position);
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
        pager_main.setCurrentItem(mApp.getCurPosition());
        if (mApp.getCurPosition() != -1) {
            updatePage(mApp.getCurPosition());
        }
    }

    /**
     * 페이지 번호와 Dot 업데이트
     * @param position
     */
    private void updatePage(int position) {
        // 이름 업데이트
        txt_name.setText(mApp.getCurSensor().getWearname());
        // 배터리 업데이트

        updateBattery();

        // 페이지 텍스트 업데이트
        txt_page.setText((position+1) + " / " + mApp.getSensorCount());

        // 페이지 Dot 업데이트
        layout_page.removeAllViews();

        for (int i = 0; i < mApp.getSensorCount(); i++) {
            int size = (int) getResources().getDimension(R.dimen.px34);
            ImageView iv = new ImageView(mContext);
            if (i == position) {
                size = (int) getResources().getDimension(R.dimen.px44);
                iv.setBackgroundResource(R.drawable.bg_pager_big);
            } else {
                iv.setBackgroundResource(R.drawable.bg_pager_small);
            }
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(size, size);
            if (i != 0) {
                lp.leftMargin = (int) getResources().getDimension(R.dimen.px15);
            }
            layout_page.addView(iv, lp);
        }
    }

    /**
     * 센서 추가하기 버튼 보이기 숨기기
     */
    private void updateAddSensor() {
        if(mApp.getSensorCount() >= Const.MAX_SENSORS) {
            btn_add.setVisibility(View.INVISIBLE);
        } else {
            btn_add.setVisibility(View.VISIBLE);
        }
    }

    private void updateBattery() {
        Sensor sensor = mApp.getCurSensor();
        if (sensor != null) {
            int level = sensor.getBatteryLevel();
            ULog.i(TAG, "updateBattery:" + level);
            if (level == 100) {
                img_battery.setBackgroundResource(R.drawable.ic_b5);
            } else if (level >= 75) {
                img_battery.setBackgroundResource(R.drawable.ic_b4);
            } else if (level >=50) {
                img_battery.setBackgroundResource(R.drawable.ic_b3);
            } else if (level >= 25) {
                img_battery.setBackgroundResource(R.drawable.ic_b2);
            } else {
                img_battery.setBackgroundResource(R.drawable.ic_b1);
            }
        }
    }

    /**
     * 앱 재시작 하기
     * @param delay
     */
    private void restartApp(int delay) {
        //Intent intent = new Intent(this, MainActivity.class);
        Intent intent = new Intent(this, SplashActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, 0);
        AlarmManager manager = (AlarmManager) this.getSystemService(Context.ALARM_SERVICE);
        manager.set(AlarmManager.RTC, System.currentTimeMillis() + delay, pendingIntent);
        mServiceShutdown = true;
        finish();
    }

    /**
     * 앱 종료하기
     */
    private void finishApp() {
        mServiceShutdown = true;
        if(mServiceShutdown) {
            Intent bindIntent = new Intent(this, SensorService.class);
            stopService(bindIntent);
        }
        finish();
    }


    //********************************************************************************
    //  Service Functions
    //********************************************************************************

    /**
     * 센서 감지 서비스 생성
     */
    private void service_init() {
        // 서비스 바인드
        Intent bindIntent = new Intent(this, SensorService.class);
        startService(bindIntent);
        bindService(bindIntent, mServiceConnection, Context.BIND_AUTO_CREATE);
        // Broadcast 등록
        LocalBroadcastManager.getInstance(this).registerReceiver(SensorStatusChangeReceiver, makeGattUpdateIntentFilter());
    }

    /**
     * 서비스 연결 이벤트 수신
     */
    private ServiceConnection mServiceConnection = new ServiceConnection() {
        /**
         * 서비스 연결 되었을 때 이벤트
         * @param className
         * @param rawBinder
         */
        public void onServiceConnected(ComponentName className, IBinder rawBinder) {
            mService = ((SensorService.LocalBinder) rawBinder).getService();
            ULog.i(TAG, "onServiceConnected mService= " + ((mService == null) ? "NULL" : mService));
            if (!mService.initialize()) {
                ULog.e(TAG, "Unable to initialize Bluetooth");
                finish();
            }
            SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(MainActivity.this);
            boolean enable = settings.getBoolean("KEY_SERVICE_ON",false);
            if(enable == true) {
                //mService.startNotification();
            }

//            ArrayList<Sensor> sensorList = mService.getSensorList();
//            if(sensorList != null) {
//                ULog.i(TAG, "Sensor List Length is " + sensorList.size());
//                //for(Sensor sensor : sensorList) {
//                //    //mSensorAdapter.addSensor(sensor);
//                //}
//                mainPagerAdapter.setList(sensorList);
//            }
        }

        /**
         * 서비스 연결 종료 되었을 때 이벤트
         * @param classname
         */
        public void onServiceDisconnected(ComponentName classname) {
            ULog.i(TAG, "Service disconnected");
        }
    };


    /**
     * 센서 Broadcast Receiver
     */
    private final BroadcastReceiver SensorStatusChangeReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            final Intent mIntent = intent;

            final String actionType = intent.getStringExtra(Const.EXTRA_ACTION_TYPE);
            //final int position = intent.getIntExtra(Const.EXTRA_ACTION_POSITION, 0);
            final String sensorId = intent.getStringExtra(Const.EXTRA_ACTION_SENSOR_ID);
            ULog.i(TAG, "MainActivity Broadcast Receiver: Action=" + action + ", Type=" + actionType + ", ID=" + sensorId);

            //BO: 배터리 수준 알림. 화면의 배터리 아이콘에 적용
            if (action.equals(Const.ACTION_SENSOR_BATTERY)) {
                runOnUiThread(new Runnable() {
                    public void run() {
                       // ULog.i(TAG, "Broadcast Receiver. Action=" + action);
                        updateBattery();
                    }
                });
            }
            //BO: 센서 연결 알림. 화면의 연결 상태 아이콘에 적용
            else if (action.equals(Const.ACTION_GATT_CONNECTED)) {
                runOnUiThread(new Runnable() {
                    public void run() {
                        // 센서의 위치가 있는 경우에는 초기화
                        Sensor sensor = mApp.getSensorFromAddr(sensorId);
                        if (sensor != null) {
                            if (sensor.getLatitude() != 0 || sensor.getLongitude() != 0) {
                                sensor.getInfo().setLocation(0, 0);
                                mApp.updateSensorLoc(sensor);
                            }
                        }
                        //ULog.i(TAG, "Broadcast Receiver. Action=" + action);
                        mainPagerAdapter.notifyDataSetChanged();
                        // 연결 끊김 다이얼로그가 떠 있으면 닫기
                        if(mDlgDisconnectedOn) {
                            mDlgDisconnected.dismiss();
                            mDlgDisconnectedOn = false;
                            mApp.stopAlarm2();
                        }
                    }
                });
            }
            else if (action.equals(Const.ACTION_SENSOR_DETECTING)) {
                runOnUiThread(new Runnable() {
                    public void run() {
                        //ULog.i(TAG, "Broadcast Receiver. Action=" + action);
                        mainPagerAdapter.notifyDataSetChanged();
                        // 연결 끊김 다이얼로그가 떠 있으면 닫기
                        if(mDlgDisconnectedOn) {
                            mDlgDisconnected.dismiss();
                            mDlgDisconnectedOn = false;
                            mApp.stopAlarm2();
                        }
                    }
                });
            }
            else {
                ULog.i(TAG, "Broadcast Receiver. Action NOT handled=" + action);
            }
        }
    };

    private static IntentFilter makeGattUpdateIntentFilter() {
        final IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(Const.ACTION_SENSOR_INITIALIZE);
        intentFilter.addAction(Const.ACTION_SENSOR_DETECTING);
        intentFilter.addAction(Const.ACTION_GATT_DISCONNECT);
        intentFilter.addAction(Const.ACTION_GATT_DISCONNECTED);
        intentFilter.addAction(Const.ACTION_GATT_CONNECTED);
        intentFilter.addAction(Const.ACTION_GATT_FAILED);
        intentFilter.addAction(Const.ACTION_GATT_CONNECTING);
        intentFilter.addAction(Const.ACTION_SENSOR_BATTERY);
        intentFilter.addAction(Const.ACTION_SENSOR_DETECTING);
        return intentFilter;
    }

    Runnable needRestart = new Runnable(){
        @Override
        public void run(){
            Utils.showPopupDlg(MainActivity.this, "블루투스 상태변화", "앱을 다시 실행하셔야 합니다.",
                    "앱 재시작", new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            restartApp(1000 * 2);
                        }
                    }, "앱 종료", new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            finishApp();
                        }
                    }, null);
        }
    };
}
