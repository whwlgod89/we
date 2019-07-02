package kr.co.theunify.wear.dialog;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.View;

import butterknife.ButterKnife;
import butterknife.OnClick;
import kr.co.theunify.wear.R;

/**
 * Wear 도움말 팝업
 */

public class HelpDialog extends Dialog {
    private String TAG = HelpDialog.class.getSimpleName();


    private Context mContext;


    public HelpDialog(@NonNull Context context) {
        super(context);
        mContext = context;
        initView();
    }

    public HelpDialog(@NonNull Context context, int themeResId) {
        super(context, themeResId);
        mContext = context;
        initView();
    }

    protected HelpDialog(@NonNull Context context, boolean cancelable, @Nullable OnCancelListener cancelListener) {
        super(context, cancelable, cancelListener);
        mContext = context;
        initView();
    }

    @OnClick(R.id.txt_ok)
    public void onClickClose(View v) {
        dismiss();
    }

    /**
     * 화면 초기화
     */
    private void initView() {
        getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        setCanceledOnTouchOutside(true);

        setContentView(R.layout.d_help);
        ButterKnife.bind(this);

        show();
    }


}
