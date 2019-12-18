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
package com.github.barteksc.pdfviewer.model;

import android.graphics.Bitmap;
import android.graphics.RectF;

public class PagePart {

    private int page;

    private Bitmap renderedBitmap;

    private RectF pageRelativeBounds;

    private boolean thumbnail;

    private int cacheOrder;

    public PagePart(int page, Bitmap renderedBitmap, RectF pageRelativeBounds, boolean thumbnail, int cacheOrder) {
        super();
        this.page = page;
        this.renderedBitmap = renderedBitmap;
        this.pageRelativeBounds = pageRelativeBounds;
        this.thumbnail = thumbnail;
        this.cacheOrder = cacheOrder;
    }

    public int getCacheOrder() {
        return cacheOrder;
    }

    public int getPage() {
        return page;
    }

    public Bitmap getRenderedBitmap() {
        return renderedBitmap;
    }

    public RectF getPageRelativeBounds() {
        return pageRelativeBounds;
    }

    public boolean isThumbnail() {
        return thumbnail;
    }

    public void setCacheOrder(int cacheOrder) {
        this.cacheOrder = cacheOrder;
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof PagePart)) {
            return false;
        }

        PagePart part = (PagePart) obj;
        return part.getPage() == page
                && part.getPageRelativeBounds().left == pageRelativeBounds.left
                && part.getPageRelativeBounds().right == pageRelativeBounds.right
                && part.getPageRelativeBounds().top == pageRelativeBounds.top
                && part.getPageRelativeBounds().bottom == pageRelativeBounds.bottom;
    }

}
