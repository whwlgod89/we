package kr.co.theunify.wear.view;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.LightingColorFilter;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.text.SpannableString;
import android.text.style.UnderlineSpan;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.TextView;

import kr.co.theunify.wear.R;


/**
 * 폰트 적용 텍스트 뷰 클래스
 */
@SuppressLint("AppCompatCustomView")
public class FontTextView extends TextView {

    private static String TAG = FontTextView.class.getSimpleName();

    private int fonttype = 1;
    private boolean underline = false;
    private boolean clickable = false;

    public FontTextView(Context context, int fonttype, boolean underLine) {
        super(context);
        this.fonttype = fonttype;
        this.underline = underLine;

        initView(context);
    }

    public FontTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.FontTextView);
        fonttype = a.getInt(R.styleable.FontTextView_fonttype, 0);
        underline = a.getBoolean(R.styleable.FontTextView_underline, false);
        clickable = a.getBoolean(R.styleable.FontTextView_clickable, false);

        initView(context);
    }

    private void initView(Context context) {
        setFont(context, fonttype);
        setIncludeFontPadding(false);

        if(underline){
            SpannableString content = new SpannableString(super.getText());
            content.setSpan(new UnderlineSpan(), 0, content.length(), 0);
            setText(content);
        }

        if (clickable) {
            setClickListener();
        }
    }

    @Override
    public void setTypeface(Typeface tf) {
        super.setTypeface(tf);
    }

    public void setFont(Context context, int fonttype) {
        Typeface tf = null;
        tf = Typeface.create(getTypeface(), fonttype==1 ? Typeface.BOLD : Typeface.NORMAL);

        if (tf != null) {
            setTypeface(tf);
        }

        setLineSpacing(0.0f, 1.2f);
    }

    private void setClickListener() {

        this.setOnTouchListener(new OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        // 0x6D6D6D sets how much to darken - tweak as desired
                        setColorFilter(v, 0xD6D6D6);
                        break;
                    // remove the filter when moving off the button
                    // the same way a selector implementation would
                    case MotionEvent.ACTION_MOVE:
                        Rect r = new Rect();
                        v.getLocalVisibleRect(r);
                        if (!r.contains((int) event.getX(), (int) event.getY())) {
                            setColorFilter(v, null);
                        }
                        break;
                    case MotionEvent.ACTION_OUTSIDE:
                    case MotionEvent.ACTION_CANCEL:
                    case MotionEvent.ACTION_UP:
                        setColorFilter(v, null);
                        break;
                }
                return false;
            }
        });

    }

    private void setColorFilter(View v, Integer filter) {
        if (filter == null) {
            v.getBackground().clearColorFilter();
        }  else {
            // To lighten instead of darken, try this:
            // LightingColorFilter lighten = new LightingColorFilter(0xFFFFFF, filter);
            LightingColorFilter darken = new LightingColorFilter(filter, 0x000000);
            v.getBackground().setColorFilter(darken);
        }
        // required on Android 2.3.7 for filter change to take effect (but not on 4.0.4)
        v.getBackground().invalidateSelf();
    }
}
