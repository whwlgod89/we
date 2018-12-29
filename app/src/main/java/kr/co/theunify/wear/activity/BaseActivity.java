package kr.co.theunify.wear.activity;

import android.support.v7.app.AppCompatActivity;
import android.view.View;

import kr.co.theunify.wear.dialog.CommonDialog;
import kr.co.theunify.wear.utils.Utils;


public class BaseActivity extends AppCompatActivity {

    public CommonDialog mAlertDialog = null;

    public CommonDialog showAlertPopup(String title, String desc, String ok, View.OnClickListener okListener, String cancel ) {
        mAlertDialog = Utils.showPopupDlg(this, title, desc,
                ok, okListener,
                cancel, null, null);
        return  mAlertDialog;
    }
}
