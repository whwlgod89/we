package kr.co.theunify.wear.view;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.LayoutInflater;

import kr.co.theunify.wear.R;

import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.ImageView;


import java.io.IOException;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;
import java.util.ResourceBundle;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class TitlebarView extends LinearLayout {

	private String TAG = TitlebarView.class.getSimpleName();


	//********************************************************************************
	//  Layout Member Variable
	//********************************************************************************

	@BindView(R.id.layout_bg)				RelativeLayout	layout_bg;
	@BindView(R.id.img_back)				ImageView	img_back;
	@BindView(R.id.img_logo)				ImageView	img_logo;
	@BindView(R.id.txt_title)				TextView	txt_title;
	@BindView(R.id.img_search)				ImageView	img_search;
	@BindView(R.id.img_setting)				ImageView	img_setting;

	//********************************************************************************
	//  Member Variable
	//********************************************************************************

	private Context mContext;
	private AnimationDrawable animationDrawable;

	//********************************************************************************
	//  Construction Functions
	//********************************************************************************

	public TitlebarView (Context context) {
		super(context);
		mContext = context;
		initView();

	}

	public TitlebarView (Context context, AttributeSet attrs) {
		super(context, attrs);
		mContext = context;
		initView();

	}

	//********************************************************************************
	//  Override Event Functions
	//********************************************************************************

	@OnClick(R.id.layout_bg)
	public void onClickLayoutBg() {

	}

	@OnClick(R.id.img_back)
	public void onClickImgBack() {

	}

	@OnClick(R.id.img_logo)
	public void onClickImgLogo() {

	}

	@OnClick(R.id.txt_title)
	public void onClickTxtTitle() {

	}

	@OnClick(R.id.img_search)
	public void onClickImgSearch() {

	}

	@OnClick(R.id.img_setting)
	public void onClickImgSetting() {

	}

	//********************************************************************************
	//  User Define Functions
	//********************************************************************************

	/** 
	* 사용자 뷰 초기화 
	*/ 
	private void initView() { 
		LayoutInflater inflater = LayoutInflater.from(mContext);
		inflater.inflate(R.layout.v_titlebar, this);
		ButterKnife.bind(this);
	}

	public void setBackVisible(int visible) {
		img_back.setVisibility(visible);
	}

	public void setLogoVisible(int visible) {
		img_logo.setVisibility(visible);
	}

	public void setTitleVisible(int visible) {
		txt_title.setVisibility(visible);
	}

	public void setTitle(String title) {
		txt_title.setText(title);
	}

	public void setSearchVisible(int visible) {
		img_search.setVisibility(visible);
	}

	public void setSearchImg(final int res) {

		new Handler().postDelayed(new Runnable() {
			@Override
			public void run() {
				img_search.setBackgroundResource(res);
			}
		},3500);

		/*animationDrawable = (AnimationDrawable) img_search.getBackground();
		img_search.post(new Runnable() {
            @Override
            public void run() {
                animationDrawable.start();
                animationDrawable.stop();
            }
        });*/
	}

	public void setSettingVisible(int visible) {
		img_setting.setVisibility(visible);
	}
	public void setBackground(Drawable res) {
	    img_search.setBackground(res);
	}

}
