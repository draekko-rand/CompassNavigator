/* =========================================================================

    Compass Navigator
    Copyright (C) 2019 Draekko, Benoit Touchette

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with this program. If not, see <http://www.gnu.org/licenses/>.

   ========================================================================= */

package com.draekko.compassnavigator.views;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.View;

import com.draekko.compassnavigator.R;

public class CompassView extends View {

    private static final String TAG = "CompassView";

    private static final int [] compass_roses = {
            0,
            R.drawable.compass_rose_1,
            R.drawable.compass_rose_1_night,
            R.drawable.compass_rose_2,
            R.drawable.compass_rose_2_night
    };

    public static final int COMPASS_ROSE_1_DAY = 0x1;
    public static final int COMPASS_ROSE_1_NIGHT = 0x2;
    public static final int COMPASS_ROSE_2_DAY = 0x3;
    public static final int COMPASS_ROSE_2_NIGHT = 0x4;

    private float mCurrentRoseDegrees = 0.0f;
    private float mCurrentBezelDegrees = 0.0f;
    private float mRoseDegrees = 0.0f;
    private float mBezelDegrees = 0.0f;
    private float mNeedlesCenterX;
    private float mNeedlesCenterY;
    private float mNeedlesOffsetX;
    private float mNeedlesOffsetY;
    private Bitmap mArrowBmp;
    private Bitmap mCompassRoseBmp;
    private Bitmap mBezelBmp;
    private Matrix mMatrix1;
    private Matrix mMatrix2;
    private Matrix mMatrix3;
    private Paint mCompassRosePaint;
    private Paint mBezelPaint;
    private Paint mArrowPaint;
    private Context mContext;
    private int mCompassRoseId;
    private int mWidth, mHeight;
    private int mLastW, mLastH;
    private boolean mNight = false;
    private int mRose = 1;
    private boolean measured = false;


    Typeface font = Typeface.createFromAsset(getContext().getAssets(), "fonts/notosans.ttf");
    Typeface typeface = Typeface.create(font, Typeface.NORMAL);

    private final int [] dayColors = { 0xFF9F0000, 0xFFFF0000, 0xDF222222, 0xFFFFFFFF, 0xFFFFFFFF };
    private final int [] nightColors = { 0xFF9F0000, 0xFFFF0000, 0xFF220000, 0xFFFF0000, 0xFFFF0000 };

    private int mRedlineColor = dayColors[0];
    private int mTickMarkColor = dayColors[1];
    private int mBezelColorBG = dayColors[2];
    private int mBezelColorTick = dayColors[3];
    private int mBezelColorMiniTick = dayColors[4];

    private int mBezelAlphaBG = 0xFF;
    private int mBezelAlphaTick = 200;
    private int mBezelAlphaMiniTick = 200;

    public CompassView(Context context) {
        super(context, null);
        init(context);
    }

    public CompassView(Context context, AttributeSet attr) {
        super(context, attr);
        init(context);
    }

    void init(Context context) {
        mMatrix1 = new Matrix();
        mMatrix2 = new Matrix();
        mMatrix3 = new Matrix();
        mCompassRosePaint = new Paint();
        mBezelPaint = new Paint();
        mArrowPaint = new Paint();
        mContext = context;
        mCompassRoseId = compass_roses[COMPASS_ROSE_1_NIGHT];
    }

    public void setRose(int value) {
        mRose = value;
        if (mRose < 1) {
            mRose = 1;
        }
        if (mRose > 2) {
            mRose = 2;
        }
        adjustView();
    }

    public int getRose() {
        return mRose;
    }

    public void setNight(boolean value) {
        mNight = value;
        adjustView();
    }

    public boolean getNight() {
        return mNight;
    }

    private void adjustView() {
        if (mNight) {
            switch(mRose) {
                case 1:
                    mCompassRoseId = compass_roses[COMPASS_ROSE_1_NIGHT];
                    break;
                case 2:
                    mCompassRoseId = compass_roses[COMPASS_ROSE_2_NIGHT];
                    break;
            }
        } else {
            switch(mRose) {
                case 1:
                    mCompassRoseId = compass_roses[COMPASS_ROSE_1_DAY];
                    break;
                case 2:
                    mCompassRoseId = compass_roses[COMPASS_ROSE_2_DAY];
                    break;
            }
        }

        if (mNight) {
            mRedlineColor = nightColors[0];
            mTickMarkColor = nightColors[1];
            mBezelColorBG = nightColors[2];
            mBezelColorTick = nightColors[3];
            mBezelColorMiniTick = nightColors[4];
        } else {
            mRedlineColor = dayColors[0];
            mTickMarkColor = dayColors[1];
            mBezelColorBG = dayColors[2];
            mBezelColorTick = dayColors[3];
            mBezelColorMiniTick = dayColors[4];
        }

        invalidate();
    }

    public Bitmap decodeBitmap(int resId, float newWidth, float newHeight) {
        if (mContext == null) {
            return null;
        }
        Bitmap bitmap;
        Drawable drawable = mContext.getDrawable(resId);
        int w = drawable.getIntrinsicWidth();
        int h = drawable.getIntrinsicHeight();
        if(w <= 0 || h <= 0) {
            bitmap = Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888);
        } else {
            try {
                bitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
            } catch (OutOfMemoryError e) {
                e.printStackTrace();
                bitmap = Bitmap.createBitmap((int)newWidth, (int)newHeight, Bitmap.Config.ARGB_8888);
            }
        }

        Canvas canvas;
        try {
            canvas = new Canvas(bitmap);
            drawable.setBounds(0, 0, w, h);
            drawable.draw(canvas);
        } catch (Exception e) {
            e.printStackTrace();
        }
        final Bitmap returnBitmap = Bitmap.createScaledBitmap(bitmap, (int)newWidth, (int)newHeight, true);
        return returnBitmap;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        if (mCompassRoseBmp == null || mBezelBmp == null || !measured) {
            super.onDraw(canvas);
            return;
        }

        float f1 = mRoseDegrees - mCurrentRoseDegrees;
        float f2 = mBezelDegrees - mCurrentBezelDegrees;
        if (Math.abs(f1) >= 180.0f) {
            if (f1 > 0.0f) {
                f1 -= 360.0f;
            } else {
                f1 += 360.0f;
            }
        }
        if (Math.abs(f2) >= 180.0f) {
            if (f2 > 0.0f) {
                f2 -= 360.0f;
            } else {
                f2 += 360.0f;
            }
        }
        mCurrentRoseDegrees = (float) (((double) mCurrentRoseDegrees) + (((double) f1) * 0.085d));
        mCurrentBezelDegrees = (float) (((double) mCurrentBezelDegrees) + (((double) f2) * 0.085d));
        if (mCurrentRoseDegrees < 0.0f) {
            mCurrentRoseDegrees += 360.0f;
        } else if (mCurrentRoseDegrees > 360.0f) {
            mCurrentRoseDegrees -= 360.0f;
        }
        if (mCurrentBezelDegrees < 0.0f) {
            mCurrentBezelDegrees += 360.0f;
        } else if (mCurrentBezelDegrees > 360.0f) {
            mCurrentBezelDegrees -= 360.0f;
        }
        mMatrix2.setRotate(mCurrentRoseDegrees, mNeedlesCenterX, mNeedlesCenterY);
        mMatrix3.setRotate(mBezelDegrees, mNeedlesCenterX, mNeedlesCenterY);

        /* DRAW COMPASS ROSE */
        if (mNight) {
            mCompassRosePaint.setAlpha(200);
        }
        try {
            canvas.drawBitmap(mCompassRoseBmp, mMatrix2, mCompassRosePaint);
        } catch (Exception e) {
            e.printStackTrace();
        }

        /* DRAW BEZEL & ARROW */
        try {
            canvas.drawBitmap(mArrowBmp, mMatrix1, mCompassRosePaint);
            canvas.drawBitmap(mBezelBmp, mMatrix3, mCompassRosePaint);
        } catch (Exception e) {
            e.printStackTrace();
        }

        super.onDraw(canvas);
    }

    public void setRoseDegrees(float f) {
        mRoseDegrees = 360.0f - f;
        invalidate();
    }

    public void setBezelDegrees(float f) {
        mBezelDegrees = 360.0f - f;
        invalidate();
    }

    private void initBitmaps(int width, int height) {
        if (mLastW == width) {
            return;
        }
        if (mLastH == height) {
            return;
        }

        if (width != height) {
            if (width < height) {
                height = width;
            }
            if (width > height) {
                width = height;
            }
        }

        /* DRAW ORIENTATION LINES */
        mArrowBmp = Bitmap.createBitmap(width, width, Bitmap.Config.ARGB_8888);
        Canvas arrowCanvas = new Canvas(mArrowBmp);

        Paint mPaintRedLine = new Paint();
        mPaintRedLine.setStrokeWidth(6.0f);
        mPaintRedLine.setColor(mRedlineColor);
        int mWOffset = (int)(width * 0.022f);
        int mHOffset = (int)(height * 0.15f);
        int w1 = width / 2 - mWOffset;
        int w2 = width / 2 + mWOffset;
        int ht = mHOffset;
        int hb = height - mHOffset;

        Paint mTickMarks = new Paint();
        mTickMarks.setStrokeWidth(6.0f);
        mTickMarks.setColor(mTickMarkColor);
        arrowCanvas.drawLine(width / 2, hb, width / 2, ht, mTickMarks);
        arrowCanvas.drawLine(width / 2, ht, width / 2 + 20, ht + 20, mTickMarks);
        arrowCanvas.drawLine(width / 2, ht, width / 2 - 20, ht + 20, mTickMarks);
        arrowCanvas.drawLine(width / 2, ht + 30, width / 2 + 20, ht + 50, mTickMarks);
        arrowCanvas.drawLine(width / 2, ht + 30, width / 2 - 20, ht + 50, mTickMarks);
        arrowCanvas.drawLine(width / 2, ht + 60, width / 2 + 20, ht + 80, mTickMarks);
        arrowCanvas.drawLine(width / 2, ht + 60, width / 2 - 20, ht + 80, mTickMarks);

        /* BEZEL */
        mBezelBmp = Bitmap.createBitmap(width, width, Bitmap.Config.ARGB_8888);
        Canvas bezelCanvas = new Canvas(mBezelBmp);

        float strokeSize = mWidth * 0.1294f;
        Paint mPaintCircle = new Paint();
        mPaintCircle.setStrokeWidth(strokeSize);
        mPaintCircle.setColor(mBezelColorBG);
        mPaintCircle.setStyle(Paint.Style.STROKE);
        mPaintCircle.setAlpha(mBezelAlphaBG);
        float centerX = width / 2;
        float centerY = height / 2;
        float radius = centerX * 0.87f;
        bezelCanvas.drawCircle(centerX, centerY , radius, mPaintCircle);

        Path circle = new Path();
        Paint mCirlcePaint = new Paint();
        Rect textBounds = new Rect();
        int mTextWidth, mTextHeight;

        Paint tPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        tPaint.setStyle(Paint.Style.FILL_AND_STROKE);
        tPaint.setStrokeCap(Paint.Cap.ROUND);
        tPaint.setAntiAlias(true);
        float fontSize = strokeSize * 0.33f;
        tPaint.setTextSize(fontSize);
        tPaint.setTypeface(typeface);
        tPaint.setAlpha(mBezelAlphaTick);
        tPaint.setColor(mBezelColorTick);

        Paint lPaint = new Paint();
        lPaint.setStrokeCap(Paint.Cap.ROUND);
        lPaint.setAntiAlias(true);
        lPaint.setColor(mBezelColorMiniTick);
        lPaint.setAlpha(mBezelAlphaMiniTick);

        boolean alter = false;
        for (int ii = 0; ii < 360; ii++) {
            bezelCanvas.save();
            bezelCanvas.rotate(ii, centerX, centerY);
            float x1 = centerX;
            float y1 = 84;
            float x2 = centerX;
            float y2 = mWidth * 0.1176f;
            //int y2 = 140;
            int mod = ii % 5;
            if (mod != 0) {
                y1 =  mWidth * 0.0824f; /* 98 */;
                lPaint.setStrokeWidth(1.0f);
                lPaint.setAlpha(165);
            } else {
                if (!alter) {
                    alter = true;
                    y1 =  mWidth * 0.0706f; /* 84 */;
                    lPaint.setStrokeWidth(2.0f);
                    lPaint.setAlpha(200);
                } else {
                    alter = false;
                    y1 =  mWidth * 0.0773f; /* 92 */;
                    lPaint.setStrokeWidth(2.0f);
                    lPaint.setAlpha(165);
                }
            }
            bezelCanvas.drawLine(x1, y1, x2, y2, lPaint);
            bezelCanvas.restore();
        }

        for (int ii = 0; ii < 360; ii+=30) {
            String QUOTE = String.valueOf(ii);
            mTextWidth = Math.round(tPaint.measureText(QUOTE.toString()));
            bezelCanvas.save();
            bezelCanvas.rotate(ii, centerX, centerY);
            float yy = mWidth * 0.0555f;
            bezelCanvas.drawText(QUOTE, centerX - (mTextWidth / 2), yy /* 66 */, tPaint);
            bezelCanvas.restore();
        }

        /* COMPASS ROSE */
        mCompassRoseBmp = decodeBitmap(mCompassRoseId, width, height);

        //int i = mCompassRoseBmp.getWidth();
        //int j = mCompassRoseBmp.getHeight();
        mNeedlesCenterX = ((float) width) / 2.0f;
        mNeedlesCenterY = ((float) height) / 2.0f;

        mLastW = width;
        mLastH = height;
    }

    @Override
    protected void onSizeChanged(int xNew, int yNew, int xOld, int yOld){
        initBitmaps(xNew, yNew);
        super.onSizeChanged(xNew, yNew, xOld, yOld);
    }

    @Override
    public void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        measured = true;
        mWidth = MeasureSpec.getSize(widthMeasureSpec);
        mHeight = MeasureSpec.getSize(heightMeasureSpec);
        initBitmaps(mWidth, mHeight);
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }
}
