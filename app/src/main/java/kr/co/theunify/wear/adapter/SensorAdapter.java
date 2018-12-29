package kr.co.theunify.wear.adapter;

import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.graphics.Color;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
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
		@BindView(R.id.layout_body)			RelativeLayout layout_body;
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

	private List<BluetoothDevice> mList;		// 리스트

	private int selectedPos = -1;

	//********************************************************************************
	//  Construction Functions
	//********************************************************************************

	public SensorAdapter(Context context) {
		mContext = context;
		inflater = (LayoutInflater) LayoutInflater.from(mContext);
		this.inflater = LayoutInflater.from(mContext);

	}

	public SensorAdapter(Context context, List<BluetoothDevice> list) {
		mContext = context;
		inflater = (LayoutInflater) LayoutInflater.from(mContext);
		this.inflater = LayoutInflater.from(mContext);
		this.mList = list;

	}

	/**
	 * 디바이스 추가하기 - 중복된 것은 제거한다.
	 */
	public void addDevice(BluetoothDevice device) {
		if (mList == null) {
			mList = new ArrayList<>();
		}
		for (BluetoothDevice already : mList) {
			if (device.getAddress().equals(already.getAddress())) {
				return;
			}
		}
		this.mList.add(device);
		notifyDataSetChanged();
	}

	public void removeAllDevice() {
		mList = null;
		notifyDataSetChanged();
	}

	/** 
	* 리스트 셋팅 
	*/ 
	public void setSelected(int pos) {
		selectedPos = pos;
		notifyDataSetChanged();
	}

	public BluetoothDevice getSelected() {
		if (selectedPos == -1) {
			return null;
		} else {
			return getItem(selectedPos);
		}
	}

	public void clear() {
		if (mList != null) {
			mList.clear();
		}
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
	public BluetoothDevice getItem(int i) {
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

		BluetoothDevice device = mList.get(position);

		String deviceName = device.getName();
		if (deviceName != null && deviceName.length() > 0) {
			viewHolder.txt_name.setText(deviceName);
		} else {
			viewHolder.txt_name.setText(R.string.unknown_device);
		}
		viewHolder.txt_serial.setText(device.getAddress());

		if (selectedPos == position) {
			viewHolder.layout_body.setBackgroundColor(Color.parseColor("#cccccc"));
		} else {
			viewHolder.layout_body.setBackgroundColor(Color.parseColor("#ececec"));
		}

		v.setTag(viewHolder);
		return v;
	}
}
