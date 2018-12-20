package kr.co.theunify.wear.activity;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;

import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;

import android.widget.RadioGroup;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.RadioButton;


import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.OnItemClick;
import kr.co.theunify.wear.Const;
import kr.co.theunify.wear.R;
import kr.co.theunify.wear.adapter.SensorAdapter;
import kr.co.theunify.wear.sensor.Sensor;
import kr.co.theunify.wear.utils.UString;
import kr.co.theunify.wear.utils.Utils;
import kr.co.theunify.wear.view.TitlebarView;

/**
 * 지갑 추가 화면
 */
public class WalletActivity extends BaseActivity {

    private String TAG = WalletActivity.class.getSimpleName();


    //********************************************************************************
    //  Layout Member Variable
    //********************************************************************************

    @BindView(R.id.v_titlebar)              TitlebarView v_titlebar;

    @BindView(R.id.txt_empty)               TextView    txt_empty;
    @BindView(R.id.list_sensor)				ListView	list_sensor;
    @BindView(R.id.edt_name)				EditText	edt_name;
    @BindView(R.id.edt_phone)				EditText	edt_phone;
    @BindView(R.id.rg_mode)                 RadioGroup  rg_mode;
    @BindView(R.id.radio_lost)				RadioButton	radio_lost;
    @BindView(R.id.radio_steal)				RadioButton	radio_steal;

    @BindView(R.id.layout_rssi)             LinearLayout layout_rssi;
    @BindView(R.id.seekbar_dimming)         SeekBar seekbar_dimming;

    @BindView(R.id.btn_add)					TextView	btn_add;

    //********************************************************************************
    //  Member Variable
    //********************************************************************************

    private Context mContext;

    private SensorAdapter mAdapter;
    private List<BluetoothDevice> mSensorList;		// 리스트

    private BluetoothAdapter mBluetoothAdapter;
    private boolean mScanning = false;          // 스캔 중인지?

    //********************************************************************************
    //  LifeCycle Functions
    //********************************************************************************

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.a_wallet);
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


    @Override
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

    //********************************************************************************
    //  Override Functions
    //********************************************************************************
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // User chose not to enable Bluetooth.
        if (requestCode == Const.REQUEST_CODE_OF_ENABLE_BT && resultCode == Activity.RESULT_CANCELED) {
            setResult(Activity.RESULT_CANCELED);
            finish();
            return;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    //********************************************************************************
    //  Override Event Functions
    //********************************************************************************

    @OnClick(R.id.img_back)
    public void onClickImgBack() {
        onBackPressed();
    }

    @OnClick(R.id.img_search)
    public void onClickImgSearch() {
        Toast.makeText(mContext, "search", Toast.LENGTH_SHORT).show();
    }

    @OnItemClick(R.id.list_sensor)
    public void onListSensorItemClick(int position){
        mAdapter.setSelected(position);
        BluetoothDevice device = mAdapter.getItem(position);
        if (device != null) {
            edt_name.setEnabled(true);
            edt_phone.setEnabled(true);
            radio_lost.setEnabled(true);
            radio_steal.setEnabled(true);

            edt_name.setText(device.getName());
            edt_name.setSelection(device.getName().length());
            edt_name.requestFocus();
            Utils.showSoftKeyboard(mContext, edt_name);
        }
    }

    @OnClick(R.id.radio_lost)
    public void onClickRadioLost() {
        layout_rssi.setVisibility(View.INVISIBLE);
    }

    @OnClick(R.id.radio_steal)
    public void onClickRadioSteal() {
        layout_rssi.setVisibility(View.VISIBLE);
    }

    @OnClick(R.id.btn_add)
    public void onClickBtnAdd() {
        BluetoothDevice device = mAdapter.getSelected();
        if (device == null) {
            Toast.makeText(mContext, "센서를 먼저 선택해 주세요.", Toast.LENGTH_SHORT).show();
            return;
        }

        String name = edt_name.getText().toString();
        if (UString.isEmpty(name)) {
            Toast.makeText(mContext, "이름을 입력해 주세요.", Toast.LENGTH_SHORT).show();
            return;
        }
        String phone = edt_phone.getText().toString();
        if (UString.isEmpty(phone)) {
            Toast.makeText(mContext, "전화벉호를 입력해 주세요.", Toast.LENGTH_SHORT).show();
            return;
        }

        int mode = (rg_mode.getCheckedRadioButtonId() == R.id.radio_lost) ? Const.ACTION_MODE_LOSS : Const.ACTION_MODE_THEFT;
        int rssi = 100;
        if (mode == Const.ACTION_MODE_THEFT) {
            rssi = seekbar_dimming.getProgress();
            if (rssi == 0) {
                rssi = 75;
            } else if (rssi == 1) {
                rssi = 85;
            } else {
                rssi = 100;
            }
        }

        Intent intent = new Intent();
        intent.putExtra(Const.SENSOR_ID, device.getAddress());
        intent.putExtra(Const.SENSOR_NAME, name);
        intent.putExtra(Const.PHONE_NUMBER, phone);
        intent.putExtra(Const.ACTION_MODE, mode);
        intent.putExtra(Const.RSSI, rssi);
        setResult(Const.RESULT_CODE_OF_SENSOR_ADDED, intent);
        finish();

    }


    //********************************************************************************
    //  User Define Functions
    //********************************************************************************

    /**
     * 사용자 뷰 초기화
     */
    private void initView() {
        initTitle();

        initListView();

        // seekbar 체인지리스너
        seekbar_dimming.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, final int progress, boolean fromUser) {
//                if (selectZoneData.getSchedule() == DIMMING_MODE_MANUAL) {
//                    txt_dimming_cur_dim.setText(progress+"%");
//                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(final SeekBar seekBar) {
                // 현재 디밍모드 탭이 매뉴얼일때만
                seekBar.getProgress();

            }
        });
    }

    private void initTitle() {
        v_titlebar.setTitleVisible(View.VISIBLE);
        v_titlebar.setTitle("지갑추가");
        v_titlebar.setBackVisible(View.VISIBLE);
        v_titlebar.setSearchVisible(View.VISIBLE);
    }


    /**
     * 리스트뷰 Adapter 세팅
     */
    private void initListView() {
//        mSensorList = new ArrayList<>();
//
//        if (mSensorList == null || mSensorList.size()==0) {
//            txt_empty.setVisibility(View.VISIBLE);
//            list_sensor.setVisibility(View.GONE);
//        } else {
            txt_empty.setVisibility(View.GONE);
            list_sensor.setVisibility(View.VISIBLE);
            if (mAdapter == null) {
                mAdapter = new SensorAdapter(mContext, mSensorList);
                list_sensor.setAdapter(mAdapter);
//            } else {
//                mAdapter.setList(mSensorList);
            }
//        }
    }

    private void scanLeDevice(final boolean enable) {
        if (enable) {
            handleStart.sendEmptyMessageDelayed(0, 10000);
            mScanning = true;
            mBluetoothAdapter.startLeScan(mLeScanCallback);
        } else {
            mScanning = false;
            mBluetoothAdapter.stopLeScan(mLeScanCallback);
        }
    }

    /*
     *  Handler
     */
    private Handler handleStart = new Handler(){
        @Override
        public void handleMessage(Message msg){
            mScanning = false;
            mBluetoothAdapter.stopLeScan(mLeScanCallback);
//            mCmdScan.setImageResource(R.drawable.search_sensor_start);
        }
    };

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
                            if(sensorName == null || (!sensorName.startsWith("HeyT") && !sensorName.startsWith("EHITAG"))) {
                                Log.w(TAG, "This is not a HeyTong Sensor : " + sensorName);
                                return;
                            }

                            Log.w(TAG, "Add Sensor : " + sensorName);
                            mAdapter.addDevice(device);
                        }
                    });
                }
            };
}
