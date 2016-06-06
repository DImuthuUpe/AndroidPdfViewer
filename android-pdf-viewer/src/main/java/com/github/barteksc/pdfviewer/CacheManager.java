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

import android.graphics.RectF;

import com.github.barteksc.pdfviewer.model.PagePart;

import java.util.Comparator;
import java.util.PriorityQueue;
import java.util.Vector;

import static com.github.barteksc.pdfviewer.util.Constants.Cache.CACHE_SIZE;
import static com.github.barteksc.pdfviewer.util.Constants.Cache.THUMBNAILS_CACHE_SIZE;

class CacheManager {

    private PriorityQueue<PagePart> passiveCache;

    private PriorityQueue<PagePart> activeCache;

    private Vector<PagePart> thumbnails;

    public CacheManager() {
        activeCache = new PriorityQueue<>(CACHE_SIZE, new PagePartComparator());
        passiveCache = new PriorityQueue<>(CACHE_SIZE, new PagePartComparator());
        thumbnails = new Vector<>();
    }

    public void cachePart(PagePart part) {

        // If cache too big, remove and recycle
        makeAFreeSpace();

        // Then add part
        activeCache.offer(part);

    }

    public void makeANewSet() {
        passiveCache.addAll(activeCache);
        activeCache.clear();
    }

    private void makeAFreeSpace() {

        while ((activeCache.size() + passiveCache.size()) >= CACHE_SIZE &&
                !passiveCache.isEmpty()) {
            passiveCache.poll().getRenderedBitmap().recycle();
        }

        while ((activeCache.size() + passiveCache.size()) >= CACHE_SIZE &&
                !activeCache.isEmpty()) {
            activeCache.poll().getRenderedBitmap().recycle();
        }
    }

    public void cacheThumbnail(PagePart part) {

        // If cache too big, remove and recycle
        if (thumbnails.size() >= THUMBNAILS_CACHE_SIZE) {
            thumbnails.remove(0).getRenderedBitmap().recycle();
        }

        // Then add thumbnail
        thumbnails.add(part);

    }

    public boolean upPartIfContained(int userPage, int page, float width, float height, RectF pageRelativeBounds, int toOrder) {
        PagePart fakePart = new PagePart(userPage, page, null, width, height, pageRelativeBounds, false, 0);

        PagePart found;
        if ((found = find(passiveCache, fakePart)) != null) {
            passiveCache.remove(found);
            found.setCacheOrder(toOrder);
            activeCache.offer(found);
            return true;
        }

        return find(activeCache, fakePart) != null;
    }

    /** Return true if already contains the described PagePart */
    public boolean containsThumbnail(int userPage, int page, float width, float height, RectF pageRelativeBounds) {
        PagePart fakePart = new PagePart(userPage, page, null, width, height, pageRelativeBounds, true, 0);
        for (PagePart part : thumbnails) {
            if (part.equals(fakePart)) {
                return true;
            }
        }
        return false;
    }

    private PagePart find(PriorityQueue<PagePart> vector, PagePart fakePart) {
        for (PagePart part : vector) {
            if (part.equals(fakePart)) {
                return part;
            }
        }
        return null;
    }

    public Vector<PagePart> getPageParts() {
        Vector<PagePart> parts = new Vector<>(passiveCache);
        parts.addAll(activeCache);
        return parts;
    }

    public Vector<PagePart> getThumbnails() {
        return thumbnails;
    }

    public void recycle() {
        for (PagePart part : activeCache) {
            part.getRenderedBitmap().recycle();
        }
        for (PagePart part : activeCache) {
            part.getRenderedBitmap().recycle();
        }
        for (PagePart part : thumbnails) {
            part.getRenderedBitmap().recycle();
        }
        passiveCache.clear();
        activeCache.clear();
        thumbnails.clear();
    }

    class PagePartComparator implements Comparator<PagePart> {
        @Override
        public int compare(PagePart part1, PagePart part2) {
            if (part1.getCacheOrder() == part2.getCacheOrder()) {
                return 0;
            }
            return part1.getCacheOrder() > part2.getCacheOrder() ? 1 : -1;
        }
    }

}
