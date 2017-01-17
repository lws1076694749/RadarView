package com.lws.radarview;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.View;

/**
 * Created by Wenshan.Lu on 2017/1/17.
 */

public class RadarView extends View {

    private static final String TAG = "RadarView";

    // 属性值范围，默认16个
    private double[] mMaxValues = {100, 100, 100, 100, 100, 100, 100, 100, 100, 100, 100, 100, 100, 100, 100, 100};
    private double[] mMinValues = {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};

    // 属性名
    private String[] mTitles = {"a", "b", "c", "d", "e", "f", "g", "h"};
    // 属性值
    private double[] mValues = {30, 70, 50, 20, 60, 100};
    //绘制的网格密度
    private int mGridNumber;

    // 中心点坐标
    private int mCenterX;
    private int mCenterY;
    // 半径
    private float mRadius;
    private float mAngle;

    private Paint mLinePaint, mFillPaint, mTextPaint;
    // 网格Path
    private Path mLinePath = new Path();
    // 覆盖区域的Path
    private Path mValuePath = new Path();

    public RadarView(Context context) {
        this(context, null);
    }

    public RadarView(Context context, AttributeSet attrs) {
        super(context, attrs);
        mLinePaint = new Paint();
        mLinePaint.setStyle(Paint.Style.STROKE);
        mFillPaint = new Paint();
        mTextPaint = new Paint();

        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.RadarView);
        mGridNumber = a.getInteger(R.styleable.RadarView_gridDensity, 5);
        mFillPaint.setColor(a.getColor(R.styleable.RadarView_fillColor, Color.BLUE));
        mLinePaint.setColor(a.getColor(R.styleable.RadarView_lineColor, Color.BLACK));
        mTextPaint.setColor(a.getColor(R.styleable.RadarView_textColor, Color.BLACK));
        mTextPaint.setTextSize(a.getDimension(R.styleable.RadarView_textSize,
                TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, 12, context.getResources().getDisplayMetrics())));
        a.recycle();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int widthSize = MeasureSpec.getSize(widthMeasureSpec);
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        int heightSize = MeasureSpec.getSize(heightMeasureSpec);

        float density = getContext().getResources().getDisplayMetrics().density;
        int width = (int) (300 * density + 0.5f);
        int height = (int) (300 * density + 0.5f);

        if (widthMode == MeasureSpec.EXACTLY) {
            width = widthSize;
        } else if (widthMode == MeasureSpec.AT_MOST) {
            width = Math.min(width, widthSize);
        }

        if (heightMode == MeasureSpec.EXACTLY) {
            height = heightSize;
        } else if (heightMode == MeasureSpec.AT_MOST) {
            height = Math.min(height, heightSize);
        }

        setMeasuredDimension(width, height);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        mCenterX = w / 2;
        mCenterY = h / 2;
        mRadius = Math.min(w, h) / 2.0f * 0.8f;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        mAngle = (float) (2 * Math.PI / mValues.length);
        drawPolygonGrid(canvas);
        drawRegion(canvas);
        drawText(canvas);
    }

    // 绘制多边形网格
    private void drawPolygonGrid(Canvas canvas) {
        canvas.save();
        canvas.translate(mCenterX, mCenterY);
        for (int i = 1; i <= mGridNumber; i++) {
            float r = mRadius / mGridNumber * i;
            mLinePath.reset();
            for (int j = 0; j < mValues.length; j++) {
                float x = (float) (r * Math.cos(mAngle * j));
                float y = (float) (r * Math.sin(mAngle * j));
                if (j == 0) {
                    mLinePath.moveTo(x, y);
                } else {
                    mLinePath.lineTo(x, y);
                }
                // 绘制中心点到各顶点的直线
                if (i == mGridNumber) {
                    canvas.drawLine(0, 0, x, y, mLinePaint);
                }
            }
            mLinePath.close();
            canvas.drawPath(mLinePath, mLinePaint);
        }
        canvas.restore();
    }

    // 绘制覆盖区域
    public void drawRegion(Canvas canvas) {
        canvas.save();
        canvas.translate(mCenterX, mCenterY);
        mFillPaint.setAlpha(255);
        mFillPaint.setStyle(Paint.Style.FILL);
        for (int i = 0; i < mValues.length; i++) {
            float percent = (float) (mValues[i] / (mMaxValues[i] - mMinValues[i]));
            float x = (float) (mRadius * Math.cos(mAngle * i) * percent);
            float y = (float) (mRadius * Math.sin(mAngle * i) * percent);
            if (i == 0) {
                mValuePath.moveTo(x, y);
            } else {
                mValuePath.lineTo(x, y);
            }
            // 绘制小圆点
            canvas.drawCircle(x, y, mRadius / 50f, mFillPaint);
        }
        mValuePath.close();
        // 填充区域
        mFillPaint.setAlpha(127);
        canvas.drawPath(mValuePath, mFillPaint);
        // 绘制外围Path
        mFillPaint.setAlpha(255);
        mFillPaint.setStyle(Paint.Style.STROKE);
        mFillPaint.setStrokeWidth(5);
        canvas.drawPath(mValuePath, mFillPaint);
        canvas.restore();
    }

    //绘制属性值名称
    public void drawText(Canvas canvas) {
        canvas.save();
        canvas.translate(mCenterX, mCenterY);
        Paint.FontMetrics fontMetrics = mTextPaint.getFontMetrics();
        float fontHeight = fontMetrics.descent - fontMetrics.ascent;
        int count = Math.min(mValues.length, mTitles.length);
        for (int i = 0; i < count; i++) {
            float a = mAngle * i;
            float x = (float) ((mRadius + fontHeight / 2) * Math.cos(a));
            float y = (float) ((mRadius + fontHeight / 2) * Math.sin(a));
            if ((0 <= a && a <= Math.PI / 2) || (a >= 3 * Math.PI / 2 && a <= 2 * Math.PI)) {
                // 中心点右边部分
                canvas.drawText(mTitles[i], x, y, mTextPaint);
            } else {
                // 中心点左边部分
                float dis = mTextPaint.measureText(mTitles[i]);
                canvas.drawText(mTitles[i], x - dis, y, mTextPaint);
            }
        }
        canvas.restore();
    }

    /**
     * *************************
     * 对外的接口
     * *************************
     */

    public void setValues(double[] values) {
        this.mValues = values;
        postInvalidate();
    }

    public void setMaxValues(double[] values) {
        this.mMaxValues = values;
        postInvalidate();
    }

    public void setMinValues(double[] values) {
        this.mMinValues = values;
        postInvalidate();
    }

    public void setTitles(String[] titles) {
        this.mTitles = titles;
        postInvalidate();
    }
}
