package com.example.androidviews.ledview;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.View;

public class LedItemView extends View {
    private static final String TAG = "LedItemView";

    private int viewNumber;//view序数
    private int columnNumber;//列序数
    private int rowNumber;//行序数
    private boolean isChecked;//是否被选中

    private Paint paint;

    public LedItemView(Context context) {
        super(context);
        init(0, 4);
    }

    public LedItemView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(0, 4);
    }

    public LedItemView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(0, 4);
    }

    private void init(int color, float strokeWidth) {
        paint = new Paint();
        paint.setAntiAlias(true);
        paint.setColor(color);
        if (isChecked) {
            paint.setStyle(Paint.Style.FILL);
            paint.setStrokeWidth(strokeWidth);
        } else {
            paint.setStyle(Paint.Style.FILL);
            paint.setStrokeWidth(strokeWidth);
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        int x1 = (int) (getWidth() * 1);
        int y1 = (int) (getHeight() * 1);
        int x2 = (int) (getWidth() * 0);
        int y2 = (int) (getHeight() * 0);
        Rect rect = new Rect(x1, y1, x2, y2);
        canvas.drawRect(rect, paint);
    }

    public int getViewNumber() {
        return viewNumber;
    }

    public void setViewNumber(int viewNumber) {
        this.viewNumber = viewNumber;
    }

    public int getColumnNumber() {
        return columnNumber;
    }

    public void setColumnNumber(int columnNumber) {
        this.columnNumber = columnNumber;
    }

    public int getRowNumber() {
        return rowNumber;
    }

    public void setRowNumber(int rowNumber) {
        this.rowNumber = rowNumber;
    }

    public boolean isChecked() {
        return isChecked;
    }

    public void setChecked(boolean checked) {
        isChecked = checked;
    }

    public void setPaint(int color) {
        init(color, 1);
    }

    public void setPaint(int color, float strokeWidth) {
        init(color, strokeWidth);
    }

    public int getColor(){
        return paint.getColor();
    }
}
