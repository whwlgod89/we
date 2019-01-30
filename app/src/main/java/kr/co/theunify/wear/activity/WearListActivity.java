package kr.co.theunify.wear.activity;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Handler;
import android.os.Message;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnItemClick;
import kr.co.theunify.wear.Const;
import kr.co.theunify.wear.R;
import kr.co.theunify.wear.adapter.SensorAdapter;
import kr.co.theunify.wear.utils.Utils;
import kr.co.theunify.wear.view.TitlebarView;

public class WearListActivity extends BaseActivity {


    private String TAG = WearListActivity.class.getSimpleName();

    private BluetoothAdapter mBluetoothAdapter;
    private SensorAdapter mAdapter;
    private Context mContext;


    @BindView(R.id.v_titlebar)      TitlebarView v_titlebar;
    @BindView(R.id.txt_empty)       TextView txt_empty;
    @BindView(R.id.list_sensor)     ListView list_sensor;
    @BindView(R.id.result)          TextView result;

    private boolean mScanning = false;          // 스캔 중인지?


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.a_wearlist);


        mContext = this;
        ButterKnife.bind(this);

        // Use this check to determine whether BLE is supported on the device.  Then you can
        // selectively disable BLE-related features.
        if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            Toast.makeText(this, R.string.BLE_not_supported, Toast.LENGTH_SHORT).show();
            setResult(Activity.RESULT_CANCELED);
            finish();
        }

        // Initializes a Bluetooth adapter.  For API level 18 and above, get a reference to
        // BluetoothAdapter through BluetoothManager.
        final BluetoothManager bluetoothManager =
                (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        mBluetoothAdapter = bluetoothManager.getAdapter();

        // Checks if Bluetooth is supported on the device.
        if (mBluetoothAdapter == null) {
            Toast.makeText(this, R.string.error_bluetooth_not_supported, Toast.LENGTH_SHORT).show();
            setResult(Activity.RESULT_CANCELED);
            finish();
            return;
        }

        initView();
    }



    @Override
    public void onResume(){
        super.onResume();

        if (!mBluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, Const.REQUEST_CODE_OF_ENABLE_BT);
        }
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
        //mSensorList.requestFocus();

        scanLeDevice(true);
    }
    public void onPause(){
        super.onPause();

        scanLeDevice(false);
        mAdapter.clear();
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
    }


    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }


    private void initView() {

        initTitle();

        initListView();
        scanLeDevice(true);

    }
    private void initTitle() {
        v_titlebar.setTitleVisible(View.VISIBLE);
        v_titlebar.setTitle(getString(R.string.search_wear));
        v_titlebar.setBackVisible(View.VISIBLE);
        v_titlebar.setSearchVisible(View.VISIBLE);
    }


    /**
     * 리스트뷰 Adapter 세팅
     */
    private void initListView() {
        mAdapter = new SensorAdapter(mContext);
        list_sensor.setAdapter(mAdapter);

        txt_empty.setVisibility(View.VISIBLE);
        txt_empty.setText(getString(R.string.start_detect_sensor));
        list_sensor.setVisibility(View.GONE);
    }




    /**
     * 센서 감지 스캔하기
     * @param enable
     */
    private void scanLeDevice(final boolean enable) {
        if (enable) {
            handleStart.sendEmptyMessageDelayed(0, 10000);
            mScanning = true;
            mBluetoothAdapter.startLeScan(mLeScanCallback);
            v_titlebar.setSearchImg(R.drawable.anim_search);
        } else {
            mScanning = false;
            mBluetoothAdapter.stopLeScan(mLeScanCallback);
            v_titlebar.setSearchImg(R.drawable.top_search_nor);
        }
    }

    /*
     *  Handler
     */
    private Handler handleStart = new Handler(){
        @Override
        public void handleMessage(Message msg){
            scanLeDevice(false);
            if (mAdapter == null || mAdapter.getCount()==0) {
                txt_empty.setText(getString(R.string.no_detect_sensor));
            }
        }
    };


    @OnItemClick(R.id.list_sensor)
    public void onListSensorItemClick(int position) {
        mAdapter.setSelected(position);
        BluetoothDevice device = mAdapter.getItem(position);
        if (device != null) {

            // 프로그래스 팝업띄우기 만들기

            //

        }
    }

    // Device scan callback.
    private BluetoothAdapter.LeScanCallback mLeScanCallback =
            new BluetoothAdapter.LeScanCallback() {

                @Override
                public void onLeScan(final BluetoothDevice device, int rssi, byte[] scanRecord) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            // HeyTong 단말기만 추가 대상으로 보여준다
                            String sensorName = device.getName();
                            if(sensorName == null) {
                                return;
                            }

                            if(sensorName == null || (!sensorName.startsWith("HeyT") && !sensorName.startsWith("EHITAG"))) {
                                Log.w(TAG, "This is not a HeyTong Sensor : " + sensorName);
                                return;
                            }

                            Log.w(TAG, "Add Sensor : " + sensorName);
                            mAdapter.addDevice(device);

                            // 하나라도 보이면 리스트가 보여야 한다.
                            if (mAdapter.getCount() > 0) {
                                txt_empty.setVisibility(View.GONE);
                                list_sensor.setVisibility(View.VISIBLE);
                            }

                        }
                    });
                }
            };

}
