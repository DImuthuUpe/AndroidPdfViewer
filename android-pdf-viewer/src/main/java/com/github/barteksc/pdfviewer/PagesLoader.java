/**
 * Copyright 2017 Bartosz Schiller
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

import android.graphics.RectF;

import com.github.barteksc.pdfviewer.util.Constants;
import com.github.barteksc.pdfviewer.util.MathUtils;
import com.github.barteksc.pdfviewer.util.Util;
import com.shockwave.pdfium.util.SizeF;

import static com.github.barteksc.pdfviewer.util.Constants.Cache.CACHE_SIZE;
import static com.github.barteksc.pdfviewer.util.Constants.PRELOAD_OFFSET;

class PagesLoader {

    private PDFView pdfView;
    private int cacheOrder;
    private float xOffset;
    private float yOffset;
    private float pageRelativePartWidth;
    private float pageRelativePartHeight;
    private float partRenderWidth;
    private float partRenderHeight;
    private final RectF thumbnailRect = new RectF(0, 0, 1, 1);
    private final int preloadOffset;
    private final Holder firstHolder = new Holder();
    private final Holder lastHolder = new Holder();
    private final GridSize firstGrid = new GridSize();
    private final GridSize lastGrid = new GridSize();
    private final GridSize middleGrid = new GridSize();

    private class Holder {
        int page;
        int row;
        int col;
    }

    private class GridSize {
        int rows;
        int cols;
    }

    PagesLoader(PDFView pdfView) {
        this.pdfView = pdfView;
        this.preloadOffset = Util.getDP(pdfView.getContext(), PRELOAD_OFFSET);
    }

    private void getPageColsRows(GridSize grid, int pageIndex) {
        SizeF size = pdfView.pdfFile.getPageSize(pageIndex);
        float ratioX = 1f / size.getWidth();
        float ratioY = 1f / size.getHeight();
        final float partHeight = (Constants.PART_SIZE * ratioY) / pdfView.getZoom();
        final float partWidth = (Constants.PART_SIZE * ratioX) / pdfView.getZoom();
        grid.rows = MathUtils.ceil(1f / partHeight);
        grid.cols = MathUtils.ceil(1f / partWidth);
    }

    private Holder getPageAndCoordsByOffset(Holder holder, GridSize grid, float localXOffset,
                                            float localYOffset, boolean endOffset) {
        float fixedXOffset = -MathUtils.max(localXOffset, 0);
        float fixedYOffset = -MathUtils.max(localYOffset, 0);
        float offset = pdfView.isSwipeVertical() ? fixedYOffset : fixedXOffset;
        holder.page = pdfView.pdfFile.getPageAtOffset(offset, pdfView.getZoom());
        getPageColsRows(grid, holder.page);
        SizeF scaledPageSize = pdfView.pdfFile.getScaledPageSize(holder.page, pdfView.getZoom());
        float rowHeight = scaledPageSize.getHeight() / grid.rows;
        float colWidth = scaledPageSize.getWidth() / grid.cols;
        float row, col;
        float secondaryOffset = pdfView.pdfFile.getSecondaryPageOffset(holder.page, pdfView.getZoom());
        if (pdfView.isSwipeVertical()) {
            row = Math.abs(fixedYOffset - pdfView.pdfFile.getPageOffset(holder.page, pdfView.getZoom())) / rowHeight;
            col = MathUtils.min(fixedXOffset - secondaryOffset, 0) / colWidth;
        } else {
            col = Math.abs(fixedXOffset - pdfView.pdfFile.getPageOffset(holder.page, pdfView.getZoom())) / colWidth;
            row = MathUtils.min(fixedYOffset - secondaryOffset, 0) / rowHeight;
        }

        if (endOffset) {
            holder.row = MathUtils.ceil(row);
            holder.col = MathUtils.ceil(col);
        } else {
            holder.row = MathUtils.floor(row);
            holder.col = MathUtils.floor(col);
        }
        return holder;
    }

    private void calculatePartSize(GridSize grid) {
        pageRelativePartWidth = 1f / (float) grid.cols;
        pageRelativePartHeight = 1f / (float) grid.rows;
        partRenderWidth = Constants.PART_SIZE / pageRelativePartWidth;
        partRenderHeight = Constants.PART_SIZE / pageRelativePartHeight;
    }

    private void loadVisible() {
        int parts = 0;
        float scaledPreloadOffset = preloadOffset * pdfView.getZoom();
        float firstXOffset = -xOffset + scaledPreloadOffset;
        float lastXOffset = -xOffset - pdfView.getWidth() - scaledPreloadOffset;
        float firstYOffset = -yOffset + scaledPreloadOffset;
        float lastYOffset = -yOffset - pdfView.getHeight() - scaledPreloadOffset;

        getPageAndCoordsByOffset(firstHolder, firstGrid, firstXOffset, firstYOffset, false);
        getPageAndCoordsByOffset(lastHolder, lastGrid, lastXOffset, lastYOffset, true);

        for (int i = firstHolder.page; i <= lastHolder.page; i++) {
            loadThumbnail(i);
        }

        int pagesCount = lastHolder.page - firstHolder.page + 1;
        for (int page = firstHolder.page; page <= lastHolder.page && parts < CACHE_SIZE; page++) {

            if (page == firstHolder.page && pagesCount > 1) {
                parts += loadPageEnd(firstHolder, firstGrid, CACHE_SIZE - parts);
            } else if (page == lastHolder.page && pagesCount > 1) {
                parts += loadPageStart(lastHolder, lastGrid, CACHE_SIZE - parts);
            } else if(pagesCount == 1) {
                parts += loadPageCenter(firstHolder, lastHolder, firstGrid, CACHE_SIZE - parts);
            } else {
                getPageColsRows(middleGrid, page);
                parts += loadWholePage(page, middleGrid, CACHE_SIZE - parts);
            }
        }

    }

    /**
     * When whole page is visible
     *
     * @return loaded parts count
     */
    private int loadWholePage(int page, GridSize grid, int nbOfPartsLoadable) {
        calculatePartSize(grid);
        return loadPage(page, 0, grid.rows - 1, 0, grid.cols - 1, nbOfPartsLoadable);
    }

    /**
     * When only part of one page is visible
     *
     * @return loaded parts count
     */
    private int loadPageCenter(Holder firstHolder, Holder lastHolder, GridSize grid, int nbOfPartsLoadable) {
        calculatePartSize(grid);
        return loadPage(firstHolder.page, firstHolder.row, lastHolder.row, firstHolder.col, lastHolder.col, nbOfPartsLoadable);
    }

    /**
     * When only end of page is visible
     *
     * @return loaded parts count
     */
    private int loadPageEnd(Holder holder, GridSize grid, int nbOfPartsLoadable) {
        calculatePartSize(grid);
        if (pdfView.isSwipeVertical()) {
            int firstRow = holder.row;
            return loadPage(holder.page, firstRow, grid.rows - 1, 0, grid.cols - 1, nbOfPartsLoadable);
        } else {
            int firstCol = holder.col;
            return loadPage(holder.page, 0, grid.rows - 1, firstCol, grid.cols - 1, nbOfPartsLoadable);
        }
    }

    /**
     * If only start of the page is visible
     *
     * @return loaded parts count
     */
    private int loadPageStart(Holder holder, GridSize grid, int nbOfPartsLoadable) {
        calculatePartSize(grid);
        if (pdfView.isSwipeVertical()) {
            int lastRow = holder.row;
            return loadPage(holder.page, 0, lastRow, 0, grid.cols - 1, nbOfPartsLoadable);
        } else {
            int lastCol = holder.col;
            return loadPage(holder.page, 0, grid.rows - 1, 0, lastCol, nbOfPartsLoadable);
        }

    }

    private int loadPage(int page, int firstRow, int lastRow, int firstCol, int lastCol,
                         int nbOfPartsLoadable) {
        int loaded = 0;
        for (int row = firstRow; row <= lastRow; row++) {
            for (int col = firstCol; col <= lastCol; col++) {
                if (loadCell(page, row, col, pageRelativePartWidth, pageRelativePartHeight)) {
                    loaded++;
                }
                if (loaded >= nbOfPartsLoadable) {
                    return loaded;
                }
            }
        }
        return loaded;
    }

    private boolean loadCell(int page, int row, int col, float pageRelativePartWidth, float pageRelativePartHeight) {

        float relX = pageRelativePartWidth * col;
        float relY = pageRelativePartHeight * row;
        float relWidth = pageRelativePartWidth;
        float relHeight = pageRelativePartHeight;

        float renderWidth = partRenderWidth;
        float renderHeight = partRenderHeight;
        if (relX + relWidth > 1) {
            relWidth = 1 - relX;
        }
        if (relY + relHeight > 1) {
            relHeight = 1 - relY;
        }
        renderWidth *= relWidth;
        renderHeight *= relHeight;
        RectF pageRelativeBounds = new RectF(relX, relY, relX + relWidth, relY + relHeight);

        if (renderWidth > 0 && renderHeight > 0) {
            if (!pdfView.cacheManager.upPartIfContained(page, pageRelativeBounds, cacheOrder)) {
                pdfView.renderingHandler.addRenderingTask(page, renderWidth, renderHeight,
                        pageRelativeBounds, false, cacheOrder, pdfView.isBestQuality(),
                        pdfView.isAnnotationRendering());
            }

            cacheOrder++;
            return true;
        }
        return false;
    }

    private void loadThumbnail(int page) {
        SizeF pageSize = pdfView.pdfFile.getPageSize(page);
        float thumbnailWidth = pageSize.getWidth() * Constants.THUMBNAIL_RATIO;
        float thumbnailHeight = pageSize.getHeight() * Constants.THUMBNAIL_RATIO;
        if (!pdfView.cacheManager.containsThumbnail(page, thumbnailRect)) {
            pdfView.renderingHandler.addRenderingTask(page,
                    thumbnailWidth, thumbnailHeight, thumbnailRect,
                    true, 0, pdfView.isBestQuality(), pdfView.isAnnotationRendering());
        }
    }

    void loadPages() {
        cacheOrder = 1;
        xOffset = -MathUtils.max(pdfView.getCurrentXOffset(), 0);
        yOffset = -MathUtils.max(pdfView.getCurrentYOffset(), 0);

        loadVisible();
    }
}
