package kr.co.theunify.wear.activity;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.drawable.AnimationDrawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.ActivityCompat;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import butterknife.BindView;
import butterknife.ButterKnife;
import kr.co.theunify.wear.Const;
import kr.co.theunify.wear.R;
import kr.co.theunify.wear.utils.ULog;

public class SplashActivity extends BaseActivity {

    private String TAG = SplashActivity.class.getSimpleName();

    private static final int PERMISSION_REQUEST_REQUEST = 1003;

    //********************************************************************************
    //  Layout Member Variable
    //********************************************************************************

    @BindView(R.id.img_logo) ImageView img_logo;

    //********************************************************************************
    //  Member Variable
    //********************************************************************************

    private Context mContext;

    private BluetoothAdapter mBluetoothAdapter;
    private Handler handler;

    //********************************************************************************
    //  LifeCycle Functions
    //********************************************************************************


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.a_splash);
        mContext = this;
        ButterKnife.bind(this);

        // selectively disable BLE-related features.
        if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            Toast.makeText(this, R.string.BLE_not_supported, Toast.LENGTH_SHORT).show();
            finish();
        }

        // Initializes a Bluetooth adapter.  For API level 18 and above, get a reference to
        // BluetoothAdapter through BluetoothManager.
        final BluetoothManager bluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        mBluetoothAdapter = bluetoothManager.getAdapter();
        if (mBluetoothAdapter == null) {
            Toast.makeText(this, R.string.BLE_not_supported, Toast.LENGTH_SHORT).show();
            finish();
        }

        if (!mBluetoothAdapter.isEnabled()) {
            Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableIntent, Const.REQUEST_CODE_OF_ENABLE_BT);

            ULog.i(TAG, "BT Adapter is not enabled. (onResume)");
            return;
        }

        processCheckPermissionAndAppReg();

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
        ULog.e(TAG, "onBackPressed");
        super.onBackPressed();
        handleStart.removeMessages(0);
    }

    //********************************************************************************
    //  Override Functions
    //********************************************************************************

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        ULog.i(TAG, "onActivityResult()");
        switch (requestCode) {
            case Const.REQUEST_CODE_OF_ENABLE_BT:
                if (resultCode == Activity.RESULT_OK) {
                    ULog.i(TAG, "BT Enable Result=OK");
                    processCheckPermissionAndAppReg();
                }
                else {
                    ULog.i(TAG, "BT Enable Result=NO");
                    showAlertPopup("", getResources().getString(R.string.error_bluetooth_not_enabled), getResources().getString(R.string.ok), new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            finish();
                        }
                    }, "");
                }
                break;
        }
    }

    private void processCheckPermissionAndAppReg() {
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP) {
            checkPermission();
        } else {
            initView();
        }
    }

    @SuppressLint("NewApi")
    private void checkPermission() {
        if (checkCallingOrSelfPermission(Manifest.permission.INTERNET) != PackageManager.PERMISSION_GRANTED
                || checkCallingOrSelfPermission(Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED
                || checkCallingOrSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED
                || checkCallingOrSelfPermission(Manifest.permission.DISABLE_KEYGUARD) != PackageManager.PERMISSION_GRANTED

                || checkCallingOrSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED
                || checkCallingOrSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                || checkCallingOrSelfPermission(Manifest.permission.RECEIVE_BOOT_COMPLETED) != PackageManager.PERMISSION_GRANTED

                || checkCallingOrSelfPermission(Manifest.permission.WAKE_LOCK) != PackageManager.PERMISSION_GRANTED
                || checkCallingOrSelfPermission(Manifest.permission.VIBRATE) != PackageManager.PERMISSION_GRANTED

                || checkCallingOrSelfPermission(Manifest.permission.BLUETOOTH) != PackageManager.PERMISSION_GRANTED
                || checkCallingOrSelfPermission(Manifest.permission.BLUETOOTH_ADMIN) != PackageManager.PERMISSION_GRANTED ) {

            ActivityCompat.requestPermissions(this, new String[]{
                    Manifest.permission.INTERNET,
                    Manifest.permission.READ_PHONE_STATE,
                    Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.DISABLE_KEYGUARD,
                    Manifest.permission.ACCESS_COARSE_LOCATION,
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.RECEIVE_BOOT_COMPLETED,
                    Manifest.permission.WAKE_LOCK,
                    Manifest.permission.VIBRATE,
                    Manifest.permission.BLUETOOTH,
                    Manifest.permission.BLUETOOTH_ADMIN
            }, PERMISSION_REQUEST_REQUEST);

        } else {
            initView();
        }
    }

    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case PERMISSION_REQUEST_REQUEST:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED &&
                        grantResults[1] == PackageManager.PERMISSION_GRANTED &&
                        grantResults[2] == PackageManager.PERMISSION_GRANTED &&
                        grantResults[3] == PackageManager.PERMISSION_GRANTED &&
                        grantResults[4] == PackageManager.PERMISSION_GRANTED &&
                        grantResults[5] == PackageManager.PERMISSION_GRANTED &&
                        grantResults[6] == PackageManager.PERMISSION_GRANTED &&
                        grantResults[7] == PackageManager.PERMISSION_GRANTED &&
                        grantResults[8] == PackageManager.PERMISSION_GRANTED &&
                        grantResults[9] == PackageManager.PERMISSION_GRANTED &&
                        grantResults[10] == PackageManager.PERMISSION_GRANTED ) {
                    initView();
                } else {
                    showAlertPopup("", getResources().getString(R.string.error_grant_permission), getResources().getString(R.string.ok), new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            finish();
                        }
                    }, "");

                }
                break;
            default:
                break;
        }
    }

    //********************************************************************************
    //  Override Event Functions
    //********************************************************************************

    //********************************************************************************
    //  User Define Functions
    //********************************************************************************

    /**
     * 사용자 뷰 초기화
     */
    private void initView() {

        // 애니메이션 시작하기
        AnimationDrawable drawable = (AnimationDrawable) img_logo.getBackground();
        drawable.start();

        // 애니메이션이 1.2초 걸리므로 3회 반복
        handleStart.sendEmptyMessageDelayed(0, 3600);
    }

    /**
     * 메인화면 이동
     */
    private void goMain() {
        ULog.e(TAG, "goMain");
        Intent i = new Intent();
        i.setClass(this, MainActivity.class);
        startActivity(i);
        finish();
    }

    /*
     *  Handler
     */
    private Handler handleStart = new Handler(){
        @Override
        public void handleMessage(Message msg){
            goMain();
        }
    };


}
