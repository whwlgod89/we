package kr.co.theunify.wear.activity;

import android.content.Context;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import android.view.View;
import android.widget.EditText;
import android.widget.ListView;

import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.RadioButton;


import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.OnItemClick;
import kr.co.theunify.wear.R;
import kr.co.theunify.wear.adapter.SensorAdapter;
import kr.co.theunify.wear.sensor.Sensor;
import kr.co.theunify.wear.view.TitlebarView;

public class WalletActivity extends AppCompatActivity {

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
    @BindView(R.id.btn_add)					TextView	btn_add;

    //********************************************************************************
    //  Member Variable
    //********************************************************************************

    private Context mContext;

    private SensorAdapter mAdapter;
    private List<Sensor> mSensorList;		// 리스트

    //********************************************************************************
    //  LifeCycle Functions
    //********************************************************************************

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.a_wallet);
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

    }

    @OnClick(R.id.edt_name)
    public void onClickEdtName() {

    }

    @OnClick(R.id.edt_phone)
    public void onClickEdtPhone() {

    }

    @OnClick(R.id.rg_mode)
    public void onClickRgMode() {

    }

    @OnClick(R.id.radio_lost)
    public void onClickRadioLost() {

    }

    @OnClick(R.id.radio_steal)
    public void onClickRadioSteal() {

    }

    @OnClick(R.id.btn_add)
    public void onClickBtnAdd() {

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
        mSensorList = new ArrayList<>();
        mSensorList.add(new Sensor());
        mSensorList.add(new Sensor());


        if (mSensorList == null || mSensorList.size()==0) {
            txt_empty.setVisibility(View.VISIBLE);
            list_sensor.setVisibility(View.GONE);
        } else {
            txt_empty.setVisibility(View.GONE);
            list_sensor.setVisibility(View.VISIBLE);
            if (mAdapter == null) {
                mAdapter = new SensorAdapter(mContext, mSensorList);
                list_sensor.setAdapter(mAdapter);
            } else {
                mAdapter.setList(mSensorList);
            }
        }
    }


}
