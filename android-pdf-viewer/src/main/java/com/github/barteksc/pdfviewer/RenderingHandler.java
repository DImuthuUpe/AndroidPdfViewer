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

import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.graphics.RectF;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;

import com.github.barteksc.pdfviewer.model.PagePart;
import com.shockwave.pdfium.PdfDocument;
import com.shockwave.pdfium.PdfiumCore;

import java.util.HashSet;
import java.util.Set;

/**
 * A {@link Handler} that will process incoming {@link RenderingTask} messages
 * and alert {@link PDFView#onBitmapRendered(PagePart)} when the portion of the
 * PDF is ready to render.
 */
class RenderingHandler extends Handler {
    /**
     * {@link Message#what} kind of message this handler processes.
     */
    static final int MSG_RENDER_TASK = 1;

    private PdfiumCore pdfiumCore;
    private PdfDocument pdfDocument;

    private PDFView pdfView;

    private RectF renderBounds = new RectF();
    private Rect roundedRenderBounds = new Rect();
    private Matrix renderMatrix = new Matrix();
    private final Set<Integer> openedPages = new HashSet<>();

    RenderingHandler(Looper looper, PDFView pdfView, PdfiumCore pdfiumCore, PdfDocument pdfDocument) {
        super(looper);
        this.pdfView = pdfView;
        this.pdfiumCore = pdfiumCore;
        this.pdfDocument = pdfDocument;
    }

    void addRenderingTask(int userPage, int page, float width, float height, RectF bounds, boolean thumbnail, int cacheOrder, boolean bestQuality, boolean annotationRendering) {
        RenderingTask task = new RenderingTask(width, height, bounds, userPage, page, thumbnail, cacheOrder, bestQuality, annotationRendering);
        Message msg = obtainMessage(MSG_RENDER_TASK, task);
        sendMessage(msg);
    }

    @Override
    public void handleMessage(Message message) {
        RenderingTask task = (RenderingTask) message.obj;
        final PagePart part = proceed(task);
        if (part != null) {
            pdfView.post(new Runnable() {
                @Override
                public void run() {
                    pdfView.onBitmapRendered(part);
                }
            });
        }
    }

    private PagePart proceed(RenderingTask renderingTask) {
        if (!openedPages.contains(renderingTask.page)) {
            openedPages.add(renderingTask.page);
            pdfiumCore.openPage(pdfDocument, renderingTask.page);
        }

        int w = Math.round(renderingTask.width);
        int h = Math.round(renderingTask.height);
        Bitmap render = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
        calculateBounds(w, h, renderingTask.bounds);
        pdfiumCore.renderPageBitmap(pdfDocument, render, renderingTask.page,
                roundedRenderBounds.left, roundedRenderBounds.top,
                roundedRenderBounds.width(), roundedRenderBounds.height(), renderingTask.annotationRendering);

        if (!renderingTask.bestQuality) {
            Bitmap cpy = render.copy(Bitmap.Config.RGB_565, false);
            render.recycle();
            render = cpy;
        }

        return new PagePart(renderingTask.userPage, renderingTask.page, render, //
                renderingTask.width, renderingTask.height, //
                renderingTask.bounds, renderingTask.thumbnail, //
                renderingTask.cacheOrder);
    }

    private void calculateBounds(int width, int height, RectF pageSliceBounds) {
        renderMatrix.reset();
        renderMatrix.postTranslate(-pageSliceBounds.left * width, -pageSliceBounds.top * height);
        renderMatrix.postScale(1 / pageSliceBounds.width(), 1 / pageSliceBounds.height());

        renderBounds.set(0, 0, width, height);
        renderMatrix.mapRect(renderBounds);
        renderBounds.round(roundedRenderBounds);
    }

    private class RenderingTask {

        float width, height;

        RectF bounds;

        int page;

        int userPage;

        boolean thumbnail;

        int cacheOrder;

        boolean bestQuality;

        boolean annotationRendering;

        RenderingTask(float width, float height, RectF bounds, int userPage, int page, boolean thumbnail, int cacheOrder, boolean bestQuality, boolean annotationRendering) {
            this.page = page;
            this.width = width;
            this.height = height;
            this.bounds = bounds;
            this.userPage = userPage;
            this.thumbnail = thumbnail;
            this.cacheOrder = cacheOrder;
            this.bestQuality = bestQuality;
            this.annotationRendering = annotationRendering;
        }
    }
}
