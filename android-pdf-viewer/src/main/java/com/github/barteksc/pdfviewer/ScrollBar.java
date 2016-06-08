/**
 * Copyright 2016 Bartosz Schiller
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.github.barteksc.pdfviewer;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PointF;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import com.github.barteksc.pdfviewer.util.Util;

public class ScrollBar extends View {

    private static final String TAG = ScrollBar.class.getSimpleName();

    private int handlerColor = 0;
    private int indicatorColor = 0;
    private int indicatorTextColor = 0;

    private Paint handlerPaint;
    private float handlerHeight = 0;
    private int viewWidth;
    private PDFView pdfView;
    private PointF handlerPos;
    private int currentPage = 0;
    private ScrollBarPageIndicator indicator;

    public ScrollBar(Context context) {
        super(context);
        init();
    }

    public ScrollBar(Context context, AttributeSet attrs) {
        super(context, attrs);
        initAttrs(attrs, 0);
        init();
    }

    public ScrollBar(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initAttrs(attrs, defStyleAttr);
        init();
    }

    private void init() {

        indicator = new ScrollBarPageIndicator(getContext());
        setIndicatorPage(currentPage);
        indicator.setBackgroundColor(indicatorColor);
        indicator.setTextColor(indicatorTextColor);

        addOnLayoutChangeListener(new OnLayoutChangeListener() {
            @Override
            public void onLayoutChange(View v, int left, int top, int right, int bottom, int oldLeft, int oldTop, int oldRight, int oldBottom) {
                indicator.addToScrollBar(ScrollBar.this);
                ScrollBar.this.removeOnLayoutChangeListener(this);
            }
        });

        handlerPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        handlerPaint.setStyle(Paint.Style.FILL);
        handlerPaint.setColor(handlerColor);

        if (getBackground() == null) {
            setBackgroundColor(Color.LTGRAY);
        }

        handlerPos = new PointF(0, 0);

        viewWidth = Util.getDP(getContext(), 30);
    }

    private void initAttrs(AttributeSet attrs, int defStyleAttr) {
        TypedArray a = getContext().getTheme().obtainStyledAttributes(attrs, R.styleable.ScrollBar, defStyleAttr, 0);

        try {
            handlerColor = a.getColor(R.styleable.ScrollBar_sb_handlerColor, Color.parseColor("#FF4081"));
            indicatorColor = a.getColor(R.styleable.ScrollBar_sb_indicatorColor, Color.parseColor("#FF4081"));
            indicatorTextColor = a.getColor(R.styleable.ScrollBar_sb_indicatorTextColor, Color.WHITE);
        } finally {
            a.recycle();
        }
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        if (isPDFViewReady()) {
            calculateHandlerHeight();
            calculateHandlerPosByPage(currentPage);
        }
    }

    float getHandlerHeight() {
        return handlerHeight;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int minw = getPaddingLeft() + getPaddingRight() + viewWidth;
        int w = resolveSizeAndState(minw, widthMeasureSpec, 1);

        int minh = MeasureSpec.getSize(heightMeasureSpec) + getPaddingBottom() + getPaddingTop();
        int h = resolveSizeAndState(minh, heightMeasureSpec, 0);

        setMeasuredDimension(w, h);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        if (isInEditMode()) {
            canvas.drawRect(0, 0, getWidth(), Util.getDP(getContext(), 40), handlerPaint);
            return;
        } else if (!isPDFViewReady()) {
            return;
        }

        if (Float.isNaN(handlerPos.y) || Float.isInfinite(handlerPos.y)) {
            calculateHandlerPosByPage(currentPage);
        }

        canvas.drawRect(handlerPos.x, handlerPos.y, getWidth(), handlerPos.y + handlerHeight, handlerPaint);

    }

    private void calculateHandlerPosByPage(int position) {
        handlerPos.y = position * handlerHeight;
    }

    private void calculateHandlerHeight() {
        handlerHeight = getHeight() / (float) getPagesCount();
    }

    private boolean isPDFViewReady() {
        return pdfView != null && pdfView.getPageCount() > 0;
    }

    private int getPagesCount() {
        return isPDFViewReady() ? pdfView.getPageCount() : 0;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {

        if(!isPDFViewReady()) {
            return super.onTouchEvent(event);
        }

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
            case MotionEvent.ACTION_MOVE:
            case MotionEvent.ACTION_POINTER_DOWN:
                float y = event.getY();
                if (y < 0 || y > getHeight())
                    return true;

                int pageNum = (int) Math.floor(y / handlerHeight);

                float handleY = pageNum * handlerHeight;
                if (handleY < 0) {
                    handleY = 0;
                } else if (y + handlerHeight / 2 > getHeight()) {
                    handleY = getHeight() - handlerHeight;
                }
                handlerPos.y = handleY;

                if (pageNum != currentPage) {
                    indicator.setPageNum(pageNum + 1);
                }
                currentPage = pageNum;
                indicator.setVisibility(VISIBLE);
                indicator.setScroll(handleY);
                invalidate();
                return true;
            case MotionEvent.ACTION_CANCEL:
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_POINTER_UP:
                int pgNum = (int) Math.floor(event.getY() / handlerHeight);
                pdfView.jumpTo(pgNum + 1);
                currentPage = pgNum;
                indicator.setVisibility(INVISIBLE);
                invalidate();
                return true;
        }

        return super.onTouchEvent(event);
    }

    /**
     * Go to the given page.
     *
     * @param page Page number starting from 1.
     */
    public void setCurrentPage(int page) {
        if (!isPDFViewReady()) {
            throw new IllegalStateException("PDFView not set");
        }
        currentPage = page;
        pdfView.jumpTo(page);
        invalidate();
    }

    private void setIndicatorPage(int position) {
        indicator.setPageNum(position + 1);
    }

    @Override
    public void onRestoreInstanceState(Parcelable state) {
        SavedState savedState = (SavedState) state;
        super.onRestoreInstanceState(savedState.getSuperState());
        currentPage = savedState.currentPage;
        setIndicatorPage(currentPage);
        requestLayout();
    }

    @Override
    public Parcelable onSaveInstanceState() {
        Parcelable superState = super.onSaveInstanceState();
        SavedState savedState = new SavedState(superState);
        savedState.currentPage = currentPage;
        return savedState;
    }

    /** methods for integration with PDFView */
    void addToPDFView(PDFView pdfView) {
        this.pdfView = pdfView;
        calculateHandlerHeight();
        pageChanged(pdfView.getCurrentPage());
    }

    void pdfLoaded() {
        calculateHandlerHeight();
    }

    void pageChanged(int page) {
        currentPage = page;
        calculateHandlerPosByPage(currentPage);
        invalidate();
    }

    static class SavedState extends BaseSavedState {
        int currentPage;

        public SavedState(Parcelable superState) {
            super(superState);
        }

        private SavedState(Parcel in) {
            super(in);
            currentPage = in.readInt();
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            super.writeToParcel(dest, flags);
            dest.writeInt(currentPage);
        }

        @SuppressWarnings("UnusedDeclaration")
        public static final Creator<SavedState> CREATOR = new Creator<SavedState>() {
            @Override
            public SavedState createFromParcel(Parcel in) {
                return new SavedState(in);
            }

            @Override
            public SavedState[] newArray(int size) {
                return new SavedState[size];
            }
        };
    }
}
