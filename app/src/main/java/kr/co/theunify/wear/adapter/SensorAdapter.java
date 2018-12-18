package kr.co.theunify.wear.adapter;

import android.content.Context;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import kr.co.theunify.wear.R;
import kr.co.theunify.wear.sensor.Sensor;

public class SensorAdapter extends BaseAdapter {

	private String TAG = SensorAdapter.class.getSimpleName();


	//********************************************************************************
	//  Layout Member Variable
	//********************************************************************************

	class viewHolder {
		@BindView(R.id.txt_name)			TextView txt_name;
		@BindView(R.id.txt_serial)			TextView txt_serial;

		@BindView(R.id.btn_modify) 			LinearLayout btn_modify;

		public viewHolder(View view) {
			ButterKnife.bind(this, view);
		}
	}

	//********************************************************************************
	//  Member Variable
	//********************************************************************************

	private Context mContext;
	private LayoutInflater inflater = null;
	private viewHolder viewHolder = null;

	private List<Sensor> mList;		// 리스트

	//********************************************************************************
	//  Construction Functions
	//********************************************************************************

	public SensorAdapter(Context context) {
		mContext = context;
		inflater = (LayoutInflater) LayoutInflater.from(mContext);
		this.inflater = LayoutInflater.from(mContext);

	}

	public SensorAdapter(Context context, List<Sensor> list) {
		mContext = context;
		inflater = (LayoutInflater) LayoutInflater.from(mContext);
		this.inflater = LayoutInflater.from(mContext);
		this.mList = list;

	}


	/** 
	* 리스트 셋팅 
	*/ 
	public void setList(List<Sensor> list) {
		this.mList = list;
		notifyDataSetChanged();
	}

	//********************************************************************************
	//  Override Event Functions
	//********************************************************************************

	@Override
	public int getCount() {
		if (mList == null) {
			return 0;
		} else {
			return mList.size();
		}
	}

	@Override
	public Object getItem(int i) {
		return mList.get(i);
	}

	@Override
	public long getItemId(int i) {
		return 0;
	}

	@Override
	public View getView(final int position, View convertView, ViewGroup parent) {
		View v = convertView;
		if (v == null) {
			v = inflater.inflate(R.layout.item_sensor_list, null);
			viewHolder = new viewHolder(v);
		} else {
			viewHolder = (viewHolder) v.getTag();
		}



		v.setTag(viewHolder);
		return v;
	}
}
