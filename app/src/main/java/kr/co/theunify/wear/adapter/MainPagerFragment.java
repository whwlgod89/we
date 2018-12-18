package kr.co.theunify.wear.adapter;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import java.util.ArrayList;
import kr.co.theunify.wear.R;

/**
 * Created by nashine40 on 2018-03-27.
 * 앱 사용안내 프래그먼트
 */

public class MainPagerFragment extends Fragment {
    private static final String KEY_CONTENT = "TestFragment:Content";
    private int mPosition = 0;
    private ArrayList<String> mList;		// 리스트

    public MainPagerFragment() {
    }

    public MainPagerFragment newInstance () {
        return null;
    }

    public static MainPagerFragment newInstance(int position, ArrayList<String> array) {
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

        ImageView iv_new = (ImageView) v.findViewById(R.id.iv_new);
        ImageView iv_wallet = (ImageView) v.findViewById(R.id.iv_wallet);

        return v;
    }

}
