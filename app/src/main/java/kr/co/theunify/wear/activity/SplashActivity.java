package kr.co.theunify.wear.activity;

import android.content.Intent;
import android.graphics.drawable.AnimationDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.widget.ImageView;

import butterknife.BindView;
import butterknife.ButterKnife;
import kr.co.theunify.wear.R;

public class SplashActivity extends AppCompatActivity {

    @BindView(R.id.img_logo) ImageView img_logo;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.a_splash);
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

    //********************************************************************************
    //  User Define Functions
    //********************************************************************************

    /**
     * 사용자 뷰 초기화
     */
    private void initView() {

        AnimationDrawable drawable = (AnimationDrawable) img_logo.getBackground();
        drawable.start();

        // 에니메이션 2회 재생
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                goMain();
            }
        },2400);

    }

    /**
     * 메인화면 이동
     */
    private void goMain() {
        Intent i = new Intent();
        i.setClass(this, MainActivity.class);
        startActivity(i);
        finish();
    }
}
