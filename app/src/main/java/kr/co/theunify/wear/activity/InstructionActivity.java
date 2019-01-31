package kr.co.theunify.wear.activity;

import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.LinearLayout;


import butterknife.BindView;
import kr.co.theunify.wear.R;

public class InstructionActivity extends AppCompatActivity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.a_instruction);
 /*       getSupportActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
        getSupportActionBar().setCustomView(R.layout.a_instruction);
        getSupportActionBar().setBackgroundDrawable(getResources().getDrawable(R.color.titlebar));
        getSupportActionBar().setTitle(R.string.titlebar_manual);*/

    }
}
