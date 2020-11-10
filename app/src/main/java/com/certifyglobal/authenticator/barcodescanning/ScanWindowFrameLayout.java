

package com.certifyglobal.authenticator.barcodescanning;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.FrameLayout;

public class ScanWindowFrameLayout extends FrameLayout {
    public ScanWindowFrameLayout(Context context) {
        super(context);
    }

    public ScanWindowFrameLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public ScanWindowFrameLayout(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        // Ensure that this view is always a square.
        if (widthMeasureSpec > heightMeasureSpec)
            widthMeasureSpec = heightMeasureSpec;
        else
            heightMeasureSpec = widthMeasureSpec;

        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }
}
