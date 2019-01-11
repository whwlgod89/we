package kr.co.theunify.wear.adapter;

import android.content.Context;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import kr.co.theunify.wear.R;
import kr.co.theunify.wear.sensor.Sensor;

public class MainPagerAdapter extends FragmentPagerAdapter {

	private String TAG = MainPagerAdapter.class.getSimpleName();

	//********************************************************************************
	//  Layout Member Variable
	//********************************************************************************

	class viewHolder {
		@BindView(R.id.iv_wallet)		ImageView iv_wallet;
		@BindView(R.id.iv_new)			ImageView iv_new;

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

	private ArrayList<Sensor> mList;		// 리스트


	public MainPagerAdapter(FragmentManager fm, ArrayList<Sensor> list){
		super(fm);
		mList = list;
	}

	public void setList(ArrayList<Sensor> list) {
		mList = list;
		notifyDataSetChanged();
	}


	public void addSensor(Sensor sensor) {
		if (mList == null) {
			mList = new ArrayList<>();
		}
		mList.add(sensor);
		notifyDataSetChanged();
	}

	public void removeSensor(Sensor sensor) {
		if (mList != null) {
			for (Sensor already : mList) {
				if (sensor.getSensorId().equals(already.getSensorId())) {
					mList.remove(already);
					break;
				}

			}
		}
		notifyDataSetChanged();
	}

	@Override
	public Fragment getItem(int position) {
		return MainPagerFragment.newInstance(position, mList);
	}


	@Override
	public int getCount() {
		if (mList == null) {
			return 0;
		} else {
			return mList.size();
		}
	}

	@Override
	public int getItemPosition(Object object) {
		return POSITION_NONE;
	}

//	//********************************************************************************
//	//  Construction Functions
//	//************************uy********************************************************
//
//	public MainPagerAdapter (Context context) {
//		mContext = context;
//		inflater = (LayoutInflater) LayoutInflater.from(mContext);
//		this.inflater = LayoutInflater.from(mContext);
//
//	}
//
//	public MainPagerAdapter (Context context, ArrayList<String> list) {
//		mContext = context;
//		inflater = (LayoutInflater) LayoutInflater.from(mContext);
//		this.inflater = LayoutInflater.from(mContext);
//		this.mList = list;
//
//	}
//
//
//	/**
//	* 리스트 셋팅
//	*/
//	public void setList(ArrayList<String> list) {
//		this.mList = list;
//		notifyDataSetChanged();
//	}
//
//	//********************************************************************************
//	//  Override Event Functions
//	//********************************************************************************
//
//	@Override
//	public int getCount() {
//		if (mList == null) {
//			return 0;
//		} else {
//			return mList.size();
//		}
//	}
//
//	@Override
//	public Object getItem(int i) {
//		return mList.get(i);
//	}
//
//	@Override
//	public long getItemId(int i) {
//		return 0;
//	}
//
//	@Override
//	public View getView(final int position, View convertView, ViewGroup parent) {
//		View v = convertView;
//		if (v == null) {
//			v = inflater.inflate(R.layout.item_main_pager, null);
//			viewHolder = new viewHolder(v);
//		} else {
//			viewHolder = (viewHolder) v.getTag();
//		}
//
//
//
//		v.setTag(viewHolder);
//		return v;
//	}
}
