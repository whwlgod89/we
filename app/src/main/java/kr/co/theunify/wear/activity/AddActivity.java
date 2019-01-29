package kr.co.theunify.wear.activity;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.Calendar;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.OnItemClick;
import kr.co.theunify.wear.Const;
import kr.co.theunify.wear.R;
import kr.co.theunify.wear.adapter.SensorAdapter;
import kr.co.theunify.wear.data.SensorInfo;
import kr.co.theunify.wear.sensor.Sensor;
import kr.co.theunify.wear.utils.ULog;
import kr.co.theunify.wear.utils.UString;
import kr.co.theunify.wear.utils.Utils;
import kr.co.theunify.wear.view.TitlebarView;

import static kr.co.theunify.wear.sensor.Sensor.UART_RX_CHAR_UUID;
import static kr.co.theunify.wear.sensor.Sensor.UART_SERVICE_UUID;

/**
 * 지갑 추가 화면
 */
public class AddActivity extends BaseActivity {

    private String TAG = AddActivity.class.getSimpleName();


    //********************************************************************************
    //  Layout Member Variable
    //********************************************************************************

    @BindView(R.id.v_titlebar)              TitlebarView v_titlebar;

     // 리스트
    @BindView(R.id.txt_empty)               TextView    txt_empty;
    @BindView(R.id.list_sensor)				ListView	list_sensor;

    // 이름 입력
    @BindView(R.id.edt_name)				EditText	edt_name;
    @BindView(R.id.del_name)                ImageView   del_name;

    // 전화번호 입력
    @BindView(R.id.edt_phone)				EditText	edt_phone;
    @BindView(R.id.del_phone)                ImageView   del_phone;

    // 모드 선택 라디오
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
//    private List<BluetoothDevice> mSensorList;		// 리스트

    private BluetoothAdapter mBluetoothAdapter;
    private boolean mScanning = false;          // 스캔 중인지?

//    private Sensor mSelectedSensor;
//    private BluetoothGatt mBluetoothGatt;


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
        final BluetoothManager bluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
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

//        disconnect();
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

    /**
     * 뒤로 이동 버튼 클릭 시
     */
    @OnClick(R.id.img_back)
    public void onClickImgBack() {
        onBackPressed();
    }

    /**
     * 타이틀 검색 버튼 클릭 시
     */
    @OnClick(R.id.img_search)
    public void onClickImgSearch() {
        // 리스트 업데이트
        initListView();
        // 스캔하기
        scanLeDevice(true);
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

//            SensorInfo info = new SensorInfo(device.getAddress(), device.getName(), "", Const.ACTION_MODE_LOSS, 100);
//            mSelectedSensor = new Sensor(info);
//            connect();

        }
    }

    @OnClick(R.id.del_name)
    public void onClickDelName() {
        edt_name.setText("");
    }

    @OnClick(R.id.del_phone)
    public void onClickDelPhone() {
        edt_phone.setText("");
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
            Toast.makeText(mContext, getString(R.string.msg_check_device), Toast.LENGTH_SHORT).show();
            return;
        }

        String name = edt_name.getText().toString();
        if (UString.isEmpty(name)) {
            Toast.makeText(mContext, getString(R.string.msg_check_name), Toast.LENGTH_SHORT).show();
            return;
        }
        String phone = edt_phone.getText().toString();

        int mode = (rg_mode.getCheckedRadioButtonId() == R.id.radio_lost) ? Const.ACTION_MODE_LOSS : Const.ACTION_MODE_THEFT;
        int rssi = Const.THEFT_LEVEL_HIGH;
        if (mode == Const.ACTION_MODE_THEFT) {
            rssi = seekbar_dimming.getProgress();
            if (rssi == 0) {
                rssi = Const.THEFT_LEVEL_LOW;
            } else if (rssi == 1) {
                rssi = Const.THEFT_LEVEL_MID;
            } else {
                rssi = Const.THEFT_LEVEL_HIGH;
            }
        }

        addSensor(device, name, phone, mode, rssi);

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

//        edt_name.addTextChangedListener(new TextWatcher() {
//            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) { }
//            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
//                del_name.setVisibility(s.length()==0? View.GONE:View.VISIBLE);
//            }
//            @Override public void afterTextChanged(Editable s) { }
//        });
//
//        edt_phone.addTextChangedListener(new TextWatcher() {
//            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) { }
//            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
//                del_phone.setVisibility(s.length()==0? View.GONE:View.VISIBLE);
//            }
//            @Override public void afterTextChanged(Editable s) { }
//        });

        layout_rssi.setVisibility(View.INVISIBLE);

    }

    private void initTitle() {
        v_titlebar.setTitleVisible(View.VISIBLE);
        v_titlebar.setTitle(getString(R.string.title_register));
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
     * 센서 추가하기 - 메시지 확인 후 추가
     * @param device
     * @param name
     * @param phone
     * @param mode
     * @param rssi
     */
    private void addSensor(final BluetoothDevice device, final String name, final String phone, final int mode, final int rssi) {
        Utils.showPopupDlg(this, getString(R.string.title_confirm_register), getString(R.string.msg_confirm_register),
                getResources().getString(R.string.ok), new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent();
                        intent.putExtra(Const.SENSOR_ID, device.getAddress());
                        intent.putExtra(Const.SENSOR_NAME, name);
                        intent.putExtra(Const.PHONE_NUMBER, phone);
                        intent.putExtra(Const.ACTION_MODE, mode);
                        intent.putExtra(Const.RSSI, rssi);
                        setResult(Const.RESULT_CODE_OF_SENSOR_ADDED, intent);
                        finish();
                    }
                }, getResources().getString(R.string.cancel), null, null);
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
            v_titlebar.setSearchImg(R.drawable.top_search_dis);
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


    //********************************************************************************
    //  Sensor Functions
    // 리스트 선택 시 센서와 연결 후 바로 끊기
    //********************************************************************************

//    public boolean connect() {
//        ULog.w(TAG, "ID: "+ mSelectedSensor.getSensorId() +" connect() called. mConnectState=" + mSelectedSensor.getConnectState());
//
//        if (mBluetoothAdapter == null || mSelectedSensor.getSensorId() == null) {
//            ULog.w(TAG, "BluetoothAdapter not initialized or unspecified address.");
//            return false;
//        }
//
//       final BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(mSelectedSensor.getSensorId());
//        if (device == null) {
//            ULog.w(TAG, "ID:" + mSelectedSensor.getSensorId() + " Device not found.  Unable to connect.");
//            return false;
//        }
//
//        ULog.w(TAG, "connect().device.connectGatt() - ID: "+ mSelectedSensor.getSensorId());
//        mBluetoothGatt = device.connectGatt(mContext, false, mGattCallback);
//        if(mBluetoothGatt != null) {
//            ULog.w(TAG, "ID: "+ mBluetoothGatt.getDevice().getAddress() + " connectGatt() OK!");
//        } else {
//            ULog.w(TAG, "ID: "+ mSelectedSensor.getSensorId() + " connectGatt() FAILED");
//        }
//        return true;
//    }
//
//    public void disconnect() {
//        ULog.w(TAG,"Disconnect++");
//        if (mSelectedSensor == null) {
//            return;
//        }
//        ULog.w(TAG,"Disconnect++1");
//        if (mBluetoothAdapter == null || mBluetoothGatt == null) {
//            ULog.w(TAG, "ID: "+ mSelectedSensor.getSensorId() + " BluetoothAdapter not initialized");
//            return;
//        }
//        ULog.w(TAG,"Disconnect++2");
//        mBluetoothGatt.disconnect();
//        mBluetoothGatt.close();
//        mBluetoothGatt = null;
//        ULog.w(TAG,"Disconnect++3");
//    }
//
//    private final BluetoothGattCallback mGattCallback = new BluetoothGattCallback() {
//        @Override
//        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
//            ULog.w(TAG,"ID: "+ mSelectedSensor.getSensorId() + " onConnectionStateChange(), newState=" + newState);
//
//            if (newState == BluetoothProfile.STATE_CONNECTED) {
//                ULog.w(TAG,"ID: "+ mSelectedSensor.getSensorId() + " Connected to GATT server.");
//                // 연결했으니 바로 끊는다.
//                disconnect();
//            } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
//                ULog.w(TAG,"ID: " + mSelectedSensor.getSensorId() + " Disconnected from GATT server.");
//            }
//        }
//
//        @Override
//        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
//        }
//
//        @Override
//        public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
//        }
//
//        @Override
//        public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
//        }
//
//        @Override
//        public void onReadRemoteRssi(BluetoothGatt gatt, int rssi, int status) {
//        }
//    };


}
