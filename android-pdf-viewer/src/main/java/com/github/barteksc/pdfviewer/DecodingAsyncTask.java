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

import android.os.AsyncTask;

import com.github.barteksc.pdfviewer.source.DocumentSource;
import com.shockwave.pdfium.PdfDocument;
import com.shockwave.pdfium.PdfiumCore;
import com.shockwave.pdfium.util.Size;

class DecodingAsyncTask extends AsyncTask<Void, Void, Throwable> {

    private boolean cancelled;

    private PDFView pdfView;

    private PdfiumCore pdfiumCore;
    private String password;
    private DocumentSource docSource;
    private int[] userPages;
    private PdfFile pdfFile;

    DecodingAsyncTask(DocumentSource docSource, String password, int[] userPages, PDFView pdfView, PdfiumCore pdfiumCore) {
        this.docSource = docSource;
        this.userPages = userPages;
        this.cancelled = false;
        this.pdfView = pdfView;
        this.password = password;
        this.pdfiumCore = pdfiumCore;
    }

    @Override
    protected Throwable doInBackground(Void... params) {
        try {
            PdfDocument pdfDocument = docSource.createDocument(pdfView.getContext(), pdfiumCore, password);
            pdfFile = new PdfFile(pdfiumCore, pdfDocument, pdfView.getPageFitPolicy(), getViewSize(),
                    userPages, pdfView.isSwipeVertical(), pdfView.getSpacingPx(), pdfView.doAutoSpacing());
            return null;
        } catch (Throwable t) {
            return t;
        }
    }

    private Size getViewSize() {
        return new Size(pdfView.getWidth(), pdfView.getHeight());
    }

    @Override
    protected void onPostExecute(Throwable t) {
        if (t != null) {
            pdfView.loadError(t);
            return;
        }
        if (!cancelled) {
            pdfView.loadComplete(pdfFile);
        }
    }

    @Override
    protected void onCancelled() {
        cancelled = true;
    }
}
