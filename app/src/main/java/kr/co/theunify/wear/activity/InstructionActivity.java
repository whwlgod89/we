package kr.co.theunify.wear.activity;

import android.os.Bundle;
import android.text.util.Linkify;
import android.view.View;
import android.widget.TextView;


import java.util.regex.Matcher;
import java.util.regex.Pattern;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import kr.co.theunify.wear.R;

public class InstructionActivity extends BaseActivity {



    @BindView(R.id.tx_smartpouch)
    TextView tx_smartpouch;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.a_instruction);
        ButterKnife.bind(this);
        InitView();
    }

    public void InitView(){
        Linkify.TransformFilter mTransform = new Linkify.TransformFilter() {
            @Override
            public String transformUrl(Matcher match, String url) {
                return "";
            }
        };
        Pattern pattern = Pattern.compile(getString(R.string.SmartPouch_Url));

        Linkify.addLinks(tx_smartpouch,pattern,"http://thesmartpouch.com/",null,mTransform);
    }

    @OnClick(R.id.btn_confirm)
    public void confirm(View v) {
        {
            onBackPressed();
        }
    }
    @Override
    public void onBackPressed() {
        super.onBackPressed();

    }


}
