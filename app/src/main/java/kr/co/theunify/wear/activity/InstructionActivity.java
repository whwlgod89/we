package kr.co.theunify.wear.activity;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.LinearLayout;


import butterknife.BindView;
import kr.co.theunify.wear.R;

public class InstructionActivity extends AppCompatActivity {

    @BindView(R.id.l_background)        LinearLayout l_background;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.a_instruction);


    }
}
