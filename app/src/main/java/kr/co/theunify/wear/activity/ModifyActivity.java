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
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.OnItemClick;
import kr.co.theunify.wear.Const;
import kr.co.theunify.wear.R;
import kr.co.theunify.wear.WearApp;
import kr.co.theunify.wear.adapter.SensorAdapter;
import kr.co.theunify.wear.sensor.Sensor;
import kr.co.theunify.wear.utils.UString;
import kr.co.theunify.wear.utils.Utils;
import kr.co.theunify.wear.view.TitlebarView;

/**
 * 지갑 추가 화면
 */
public class ModifyActivity extends BaseActivity {

    private String TAG = ModifyActivity.class.getSimpleName();


    //********************************************************************************
    //  Layout Member Variable
    //********************************************************************************

    @BindView(R.id.v_titlebar)              TitlebarView v_titlebar;

    @BindView(R.id.layout_list)               LinearLayout    layout_list;
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

    private WearApp mApp = null;
    private Sensor mSensor;

    //********************************************************************************
    //  LifeCycle Functions
    //********************************************************************************

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.a_wallet);
        mContext = this;
        ButterKnife.bind(this);

        mApp = (WearApp) getApplication();

        mSensor = mApp.getCurSensor();
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

    //********************************************************************************
    //  Override Event Functions
    //********************************************************************************

    @OnClick(R.id.img_back)
    public void onClickImgBack() {
        onBackPressed();
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

        mSensor.getInfo().setName(name);
        mSensor.getInfo().setPhone(phone);
        mSensor.getInfo().setMode(mode);
        mSensor.getInfo().setRssi(rssi);
        mApp.updateSensor(mSensor);

        setResult(RESULT_OK);
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

        layout_list.setVisibility(View.GONE);
        btn_add.setText(getResources().getString(R.string.modify_button));

        edt_name.setText(mSensor.getSensorName());
        edt_phone.setText(mSensor.getPhoneNumber());
        if (mSensor.getActionMode() == Const.ACTION_MODE_LOSS) {
            rg_mode.check(R.id.radio_lost);
        } else {
            rg_mode.check(R.id.radio_steal);
            seekbar_dimming.setProgress(mSensor.getInfo().getRssi());
        }

        edt_name.setEnabled(true);
        edt_phone.setEnabled(true);
        radio_lost.setEnabled(true);
        radio_steal.setEnabled(true);
    }

    private void initTitle() {
        v_titlebar.setTitleVisible(View.VISIBLE);
        v_titlebar.setTitle("지갑수정");
        v_titlebar.setBackVisible(View.VISIBLE);
    }


}
