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
import android.os.AsyncTask;

import com.github.barteksc.pdfviewer.source.DocumentSource;
import com.shockwave.pdfium.PdfDocument;
import com.shockwave.pdfium.PdfiumCore;

class DecodingAsyncTask extends AsyncTask<Void, Void, Throwable> {

    private boolean cancelled;

    private PDFView pdfView;

    private Context context;
    private PdfiumCore pdfiumCore;
    private PdfDocument pdfDocument;
    private String password;
    private DocumentSource docSource;
    private int firstPageIdx;
    private int pageWidth;
    private int pageHeight;

    DecodingAsyncTask(DocumentSource docSource, String password, PDFView pdfView, PdfiumCore pdfiumCore, int firstPageIdx) {
        this.docSource = docSource;
        this.firstPageIdx = firstPageIdx;
        this.cancelled = false;
        this.pdfView = pdfView;
        this.password = password;
        this.pdfiumCore = pdfiumCore;
        context = pdfView.getContext();
    }

    @Override
    protected Throwable doInBackground(Void... params) {
        try {
            pdfDocument = docSource.createDocument(context, pdfiumCore, password);
            // We assume all the pages are the same size
            pdfiumCore.openPage(pdfDocument, firstPageIdx);
            pageWidth = pdfiumCore.getPageWidth(pdfDocument, firstPageIdx);
            pageHeight = pdfiumCore.getPageHeight(pdfDocument, firstPageIdx);
            return null;
        } catch (Throwable t) {
            return t;
        }
    }

    @Override
    protected void onPostExecute(Throwable t) {
        if (t != null) {
            pdfView.loadError(t);
            return;
        }
        if (!cancelled) {
            pdfView.loadComplete(pdfDocument, pageWidth, pageHeight);
        }
    }

    @Override
    protected void onCancelled() {
        cancelled = true;
    }
}
