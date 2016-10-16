package nkarasch.repeatingreminder.gui;

import android.animation.Animator;
import android.animation.ArgbEvaluator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.LinearLayout;
import android.widget.TextView;

import nkarasch.repeatingreminder.R;

/**
 * Created by arsalan.chishti on 7/24/2016.
 * https://github.com/MuhammadArsalanChishti/Cuboid-Circle-Button
 *
 * then completely butchered and simplified by Nick Karasch 10/8/2016 to make it work as a simple toggle button
 */
public class CircleToggleButton extends TextView {

    private int mOffCircleColor, mOnCircleColor, mOffTextColor, mOnTextColor, mIntermediateColor;
    private int mCircleX, mCircleY;
    private Paint mCirclePaint;
    private boolean mAnimating;

    public CircleToggleButton(Context context) {
        super(context);
    }

    public CircleToggleButton(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(attrs);
    }

    public CircleToggleButton(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(attrs);
    }

    private void init(AttributeSet attrs) {

        mCirclePaint = new Paint();
        mCirclePaint.setAntiAlias(true);
        TypedArray properties = getContext().getTheme().obtainStyledAttributes(attrs, R.styleable.CircleToggleButton, 0, 0);
        try {
            mOffCircleColor = properties.getInt(R.styleable.CircleToggleButton_ctb_off_color, Color.BLACK);
            mOnCircleColor = properties.getInt(R.styleable.CircleToggleButton_ctb_on_color, Color.BLACK);
            mOnTextColor = properties.getInt(R.styleable.CircleToggleButton_ctb_on_text_color, Color.BLACK);
            mOffTextColor = properties.getInt(R.styleable.CircleToggleButton_ctb_off_text_color, Color.BLACK);
        } catch (Exception e) {
            Log.d("CircleToggleButton-init", e.toString());
        } finally {
            properties.recycle();
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        mCircleX = getWidth() / 2;
        mCircleY = getHeight() / 2;
        mCirclePaint.setStyle(Paint.Style.FILL);
        if (mAnimating) {
            mCirclePaint.setColor(mIntermediateColor);
        } else {
            mCirclePaint.setColor(isActivated() ? mOnCircleColor : mOffCircleColor);
            setTextColor(isActivated() ? mOnTextColor : mOffTextColor);
        }
        canvas.drawCircle(mCircleX, mCircleY, (((LinearLayout) getParent()).getWidth() / 14) - 4, mCirclePaint);

        setText(getText());
        setGravity(Gravity.CENTER);
        super.onDraw(canvas);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, widthMeasureSpec);
    }

    public void actionUp() {
        if (isActivated()) {
            setOn();
        } else {
            setOff();
        }
    }

    public void actionDown(MotionEvent event) {
        if (inCircle(event.getX(), event.getY(), mCircleX, mCircleY, getWidth() / 2)) {
            if (isActivated()) {
                setOff();
            } else {
                setOn();
            }
        }
    }

    public void actionCancel() {
        if (isActivated()) {
            setOn();
        } else {
            setOff();
        }
    }

    private void setOn() {
        setTextColor(mOnTextColor);
        setColorAnimation(mOffCircleColor, mOnCircleColor);
    }

    private void setOff() {
        setTextColor(mOffTextColor);
        setColorAnimation(mOnCircleColor, mOffCircleColor);
    }

    private boolean inCircle(float x, float y, float circleCenterX, float circleCenterY, float circleRadius) {
        double dx = Math.pow(x - circleCenterX, 2);
        double dy = Math.pow(y - circleCenterY, 2);

        return ((dx + dy) < Math.pow(circleRadius, 2));
    }

    private void setColorAnimation(int start, int end) {
        ValueAnimator animator = ValueAnimator.ofObject(new ArgbEvaluator(), start, end);
        animator.setDuration(500);
        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                mIntermediateColor = (int) animation.getAnimatedValue();
                invalidate();
            }
        });
        animator.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {
                mAnimating = true;
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                mAnimating = false;
            }

            @Override
            public void onAnimationCancel(Animator animation) {
                mAnimating = false;
            }

            @Override
            public void onAnimationRepeat(Animator animation) {
                mAnimating = true;
            }
        });
        animator.setInterpolator(new AccelerateDecelerateInterpolator());
        animator.start();
    }
}