package com.github.barteksc.pdfviewer;


import android.graphics.RectF;
import android.util.Pair;

import com.github.barteksc.pdfviewer.util.Constants;
import com.github.barteksc.pdfviewer.util.MathUtils;

import static com.github.barteksc.pdfviewer.util.Constants.Cache.CACHE_SIZE;

class PagesLoader {

    private PDFView pdfView;


    // variables set on every call to loadPages()
    private int cacheOrder;
    private float scaledHeight;
    private float scaledWidth;
    private Pair<Integer, Integer> colsRows;
    private float xOffset;
    private float yOffset;
    private float rowHeight;
    private float colWidth;
    private float pageRelativePartWidth;
    private float pageRelativePartHeight;
    private float partRenderWidth;
    private float partRenderHeight;
    private int thumbnailWidth;
    private int thumbnailHeight;
    private final RectF thumbnailRect = new RectF(0, 0, 1, 1);

    private class Holder {
        int page;
        int row;
        int col;
    }

    public PagesLoader(PDFView pdfView) {
        this.pdfView = pdfView;
    }

    private Pair<Integer, Integer> getPageColsRows() {
        float ratioX = 1f / pdfView.getOptimalPageWidth();
        float ratioY = 1f / pdfView.getOptimalPageHeight();
        final float partHeight = (Constants.PART_SIZE * ratioY) / pdfView.getZoom();
        final float partWidth = (Constants.PART_SIZE * ratioX) / pdfView.getZoom();
        final int nbRows = MathUtils.ceil(1f / partHeight);
        final int nbCols = MathUtils.ceil(1f / partWidth);
        return new Pair<>(nbCols, nbRows);
    }

    private int documentPage(int userPage) {
        int documentPage = userPage;
        if (pdfView.getFilteredUserPages() != null) {
            if (userPage < 0 || userPage >= pdfView.getFilteredUserPages().length) {
                return -1;
            } else {
                documentPage = pdfView.getFilteredUserPages()[userPage];
            }
        }

        if (documentPage < 0 || userPage >= pdfView.getDocumentPageCount()) {
            return -1;
        }

        return documentPage;
    }

    private Holder getPageAndCoordsByOffset(float offset) {
        Holder holder = new Holder();
        float fixOffset = -MathUtils.max(offset, 0);

        if (pdfView.isSwipeVertical()) {
            holder.page = MathUtils.floor(fixOffset / scaledHeight);
            holder.row = MathUtils.floor(Math.abs(fixOffset - scaledHeight * holder.page) / rowHeight);
            holder.col = MathUtils.floor(xOffset / colWidth);
        } else {
            holder.page = MathUtils.floor(fixOffset / scaledWidth);
            holder.col = MathUtils.floor(Math.abs(fixOffset - scaledWidth * holder.page) / colWidth);
            holder.row = MathUtils.floor(yOffset / rowHeight);
        }
        return holder;
    }

    private void loadThumbnail(int userPage, int documentPage) {
        if (!pdfView.cacheManager.containsThumbnail(userPage, documentPage,
                thumbnailWidth, thumbnailHeight, thumbnailRect)) {
            pdfView.renderingAsyncTask.addRenderingTask(userPage, documentPage,
                    thumbnailWidth, thumbnailHeight, thumbnailRect,
                    true, 0, pdfView.isBestQuality(), pdfView.isAnnotationRendering());
        }
    }

    /**
     * @param number if < 0 then row (column) is above view, else row (column) is visible or below view
     * @return
     */
    private int loadRelative(int number, int nbOfPartsLoadable, boolean outsideView) {
        int loaded = 0;
        float newOffset;
        if (pdfView.isSwipeVertical()) {
            float rowsHeight = rowHeight * number + 1;
            newOffset = pdfView.getCurrentYOffset() - (outsideView ? pdfView.getHeight() : 0) - rowsHeight;
        } else {
            float colsWidth = colWidth * number;
            newOffset = pdfView.getCurrentXOffset() - (outsideView ? pdfView.getWidth() : 0) - colsWidth;
        }

        Holder holder = getPageAndCoordsByOffset(newOffset);
        int documentPage = documentPage(holder.page);
        if (documentPage < 0) {
            return 0;
        }
        loadThumbnail(holder.page, documentPage);

        if (pdfView.isSwipeVertical()) {
            for (int col = 0; col < colsRows.first; col++) {
                if (loadCell(holder.page, documentPage, holder.row, col, pageRelativePartWidth, pageRelativePartHeight)) {
                    loaded++;
                }
                if (loaded >= nbOfPartsLoadable) {
                    return loaded;
                }
            }
        } else {
            for (int row = 0; row < colsRows.second; row++) {
                if (loadCell(holder.page, documentPage, row, holder.col, pageRelativePartWidth, pageRelativePartHeight)) {
                    loaded++;
                }
                if (loaded >= nbOfPartsLoadable) {
                    return loaded;
                }
            }
        }

        return loaded;
    }

    public int loadVisible() {
        int parts = 0;
        Holder firstHolder, lastHolder;
        if (pdfView.isSwipeVertical()) {
            firstHolder = getPageAndCoordsByOffset(pdfView.getCurrentYOffset());
            lastHolder = getPageAndCoordsByOffset(pdfView.getCurrentYOffset() - pdfView.getHeight() + 1);
            int visibleRows = 0;
            if (firstHolder.page == lastHolder.page) {
                visibleRows = lastHolder.row - firstHolder.row + 1;
            } else {
                visibleRows += colsRows.second - firstHolder.row;
                for (int page = firstHolder.page + 1; page < lastHolder.page; page++) {
                    visibleRows += colsRows.second;
                }
                visibleRows += lastHolder.row + 1;
            }

            for (int i = 0; i < visibleRows && parts < CACHE_SIZE; i++) {
                parts += loadRelative(i, CACHE_SIZE - parts, false);
            }
        } else {
            firstHolder = getPageAndCoordsByOffset(pdfView.getCurrentXOffset());
            lastHolder = getPageAndCoordsByOffset(pdfView.getCurrentXOffset() - pdfView.getWidth() + 1);
            int visibleCols = 0;
            if (firstHolder.page == lastHolder.page) {
                visibleCols = lastHolder.col - firstHolder.col + 1;
            } else {
                visibleCols += colsRows.first - firstHolder.col;
                for (int page = firstHolder.page + 1; page < lastHolder.page; page++) {
                    visibleCols += colsRows.first;
                }
                visibleCols += lastHolder.col + 1;
            }

            for (int i = 0; i < visibleCols && parts < CACHE_SIZE; i++) {
                parts += loadRelative(i, CACHE_SIZE - parts, false);
            }
        }
        int prevDocPage = documentPage(firstHolder.page - 1);
        if (prevDocPage >= 0) {
            loadThumbnail(firstHolder.page - 1, prevDocPage);
        }
        int nextDocPage = documentPage(firstHolder.page + 1);
        if (nextDocPage >= 0) {
            loadThumbnail(firstHolder.page + 1, nextDocPage);
        }
        return parts;
    }

    private boolean loadCell(int userPage, int documentPage, int row, int col, float pageRelativePartWidth, float pageRelativePartHeight) {

        float relX = pageRelativePartWidth * col;
        float relY = pageRelativePartHeight * row;
        float relWidth = pageRelativePartWidth;
        float relHeight = pageRelativePartHeight;

        // Adjust width and height to
        // avoid being outside the page
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
            if (!pdfView.cacheManager.upPartIfContained(userPage, documentPage, renderWidth, renderHeight, pageRelativeBounds, cacheOrder)) {
                pdfView.renderingAsyncTask.addRenderingTask(userPage, documentPage,
                        renderWidth, renderHeight, pageRelativeBounds, false, cacheOrder,
                        pdfView.isBestQuality(), pdfView.isAnnotationRendering());
            }

            cacheOrder++;
            return true;
        }
        return false;
    }

    public void loadPages() {
        scaledHeight = pdfView.toCurrentScale(pdfView.getOptimalPageHeight());
        scaledWidth = pdfView.toCurrentScale(pdfView.getOptimalPageWidth());
        thumbnailWidth = (int) (pdfView.getOptimalPageWidth() * Constants.THUMBNAIL_RATIO);
        thumbnailHeight = (int) (pdfView.getOptimalPageHeight() * Constants.THUMBNAIL_RATIO);
        colsRows = getPageColsRows();
        xOffset = -MathUtils.max(pdfView.getCurrentXOffset(), 0);
        yOffset = -MathUtils.max(pdfView.getCurrentYOffset(), 0);
        rowHeight = scaledHeight / colsRows.second;
        colWidth = scaledWidth / colsRows.first;
        pageRelativePartWidth = 1f / (float) colsRows.first;
        pageRelativePartHeight = 1f / (float) colsRows.second;
        partRenderWidth = Constants.PART_SIZE / pageRelativePartWidth;
        partRenderHeight = Constants.PART_SIZE / pageRelativePartHeight;
        cacheOrder = 1;
        int loaded = loadVisible();
        if (pdfView.getScrollDir().equals(PDFView.ScrollDir.END)) { // if scrolling to end, preload next view
            for (int i = 0; i < Constants.PRELOAD_COUNT && loaded < CACHE_SIZE; i++) {
                loaded += loadRelative(i, loaded, true);
            }
        } else { // if scrolling to start, preload previous view
            for (int i = 0; i > -Constants.PRELOAD_COUNT && loaded < CACHE_SIZE; i--) {
                loaded += loadRelative(i, loaded, false);
            }
        }
    }
}