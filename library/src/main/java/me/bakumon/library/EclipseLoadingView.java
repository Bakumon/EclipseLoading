package me.bakumon.library;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.TypedArray;
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
     * 恢复过程
     */
    private static final int STATE_RECOVER = 2;
    /**
     * view 默认宽高
     */
    private int defaultSize;
    /**
     * 中心点坐标，xy
     */
    private int center;
    /**
     * 太阳半径
     */
    private int radius;
    /**
     * 圈圈线条的宽度
     */
    private int arcWidth;
    /**
     * 太阳颜色
     */
    private int mSunColor;
    /**
     * @see EclipseLoadingView#STATE_ECLIPSE
     * @see EclipseLoadingView#STATE_ROTATE
     * @see EclipseLoadingView#STATE_RECOVER
     * 为了在 onDraw 中不绘制看不到的部分
     */
    private int status;

    private Paint paint;
    /**
     * 裁剪绘画区域
     */
    private Path canvasClipPath;
    /**
     * 日食过程移动的 path
     */
    private Path eclipseClipPath;
    /**
     * 恢复过程移动的 path
     */
    private Path recoverClipPath;
    /**
     * 圈圈过程圆弧相关
     */
    private RectF rectF;
    /**
     * 日食和恢复过程 path 移动的 offset
     */
    private float eclipseOffsetX;
    /**
     * 圈圈过程圆弧显示的角度
     */
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
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.EclipseLoadingView);
        mSunColor = a.getColor(R.styleable.EclipseLoadingView_sunColor, Color.parseColor("#FDAC2A"));
        a.recycle();
        init();
    }

    private void init() {
        paint = new Paint(Paint.ANTI_ALIAS_FLAG);

        defaultSize = dp2px(60);
        arcWidth = dp2px(2);
        paint.setStrokeWidth(arcWidth);
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
        center = w / 2;
        int bottom = getPaddingBottom();
        int top = getPaddingTop();
        int right = getPaddingRight();
        int left = getPaddingLeft();
        // 最大内边距
        int maxPadding = Math.max(Math.max(Math.max(bottom, top), right), left);
        radius = w / 2 - maxPadding;
        int rectFLeftTop = maxPadding + arcWidth;
        int rectFRightBottom = w - maxPadding - arcWidth;
        rectF = new RectF(rectFLeftTop, rectFLeftTop, rectFRightBottom, rectFRightBottom);
        canvasClipPath = new Path();
        canvasClipPath.addCircle(center, center, center, Path.Direction.CW);

        eclipseClipPath = new Path();
        eclipseClipPath.setFillType(Path.FillType.INVERSE_WINDING);
        eclipseClipPath.addCircle(center + radius * 2, center, radius, Path.Direction.CW);

        recoverClipPath = new Path();
        recoverClipPath.setFillType(Path.FillType.INVERSE_WINDING);
        recoverClipPath.addCircle(center, center, radius, Path.Direction.CW);

        initAnimator();
    }

    private void initAnimator() {
        // animator1 日食过程
        ValueAnimator animator1 = ValueAnimator.ofFloat(0, -((radius) * 2));
        animator1.setDuration(2000);
        animator1.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationStart(Animator animation) {
                status = STATE_ECLIPSE;
            }
        });
        animator1.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                setEclipseOffsetX((Float) animation.getAnimatedValue());
            }
        });

        // animator2 animator3 转圈过程
        ValueAnimator animator2 = ValueAnimator.ofFloat(0, -360);
        animator2.setDuration(400);
        animator2.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationStart(Animator animation) {
                status = STATE_ROTATE;
            }
        });
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

        // animator4 恢复过程
        ValueAnimator animator4 = ValueAnimator.ofFloat(0, -((radius) * 2));
        animator4.setDuration(2000);
        animator4.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationStart(Animator animation) {
                status = STATE_RECOVER;
            }
        });
        animator4.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                setEclipseOffsetX((Float) animation.getAnimatedValue());
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

    private void setEclipseOffsetX(float eclipseOffsetX) {
        this.eclipseOffsetX = eclipseOffsetX;
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
        canvas.clipPath(canvasClipPath);
        if (status == STATE_ROTATE) {
            // 圈圈
            drawRotate(canvas);
        } else {
            if (status == STATE_ECLIPSE) {
                // 日食过程
                drawEclipse(canvas, eclipseClipPath);
            } else {
                // 恢复过程
                drawEclipse(canvas, recoverClipPath);
            }
        }
    }

    /**
     * 画圈圈
     */
    private void drawRotate(Canvas canvas) {
        paint.setStyle(Paint.Style.STROKE);
        paint.setColor(mSunColor);
        canvas.drawArc(rectF, -90, eclipseSweepAngle, false, paint);
    }

    /**
     * 日食或恢复过程
     *
     * @param eclipsePath eclipseClipPath：日食过程 recoverClipPath：恢复过程
     */
    private void drawEclipse(Canvas canvas, Path eclipsePath) {
        canvas.save();
        eclipsePath.offset(eclipseOffsetX, 0);
        canvas.clipPath(eclipsePath);
        eclipsePath.offset(-eclipseOffsetX, 0);
        // 太阳
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(mSunColor);
        canvas.drawCircle(center, center, radius, paint);
        canvas.restore();
    }

    public int getSunColor() {
        return mSunColor;
    }

    public void setSunColor(int sunColor) {
        this.mSunColor = sunColor;
    }

    private int dp2px(float dipValue) {
        float density = getContext().getResources().getDisplayMetrics().density;
        return (int) (dipValue * density + 0.5f);
    }
}
