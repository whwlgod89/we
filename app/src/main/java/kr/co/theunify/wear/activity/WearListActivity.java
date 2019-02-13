package kr.co.theunify.wear.activity;

import android.app.Activity;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Handler;
import android.os.Message;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.OnItemClick;
import kr.co.theunify.wear.Const;
import kr.co.theunify.wear.R;
import kr.co.theunify.wear.adapter.SensorAdapter;
import kr.co.theunify.wear.data.SensorInfo;
import kr.co.theunify.wear.utils.ULog;
import kr.co.theunify.wear.view.TitlebarView;

public class WearListActivity extends BaseActivity {


    private String TAG = WearListActivity.class.getSimpleName();

    //********************************************************************************
    //  Layout Member Variable
    //********************************************************************************


    @BindView(R.id.v_titlebar)      TitlebarView v_titlebar;
    @BindView(R.id.txt_empty)       TextView txt_empty;
    @BindView(R.id.list_sensor)     ListView list_sensor;
    @BindView(R.id.btn_close)       TextView btn_close;

    //********************************************************************************
    //  Member Variable
    //********************************************************************************

    private Context mContext;

    private BluetoothAdapter mBluetoothAdapter;
    private SensorAdapter mAdapter;


    private boolean mScanning = false;          // 스캔 중인지?

    private SensorInfo mSelectedSensor;
    private BluetoothGatt mBluetoothGatt;

    private ProgressDialog mProgress;

    //********************************************************************************
    //  LifeCycle Functions
    //********************************************************************************

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
        setButtonEnable(false);
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

        disconnect();
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
        if (requestCode == Const.REQUEST_CODE_OF_ADD_SENSOR && resultCode == Const.RESULT_CODE_OF_SENSOR_ADDED) {
            String sensorId = data.getStringExtra(Const.SENSOR_ID);
            String sensorName = data.getStringExtra(Const.SENSOR_NAME);
            String walletName = data.getStringExtra(Const.WEAR_NAME);
            int cover = data.getIntExtra(Const.WEAR_COVER, R.drawable.purse_01);
            String phoneNumber = data.getStringExtra(Const.PHONE_NUMBER);
            int actionMode = data.getIntExtra(Const.ACTION_MODE, Const.ACTION_MODE_LOSS);
            int rssi = data.getIntExtra(Const.RSSI, 100);

            setResult(Const.RESULT_CODE_OF_SENSOR_ADDED, data);
            finish();
        }
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
     * 선택완료 버튼 클릭 시
     */
    @OnClick(R.id.btn_close)
    public void onClickSelectComplete() {
        Intent i = new Intent();
        i.setClass(WearListActivity.this, AddActivity.class);
        i.putExtra("wear", mSelectedSensor);
        startActivityForResult(i, Const.REQUEST_CODE_OF_ADD_SENSOR);
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
    public void onListSensorItemClick(int position) {
        mAdapter.setSelected(position);
        BluetoothDevice device = mAdapter.getItem(position);
        if (device != null) {
//          // 센서 정보 추가하고, 알림 울리기 시작
            mSelectedSensor = new SensorInfo(device.getAddress(), device.getName(), device.getName(), R.drawable.purse_01, "", Const.ACTION_MODE_LOSS, 100);
            // 버튼 활성화
            setButtonEnable(true);
            if (connect() ) {
                showProgress("웨어 확인 중입니다.");
            }
        }
    }

    //********************************************************************************
    //  User Define Functions
    //********************************************************************************


    private void initView() {


        initTitle();
        initListView();
        scanLeDevice(true);
        setButtonEnable(false);
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
     * 프로그래스 보이기
     */
    public void showProgress(String msg) {
        hideProgress();
        if (!isFinishing()) {
            mProgress = new ProgressDialog(mContext);
            mProgress.setMessage(msg);
            mProgress.setCancelable(false);
            mProgress.setCanceledOnTouchOutside(false);
            mProgress.show();
        }
    }

    /**
     * 프로그래서 숨기기
     */
    public void hideProgress() {
        if (mProgress != null) {
            mProgress.dismiss();
        }
    }

    private void setButtonEnable(boolean enable) {
        if (enable) {
            btn_close.setEnabled(true);   //버튼 활성화
            btn_close.setBackgroundResource(R.drawable.selector_btn_add_sensor);
        } else {
            btn_close.setEnabled(false);
            btn_close.setBackgroundResource(R.drawable.selector_btn_non_add_sensor);
        }
    }

    /**
     * 센서 감지 스캔하기
     * @param enable
     */
    private void scanLeDevice(final boolean enable) {
        if (enable) {
            mAdapter.removeAllDevice();
            handleStart.sendEmptyMessageDelayed(0, 10000);
            mScanning = true;
            v_titlebar.startSearch();
            mBluetoothAdapter.startLeScan(mLeScanCallback);
        } else {
            mScanning = false;
            v_titlebar.stopSearch();

            mBluetoothAdapter.stopLeScan(mLeScanCallback);
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
                                Log.w(TAG, "This is not a Wear Sensor : " + sensorName);
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


    //********************************************************************************
    //  Sensor Functions
    // 리스트 선택 시 센서와 연결 후 바로 끊기
    //********************************************************************************

    public boolean connect() {
        ULog.w(TAG, "ID: "+ mSelectedSensor.getId() +" connect() called. mConnectState=" + mSelectedSensor.getState());

        if (mBluetoothAdapter == null || mSelectedSensor.getId() == null) {
            ULog.w(TAG, "BluetoothAdapter not initialized or unspecified address.");
            return false;
        }

        final BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(mSelectedSensor.getId());
        if (device == null) {
            ULog.w(TAG, "ID:" + mSelectedSensor.getId() + " Device not found.  Unable to connect.");
            return false;
        }

        ULog.w(TAG, "connect().device.connectGatt() - ID: "+ mSelectedSensor.getId());
        mBluetoothGatt = device.connectGatt(mContext, false, mGattCallback);
        if(mBluetoothGatt != null) {
            ULog.w(TAG, "ID: "+ mBluetoothGatt.getDevice().getAddress() + " connectGatt() OK!");
        } else {
            ULog.w(TAG, "ID: "+ mSelectedSensor.getId() + " connectGatt() FAILED");
        }
        return true;
    }

    public void disconnect() {

        hideProgress();

        ULog.w(TAG,"Disconnect++");
        if (mSelectedSensor == null) {
            return;
        }
        ULog.w(TAG,"Disconnect++1");
        if (mBluetoothAdapter == null || mBluetoothGatt == null) {
            ULog.w(TAG, "ID: "+ mSelectedSensor.getId() + " BluetoothAdapter not initialized");
            return;
        }
        ULog.w(TAG,"Disconnect++2");
        mBluetoothGatt.disconnect();
        mBluetoothGatt.close();
        mBluetoothGatt = null;
        ULog.w(TAG,"Disconnect++3");
    }

    private final BluetoothGattCallback mGattCallback = new BluetoothGattCallback() {
        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            ULog.w(TAG,"ID: "+ mSelectedSensor.getId() + " onConnectionStateChange(), newState=" + newState);

            if (newState == BluetoothProfile.STATE_CONNECTED) {
                ULog.w(TAG,"ID: "+ mSelectedSensor.getId() + " Connected to GATT server.");
                // 연결했으니 바로 끊는다.
                disconnect();
                // 알림음 울렸으니 추가화면으로 이동한다.
            } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                hideProgress();
                ULog.w(TAG,"ID: " + mSelectedSensor.getId() + " Disconnected from GATT server.");
            }
        }

        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
        }

        @Override
        public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
        }

        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
        }

        @Override
        public void onReadRemoteRssi(BluetoothGatt gatt, int rssi, int status) {
        }
    };

}
