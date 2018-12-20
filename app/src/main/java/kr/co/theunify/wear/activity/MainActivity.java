package kr.co.theunify.wear.activity;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Parcelable;
import android.preference.PreferenceManager;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.io.Serializable;
import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import kr.co.theunify.wear.Const;
import kr.co.theunify.wear.R;
import kr.co.theunify.wear.WearApp;
import kr.co.theunify.wear.adapter.MainPagerAdapter;
import kr.co.theunify.wear.sensor.Sensor;
import kr.co.theunify.wear.service.SensorService;
import kr.co.theunify.wear.utils.Utils;
import kr.co.theunify.wear.view.TitlebarView;

public class MainActivity extends AppCompatActivity {

    private String TAG = MainActivity.class.getSimpleName();


    //********************************************************************************
    //  Layout Member Variable
    //********************************************************************************

    @BindView(R.id.v_titlebar)      TitlebarView v_titlebar;        // 타이틀 바

    @BindView(R.id.txt_name)         TextView txt_name;

    @BindView(R.id.pager_main)      ViewPager pager_main;

    @BindView(R.id.img_battery)     ImageView img_battery;
    @BindView(R.id.txt_page)         TextView txt_page;
    @BindView(R.id.layout_page)        LinearLayout layout_page;

    @BindView(R.id.btn_add)         TextView btn_add;

    //********************************************************************************
    //  Member Variable
    //********************************************************************************

    private Context mContext;

    private SensorService mService = null;
    private WearApp mApp = null;


    private boolean mServiceShutdown = false;    // 강제 종료 여부 (App 재시작, App 완전 종료)

    private MainPagerAdapter mainPagerAdapter;

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

        Log.d(TAG, "onCreate() - mAppLaunched=" + mApp.mAppLaunched);
        mApp.mAppLaunched = true;
        Log.d(TAG, "onCreate() - mAppLaunched=" + mApp.mAppLaunched);


        service_init();
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

        if(mServiceShutdown) {
            Intent bindIntent = new Intent(this, SensorService.class);
            stopService(bindIntent);
        }
        LocalBroadcastManager.getInstance(this).unregisterReceiver(SensorStatusChangeReceiver);
        Log.d(TAG, "Main Activity Stopped, with service=" + mServiceShutdown);
    }


    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    //********************************************************************************
    //  Override Functions
    //********************************************************************************

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.d(TAG, "onActivityResult - Request=" + requestCode + ", Result=" + resultCode + ".............");

        if(requestCode == Const.REQUEST_CODE_OF_ADD_SENSOR) {
            if (resultCode == Const.RESULT_CODE_OF_SENSOR_ADDED) {

                String sensorId = data.getStringExtra(Const.SENSOR_ID);
                String sensorName = data.getStringExtra(Const.SENSOR_NAME);
                String phoneNumber = data.getStringExtra(Const.PHONE_NUMBER);
                int actionMode = data.getIntExtra(Const.ACTION_MODE, Const.ACTION_MODE_LOSS);
                int rssi = data.getIntExtra(Const.RSSI, 100);

// 모든 센서 관리는 (추가 삭제 등)은 App을 통해서 일관되게 한다.
//                String a = BluetoothDevice.EXTRA_DEVICE;
//                BluetoothDevice device = BluetoothAdapter.getDefaultAdapter().getRemoteDevice(sensorId);
//                if(device != null)
//                {
//                    Sensor sensor = mService.addSensor(device, sensorId, sensorName, phoneNumber, actionMode);
//                    mApp.insertSensor(sensor);
//                }
//
//                mSensorAdapter.setDataSet(mApp.getAllSensors());
//                mSensorAdapter.notifyDataSetChanged();
                mApp.addSensor(sensorId, sensorName, phoneNumber, actionMode, rssi);  // 센서 추가 (App에서 DB & 목록에 추가, 서비스 연결)
                //mSensorAdapter.notifyDataSetChanged();                          // 리스트 화면 갱신
                mainPagerAdapter.notifyDataSetChanged();                      // 리스트 화면 갱신
//                if(mApp.getSensorCount() == Const.MAX_SENSORS) {
//                    showAddSensor(false);       // Show/Hide Widgets for Add Sensor
//                }

                if(Const.DEBUG) {
                    int index = mApp.getAllSensors().size()-1;
                    //Sensor sensor = mApp.getSensorItem(index);
                    Sensor sensor = mApp.getSensor(index);
                    String strRegister = "ID=[" + sensor.getSensorId() + "], Name=[" + sensor.getSensorName() + "], Phone=[" + sensor.getPhoneNumber() + "], Mode=[" + sensor.getActionMode() + "]";
                    Toast.makeText(MainActivity.this, strRegister, Toast.LENGTH_SHORT).show();
                }
            }
            else if (resultCode == RESULT_CANCELED) {
                Toast.makeText(MainActivity.this, "Registration Canceled~!", Toast.LENGTH_SHORT).show();
            }
        }
        else if(requestCode == Const.REQUEST_CODE_OF_MODIFY_SENSOR) {
            // 센서 설정 변경 (이름, 폰번, 모드) - ModifySensor 모듈로부터 전달
            if(resultCode == RESULT_OK) {
                Toast.makeText(MainActivity.this, "Sensor Modified~!", Toast.LENGTH_SHORT).show();
                mainPagerAdapter.notifyDataSetChanged();
            }
        }
         if(requestCode == Const.REQUEST_CODE_OF_APP_SETTINGS) {
            // 재시작
            if(resultCode == Const.RESULT_CODE_OF_RESTART_APP) {
                Toast.makeText(MainActivity.this, "Setting Result - RESTART", Toast.LENGTH_SHORT).show();
                restartApp(2000);
            }
            // 종료
            else if(resultCode == Const.RESULT_CODE_OF_FINISH_APP) {
                Toast.makeText(MainActivity.this, "Setting Result - FINISH", Toast.LENGTH_SHORT).show();
                finishApp();
            }
//            else if (resultCode == Activity.RESULT_CANCELED) {
//                Toast.makeText(MainActivity.this, "Setting Result - NORMAL", Toast.LENGTH_SHORT).show();
//            }
            else {
                SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(this);
                String strAlarm1 = settings.getString("pref_key_alarm_disconnected", "FAIL");
                String strAlarm2 = settings.getString("pref_key_alarm_find_phone", "FAIL");
                Log.d(TAG, "Conn=" + strAlarm1 + ", Find=" + strAlarm2);
            }
        }
    }

    //********************************************************************************
    //  Override Event Functions
    //********************************************************************************


//    @OnItemClick(R.id.pager_main)
//    public void onPagerMainItemClick(int position){
//
//    }

    @OnClick(R.id.btn_add)
    public void onClickBtnAdd() {
        if(mApp.getSensorCount() == Const.MAX_SENSORS) {
//            showAddSensor(false);       // Show/Hide Widgets for Add Sensor
        } else {
            Intent i = new Intent();
            i.setClass(this, WalletActivity.class);
            startActivityForResult(i, Const.REQUEST_CODE_OF_ADD_SENSOR);
        }
    }

    @OnClick(R.id.img_setting)
    public void onClickImgSetting() {
        Intent intent = new Intent();
        intent.setClass(this, SettingActivity.class);
        startActivityForResult(intent, Const.REQUEST_CODE_OF_APP_SETTINGS);
    }

    @OnClick({R.id.btn_find, R.id.btn_location, R.id.btn_remove, R.id.btn_setting} )
    public void onClickOption(View v) {
        if (v.getId() == R.id.btn_find) {
            mApp.getCurSensor().findSensor();
        } else if (v.getId() == R.id.btn_location) {

        } else if (v.getId() == R.id.btn_remove) {
            Utils.showPopupDlg(this, getString(R.string.remove_title), getString(R.string.remove_message),
                    getResources().getString(R.string.ok), new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            mainPagerAdapter.removeSensor(mApp.getCurSensor());
                            mApp.removeSensor();
                            updatePage(0);

                        }
                    }, getResources().getString(R.string.cancel), null, null);
        } else if (v.getId() == R.id.btn_setting) {
            Intent i = new Intent();
            i.setClass(this, ModifyActivity.class);
            startActivityForResult(i, Const.REQUEST_CODE_OF_MODIFY_SENSOR);
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
            updatePage(0);
        }
    }

    private void initTitle() {
        v_titlebar.setLogoVisible(View.VISIBLE);
        v_titlebar.setSettingVisible(View.VISIBLE);
    }


    private void initViewPager() {

        mainPagerAdapter = new MainPagerAdapter(getSupportFragmentManager(), null);
        pager_main.setAdapter(mainPagerAdapter);

        pager_main.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                mApp.setCurSensor(position);
                txt_name.setText(mApp.getCurSensor().getSensorName());
                updatePage(position);
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
    }

    private void restartApp(int delay) {
        //Intent intent = new Intent(this, MainActivity.class);
        Intent intent = new Intent(this, SplashActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, 0);
        AlarmManager manager = (AlarmManager) this.getSystemService(Context.ALARM_SERVICE);
        manager.set(AlarmManager.RTC, System.currentTimeMillis() + delay, pendingIntent);
        mServiceShutdown = true;
        finish();
    }

    private void finishApp() {
        mServiceShutdown = true;
        finish();
    }


    private void updatePage(int position) {
        txt_page.setText((position+1) + " / " + mApp.getSensorCount());
        layout_page.removeAllViews();

        for (int i = 0; i < mApp.getSensorCount(); i++) {
            int size = (int) getResources().getDimension(R.dimen.px40);
            ImageView iv = new ImageView(mContext);
            if (i == position) {
                size = (int) getResources().getDimension(R.dimen.px50);
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


    //********************************************************************************
    //  Service Functions
    //********************************************************************************

    private void service_init() {
        Intent bindIntent = new Intent(this, SensorService.class);
        startService(bindIntent);
        bindService(bindIntent, mServiceConnection, Context.BIND_AUTO_CREATE);
        LocalBroadcastManager.getInstance(this).registerReceiver(SensorStatusChangeReceiver, makeGattUpdateIntentFilter());
    }

    private ServiceConnection mServiceConnection = new ServiceConnection() {
        public void onServiceConnected(ComponentName className, IBinder rawBinder) {
            mService = ((SensorService.LocalBinder) rawBinder).getService();
            Log.d(TAG, "onServiceConnected mService= " + ((mService == null) ? "NULL" : mService));
            if (!mService.initialize()) {
                Log.e(TAG, "Unable to initialize Bluetooth");
                finish();
            }
            SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(MainActivity.this);
            boolean enable = settings.getBoolean("KEY_SERVICE_ON",false);
            if(enable == true) {
                //mService.startNotification();
            }

            ArrayList<Sensor> sensorList = mService.getSensorList();
            if(sensorList != null) {
                Log.d(TAG, "Sensor List Length is " + sensorList.size());
                //for(Sensor sensor : sensorList) {
                //    //mSensorAdapter.addSensor(sensor);
                //}
                mainPagerAdapter.setList(sensorList);
            }
        }
        public void onServiceDisconnected(ComponentName classname) {
            Log.d(TAG, "Service disconnected");
        }
    };


    private final BroadcastReceiver SensorStatusChangeReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            final Intent mIntent = intent;

            final String actionType = intent.getStringExtra(Const.EXTRA_ACTION_TYPE);
            //final int position = intent.getIntExtra(Const.EXTRA_ACTION_POSITION, 0);
            final String sensorId = intent.getStringExtra(Const.EXTRA_ACTION_SENSOR_ID);
            Log.w(TAG, "MainActivity Broadcast Receiver: Action=" + action + ", Type=" + actionType + ", ID=" + sensorId);

//            if (action.equals(Const.ACTION_SENSOR_INITIALIZE) ||
//                    action.equals(Const.ACTION_SENSOR_DETECTING)   ||
//                    action.equals(Const.ACTION_SENSOR_BATTERY)   ||
//                    action.equals(Const.ACTION_GATT_DISCONNECT)   ||
//                    action.equals(Const.ACTION_GATT_DISCONNECTED)  ||
//                    action.equals(Const.ACTION_GATT_CONNECTED)      ||
//                    action.equals(Const.ACTION_GATT_CONNECTING)      ||
//                    action.equals(Const.ACTION_GATT_FAILED) )
//            {
//                runOnUiThread(new Runnable() {
//                    public void run() {
//                        mSensorAdapter.notifyDataSetChanged();
//                        if(action.equals(Const.ACTION_GATT_CONNECTED)) {
//                            //stopAlarm();
//                            //hAnimation.postDelayed(sAnim, 100);
//                        }
//                        Log.w(TAG, "Broadcast receiver " + action);
//                    }
//                });
//            }
            //BO: 배터리 수준 알림. 화면의 배터리 아이콘에 적용
            if (action.equals(Const.ACTION_SENSOR_BATTERY))
            {
                runOnUiThread(new Runnable() {
                    public void run() {
                        Log.w(TAG, "Broadcast Receiver. Action=" + action);
                        mainPagerAdapter.notifyDataSetChanged();
                    }
                });
            }
            //BO: 센서 연결 알림. 화면의 연결 상태 아이콘에 적용
            else if (action.equals(Const.ACTION_GATT_CONNECTED)) {
                runOnUiThread(new Runnable() {
                    public void run() {
                        Log.w(TAG, "Broadcast Receiver. Action=" + action);
                        mainPagerAdapter.notifyDataSetChanged();
                        // 연결 끊김 다이얼로그가 떠 있으면 닫기
//                        if(mDlgDisconnectedOn) {
//                            mDlgDisconnected.dismiss();
//                            mDlgDisconnectedOn = false;
//                            mApp.stopAlarm2();
//                        }
                    }
                });
            }
//BO: 변경 from updateUI to actionUI
//            else if(action.equals(Const.ACTION_GATT_DISCONNECTED)) {
//                runOnUiThread(new Runnable() {
//                    public void run() {
//                        Log.w(TAG, "Broadcast Receiver. Action=" + action);
//                        mSensorAdapter.notifyDataSetChanged();
//                        getWindow().addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED
//                                | WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD
//                                | WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
//                                | WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON);
//                        getWindow().makeActive();
//                        mApp.startAlarm2(true);
//                    }
//                });
//            }
//            else if(action.equals(Const.ACTION_SENSOR_FIND_PHONE_START))
//            {
//                runOnUiThread(new Runnable() {
//                    public void run() {
//                        Log.w(TAG, "Broadcast Receiver. Action=" + action);
//                        mApp.startAlarm2(false);
//                    }
//                });
//            }
//            else if(action.equals(Const.ACTION_SENSOR_FIND_PHONE_STOP))
//            {
//                runOnUiThread(new Runnable() {
//                    public void run() {
//                        Log.w(TAG, "Broadcast Receiver. Action=" + action);
//                        mApp.stopAlarm2();
//                    }
//                });
//            }
            else {
                Log.d(TAG, "Broadcast Receiver. Action NOT handled=" + action);
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
        return intentFilter;
    }

}
