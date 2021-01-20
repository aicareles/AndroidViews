package com.example.androidviews.ledview;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import java.util.Arrays;


public class LedView extends View {
    private static final String TAG = "LedView";
    private int widthCount;//宽的点个数(36,48,56)
    private int heightCount;//高的点个数(5,12,14)
    private int widthSize;//宽
    private int heightSize;//高
    private int pointAllLength;//点整体边长
    private int pointMargin = 1;//点的边距
    private int pointLength;//点边长
    private int offset;//x,y轴上偏移量
    private int moveMax;//可移动最大值
    private int unSelectedColor = Color.parseColor("#252525");//没选中颜色
    private int selectedColor = Color.parseColor("#62ff3c");//选中颜色

    private int mode = LedView.MODE_PAINT;//动作模式
    public static final int MODE_NO = 0;//没操作
    public static final int MODE_PAINT = 1;//笔
    public static final int MODE_ERASER = 2;//橡皮擦

    private int orientation = ORIENTATION_PORTRAIT;//屏幕显示方向
    public static final int ORIENTATION_PORTRAIT = 0;//竖屏
    public static final int ORIENTATION_LANDSCAPE = 1;//横屏
    public static final int MIRROR_HORIZONTAL = 0;//水平镜像
    public static final int MIRROR_VERTICAL = 1;//竖直镜像
    private int mirrorMode = -1;
    private LedListener ledListener;
    private RealTimeDataListener realTimeDataListener;//实时操作回调

    private boolean isDispatchTouch = true;
    private float oldDist = 1f;
    private float mScale = 1f;

    private static final float MAX_SCALE = 4.0F;
    private static final float MIN_SCALE = 1.0F;
    private float x = 1, y = 1;
    private double now = 0;
    private byte[] data;
    private final RectF rectFBuffer = new RectF();
    private Paint paint;

    public LedView(Context context) {
        super(context);
    }

    public LedView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public LedView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    //宽点阵数，高点阵数
    public void init(int widthCount, int heightCount, float strokeWidth) {
        paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setStyle(Paint.Style.FILL);
        paint.setStrokeWidth(strokeWidth);

        this.widthCount = widthCount;
        this.heightCount = heightCount;
        data = new byte[getTotalCount()];
        invalidate();
    }

    private int getTotalCount() {
        return widthCount * heightCount;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        int totalCount = getTotalCount();
        if (totalCount <= 0) {
            return;
        }
        for (int i = 0; i < totalCount; i++) {
            if (data[i] == 1) {
                paint.setColor(selectedColor);
            } else {
                paint.setColor(unSelectedColor);
            }

            int rowNunber = getRowNumber(i);//第几行
            int top = rowNunber * pointAllLength + pointMargin + offset;
            int columnNumber = getColumnNumber(i);//第几列
            int left = columnNumber * pointAllLength + pointMargin + offset;
            int right = left + pointLength;
            int bottom = top + pointLength;

            rectFBuffer.set(left, top, right, bottom);
            canvas.drawRect(rectFBuffer, paint);
        }
    }

    private int getRowNumber(int i) {
        return i / widthCount;
    }

    private int getColumnNumber(int i) {
        return i % widthCount;
    }


    private long lastTime = 0L;
    private static final float SCALING_FACTOR = 0.05f;//缩放因子

    private void handleZoom(boolean isZoomIn) {
        if (System.currentTimeMillis() - lastTime < 50) return;
        lastTime = System.currentTimeMillis();
        if (isZoomIn) {//放大
            mScale += SCALING_FACTOR;
            setScale(mScale);
        } else {
            mScale -= SCALING_FACTOR;
            if (mScale <= 0.4f) {
                mScale = 0.4f;
            }
            setScale(mScale);

        }
    }

    public void setScale(float scale) {
        float value = MIN_SCALE;
        if (scale >= MIN_SCALE && scale <= MAX_SCALE) {
            value = scale;
        } else if (scale > MAX_SCALE) {
            value = MAX_SCALE;
        }
        mScale = value;
        if (scaleCallback != null) {
            scaleCallback.onScale(mScale);
        }
        setScaleX(mScale);
        setScaleY(mScale);
    }

    /**
     * 获得当前的缩放比例
     *
     * @return
     */
    public float getScale() {
        return mScale;
    }


    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {

        widthSize = MeasureSpec.getSize(widthMeasureSpec);
        heightSize = widthSize * heightCount / widthCount;
        //x轴上平均后的余量
        int xMore = widthSize % widthCount;
        pointAllLength = widthSize / widthCount;
        pointLength = pointAllLength - pointMargin * 2;
        offset = xMore / 2;
        moveMax = pointAllLength * widthCount - widthSize;
        setMeasuredDimension(widthSize, heightSize);
    }

    public int getPointAllLength() {
        return pointAllLength;
    }

    public void clear() {
        Arrays.fill(data, (byte) 0);
        invalidate();
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        if (ev.getPointerCount() == 2) {
            switch (ev.getAction()) {
                case MotionEvent.ACTION_POINTER_DOWN:
                    oldDist = getFingerSpacing(ev);
                    break;
                case MotionEvent.ACTION_MOVE:
                    float newDist = getFingerSpacing(ev);
                    if (newDist > oldDist) {
                        handleZoom(true);
                    } else if (newDist < oldDist) {
                        handleZoom(false);
                    }
                    oldDist = newDist;
                    double an = Math.sqrt(Math.pow(ev.getX(0) - ev.getX(1), 2))
                            + Math.sqrt(Math.pow(ev.getY(0) - ev.getY(1), 2));
                    if (now != 0) {
                        if (an > now) {
                            if (x < 3) {
                                x += 0.05;
                                y += 0.05;
                                handleZoom(true);
                            }
                        } else {
                            if (x > 0.1) {
                                x -= 0.05;
                                y -= 0.05;
                                handleZoom(false);
                            }
                        }
                    }
                    now = an;
                    break;
                default:
                    if (mScale < MIN_SCALE) {//回弹
                        setScale(1f);
                    }
                    break;
            }
        } else {
            if (isDispatchTouch) {
                int action = ev.getAction();
                float sx = getScrollX();
                float y = ev.getY();
                float x = ev.getX() + sx;
                if (action == MotionEvent.ACTION_DOWN || action == MotionEvent.ACTION_MOVE) {
                    if (x < 0 || widthSize + sx < x) {
                        return true;
                    }
                    if (y < 0 || heightSize < y) {
                        return true;
                    }
                    int whichRow = (int) ((y - offset) / pointAllLength);//行
                    if (whichRow >= heightCount) {
                        return true;
                    }
                    int whichColumn = (int) ((x - offset) / pointAllLength);//列
                    int whichChildView = whichRow * widthCount + whichColumn;

                    if (0 <= whichChildView && whichChildView < getTotalCount()) {
                        if (mode == MODE_PAINT) {
                            data[whichChildView] = 1;
                            if (mirrorMode == MIRROR_HORIZONTAL) {//水平（行变化   列不变）
                                int which = (heightCount - whichRow) * widthCount + whichColumn;
                                data[which] = 1;
                            } else if (mirrorMode == MIRROR_VERTICAL) {
                                int which = whichRow * widthCount + (widthCount - whichColumn);
                                data[which] = 1;
                            }
                            invalidate();
                        } else if (mode == MODE_ERASER) {
                            if (data[whichChildView] == 0) return true;
                            data[whichChildView] = 0;
                            invalidate();
                        } else {
                            return true;
                        }
                        if (null != ledListener) {
                            ledListener.onItemSelect(whichColumn * whichRow, whichColumn, whichRow, data[whichColumn * whichRow] == 1);
                        }
                    }
                }
            } else {
                int x = (int) ev.getRawX(); //触摸点相对于屏幕的横坐标
                int y = (int) ev.getRawY(); //触摸点相对于屏幕的纵坐标
                switch (ev.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        break;
                    case MotionEvent.ACTION_MOVE: //当手势类型为移动时
                        int deltaX = x - mLastX; //两次移动的x距离差
                        int deltaY = y - mLastY;//两次移动的y的距离差
                        //重新设置此view相对父容器的偏移量
                        int translationX = (int) getTranslationX() + deltaX;
                        int translationY = (int) getTranslationY() + deltaY;
                        setTranslationX(translationX);
                        setTranslationY(translationY);
                        break;
                    case MotionEvent.ACTION_UP:
                        break;
                    default:
                        break;
                }
                //记录上一次移动的坐标
                mLastX = x;
                mLastY = y;
                return true;
            }
        }
        return isDispatchTouch;
    }


    private int mLastX = 0;
    private int mLastY = 0;


    public int getWidthCount() {
        return widthCount;
    }

    public int getHeightCount() {
        return heightCount;
    }

    public void setPointMargin(int pointMargin) {
        this.pointMargin = pointMargin;
    }

    public int getMoveMax() {
        return moveMax;
    }

    public void setUnSelectedColor(int unSelectedColor) {
        this.unSelectedColor = unSelectedColor;
    }

    public void setSelectedColor(int selectedColor) {
        this.selectedColor = selectedColor;
    }

    public void setMode(int mode) {
        this.mode = mode;
    }

    public int getMode() {
        return mode;
    }

    public int getOrientation() {
        return orientation;
    }

    public void setOrientation(int orientation) {
        this.orientation = orientation;
    }

    public void setMirrorMode(int mirrorMode) {
        this.mirrorMode = mirrorMode;
    }

    public interface LedListener {
        void onItemSelect(int viewNumber, int columnNumber, int rowNumber, boolean isSelect);
    }

    public LedListener getLedListener() {
        return ledListener;
    }

    public void setLedListener(LedListener ledListener) {
        this.ledListener = ledListener;
    }


    public void setDispatchTouch(boolean dispatchTouch) {
        isDispatchTouch = dispatchTouch;
    }

    public interface RealTimeDataListener {
        void onRealTimeData(byte[] data);
    }

    public RealTimeDataListener getRealTimeDataListener() {
        return realTimeDataListener;
    }

    public void setRealTimeDataListener(RealTimeDataListener realTimeDataListener) {
        this.realTimeDataListener = realTimeDataListener;
    }

    //手势缩放
    private float getFingerSpacing(MotionEvent event) {
        float x = event.getX(0) - event.getX(1);
        float y = event.getY(0) - event.getY(1);
        return (float) Math.sqrt(x * x + y * y);
    }

    private ScaleCallback scaleCallback;

    public void setScaleCallback(ScaleCallback callback) {
        this.scaleCallback = callback;
    }

    public interface ScaleCallback {
        void onScale(float scale);
    }
}
