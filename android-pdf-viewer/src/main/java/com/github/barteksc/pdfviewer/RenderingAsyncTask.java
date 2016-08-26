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
import android.os.AsyncTask;

import com.github.barteksc.pdfviewer.model.PagePart;
import com.shockwave.pdfium.PdfDocument;
import com.shockwave.pdfium.PdfiumCore;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

class RenderingAsyncTask extends AsyncTask<Void, PagePart, Void> {

    private PdfiumCore pdfiumCore;
    private PdfDocument pdfDocument;

    private final List<RenderingTask> renderingTasks;
    private PDFView pdfView;

    private RectF renderBounds = new RectF();
    private Rect roundedRenderBounds = new Rect();
    private Matrix renderMatrix = new Matrix();
    private final Set<Integer> openedPages = new HashSet<>();

    public RenderingAsyncTask(PDFView pdfView, PdfiumCore pdfiumCore, PdfDocument pdfDocument) {
        this.pdfView = pdfView;
        this.pdfiumCore = pdfiumCore;
        this.pdfDocument = pdfDocument;
        this.renderingTasks = Collections.synchronizedList(new ArrayList<RenderingTask>());

    }

    public void addRenderingTask(int userPage, int page, float width, float height, RectF bounds, boolean thumbnail, int cacheOrder, boolean bestQuality, boolean annotationRendering) {
        RenderingTask task = new RenderingTask(width, height, bounds, userPage, page, thumbnail, cacheOrder, bestQuality, annotationRendering);
        renderingTasks.add(task);
        wakeUp();
    }

    @Override
    protected Void doInBackground(Void... params) {
        while (!isCancelled()) {

            // Proceed all tasks
            while (true) {
                RenderingTask task;
                synchronized (renderingTasks) {
                    if (!renderingTasks.isEmpty()) {
                        task = renderingTasks.get(0);
                    } else {
                        break;
                    }
                }
                //it is very rare case, but sometimes null can appear
                if (task != null) {
                    PagePart part = proceed(task);
                    if (part == null) {
                        break;
                    } else if (renderingTasks.remove(task)) {
                        publishProgress(part);
                    } else {
                        part.getRenderedBitmap().recycle();
                    }
                }
            }

            // Wait for new task, return if canceled
            if (!waitForRenderingTasks() || isCancelled()) {
                return null;
            }

        }
        return null;

    }

    @Override
    protected void onProgressUpdate(PagePart... part) {
        pdfView.onBitmapRendered(part[0]);
    }

    private boolean waitForRenderingTasks() {
        try {
            synchronized (renderingTasks) {
                renderingTasks.wait();
            }
            return true;
        } catch (InterruptedException e) {
            return false;
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

        if (!isCancelled()) {
            pdfiumCore.renderPageBitmap(pdfDocument, render, renderingTask.page,
                    roundedRenderBounds.left, roundedRenderBounds.top,
                    roundedRenderBounds.width(), roundedRenderBounds.height(), renderingTask.annotationRendering);
        } else {
            render.recycle();
            return null;
        }

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

    public void removeAllTasks() {
        synchronized (renderingTasks) {
            renderingTasks.clear();
        }
    }

    public void wakeUp() {
        synchronized (renderingTasks) {
            renderingTasks.notify();
        }
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

        public RenderingTask(float width, float height, RectF bounds, int userPage, int page, boolean thumbnail, int cacheOrder, boolean bestQuality, boolean annotationRendering) {
            super();
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
