package kr.co.theunify.wear.activity;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
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
import kr.co.theunify.wear.WearApp;
import kr.co.theunify.wear.dialog.CommonDialog;
import kr.co.theunify.wear.dialog.HelpDialog;
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

    @BindView(R.id.v_titlebar)          TitlebarView v_titlebar;

    @BindView(R.id.edt_name)            EditText edt_name;
    @BindView(R.id.edt_Wear_name)       EditText edt_Wear_name;
    @BindView(R.id.edt_phone)           EditText edt_phone;
    @BindView(R.id.rg_mode)             RadioGroup rg_mode;
    @BindView(R.id.radio_lost)          RadioButton radio_lost;
    @BindView(R.id.radio_steal)         RadioButton radio_steal;

    @BindView(R.id.layout_rssi)         LinearLayout layout_rssi;
    @BindView(R.id.seekbar_dimming)     SeekBar seekbar_dimming;

    //  지갑선택
    @BindView(R.id.rg_wallet)       RadioGroup rg_wallet;
    @BindView(R.id.radio_brown)     RadioButton radio_brwon;
    @BindView(R.id.radio_green)     RadioButton radio_green;
    @BindView(R.id.radio_purple)    RadioButton radio_purple;

    @BindView(R.id.btn_add)             TextView btn_add;
    @BindView(R.id.delete_layout)       LinearLayout delete_layout;
    @BindView(R.id.bg_brown)            ImageView bg_brown;
    @BindView(R.id.bg_green)            ImageView bg_green;
    @BindView(R.id.bg_purple)           ImageView bg_orange;

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

    @OnClick(R.id.btn_question)
    public void onClickBubble() {
        HelpDialog helpDialog = new HelpDialog(mContext);
    }

    @OnClick(R.id.delete_layout)
    public void onClickDelete(){
        Utils.showPopupDlg(this, getString(R.string.remove_title), getString(R.string.remove_message),
                getResources().getString(R.string.ok), new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        // 센서 삭제
                        mApp.removeSensor();
                        // 화면 업데이트
                        initView();

                        Intent intent = new Intent(mContext, MainActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        mContext.startActivity(intent);
                    }
                }, getResources().getString(R.string.cancel), null, null);
    }
    @OnClick({R.id.bg_brown,R.id.bg_green,R.id.bg_purple,R.id.radio_green,R.id.radio_purple,R.id.radio_brown})
    public void onSelectwear(View v) {
        switch (v.getId()) {
            case R.id.bg_brown :
            case R.id.radio_brown:{ radio_brwon.setChecked(true);radio_green.setChecked(false);radio_purple.setChecked(false);break;}
            case R.id.bg_purple:
            case R.id.radio_purple:{ radio_purple.setChecked(true);radio_brwon.setChecked(false);radio_green.setChecked(false);break;}
            case R.id.bg_green:
            case R.id.radio_green:{ radio_green.setChecked(true);radio_purple.setChecked(false);radio_brwon.setChecked(false);break;}
        }
    }
    @OnClick(R.id.btn_add)
    public void onClickBtnAdd() {
        String name = edt_name.getText().toString();
        String wearname = edt_Wear_name.getText().toString();
        if (UString.isEmpty(wearname)) {
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

        int radioButtonID = rg_wallet.getCheckedRadioButtonId();
        int cover = R.drawable.purse_01;
        switch (radioButtonID) {
            case R.id.radio_brown: cover = 0; break;
            case R.id.radio_green: cover = 1; break;
            case R.id.radio_purple: cover = 2; break;
        }

        modifySensor(name, wearname, cover, phone, mode, rssi);
    }

    //********************************************************************************
    //  User Define Functions
    //********************************************************************************

    /**
     * 사용자 뷰 초기화
     */
    private void initView() {
        initTitle();

        btn_add.setText(getResources().getString(R.string.modify));

        edt_name.setText(mSensor.getSensorName());
        edt_Wear_name.setText(mSensor.getWearname());
        edt_phone.setText(mSensor.getPhoneNumber());
        edt_phone.setEnabled(false);
        if (mSensor.getActionMode() == Const.ACTION_MODE_LOSS) {
            layout_rssi.setVisibility(View.INVISIBLE);
            rg_mode.check(R.id.radio_lost);
        } else {
            rg_mode.check(R.id.radio_steal);
            layout_rssi.setVisibility(View.VISIBLE);

            int rssi = mSensor.getInfo().getRssi();
            if (rssi == Const.THEFT_LEVEL_LOW) {
                seekbar_dimming.setProgress(0);
            } else if (rssi == Const.THEFT_LEVEL_MID) {
                seekbar_dimming.setProgress(1);
            } else {
                seekbar_dimming.setProgress(2);
            }
        }

        switch (mSensor.getCover()) {
            case 0: radio_brwon.setChecked(true); break;
            case 1: radio_green.setChecked(true); break;
            case 2: radio_purple.setChecked(true); break;
            default: radio_brwon.setChecked(true); break;
        }

        radio_lost.setEnabled(true);
        radio_steal.setEnabled(true);

        edt_Wear_name.requestFocus();
        Utils.showSoftKeyboard(mContext, edt_Wear_name);
    }

    private void initTitle() {
        v_titlebar.setTitleVisible(View.VISIBLE);
        v_titlebar.setTitle(getString(R.string.title_modify));
        v_titlebar.setBackVisible(View.VISIBLE);
    }

    private void modifySensor(final String name, final String wearnName, final int cover, final String phone, final int mode, final int rssi) {
        Utils.showPopupDlg(this, getString(R.string.title_confirm_modify), getString(R.string.msg_confirm_modify),
                getResources().getString(R.string.ok), new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        mSensor.getInfo().setName(name);
                        mSensor.getInfo().setPhone(phone);
                        mSensor.getInfo().setWearname(wearnName);
                        mSensor.getInfo().setCover(cover);
                        mSensor.getInfo().setMode(mode);
                        mSensor.getInfo().setRssi(rssi);
                        mApp.updateSensor(mSensor);

                        setResult(RESULT_OK);
                        finish();
                    }
                }, getResources().getString(R.string.cancel), null, null);
    }

}
