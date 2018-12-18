package kr.co.theunify.wear.activity;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import kr.co.theunify.wear.Const;
import kr.co.theunify.wear.R;
import kr.co.theunify.wear.adapter.MainPagerAdapter;
import kr.co.theunify.wear.view.TitlebarView;

public class MainActivity extends AppCompatActivity {

    private String TAG = MainActivity.class.getSimpleName();


    //********************************************************************************
    //  Layout Member Variable
    //********************************************************************************

    @BindView(R.id.v_titlebar)      TitlebarView v_titlebar;        // 타이틀 바

    @BindView(R.id.pager_main)      ViewPager pager_main;
    @BindView(R.id.btn_add)         TextView btn_add;

    //********************************************************************************
    //  Member Variable
    //********************************************************************************

    private Context mContext;

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

        initView();
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

//        if(requestCode == Const.REQUEST_CODE_OF_ADD_SENSOR) {
//            if (resultCode == Const.RESULT_CODE_OF_SENSOR_ADDED) {
//                //String sensorId = data.getStringExtra(BluetoothDevice.EXTRA_DEVICE);
//                String sensorId = data.getStringExtra(Const.SENSOR_ID);
//                String sensorName = data.getStringExtra(Const.SENSOR_NAME);
//                String phoneNumber = data.getStringExtra(Const.PHONE_NUMBER);
//                int actionMode = data.getIntExtra(Const.ACTION_MODE, Const.ACTION_MODE_LOSS);
//
//// 모든 센서 관리는 (추가 삭제 등)은 App을 통해서 일관되게 한다.
////                String a = BluetoothDevice.EXTRA_DEVICE;
////                BluetoothDevice device = BluetoothAdapter.getDefaultAdapter().getRemoteDevice(sensorId);
////                if(device != null)
////                {
////                    Sensor sensor = mService.addSensor(device, sensorId, sensorName, phoneNumber, actionMode);
////                    mApp.insertSensor(sensor);
////                }
////
////                mSensorAdapter.setDataSet(mApp.getAllSensors());
////                mSensorAdapter.notifyDataSetChanged();
//                mApp.addSensor(sensorId, sensorName, phoneNumber, actionMode);  // 센서 추가 (App에서 DB & 목록에 추가, 서비스 연결)
//                //mSensorAdapter.notifyDataSetChanged();                          // 리스트 화면 갱신
//                mSensorAdapter.notifyDataSetInvalidated();                      // 리스트 화면 갱신
//                if(mApp.getSensorCount() == Const.MAX_SENSORS) {
//                    showAddSensor(false);       // Show/Hide Widgets for Add Sensor
//                }
//
//                if(Const.DEBUG) {
//                    int index = mApp.getAllSensors().size()-1;
//                    //Sensor sensor = mApp.getSensorItem(index);
//                    Sensor sensor = mApp.getSensor(index);
//                    String strRegister = "ID=[" + sensor.getSensorId() + "], Name=[" + sensor.getSensorName() + "], Phone=[" + sensor.getPhoneNumber() + "], Mode=[" + sensor.getActionMode() + "]";
//                    Toast.makeText(MainActivity.this, strRegister, Toast.LENGTH_SHORT).show();
//                }
//            }
//            else if (resultCode == Activity.RESULT_CANCELED) {
//                Toast.makeText(MainActivity.this, "Registration Canceled~!", Toast.LENGTH_SHORT).show();
//            }
//        }
//        else if(requestCode == Const.REQUEST_CODE_OF_MODIFY_SENSOR) {
//            // 센서 설정 변경 (이름, 폰번, 모드) - ModifySensor 모듈로부터 전달
//            if(resultCode == Const.RESULT_CODE_OF_SENSOR_MODIFIED) {
//                Toast.makeText(MainActivity.this, "Sensor Modified~!", Toast.LENGTH_SHORT).show();
//                mSensorAdapter.notifyDataSetChanged();
//            }
//            else if (resultCode == Activity.RESULT_CANCELED) {
//                Toast.makeText(MainActivity.this, "Modification Canceled~!", Toast.LENGTH_SHORT).show();
//            }
//        }
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
        Intent i = new Intent();
        i.setClass(this, WalletActivity.class);
        startActivity(i);
    }

    @OnClick(R.id.img_setting)
    public void onClickImgSetting() {
        Intent intent = new Intent();
        intent.setClass(this, SettingActivity.class);
        startActivityForResult(intent, Const.REQUEST_CODE_OF_APP_SETTINGS);
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
    }

    private void initTitle() {
        v_titlebar.setLogoVisible(View.VISIBLE);
        v_titlebar.setSettingVisible(View.VISIBLE);
    }


    private void initViewPager() {
        ArrayList<String> mList = new ArrayList<>();
        mList.add("123");
        mList.add("123");
        mList.add("123");

        mainPagerAdapter = new MainPagerAdapter(getSupportFragmentManager(), mList);
        pager_main.setAdapter(mainPagerAdapter);

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

}
