package kr.co.theunify.wear.view;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.LightingColorFilter;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.RelativeLayout;

import kr.co.theunify.wear.R;


/**
 * 클릭 가능한 RelativeLayout 구성
 */
@SuppressLint("AppCompatCustomView")
public class ClickRelativeLayout extends RelativeLayout {

    private static String TAG = ClickRelativeLayout.class.getSimpleName();

    private boolean clickable = false;

    public ClickRelativeLayout(Context context) {
        super(context);
    }

    public ClickRelativeLayout(Context context, AttributeSet attrs) {
        super(context, attrs, R.attr.borderlessButtonStyle);
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.FontTextView);
        clickable = a.getBoolean(R.styleable.FontTextView_clickable, false);

        initView(context);
    }

    public ClickRelativeLayout(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.FontTextView);
        clickable = a.getBoolean(R.styleable.FontTextView_clickable, false);

        initView(context);
    }

    private void initView(Context context) {
        if (clickable) {
            setClickListener();
        }
    }

    /**
     * 클릭 리스너 등록 및 이벤트 받기
     */
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

    /**
     * 클릭 효과를 위한 필터 적용
     * @param v
     * @param filter
     */
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
