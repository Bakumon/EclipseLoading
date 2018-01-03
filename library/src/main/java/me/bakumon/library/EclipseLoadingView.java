package me.bakumon.library;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;

/**
 * 日食加载动画
 *
 * @author Bakumon https://bakumon.me
 * @date 2018/01/03
 */

public class EclipseLoadingView extends View {
    /**
     * 左右日食过程
     */
    private static final int STATE_ECLIPSE = 0;
    /**
     * 转圈过程
     */
    private static final int STATE_ROTATE = 1;
    /**
     * view 默认宽高
     */
    private int defaultSize;
    /**
     * 宽高
     */
    private int size;
    /**
     * 中心点坐标，xy
     */
    private int center;
    /**
     * 太阳半径
     */
    private int radius;
    /**
     *
     */
    private int arcWidth;
    /**
     * 太阳颜色
     */
    private int colorSun;
    /**
     * 背景颜色
     */
    private int colorBackground;
    /**
     * @see EclipseLoadingView#STATE_ECLIPSE
     * @see EclipseLoadingView#STATE_ROTATE
     * 为了在 onDraw 中不绘制看不到的部分
     */
    private int status;

    private Paint paint;
    private Path path;
    private RectF rectF;

    private float eclipseX;
    private float eclipseSweepAngle;
    private AnimatorSet animatorSet;

    public EclipseLoadingView(Context context) {
        this(context, null);
    }

    public EclipseLoadingView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public EclipseLoadingView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        paint = new Paint(Paint.ANTI_ALIAS_FLAG);

        defaultSize = dp2px(60);
        arcWidth = dp2px(5);
        colorSun = Color.parseColor("#FDAC2A");
        colorBackground = Color.parseColor("#ffffff");
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        // 取宽高的最小值做整个 view 的边长
        int size = Math.min(measureSize(widthMeasureSpec), measureSize(heightMeasureSpec));
        setMeasuredDimension(size, size);
    }

    private int measureSize(int measureSpec) {
        int result;
        int specMode = MeasureSpec.getMode(measureSpec);
        int specSize = MeasureSpec.getSize(measureSpec);
        if (specMode == MeasureSpec.EXACTLY) {
            result = specSize;
        } else {
            result = defaultSize;
            if (specMode == MeasureSpec.AT_MOST) {
                result = Math.min(result, specSize);
            }
        }
        return result;
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        // 宽和高相等
        size = w;
        center = size / 2;
        int bottom = getPaddingBottom();
        int top = getPaddingTop();
        int right = getPaddingRight();
        int left = getPaddingLeft();
        // 最大内边距
        int maxPadding = Math.max(Math.max(Math.max(bottom, top), right), left);
        radius = size / 2 - maxPadding;
        rectF = new RectF(maxPadding, maxPadding, size - maxPadding, size - maxPadding);
        path = new Path();
        path.addCircle(size / 2, size / 2, size / 2, Path.Direction.CW);

        initAnimator();
    }

    private void initAnimator() {
        status = STATE_ECLIPSE;
        ValueAnimator animator1 = ValueAnimator.ofFloat(size / 2 + (radius) * 2, size / 2);
        animator1.setDuration(2000);
        animator1.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                setEclipseX((Float) animation.getAnimatedValue());
            }
        });
        animator1.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                status = STATE_ROTATE;
            }
        });

        ValueAnimator animator2 = ValueAnimator.ofFloat(0, -360);
        animator2.setDuration(400);
        animator2.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                setEclipseSweepAngle((Float) animation.getAnimatedValue());
            }
        });

        ValueAnimator animator3 = ValueAnimator.ofFloat(360, 0);
        animator3.setDuration(400);
        animator3.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                setEclipseSweepAngle((Float) animation.getAnimatedValue());
            }
        });
        animator3.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                status = STATE_ECLIPSE;
            }
        });

        ValueAnimator animator4 = ValueAnimator.ofFloat(size / 2, size / 2 - (radius) * 2);
        animator4.setDuration(2000);
        animator4.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                setEclipseX((Float) animation.getAnimatedValue());
            }
        });

        animatorSet = new AnimatorSet();
        animatorSet.playSequentially(animator1, animator2, animator3, animator4);
        animatorSet.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                animatorSet.start();
            }
        });
        animatorSet.start();
    }

    private void setEclipseX(float eclipseX) {
        this.eclipseX = eclipseX;
        invalidate();
    }

    private void setEclipseSweepAngle(float eclipseSweepAngle) {
        this.eclipseSweepAngle = eclipseSweepAngle;
        invalidate();
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        animatorSet.cancel();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        canvas.clipPath(path);
        if (status == 0) {
            // 太阳
            paint.setStyle(Paint.Style.FILL);
            paint.setStrokeWidth(0);
            paint.setColor(colorSun);
            canvas.drawCircle(center, center, radius, paint);
            // 月亮
            paint.setColor(colorBackground);
            canvas.drawCircle(eclipseX, center, radius, paint);
        } else if (status == 1) {
            // 圈圈
            paint.setStyle(Paint.Style.STROKE);
            paint.setStrokeWidth(arcWidth);
            paint.setColor(colorSun);
            canvas.drawArc(rectF, -90, eclipseSweepAngle, false, paint);
        }
    }

    public int dp2px(float dipValue) {
        float density = getContext().getResources().getDisplayMetrics().density;
        return (int) (dipValue * density + 0.5f);
    }
}
