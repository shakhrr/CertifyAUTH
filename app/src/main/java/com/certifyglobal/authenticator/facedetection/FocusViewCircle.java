package com.certifyglobal.authenticator.facedetection;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.util.AttributeSet;
import android.view.View;

import com.certifyglobal.authenticator.R;
import com.certifyglobal.utils.Logger;

public class FocusViewCircle extends View {

    private Paint mCutPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private Bitmap mBitmap;
    private Canvas mInternalCanvas;

    public FocusViewCircle(Context context) {
        super(context);
        init();
    }

    public FocusViewCircle(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public FocusViewCircle(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    private void init() {
        mCutPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.CLEAR));
        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);

        if (mInternalCanvas != null) {
            mInternalCanvas.setBitmap(null);
            mInternalCanvas = null;
        }

        if (mBitmap != null) {
            mBitmap.recycle();
            mBitmap = null;
        }

        mBitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
        mInternalCanvas = new Canvas(mBitmap);
    }

    @SuppressLint("NewApi")
    @Override
    protected void onDraw(Canvas canvas) {
        if (mInternalCanvas == null || mBitmap == null) {
            return;
        }

        final int width = getWidth();
        final int height = getHeight();

        // make the radius as large as possible within the view bounds
        final int radius = Math.min(width, height) / 3;
        mInternalCanvas.drawColor(0x80FFFFFF);
        // mInternalCanvas.col
        mInternalCanvas.drawCircle(width / 2, height / 2, radius, mCutPaint);

        paint.setStyle(Paint.Style.STROKE);
        paint.setAntiAlias(true);
        paint.setStrokeJoin(Paint.Join.ROUND);
        paint.setStrokeCap(Paint.Cap.ROUND);
        paint.setStrokeWidth(20);
        canvas.drawPaint(paint);
        paint.setColor(getResources().getColor(R.color.green));
        mInternalCanvas.drawCircle(width / 2, height / 2, radius, paint);
        canvas.drawBitmap(mBitmap, 0, 0, null);
    }

}