package com.hfad.guitarassistant.components;

import android.animation.ValueAnimator;


import android.media.SoundPool;
import android.view.View;
import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Color;
import android.graphics.RectF;
import android.graphics.Typeface;

import com.hfad.guitarassistant.R;


public class DialView extends View {

    Context context;

    /*
     * 设置静态绘图参数
     */
    static final double POSITION_VERTICAL = 0.22;   // 视图中心纵向位置，数值越小，越靠下
    static final int MARGIN_AS_RECT = 100;          // 表盘作为矩形时的左右边距
    static final int START_ANGLE = -25;             // 开始角度
    static final int END_ANGLE = -155;              // 结束角度
    static final int EXT_ARC_COLOR = Color.GRAY;    // 外弧颜色
    static final int EXT_ARC_TARGET = 0xff00ff99;
    static final int EXT_ARC_COLOR_START = 0xff9FB6CD;
    static final int SCALE_TEXT_COLOR = 0Xff8B864E;
    static final int INR_ARC_COLOR = Color.GRAY;    // 内弧颜色
    static final int INR_ARC_COLOR_START = 0XFFCD6839;
    static final int INNER_ARC_VARIABLE = 10;       // 内弧弧度变化
    static final int POINTER_COLOR = Color.RED;     // 指针颜色
    static final int POINTER_OUT_DURATION = 700;    // 指针伸展动画持续时间
    static final int POINTER_ROTATE_DURATION = 500; // 每次旋转指针耗时
    static final int DOWN_OUT_DURATION = 600;       // 底部文字背景伸展持续时间
    static final int START_ACTION = 0;              // 标志开启指针动作
    static final int ROTATE_ACTION = 1;             // 标志旋转指针动作
    static final int STOP_ACTION = 2;               // 标志收回指针动作
    static final int UP_TEXT_COLOR = Color.rgb(0x61, 0x61, 0x61);
    static final String UP_TEXT_SUFFIX = " Hz";     // 上部文字后缀
    static final int DOWN_BG_HELP_MODE = Color.rgb(0xB2, 0xDF, 0xEE);
    static final int DOWN_BG_FREE_MODE = Color.rgb(0xB4, 0xCD, 0xCD);
    static final int DOWN_TEXT_HELP_MODE = Color.rgb(0x12, 0x96, 0xdb);
    static final int DOWN_TEXT_FREE_MODE = Color.rgb(0x51, 0x51, 0x51);
    static final int MAX_TARGET_ANGLE = 5;
    static final int MIN_TARGET_ANGLE = -5;

    // 基于以上参数的派生参数
    static final int MAX_ANGLE = START_ANGLE - 15;  // 刻度能够指向的最大角度
    static final int MIN_ANGLE = END_ANGLE + 15;    // 刻度能够指向的最小角度
    static final int ROTATE_MAX_ANGLE = (MAX_ANGLE - MIN_ANGLE) / 2;    // 表指针能够旋转的最大角度
    static final int ROTATE_MIN_ANGLE = -(MAX_ANGLE - MIN_ANGLE) / 2;   // 表指针能够指向的最小角度

    /*
     * 动态绘图参数
     */
    private int curState = STOP_ACTION;  // 标志当前表盘指针处于的状态或将要执行的动作
    private int pointerAngle;            // 标志当前表盘指针的角度
    private float rotateAngel;           // 标志每次刷新需要旋转的角度
    private float pointLength;           // 标志每次刷新的指针长度

    Canvas canvas;                       // 唯一的画布
    int r;                               // 外弧表盘半径
    private SoundPool tipSounder;        // 音高匹配时的提示音效
    private int soundId;
    private String upText;
    private String downText;
    private boolean isFreeMode = true;
    private float downBgWidth;
    private float finalDownWidth;
    // 颜色变化暂时量
    private int extArcColor = EXT_ARC_COLOR;        // 外弧
    private int inrArcColor = INR_ARC_COLOR;        // 内弧和基座
    private int curArcColor = EXT_ARC_COLOR;
    /*
     * 创建绘图对象
     */
    private final Paint paint = new Paint();
    private final Paint textPaint = new Paint();
    private final RectF arcRectF = new RectF();         // 外弧
    private final RectF innerArcRectF = new RectF();    // 内弧
    ValueAnimator pointerAnimator = new ValueAnimator();
    ValueAnimator rotateAnimator = new ValueAnimator();
    ValueAnimator downWidthAnimator = new ValueAnimator();


    public DialView(Context context) {
        super(context);
        init();
        this.context=context;
    }

    public DialView(Context context, AttributeSet attrs) {
        super(context,attrs);
        init();
        this.context=context;
    }

    public DialView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
        this.context=context;
    }

    // 初始化动态绘图参数
    private void init() {

        pointerAnimator.setDuration(POINTER_OUT_DURATION);
        rotateAnimator.setDuration(POINTER_ROTATE_DURATION);
        downWidthAnimator.setDuration(DOWN_OUT_DURATION);

        Log.i("note", "init");
        // 设置valuePointer触发器
        pointerAnimator.addUpdateListener(
                valueAnimator -> {
                    pointLength = (float) valueAnimator.getAnimatedValue();
                    extArcColor = blendColors(EXT_ARC_COLOR_START, curArcColor,  pointLength);
                    inrArcColor = blendColors(INR_ARC_COLOR_START, INR_ARC_COLOR,  pointLength);
                    invalidate();
                }
        );

        rotateAnimator.addUpdateListener(
                valueAnimator -> {
                    rotateAngel = (float) valueAnimator.getAnimatedValue();
                    invalidate();
                }
        );

        downWidthAnimator.addUpdateListener(
                valueAnimator ->
                        downBgWidth = (float) valueAnimator.getAnimatedValue()

        );
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        this.canvas = canvas;

        // 获取空间长和宽
        int w = getMeasuredWidth();     // 1080
        int h = getMeasuredHeight();    // 1810
        // 设置画布中心
        int x0 = w / 2;
        int y0 = (int)(h * (1 - POSITION_VERTICAL));
        canvas.translate(x0, y0);
        // 计算半径
        r = w / 2 - MARGIN_AS_RECT;
        finalDownWidth = (float) (r / 4.5);
        // 设置画笔
        paint.setColor(extArcColor);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(20);
        paint.setAntiAlias(true);
        // 绘制外弧
        arcRectF.set(-r, -r, r, r);

        /*
         * 这里要注意，正的弧度值代表顺时针🔃，和数学的角坐标系不同，
         * 所以设置弧度值为负数
         */
        canvas.drawArc(arcRectF, START_ANGLE, END_ANGLE - START_ANGLE, false, paint);
        // 外弧端点
        int x_left = (int)(r * Math.cos(END_ANGLE * Math.PI / 180));
        int y_left = (int)(r * Math.sin(END_ANGLE * Math.PI / 180));
        int x_right = (int)(r * Math.cos(START_ANGLE * Math.PI / 180));
        int y_right = (int)(r * Math.sin(START_ANGLE * Math.PI / 180));
        paint.setStrokeWidth(24);
        canvas.drawPoint(x_left, y_left, paint);
        canvas.drawPoint(x_right, y_right, paint);
        paint.setStrokeWidth(20);
        // 绘制内弧
        paint.setStrokeWidth(8);
        paint.setColor(inrArcColor);
        float innerR = (float) r / 7;
        innerArcRectF.set(-innerR, -innerR, innerR, innerR);
        canvas.drawArc(innerArcRectF, START_ANGLE + INNER_ARC_VARIABLE,
                END_ANGLE - START_ANGLE - 2*INNER_ARC_VARIABLE, false, paint);
        paint.setColor(extArcColor);
        // 设置刻度文本画笔参数
        textPaint.setColor(SCALE_TEXT_COLOR);
        textPaint.setAntiAlias(true);
        textPaint.setStrokeWidth(8);
        textPaint.setTextSize(35);
        textPaint.setTextAlign(Paint.Align.CENTER);
        textPaint.setTypeface(Typeface.create("serif", Typeface.BOLD));

        // 绘制刻度
        for(int start=MAX_ANGLE; start>=MIN_ANGLE; start-=5) {
            // 角度转换为弧度
            double startRadian = start * Math.PI / 180;
            // 计算刻度外端
            int x1 = (int)(r * Math.cos(startRadian));
            int y1 = (int)(r * Math.sin(startRadian));
            // 计算刻度内端
            int x2 = (int)(r * 0.92 * Math.cos(startRadian));
            int y2 = (int)(r * 0.92 * Math.sin(startRadian));
            // 计算小刻度内端
            int x4 = (int)(r * 0.97 * Math.cos(startRadian));
            int y4 = (int)(r * 0.97 * Math.sin(startRadian));
            // 计算刻度值文本位置
            int x3 = (int)(r * 0.83 * Math.cos(startRadian));
            int y3 = (int)(r * 0.83 * Math.sin(startRadian));
            // 计算刻度值
            int scaleValue = start + 90;
            // 开始绘制
            if(-start % 20 == 0) {
                canvas.drawLine(x1, y1, x2, y2, paint);
                canvas.drawText(scaleValue + "", x3, y3, textPaint);
            } else {
                canvas.drawLine(x1, y1, x4, y4, paint);
            }
        }
        // 绘制底部文字背景
        innerArcRectF.set(-downBgWidth, innerR / 2,
                downBgWidth, (float) 2.5 * innerR);
        paint.setStyle(Paint.Style.FILL);

        if(isFreeMode) {
            paint.setColor(DOWN_BG_FREE_MODE);
        } else {
            paint.setColor(DOWN_BG_HELP_MODE);
        }
        canvas.drawRoundRect(innerArcRectF, innerR, innerR, paint);

        // 绘制文字
        drawText();

        // 调整方向准备画指针
        canvas.rotate(rotateAngel);
        // 绘制指针底基
        drawPointerBase();
        // 伸展指针
        drawPointer();
    }

    // 绘制指针基座
    private void drawPointerBase() {
        // 计算端点位置
        int x1 = (int)(r / 7 * Math.cos(-110* Math.PI / 180));
        int y1 = -r / 5;
        int x2 = (int)(r / 7 * Math.cos(-70 * Math.PI / 180));
        int y2 = -r / 7;
        paint.setColor(inrArcColor);
        paint.setStyle(Paint.Style.FILL);
        arcRectF.set(x1, y1, x2, y2);
        canvas.drawArc(arcRectF, 20, -220, false, paint);
    }

    // 根据pointLength绘制指针
    private void drawPointer() {
        // 计算端点位置
        int x1 = 0;
        int y1 = -r / 5;
        int x2 = 0;
        int y2 = -(int)(r / 5 + r * 0.65 * pointLength);
        paint.setColor(POINTER_COLOR);
        paint.setStrokeWidth(5);
        canvas.drawLine(x1, y1, x2, y2, paint);
    }

    private void drawText() {
        if(upText != null) {
            textPaint.setColor(UP_TEXT_COLOR);
            textPaint.setStrokeWidth(40);
            textPaint.setTextSize(60);
            textPaint.setTextAlign(Paint.Align.CENTER);

            int x = 0;
            int y = -r - 100;
            canvas.drawText(upText, x, y, textPaint);
        }
        if(downText != null) {
            if(isFreeMode) {
                textPaint.setColor(DOWN_TEXT_FREE_MODE);
            } else {
                textPaint.setColor(DOWN_TEXT_HELP_MODE);
            }
            textPaint.setTextSize(80);
            int x = 0;
            int y = (int) (r / 3.5);
            canvas.drawText(downText, x, y, textPaint);

        }
    }

    // 开启表盘
    public void startPointer() {
        assert curState == STOP_ACTION;
        curState = START_ACTION;
        pointerAnimator.setFloatValues(0, (float) 1.0);
        downWidthAnimator.setFloatValues(finalDownWidth / 2, finalDownWidth);
        pointerAnimator.start();
        downWidthAnimator.start();
    }

    // 旋转表盘指针
    public void rotatePointer(int toAngle) {
        if(curState != STOP_ACTION) {
            assert ROTATE_MIN_ANGLE <= toAngle && toAngle <= ROTATE_MAX_ANGLE;
            curState = ROTATE_ACTION;
            if(toAngle >= MIN_TARGET_ANGLE && toAngle <= MAX_TARGET_ANGLE) {
                setTargetColor();

            } else {
                restoreColor();
            }
            // 计算需要旋转的角度
            rotateAnimator.setFloatValues(pointerAngle, toAngle);
            rotateAnimator.start();
            pointerAngle = toAngle;
        }
    }

    // 关闭表盘
    public void stopPointer() {
        rotatePointer(0);
        curArcColor = EXT_ARC_COLOR;
        curState = STOP_ACTION;
        upText = null;
        downText = null;
        downWidthAnimator.setFloatValues(finalDownWidth, 0);
        pointerAnimator.setFloatValues((float) 1.0, 0);
        pointerAnimator.start();
        downWidthAnimator.start();
    }

    // 设置文字
    public void setText(String upText, String downText) {
        if(curState != STOP_ACTION) {
            if(upText != null) {
                this.upText = upText + UP_TEXT_SUFFIX;
            }
            if(downText != null) {
                this.downText = downText;
            }
            invalidate();
        }

    }

    // 设置调试模式——即底部文字背景颜色
    public void setMode(boolean isFreeMode) {
        assert curState != STOP_ACTION;
        this.isFreeMode = isFreeMode;
        if(isFreeMode){
            setText(null, null);
        }
    }

    // 颜色渐变函数
    private int blendColors(int color1, int color2, float ratio) {
        final float inverseRation = 1f - ratio;
        float r = (Color.red(color1) * ratio)
                + (Color.red(color2) * inverseRation);
        float g = (Color.green(color1) * ratio)
                + (Color.green(color2) * inverseRation);
        float b = (Color.blue(color1) * ratio)
                + (Color.blue(color2) * inverseRation);
        return Color.rgb((int) r, (int) g, (int) b);
    }

    // 指针指向中心时，设置突出颜色
    private void setTargetColor() {
        extArcColor = EXT_ARC_TARGET;
        curArcColor = EXT_ARC_TARGET;
        invalidate();
    }

    // 恢复开启时的颜色
    private void restoreColor() {
        extArcColor = EXT_ARC_COLOR_START;
        curArcColor = EXT_ARC_COLOR_START;
        invalidate();
    }

    public void setTipSounder(SoundPool tipSounder, int soundId) {
        this.tipSounder = tipSounder;
        this.soundId = soundId;
    }
}
