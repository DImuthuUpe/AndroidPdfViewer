/**
 * Copyright 2016 Bartosz Schiller
 * <p/>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p/>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p/>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.github.barteksc.pdfviewer;

import android.content.Context;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.github.barteksc.pdfviewer.util.Util;

public class ScrollBarPageIndicator extends RelativeLayout {

    private final static int INDICATOR_WIDTH = 75;
    private final static int INDICATOR_HEIGHT = 75;
    private final static int INDICATOR_TEXT_SIZE = 40;

    protected TextView textView;
    protected Context context;
    private ScrollBar scrollBar;

    public ScrollBarPageIndicator(Context context) {
        super(context);
        this.context = context;
        textView = new TextView(context);
        setVisibility(INVISIBLE);
    }

    public void setSize(int size) {
        LayoutParams lp = (LayoutParams) getLayoutParams();
        lp.setMargins(0, 0, size + Util.getDP(getContext(), 10), 0);
        setLayoutParams(lp);
    }

    void addToScrollBar(ScrollBar scrollBar) {

        //determine ScrollBar's position
        LayoutParams lp = new LayoutParams(Util.getDP(getContext(), INDICATOR_WIDTH), Util.getDP(getContext(), INDICATOR_HEIGHT));
        int align, left = 0, top = 0, right = 0, bottom = 0, margin = Util.getDP(getContext(), 15);
        View parent = (View) scrollBar.getParent();
        if (!scrollBar.isHorizontal()) {
            if (scrollBar.getX() > parent.getWidth() - scrollBar.getX() + scrollBar.getWidth()) { //scrollbar to the right
                right = margin + scrollBar.getWidth();
                align = ALIGN_RIGHT;
            } else { //scrollBar to the left
                left = margin + scrollBar.getWidth();
                align = ALIGN_LEFT;
            }
        } else {
            if (scrollBar.getY() > parent.getHeight() - scrollBar.getY() + scrollBar.getHeight()) { //scrollbar to the bottom
                bottom = margin + scrollBar.getHeight();
                align = ALIGN_BOTTOM;
            } else { //scrollbar to the top
                top = margin + scrollBar.getHeight();
                align = ALIGN_TOP;
            }
        }

        lp.setMargins(left, top, right, bottom);
        lp.addRule(align, scrollBar.getId());

        textView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, INDICATOR_TEXT_SIZE);
        LayoutParams tvlp = new LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        tvlp.addRule(RelativeLayout.CENTER_IN_PARENT, RelativeLayout.TRUE);

        addView(textView, tvlp);

        ((ViewGroup) scrollBar.getParent()).addView(this, lp);

        this.scrollBar = scrollBar;
    }

    /**
     * Used by the ScrollBar to move the indicator with the handle
     *
     * @param pos Position to which the indicator should move.
     */
    void setScroll(float pos) {
        if (getVisibility() == VISIBLE) {
            int indicatorSize, viewSize;
            if (!scrollBar.isHorizontal()) {
                indicatorSize = INDICATOR_HEIGHT;
                viewSize = scrollBar.getHeight();
            } else {
                indicatorSize = INDICATOR_WIDTH;
                viewSize = scrollBar.getWidth();
            }
            pos -= Util.getDP(getContext(), indicatorSize / 2) - scrollBar.getHandlerHeight() / 2;

            if (pos < 5) {
                pos = 5;
            } else if (pos > viewSize - Util.getDP(getContext(), indicatorSize)) {
                pos = viewSize - Util.getDP(getContext(), indicatorSize);
            }

            if (!scrollBar.isHorizontal()) {
                setY(pos);
            } else {
                setX(pos);
            }
        }
    }

    void setPageNum(int page) {
        String text = String.valueOf(page);
        if (!textView.getText().equals(text)) {
            textView.setText(text);
        }
    }

    void setTextColor(int color) {
        textView.setTextColor(color);
    }

}
