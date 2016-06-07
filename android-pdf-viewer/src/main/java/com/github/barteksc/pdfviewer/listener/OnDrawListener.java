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
package com.github.barteksc.pdfviewer.listener;

import android.graphics.Canvas;

/**
 * This interface allows an extern class to draw
 * something on the PDFView canvas, above all images.
 */
public interface OnDrawListener {

    /**
     * This method is called when the PDFView is
     * drawing its view.
     * <p>
     * The page is starting at (0,0)
     *
     * @param canvas        The canvas on which to draw things.
     * @param pageWidth     The width of the current page.
     * @param pageHeight    The height of the current page.
     * @param displayedPage The current page index
     */
    void onLayerDrawn(Canvas canvas, float pageWidth, float pageHeight, int displayedPage);
}
