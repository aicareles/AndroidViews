package com.example.androidviews.ledview;

import android.content.Context;
import android.graphics.Color;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.view.ViewGroup;


public class LedView extends ViewGroup {
    private static final String TAG = "LedView";
    private int widthCount = 128;//宽的点个数(36,48,56)
    private int heightCount = 16;//高的点个数(5,12,14)
    int widthSize;//宽
    int heightSize;//高
    private int xMore;//x轴上平均后的余量
    private int pointAllLength;//点整体边长
    private int pointMargin = 1;//点的边距
    private int pointLength;//点边长
    private int offset;//x,y轴上偏移量
    int moveMax;//可移动最大值
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
    private ScaleGestureDetector mScaleGestureDetector;
    private float oldDist = 1f;
    private float mScale = 1f;
    private float scaleTemp = 1;

    private static final float MAX_SCALE=4.0F;
    private static final float MIN_SCALE=1.0F;
    float x = 1, y = 1;
    double now = 0;
    private byte[] data;

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
    public void init(int widthCount, int heightCount) {
        this.widthCount = widthCount;
        this.heightCount = heightCount;
        for (int i = 0; i < widthCount * heightCount; i++) {
            LedItemView view = new LedItemView(getContext());
            view.setViewNumber(i);
            int columnNumber = i / heightCount;
            int rowNumber = i % heightCount;
            view.setColumnNumber(columnNumber);
            view.setRowNumber(rowNumber);
            view.setPaint(unSelectedColor);
            view.postInvalidate();
            addView(view);
        }
        initScaleGestureDetector();
    }

    //宽点阵数，高点阵数
    public void init(int widthCount, int heightCount, float strokeWidth) {
        this.widthCount = widthCount;
        this.heightCount = heightCount;
        for (int i = 0; i < widthCount * heightCount; i++) {
            LedItemView view = new LedItemView(getContext());
            view.setViewNumber(i);
            int columnNumber = i / heightCount;
            int rowNumber = i % heightCount;
            view.setColumnNumber(columnNumber);
            view.setRowNumber(rowNumber);
            view.setPaint(unSelectedColor, strokeWidth);
            view.postInvalidate();
            addView(view);
        }
        initScaleGestureDetector();
    }

    private void initScaleGestureDetector() {
        mScaleGestureDetector = new ScaleGestureDetector(getContext(),
                new ScaleGestureDetector.SimpleOnScaleGestureListener() {
                    @Override
                    public boolean onScaleBegin(ScaleGestureDetector detector) {
                        return true;
                    }

                    @Override
                    public boolean onScale(ScaleGestureDetector detector) {
                        Log.i(TAG, "focusX = " + detector.getFocusX());       // 缩放中心，x坐标
                        Log.i(TAG, "focusY = " + detector.getFocusY());       // 缩放中心y坐标
                        Log.i(TAG, "scale = " + detector.getScaleFactor());   // 缩放因子
                        /*float scaleFactor=detector.getScaleFactor();
                        float currentScale = getScale();//相对原图的缩放比例
                        scale = currentScale;
                        if(currentScale>MAX_SCALE && scaleFactor<1.0f || currentScale<MIN_SCALE
                                && scaleFactor>1.0f || currentScale<MAX_SCALE && currentScale>MIN_SCALE){
                            handleZoom(currentScale);
                        }*/
                        handleZoom(detector.getScaleFactor()>=1.0f);
                        return true;
                    }

                    @Override
                    public void onScaleEnd(ScaleGestureDetector detector) {
                        scaleTemp = mScale;
                    }
                });
    }

    private long lastTime = 0L;
    private static final float SCALING_FACTOR = 0.05f;//缩放因子
    private void handleZoom(boolean isZoomIn) {
        if (System.currentTimeMillis()-lastTime<50)return;
        lastTime = System.currentTimeMillis();
        if (isZoomIn){//放大
            mScale +=SCALING_FACTOR;
            setScaleX(mScale);
            setScaleY(mScale);
        }else {
            mScale -=SCALING_FACTOR;
            if (mScale<=0.4f){
                mScale=0.4f;
            }
            setScaleX(mScale);
            setScaleY(mScale);
        }
        if (scaleCallback != null){
            scaleCallback.onScale(mScale);
        }
    }

    public void setScale(float scale){
        float value = MIN_SCALE;
        if (scale >=MIN_SCALE && scale <=MAX_SCALE){
            value = scale;
        }else if (scale > MAX_SCALE){
            value = MAX_SCALE;
        }
        mScale = value;
        if (scaleCallback != null){
            scaleCallback.onScale(mScale);
        }
        setScaleX(mScale);
        setScaleY(mScale);
    }

    private void handleZoom(float scale) {
        if (System.currentTimeMillis()-lastTime<50)return;
        lastTime = System.currentTimeMillis();
        setScaleX(scale);
        setScaleY(scale);
    }

    /**
     * 获得当前的缩放比例
     *
     * @return
     */
    public float getScale() {
        return mScale;
    }

    //删除所有子view
    public void removeAllChildView() {
        int childCount = getChildCount();
        if (0 < childCount) {
            for (int i = 0; i < childCount; i++) {
                removeViewAt(0);
            }
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {

        measureChildren(widthMeasureSpec, heightMeasureSpec);
        widthSize = MeasureSpec.getSize(widthMeasureSpec);
        heightSize = widthSize*heightCount/widthCount;
        xMore = widthSize % widthCount;
        pointAllLength = widthSize / widthCount;
        pointLength = pointAllLength - pointMargin * 2;
        offset = xMore / 2;
        moveMax = pointAllLength * widthCount - widthSize;
        setMeasuredDimension(widthSize, heightSize);
    }

    public int getPointAllLength() {
        return pointAllLength;
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        //先行后列
        int childCount = getChildCount();
        for (int i = 0; i < childCount; i++) {
            View childView = getChildAt(i);
            int rowNunber = i / widthCount;//第几行
            int top = rowNunber * pointAllLength + pointMargin + offset;
            int columnNumber = i % widthCount;//第几列
            int left = columnNumber * pointAllLength + pointMargin + offset;
            int right = left + pointLength;
            int bottom = top + pointLength;
            childView.layout(left, top, right, bottom);
        }
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        if (ev.getPointerCount() ==2){
            switch (ev.getAction()){
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
                    if (mScale < MIN_SCALE){//回弹
                        setScale(1f);
                    }
                    break;
            }
        }else {
            if (isDispatchTouch){
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
                    if (0 <= whichChildView && whichChildView < getChildCount()) {
                        LedItemView view = (LedItemView) getChildAt(whichChildView);
                        if (mode == MODE_PAINT) {
                            view.setChecked(true);
                            view.setPaint(selectedColor);
                            view.postInvalidate();
                            if (mirrorMode == MIRROR_HORIZONTAL){//水平（行变化   列不变）
                                int which = (heightCount-whichRow)*widthCount+whichColumn;
                                LedItemView mirrorView = (LedItemView) getChildAt(which);
                                mirrorView.setChecked(true);
                                mirrorView.setPaint(selectedColor);
                                mirrorView.postInvalidate();
                            }else if (mirrorMode == MIRROR_VERTICAL){
                                int which = whichRow*widthCount+(widthCount-whichColumn);
                                LedItemView mirrorView = (LedItemView) getChildAt(which);
                                mirrorView.setChecked(true);
                                mirrorView.setPaint(selectedColor);
                                mirrorView.postInvalidate();
                            }
                        } else if (mode == MODE_ERASER) {
                            if (!view.isChecked()) return true;
                            view.setChecked(false);
                            view.setPaint(unSelectedColor);
                            view.postInvalidate();
                        } else {
                            return true;
                        }
                        if (null != ledListener) {
                            ledListener.onItemSelect(view.getViewNumber(), view.getColumnNumber(), view.getRowNumber(), view.isChecked());
                        }
                    }
                }
            }else {
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
                    default:break;
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

    //清空所选
    public void clearSelected() {
        for (int i = 0; i < getChildCount(); i++) {
            LedItemView ledItemView = (LedItemView) getChildAt(i);
            ledItemView.setChecked(false);
            ledItemView.setPaint(unSelectedColor);
            ledItemView.postInvalidate();
        }
    }

    //设置item选中或不选中
    public void setItemSelected(int index, boolean isSelect) {
        if (0 <= index && index < getChildCount()) {
            LedItemView ledItemView = (LedItemView) getChildAt(index);
            if (isSelect) {
                ledItemView.setChecked(true);
                ledItemView.setPaint(selectedColor);
                ledItemView.postInvalidate();
            } else {
                ledItemView.setChecked(false);
                ledItemView.setPaint(unSelectedColor);
                ledItemView.postInvalidate();
            }
        }
    }

    //得到数据，orientation=ORIENTATION_PORTRAIT=竖屏数据，orientation=ORIENTATION_LANDSCAPE=横屏数据
    public byte[] getData(int orientation) {
        int childCount = getChildCount();
        byte[] data = new byte[childCount];
        if (this.orientation == ORIENTATION_PORTRAIT) {//竖屏显示
            if (orientation == ORIENTATION_PORTRAIT) {//竖屏数据
                for (int i = 0; i < childCount; i++) {
                    LedItemView ledItemView = (LedItemView) getChildAt(i);
                    if (ledItemView.isChecked()) {
                        data[i] = 1;
                    }
                }
            } else if (orientation == ORIENTATION_LANDSCAPE) {//横屏数据
                for (int i = 0; i < childCount; i++) {
                    LedItemView ledItemView = (LedItemView) getChildAt(i);
                    if (ledItemView.isChecked()) {
                        int columnNumber = i / heightCount;
                        int rowNumber = i % heightCount;
                        int dataColumnNumber = rowNumber;
                        int dataRowNumber = widthCount - columnNumber - 1;
                        int dataViewNumber = dataColumnNumber * widthCount + dataRowNumber;
                        data[dataViewNumber] = 1;
                    }
                }
            }
        } else if (this.orientation == ORIENTATION_LANDSCAPE) {//横屏显示
            if (orientation == ORIENTATION_PORTRAIT) {//竖屏数据
                for (int i = 0; i < childCount; i++) {
                    LedItemView ledItemView = (LedItemView) getChildAt(i);
                    if (ledItemView.isChecked()) {
                        int columnNumber = i / heightCount;
                        int rowNumber = i % heightCount;
                        int dataColumnNumber = heightCount - rowNumber - 1;
                        int dataRowNumber = columnNumber;
                        int dataViewNumber = dataColumnNumber * widthCount + dataRowNumber;
                        data[dataViewNumber] = 1;
                    }
                }
            } else if (orientation == ORIENTATION_LANDSCAPE) {//横屏数据
                for (int i = 0; i < childCount; i++) {
                    LedItemView ledItemView = (LedItemView) getChildAt(i);
                    if (ledItemView.isChecked()) {
                        data[i] = 1;
                    }
                }
            }
        }
        return data;
    }

    //设置bgr数据
    public void setData(int[] colors) {
        int childCount = getChildCount();
        for (int i = 0; i < childCount; i++) {
            LedItemView ledItemView = (LedItemView) getChildAt(i);
            ledItemView.setPaint(colors[i]);
            ledItemView.postInvalidate();
        }
    }

    //设置数据
    public void setData(byte[] data, float strokeWidth) {
        if (null == data) return;
        this.data = data;
        int childCount = getChildCount();
        if (data.length != childCount) return;
        for (int i = 0; i < childCount; i++) {
            LedItemView ledItemView = (LedItemView) getChildAt(i);
            if (data[i] == 1) {//选中
                ledItemView.setChecked(true);
                ledItemView.setPaint(selectedColor, strokeWidth);
            } else {//没选中
                ledItemView.setChecked(false);
                ledItemView.setPaint(unSelectedColor, strokeWidth);
            }
            ledItemView.postInvalidate();
        }
    }

    public byte[] getData(){
        if (data == null){
            data = new byte[widthCount*heightCount*3];
        }
        int color_size = data.length/3;
        for (int i = 0; i < color_size; i++) {
            LedItemView ledItemView = (LedItemView) getChildAt(i);
            if (ledItemView.isChecked()) {
                int selectedColor = ledItemView.getColor();
                int[] colors = new int[]{Color.blue(selectedColor), Color.green(selectedColor), Color.red(selectedColor)};
                data[i*3] = (byte) colors[0];
                data[i*3+1] = (byte) colors[1];
                data[i*3+2] = (byte) colors[2];
            }
        }
        return data;
    }

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

    public int getMode(){
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

    public void setItemSelect(int viewNumber, boolean isSelect) {
        if (0 <= viewNumber && viewNumber < getChildCount()) {
            LedItemView ledItemView = (LedItemView) getChildAt(viewNumber);
            ledItemView.setChecked(isSelect);
            if (isSelect) {
                ledItemView.setPaint(selectedColor);
                ledItemView.postInvalidate();
            } else {
                ledItemView.setPaint(unSelectedColor);
                ledItemView.postInvalidate();
            }
        }
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
    private float getFingerSpacing(MotionEvent event){
        float x = event.getX(0) - event.getX(1);
        float y = event.getY(0) - event.getY(1);
        return (float) Math.sqrt(x * x + y * y);
    }

    private ScaleCallback scaleCallback;

    public void setScaleCallback(ScaleCallback callback){
        this.scaleCallback = callback;
    }

    public interface ScaleCallback {
        void onScale(float scale);
    }
}
