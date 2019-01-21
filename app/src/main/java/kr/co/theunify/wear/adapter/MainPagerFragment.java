package kr.co.theunify.wear.adapter;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import java.util.ArrayList;

import kr.co.theunify.wear.R;
import kr.co.theunify.wear.sensor.Sensor;

/**
 * Created by beksung
 * 뷰 페이저 프래그먼트
 */

public class MainPagerFragment extends Fragment {

    private static String TAG = MainPagerFragment.class.getSimpleName();


    private static final String KEY_CONTENT = "TestFragment:Content";
    private int mPosition = 0;
    private ArrayList<Sensor> mList;		// 리스트

    public MainPagerFragment() {
    }

    public MainPagerFragment newInstance () {
        return null;
    }

    public static MainPagerFragment newInstance(int position, ArrayList<Sensor> array) {
        MainPagerFragment fragment = new MainPagerFragment();
        fragment.mPosition = position;
        fragment.mList = array;
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if ((savedInstanceState != null) && savedInstanceState.containsKey(KEY_CONTENT)) {
            mPosition = savedInstanceState.getInt(KEY_CONTENT);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.item_main_pager, null);

        if (mList != null) {
            Sensor sensor = mList.get(mPosition);

            ImageView iv_new = (ImageView) v.findViewById(R.id.iv_new);
            iv_new.setVisibility(View.GONE);

            ImageView iv_wallet = (ImageView) v.findViewById(R.id.iv_wallet);

            if (sensor.getConnectState() == Sensor.CONNECT_STATE.CONNECTED.ordinal()) {
                int resource = R.drawable.purse_00;
                switch (mPosition % 3) {
                    case 0:
                        resource = R.drawable.purse_00;
                        break;
                    case 1:
                        resource = R.drawable.purse_01;
                        break;
                    case 2:
                        resource = R.drawable.purse_02;
                        break;
                }
                iv_wallet.setBackgroundResource(resource);
            } else {
                iv_wallet.setBackgroundResource(R.drawable.purse_03);
            }
        }

        return v;
    }

}
