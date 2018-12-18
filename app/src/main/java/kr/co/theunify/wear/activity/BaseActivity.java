package kr.co.theunify.wear.activity;

import android.app.Activity;
import android.view.View;

import kr.co.theunify.wear.dialog.CommonDialog;
import kr.co.theunify.wear.utils.Utils;


public class BaseActivity extends Activity {

    public CommonDialog mAlertDialog = null;

    public CommonDialog showAlertPopup(String title, String desc, String ok, View.OnClickListener okListener, String cancel ) {
        mAlertDialog = Utils.showPopupDlg(this, title, desc,
                ok, okListener,
                cancel, null, null);
        return  mAlertDialog;
    }
}
