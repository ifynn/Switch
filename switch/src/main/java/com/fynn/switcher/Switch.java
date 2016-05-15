package com.fynn.switcher;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ArgbEvaluator;
import android.animation.ValueAnimator;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.os.Build;
import android.support.annotation.ColorInt;
import android.util.AttributeSet;
import android.view.SoundEffectConstants;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.Checkable;

/**
 * Created by Fynn on 2016/5/13.
 */
public class Switch extends View implements Checkable {
    private static final int ANIMATION_DURATION = 300;

    private static final int DEFAULT_WIDTH = 50;
    private static final int DEFAULT_HEIGHT = DEFAULT_WIDTH / 2;
    private static final int DEFAULT_SPOT_PADDING = 2;

    private static final int DEFAULT_SWITCH_ON_COLOR = 0xFF66CD00;
    private static final int DEFAULT_SWITCH_OFF_COLOR = 0xFFE8E8E8;
    private static final int DEFAULT_SPOT_ON_COLOR = 0xFFFFFFFF;
    private static final int DEFAULT_SPOT_OFF_COLOR = 0xFFFFFFFF;

    private static final int SWITCH_OFF_POS = 0;
    private static final int SWITCH_ON_POS = 1;

    private int switchOnColor;
    private int switchOffColor;
    private int spotOnColor;
    private int spotOffColor;
    private int spotPadding;
    private float currentPos;
    private boolean mChecked;
    private boolean mBroadcasting;
    private boolean isMoving;
    private int duration;

    private OnCheckedChangeListener onCheckedChangeListener;

    private ValueAnimator valueAnimator;

    private enum State {
        SWITCH_ANIMATION_OFF, SWITCH_ANIMATION_ON, SWITCH_ON, SWITCH_OFF
    }

    private State state;

    public Switch(Context context) {
        super(context);
        switchOnColor = DEFAULT_SWITCH_ON_COLOR;
        switchOffColor = DEFAULT_SWITCH_OFF_COLOR;
        spotOnColor = DEFAULT_SPOT_ON_COLOR;
        spotOffColor = DEFAULT_SPOT_OFF_COLOR;
        spotPadding = dp2px(DEFAULT_SPOT_PADDING);
        duration = ANIMATION_DURATION;
        state = mChecked ? State.SWITCH_ON : State.SWITCH_OFF;

        setClickable(true);
    }

    public Switch(Context context, AttributeSet attrs) {
        super(context, attrs);
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.Switch);
        switchOnColor = a.getColor(R.styleable.Switch_switchOnColor, DEFAULT_SWITCH_ON_COLOR);
        switchOffColor = a.getColor(R.styleable.Switch_switchOffColor, DEFAULT_SWITCH_OFF_COLOR);
        spotOnColor = a.getColor(R.styleable.Switch_spotOnColor, DEFAULT_SPOT_ON_COLOR);
        spotOffColor = a.getColor(R.styleable.Switch_spotOffColor, DEFAULT_SPOT_OFF_COLOR);
        spotPadding = a.getDimensionPixelSize(R.styleable.Switch_spotPadding, dp2px(DEFAULT_SPOT_PADDING));
        duration = a.getInteger(R.styleable.Switch_duration, ANIMATION_DURATION);
        mChecked = a.getBoolean(R.styleable.Switch_checked, false);
        a.recycle();

        state = mChecked ? State.SWITCH_ON : State.SWITCH_OFF;
        setClickable(true);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int widthSpecSize = MeasureSpec.getSize(widthMeasureSpec);
        int heightSpecSize = MeasureSpec.getSize(heightMeasureSpec);
        int widthSpecMode = MeasureSpec.getMode(widthMeasureSpec);
        int heightSpecMode = MeasureSpec.getMode(heightMeasureSpec);

        int width = dp2px(DEFAULT_WIDTH) + getPaddingLeft() + getPaddingRight();
        int height = dp2px(DEFAULT_HEIGHT) + getPaddingTop() + getPaddingBottom();

        if (widthSpecMode != MeasureSpec.AT_MOST) {
            width = Math.max(width, widthSpecSize);
        }

        if (heightSpecMode != MeasureSpec.AT_MOST) {
            height = Math.max(height, heightSpecSize);
        }

        setMeasuredDimension(width, height);

    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        int w = getWidth();
        int h = getHeight();
        int pl = getPaddingLeft();
        int pt = getPaddingTop();
        int pr = getPaddingRight();
        int pb = getPaddingBottom();
        int wp = w - pl - pr;
        int hp = h - pt - pb;
        int sw = dp2px(DEFAULT_WIDTH);
        int sh = dp2px(DEFAULT_HEIGHT);

        int dx = pl + (wp - sw) / 2;
        int dy = pt + (hp - sh) / 2;
        canvas.translate(dx, dy);

        switch (state) {
            case SWITCH_ON:
                drawSwitchOn(canvas);
                break;
            case SWITCH_OFF:
                drawSwitchOff(canvas);
                break;
            case SWITCH_ANIMATION_ON:
                drawSwitchOnAnim(canvas);
                break;
            case SWITCH_ANIMATION_OFF:
                drawSwitchOffAnim(canvas);
                break;

        }
    }

    private void drawSwitchOn(Canvas canvas) {
        float[] rectAttrs = compRoundRectAttr(SWITCH_OFF_POS);
        drawRoundRect(canvas, switchOnColor, rectAttrs);

        float[] ovalAttrs = compOvalAttr(SWITCH_ON_POS);
        drawOval(canvas, spotOnColor, ovalAttrs);
    }

    private void drawSwitchOff(Canvas canvas) {
        float[] rectAttrs = compRoundRectAttr(SWITCH_OFF_POS);
        drawRoundRect(canvas, switchOffColor, rectAttrs);

        float[] ovalAttrs = compOvalAttr(SWITCH_OFF_POS);
        drawOval(canvas, spotOffColor, ovalAttrs);
    }

    private void drawSwitchOnAnim(Canvas canvas) {
        float[] rectAttrs = compRoundRectAttr(SWITCH_OFF_POS);
        drawRoundRect(canvas, switchOnColor, rectAttrs);

        rectAttrs = compRoundRectAttr(currentPos);
        drawRoundRect(canvas, switchOffColor, rectAttrs);

        float[] ovalAttrs = compOvalAttr(currentPos);
        int color = compSpotColor(currentPos);
        drawOval(canvas, color, ovalAttrs);
    }

    private void drawSwitchOffAnim(Canvas canvas) {
        float[] rectAttrs = compRoundRectAttr(SWITCH_OFF_POS);
        drawRoundRect(canvas, switchOnColor, rectAttrs);

        rectAttrs = compRoundRectAttr(1 - currentPos);
        drawRoundRect(canvas, switchOffColor, rectAttrs);

        float[] ovalAttrs = compOvalAttr(1 - currentPos);
        int color = compSpotColor(1 - currentPos);
        drawOval(canvas, color, ovalAttrs);
    }

    private void drawRoundRect(Canvas canvas, int color, float[] attrs) {
        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setStyle(Paint.Style.FILL);
        paint.setStrokeCap(Paint.Cap.ROUND);
        RectF rectF = new RectF();
        paint.setColor(color);
        rectF.set(attrs[0], attrs[1], attrs[2], attrs[3]);
        canvas.drawRoundRect(rectF, attrs[4], attrs[4], paint);
    }

    private void drawOval(Canvas canvas, int color, float[] attrs) {
        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(color);
        RectF rectF = new RectF(attrs[0], attrs[1], attrs[2], attrs[3]);
        canvas.drawOval(rectF, paint);
    }

    private float[] compRoundRectAttr(float pos) {
        int sw = dp2px(DEFAULT_WIDTH);
        int sh = dp2px(DEFAULT_HEIGHT);

        float left = sw * pos;
        float right = sw - left;
        float top = sh * pos;
        float bottom = sh - top;
        float radius = (bottom - top) * 0.5f;

        return new float[]{left, top, right, bottom, radius};
    }

    private float[] compOvalAttr(float pos) {
        int sw = dp2px(DEFAULT_WIDTH);
        int sh = dp2px(DEFAULT_HEIGHT);
        int oh = sh - 2 * spotPadding;

        float left = spotPadding + (sw - sh) * pos;
        float right = left + oh;
        float top = spotPadding;
        float bottom = oh + top;

        return new float[]{left, top, right, bottom};
    }

    private int compSpotColor(float fraction) {
        return compColor(fraction, spotOffColor, spotOnColor);
    }

    private int compColor(float fraction, int startColor, int endColor) {
        return (Integer) new ArgbEvaluator().evaluate(fraction, startColor, endColor);
    }

    @Override
    public boolean performClick() {
        toggle();

        final boolean handled = super.performClick();
        if (!handled) {
            // View only makes a sound effect if the onClickListener was
            // called, so we'll need to make one here instead.
            playSoundEffect(SoundEffectConstants.CLICK);
        }

        return handled;
    }

    public int dp2px(float dpValue) {
        float scale = getResources().getDisplayMetrics().density;
        return (int) (dpValue * scale + 0.5f);
    }

    @TargetApi(Build.VERSION_CODES.KITKAT)
    @Override
    public void setChecked(boolean checked) {
        if (isMoving) {
            return;
        }

        if (mChecked != checked) {
            mChecked = checked;

            // Avoid infinite recursions if setChecked() is called from a listener
            if (mBroadcasting) {
                return;
            }

            mBroadcasting = true;
            if (onCheckedChangeListener != null) {
                onCheckedChangeListener.onCheckedChanged(this, mChecked);
            }
            mBroadcasting = false;

            if (mChecked) {
                state = State.SWITCH_ANIMATION_ON;
            } else {
                state = State.SWITCH_ANIMATION_OFF;
            }

            if (isAttachedToWindow() && isLaidOut()) {
                animateToCheckedState();
            } else {
                // Immediately move the thumb to the new position.
                cancelPositionAnimator();
                currentPos = 0;
            }
        }
    }

    private void cancelPositionAnimator() {
        if (valueAnimator != null) {
            valueAnimator.cancel();
        }
    }

    private void animateToCheckedState() {
        valueAnimator = ValueAnimator.ofFloat(0, 1);
        valueAnimator.setDuration(duration);
        valueAnimator.setInterpolator(new AccelerateDecelerateInterpolator());
        valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                currentPos = (float) animation.getAnimatedValue();
                invalidate();
            }
        });

        valueAnimator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationStart(Animator animation) {
                super.onAnimationStart(animation);
                isMoving = true;
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                isMoving = false;
            }
        });

        if (!valueAnimator.isRunning()) {
            valueAnimator.start();
            currentPos = 0;
        }
    }

    public int getDuration() {
        return duration;
    }

    public void setDuration(int duration) {
        this.duration = duration;
    }

    @Override
    public boolean isChecked() {
        return mChecked;
    }

    @Override
    public void toggle() {
        setChecked(!mChecked);
    }

    public int getSwitchOnColor() {
        return switchOnColor;
    }

    public void setSwitchOnColor(@ColorInt int switchOnColor) {
        this.switchOnColor = switchOnColor;
        invalidate();
    }

    public int getSwitchOffColor() {
        return switchOffColor;
    }

    public void setSwitchOffColor(@ColorInt int switchOffColor) {
        this.switchOffColor = switchOffColor;
        invalidate();
    }

    public int getSpotOnColor() {
        return spotOnColor;
    }

    public void setSpotOnColor(@ColorInt int spotOnColor) {
        this.spotOnColor = spotOnColor;
        invalidate();
    }

    public int getSpotOffColor() {
        return spotOffColor;
    }

    public void setSpotOffColor(@ColorInt int spotOffColor) {
        this.spotOffColor = spotOffColor;
        invalidate();
    }

    public int getSpotPadding() {
        return spotPadding;
    }

    public void setSpotPadding(int spotPadding) {
        this.spotPadding = spotPadding;
        invalidate();
    }

    public OnCheckedChangeListener getOnCheckedChangeListener() {
        return onCheckedChangeListener;
    }

    public void setOnCheckedChangeListener(OnCheckedChangeListener onCheckedChangeListener) {
        this.onCheckedChangeListener = onCheckedChangeListener;
    }

    public interface OnCheckedChangeListener {
        /**
         * Called when the checked state of a switch has changed.
         *
         * @param s  The switch whose state has changed.
         * @param isChecked The new checked state of switch.
         */
        void onCheckedChanged(Switch s, boolean isChecked);
    }
}
