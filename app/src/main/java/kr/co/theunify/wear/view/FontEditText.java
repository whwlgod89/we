package kr.co.theunify.wear.view;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.widget.EditText;

import kr.co.theunify.wear.R;


/**
 * 폰트 적용 EditText 클래스 - 공용
 */
@SuppressLint("AppCompatCustomView")
public class FontEditText extends EditText {

    private static String TAG = FontEditText.class.getSimpleName();

    private int fonttype = 1;

    public FontEditText(Context context) {
        super(context);
    }

    public FontEditText(Context context, AttributeSet attrs) {
        super(context, attrs);
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.FontTextView);
        fonttype = a.getInt(R.styleable.FontTextView_fonttype, 0);

        setFont(context, fonttype);
    }

    @Override
    public void setTypeface(Typeface tf) {
        super.setTypeface(tf);
    }

    private void setFont(Context context, int fonttype) {
        Typeface tf = null;
        tf = Typeface.create(getTypeface(), fonttype==1 ? Typeface.BOLD : Typeface.NORMAL);

        if (tf != null) {
            setTypeface(tf);
        }

        setLineSpacing(0.0f, 1.2f);
    }

}
