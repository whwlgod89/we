package kr.co.theunify.wear.activity;

import android.Manifest;
import android.app.Activity;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.telephony.TelephonyManager;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import kr.co.theunify.wear.Const;
import kr.co.theunify.wear.R;
import kr.co.theunify.wear.data.SensorInfo;
import kr.co.theunify.wear.dialog.CommonDialog;
import kr.co.theunify.wear.dialog.HelpDialog;
import kr.co.theunify.wear.sensor.Sensor;
import kr.co.theunify.wear.utils.UString;
import kr.co.theunify.wear.utils.Utils;
import kr.co.theunify.wear.view.TitlebarView;

/**
 * 지갑 추가 화면
 */
public class AddActivity extends BaseActivity {


    private String TAG = AddActivity.class.getSimpleName();


    //********************************************************************************
    //  Layout Member Variable
    //********************************************************************************

    @BindView(R.id.v_titlebar)      TitlebarView v_titlebar;

    // 이름 입력
    @BindView(R.id.edt_name)        EditText edt_name;
    @BindView(R.id.edt_Wear_name)   EditText edt_Wear_name;
    @BindView(R.id.del_name)        ImageView del_name;

    // 전화번호 입력
    @BindView(R.id.edt_phone)       EditText edt_phone;
    @BindView(R.id.del_phone)
    ImageView del_phone;


    // 모드 선택 라디오
    @BindView(R.id.rg_mode)
    RadioGroup rg_mode;
    @BindView(R.id.radio_lost)
    RadioButton radio_lost;
    @BindView(R.id.radio_steal)
    RadioButton radio_steal;

    @BindView(R.id.layout_rssi)
    LinearLayout layout_rssi;
    @BindView(R.id.btn_question)
    LinearLayout btn_question;
    @BindView(R.id.delete_layout)
    LinearLayout delete_layout;

    @BindView(R.id.seekbar_dimming)
    SeekBar seekbar_dimming;

    @BindView(R.id.btn_add)
    TextView btn_add;
    @BindView(R.id.btn_delete)
    TextView btn_delete;


    //  지갑선택
    @BindView(R.id.rg_wallet)
    RadioGroup rg_wallet;
    @BindView(R.id.radio_brown)
    RadioButton radio_brwon;
    @BindView(R.id.radio_green)
    RadioButton radio_green;
    @BindView(R.id.radio_purple)
    RadioButton radio_purple;
    @BindView(R.id.bg_brown)
    LinearLayout bg_brown;
    @BindView(R.id.bg_green)
    LinearLayout bg_green;
    @BindView(R.id.bg_purple)
    LinearLayout bg_orange;


    //********************************************************************************
    //  Member Variable
    //********************************************************************************

    private Context mContext;

    private SensorInfo mSensor;

    //********************************************************************************
    //  LifeCycle Functions
    //********************************************************************************

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.a_wallet);
        mContext = this;
        ButterKnife.bind(this);

        mSensor = (SensorInfo) getIntent().getSerializableExtra("wear");

        initView();
    }


    @Override
    public void onResume() {
        super.onResume();
    }


    @Override
    public void onPause() {
        super.onPause();
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
    }


    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Utils.hideSoftKeyboard(mContext, edt_name);
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

    @OnClick(R.id.btn_question)
    public void onClickBubble() {
        HelpDialog helpDialog = new HelpDialog(mContext);
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

        String name = edt_name.getText().toString();

        String wear_name = edt_Wear_name.getText().toString();
        if (UString.isEmpty(wear_name)) {
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

        int cover = R.drawable.purse_01;

        if (radio_brwon.isChecked()){
            cover = 0;
        }else if (radio_green.isChecked()){
            cover = 1;
        }else if(radio_purple.isChecked()){
            cover = 2;
        }
        addSensor(name, wear_name, cover, phone, mode, rssi);
    }


    //********************************************************************************
    //  User Define Functions
    //********************************************************************************

    /**
     * 사용자 뷰 초기화
     */
    private void initView() {

        initTitle();

        radio_lost.setEnabled(true);
        radio_steal.setEnabled(true);

        edt_phone.setText(getPhoneNumber());
        edt_phone.setEnabled(false);

        edt_name.setText(mSensor.getName());
        edt_Wear_name.setText(mSensor.getWearname());
        edt_Wear_name.setSelection(mSensor.getWearname().length());
        edt_Wear_name.setText("");
        radio_brwon.setChecked(true);

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

        layout_rssi.setVisibility(View.INVISIBLE);
        delete_layout.setVisibility(View.GONE);

        edt_Wear_name.requestFocus();
        Utils.showSoftKeyboard(mContext, edt_Wear_name);

    }

    private void initTitle() {
        v_titlebar.setTitleVisible(View.VISIBLE);
        v_titlebar.setTitle(getString(R.string.title_register));
        v_titlebar.setBackVisible(View.VISIBLE);
    }

    @OnClick({R.id.bg_brown, R.id.bg_green, R.id.bg_purple, R.id.radio_green, R.id.radio_purple, R.id.radio_brown})
    public void onSelectwear(View v) {
        switch (v.getId()) {
            case R.id.bg_brown:
            case R.id.radio_brown: {
                radio_brwon.setChecked(true);
                radio_green.setChecked(false);
                radio_purple.setChecked(false);
                break;
            }
            case R.id.bg_purple:
            case R.id.radio_purple: {
                radio_purple.setChecked(true);
                radio_brwon.setChecked(false);
                radio_green.setChecked(false);
                break;
            }
            case R.id.bg_green:
            case R.id.radio_green: {
                radio_green.setChecked(true);
                radio_purple.setChecked(false);
                radio_brwon.setChecked(false);
                break;
            }
        }
    }

    /**
     * 센서 추가하기 - 메시지 확인 후 추가
     *
     * @param name
     * @param phone
     * @param mode
     * @param rssi
     */

    private void addSensor(final String name, final String wearName, final int cover, final String phone, final int mode, final int rssi) {
        Utils.showPopupDlg(this, getString(R.string.title_confirm_register), getString(R.string.msg_confirm_register),
                getResources().getString(R.string.ok), new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent();
                        intent.putExtra(Const.SENSOR_ID, mSensor.getId());
                        intent.putExtra(Const.SENSOR_NAME, name);
                        intent.putExtra(Const.WEAR_NAME, wearName);
                        intent.putExtra(Const.WEAR_COVER, cover);
                        intent.putExtra(Const.PHONE_NUMBER, phone);
                        intent.putExtra(Const.ACTION_MODE, mode);
                        intent.putExtra(Const.RSSI, rssi);
                        setResult(Const.RESULT_CODE_OF_SENSOR_ADDED, intent);
                        finish();
                    }
                }, getResources().getString(R.string.cancel), null, null);
    }

    private String getPhoneNumber() {
        TelephonyManager telManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);

        String phoneNumber = "";
        try {

            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_SMS) !=
                    PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_NUMBERS)
                    != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE)
                    != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
            }
            String tmpPhoneNumber = telManager.getLine1Number();
            phoneNumber = tmpPhoneNumber.replace("+82", "0");

        } catch (Exception e) {
            phoneNumber = "";
        }

        return phoneNumber;
    }
}
