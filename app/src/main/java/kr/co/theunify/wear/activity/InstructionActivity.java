package kr.co.theunify.wear.activity;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;


import butterknife.BindView;
import butterknife.OnClick;
import kr.co.theunify.wear.R;

public class InstructionActivity extends BaseActivity {


    @BindView(R.id.btn_instruction)
    TextView btn_confirm;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.a_instruction);


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
