package cn.pinode.aswitch;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;

import java.util.Timer;
import java.util.TimerTask;

/**
  * @Author Vito
  * @desc Switch.java 自定义的switch 控件
  * @date 2019/4/20
 **/
public class Switch extends View {
    private static final float DEFAULT_PADDING = 0;
    // 底色画笔
    private Paint backgroundPaint;
    // 滑动块画笔
    private Paint ballPaint;
    // 半径 背景的半径和 ball的半径是一样的
    private float radius;
    private float width;
    private float height;
    private float innerPadding;
    private int bgColor;
    private int ballColor;
    private RectF bgRectF = new RectF(0, 0, 0, 0);
    // 状态， left->false ，right -> true;
    private boolean direction = false;

    private float ballPositionX;
    private float delatX; // ball 可以滑动的距离

    private Timer timer;
    private SwitchListener listener;
    public Switch(@NonNull Context context) {
        this(context, null);
    }

    public Switch(@NonNull  Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public Switch(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initAttrs(attrs, defStyleAttr);
        initPaint();
    }

    private void initAttrs(AttributeSet attrs, int defStyleAttr) {
        TypedArray array = getContext().obtainStyledAttributes(attrs, R.styleable.Switch, defStyleAttr, 0);
        innerPadding = array.getDimension(R.styleable.Switch_innerPadding, DEFAULT_PADDING);
        bgColor = array.getColor(R.styleable.Switch_bgColor, Color.LTGRAY);
        ballColor = array.getColor(R.styleable.Switch_ballColor, Color.GREEN);
        array.recycle();

        setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                doSwitch();
            }
        });
    }

    private void initPaint(){
        backgroundPaint = new Paint();
        ballPaint = new Paint();
        backgroundPaint.setColor(bgColor);
        backgroundPaint.setAntiAlias(true);
        ballPaint.setColor(ballColor);
        ballPaint.setAntiAlias(true);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        width = MeasureSpec.getSize(widthMeasureSpec);
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        height = MeasureSpec.getSize(heightMeasureSpec);
        radius = (height - innerPadding*2)/2;
        bgRectF.right = width;
        bgRectF.bottom = height;
        ballPositionX = height/2; // 初始位置
        delatX = width - height; // ball 可以滑动的距离
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        // 画出 背景

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            canvas.drawRoundRect(0, 0, width, height, height/2, height/2,backgroundPaint);
        }else {

            canvas.drawRoundRect(bgRectF, height/2, height/2,backgroundPaint);
        }
        // 画出ball
        canvas.drawCircle(ballPositionX,height/2, radius, ballPaint);
    }



    public void setDirection(boolean direction){
        if (this.direction!=direction){
            if (direction){
                ballPositionX = width- height/2;
                invalidate();
            }else {
                ballPositionX = height/2;
            }
            this.direction = direction;
        }
    }

    public boolean getDirection(){
        return direction;
    }

    public void doSwitch(){
        if (timer!=null){
            timer.cancel();
        }
        timer = new Timer();
        if (getDirection()){ // 滑到左边
            setDirection(false); // 标记状态
            if (listener !=null){
                listener.onSwitch(false);
            }
            final AccelerateDecelerateInterpolator interpolator = new AccelerateDecelerateInterpolator();
            timer.schedule(new TimerTask() {
                float time = 0;
                float totalTime = delatX*2/radius;
                @Override
                public void run() {
                    time+= 1;
                    if (time/totalTime>=1.0){
                        ballPositionX = height/2;
                        doInvalidate();
                        timer.cancel();
                        timer = null;
                    }else {
                        ballPositionX = (width - height/2) - interpolator.getInterpolation(time/totalTime)*delatX;
                        doInvalidate();
                    }

                }
            }, 0,16);


        }else {
            setDirection(true);
            if (listener !=null){
                listener.onSwitch(true);
            }
            final AccelerateDecelerateInterpolator interpolator = new AccelerateDecelerateInterpolator();
            timer.schedule(new TimerTask() {
                float time = 0;
                float totalTime = delatX*2/radius;
                @Override
                public void run() {
                    time+= 1;
                    if (time/totalTime>=1.0){
                        ballPositionX = width - height/2;
                        doInvalidate();
                        timer.cancel();
                        timer = null;
                    }else {
                        ballPositionX = height/2 + interpolator.getInterpolation(time/totalTime)*delatX;
                        doInvalidate();
                    }

                }
            }, 0,16);

        }



    }

    public void setListener(SwitchListener listener) {
        this.listener = listener;
    }

    public interface SwitchListener {
        /**
         *  转换回调
         * @param direction 转换之后的状态
         */
        void onSwitch(boolean direction);
    }

    public void doInvalidate(){
        post(new Runnable() {
            @Override
            public void run() {
                invalidate();
            }
        });
    }
}
