package com.zwsb.palmsdk.customViews;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;

import com.zwsb.palmsdk.R;
import com.zwsb.palmsdk.helpers.BaseUtil;



public class CircleAnimationView extends View
{
	private int circleSuccessColor;
	private int circleFailureColor;
	private int otherColor;

	private Paint     paint;

	private Animation animation;

	private boolean isSuccess    = true;
	private boolean isOtherColor = false;

	public CircleAnimationView(Context context)
	{
		super(context);
		init();
	}

	public CircleAnimationView(Context context, AttributeSet attrs)
	{
		super(context, attrs);
		init();
	}

	public CircleAnimationView(Context context, AttributeSet attrs, int defStyleAttr)
	{
		super(context, attrs, defStyleAttr);
		init();
	}

	private void init() {
		circleSuccessColor = getResources().getColor(R.color.circleSuccessColor);
		circleFailureColor = getResources().getColor(R.color.circleFailureColor);
		paint = new Paint();
		paint.setStyle(Paint.Style.FILL);
		paint.setAntiAlias(true);

		animation = AnimationUtils.loadAnimation(getContext(), R.anim.status_circle_animation);
		animation.setAnimationListener(new Animation.AnimationListener()
		{
			@Override
			public void onAnimationStart(Animation animation) {
				setVisibility(VISIBLE);
			}

			@Override
			public void onAnimationEnd(Animation animation) {
				setVisibility(GONE);
			}

			@Override
			public void onAnimationRepeat(Animation animation) {}
		});
		animation.setFillAfter(true);
	}

	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);

		float scanR = (getWidth() * 4 / 9);
		scanR = (scanR - (scanR * BaseUtil.decreaseCoefficient));

		float scanX = getWidth() / 2;
		float scanY = getHeight() / 2;

		if (isOtherColor) {
			paint.setColor(otherColor);
		} else {
			paint.setColor(isSuccess ? circleSuccessColor : circleFailureColor);
		}
		canvas.drawCircle(scanX, scanY, scanR, paint);
	}

	public void setSuccess(boolean success) {
		isOtherColor = false;
		isSuccess = success;
		setVisibility(VISIBLE);
		invalidate();
	}

	public void setFistStepColor() {
		isOtherColor = true;
		otherColor = getResources().getColor(R.color.fistColor);
		setVisibility(VISIBLE);
		invalidate();
	}
}
