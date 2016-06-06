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
import android.util.TypedValue;
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

        LayoutParams lp = new LayoutParams(Util.getDP(getContext(), INDICATOR_WIDTH), Util.getDP(getContext(), INDICATOR_HEIGHT));
        lp.setMargins(0, 0, Util.getDP(getContext(), 15) + scrollBar.getWidth(), 0);

        textView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, INDICATOR_TEXT_SIZE);
        LayoutParams tvlp = new LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        tvlp.addRule(RelativeLayout.CENTER_IN_PARENT, RelativeLayout.TRUE);

        addView(textView, tvlp);

        lp.addRule(ALIGN_RIGHT, scrollBar.getId());
        ((ViewGroup) scrollBar.getParent()).addView(this, lp);

        this.scrollBar = scrollBar;
    }

    /**
     * Used by the ScrollBar to move the indicator with the handle
     *
     * @param y Position to which the indicator should move.
     */
    void setScroll(float y) {
        if (getVisibility() == VISIBLE) {
            y -= Util.getDP(getContext(), INDICATOR_HEIGHT / 2) - scrollBar.getHandlerHeight() / 2;

            if (y < 5) {
                y = 5;
            } else if (y > scrollBar.getHeight() - Util.getDP(getContext(), INDICATOR_HEIGHT)) {
                y = scrollBar.getHeight() - Util.getDP(getContext(), INDICATOR_HEIGHT);
            }

            setY(y);
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
