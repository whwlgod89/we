package kr.co.theunify.wear.dialog;

import android.app.Dialog;
import android.content.Context;
import android.text.TextUtils;
import android.view.View;
import android.view.Window;
import android.widget.TextView;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import kr.co.theunify.wear.R;

/**
 * 공용 다이얼로그
 */
public class CommonDialog extends Dialog {

    private String TAG = CommonDialog.class.getSimpleName();

    //********************************************************************************
    //  Layout Member Variable
    //********************************************************************************
    @BindView(R.id.txt_title)       TextView txt_title;
    @BindView(R.id.txt_msg)         TextView txt_msg;
    @BindView(R.id.txt_ok)          TextView txt_ok;
    @BindView(R.id.txt_cancel)      TextView txt_cancel;

    //********************************************************************************
    //  Member Variable
    //********************************************************************************
    private Context mContext;

    private View.OnClickListener okListener;
    private View.OnClickListener cancelListener;

    //********************************************************************************
    //  Construction Functions
    //********************************************************************************

    public CommonDialog(Context context) {
        super(context);
        mContext = context;
        requestWindowFeature( Window.FEATURE_NO_TITLE );
        getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        setCanceledOnTouchOutside(false);
        setCancelable(false);

        setContentView(R.layout.d_common);
        ButterKnife.bind(this);

        show();
    }

    //********************************************************************************
    //  Override Functions
    //********************************************************************************

    /**
     * 닫기버튼
     */
    @OnClick(R.id.txt_ok)
    public void onClickClose(View v) {
        dismiss();
        if (okListener != null) {
            okListener.onClick(v);
        }
    }

    @OnClick(R.id.txt_cancel)
    public void onClickCancel(View v) {
        dismiss();
        if (cancelListener != null) {
            cancelListener.onClick(v);
        }
    }


    public void setTitle(String s) {
        txt_title.setText(s);
    }

    public void setMsg(String s) {
        txt_msg.setText(s);
    }

    public void setOk(String s) {
        if (TextUtils.isEmpty(s)) {
            txt_ok.setVisibility(View.GONE);
        } else {
            txt_ok.setText(s);
        }
    }

    public void setCancel(String s) {
        if (TextUtils.isEmpty(s)) {
            txt_cancel.setVisibility(View.GONE);
        } else {
            txt_cancel.setText(s);
        }
    }

    public void setOkListener(View.OnClickListener listener) {
        okListener = listener;
    }

    public void setCancelListener(View.OnClickListener listener) {
        cancelListener = listener;
    }

    public void setMsgColor(Integer descColor) {
        txt_msg.setTextColor(descColor);
    }

    //********************************************************************************
    //  Network Function
    //********************************************************************************


    //********************************************************************************
    //  Network Response
    //********************************************************************************

}
