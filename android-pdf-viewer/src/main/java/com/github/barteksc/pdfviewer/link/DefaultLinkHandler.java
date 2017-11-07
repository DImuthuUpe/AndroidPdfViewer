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
package com.github.barteksc.pdfviewer.link;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;

import com.github.barteksc.pdfviewer.PDFView;
import com.github.barteksc.pdfviewer.model.LinkTapEvent;

public class DefaultLinkHandler implements LinkHandler {

    private static final String TAG = DefaultLinkHandler.class.getSimpleName();

    private PDFView pdfView;

    public DefaultLinkHandler(PDFView pdfView) {
        this.pdfView = pdfView;
    }

    @Override
    public void handleLinkEvent(LinkTapEvent event) {
        String uri = event.getLink().getUri();
        Integer page = event.getLink().getDestPageIdx();
        if (uri != null && !uri.isEmpty()) {
            handleUri(uri);
        } else if (page != null) {
            handlePage(page);
        }
    }

    private void handleUri(String uri) {
        Uri parsedUri = Uri.parse(uri);
        Intent intent = new Intent(Intent.ACTION_VIEW, parsedUri);
        Context context = pdfView.getContext();
        if (intent.resolveActivity(context.getPackageManager()) != null) {
            context.startActivity(intent);
        } else {
            Log.w(TAG, "No activity found for URI: " + uri);
        }
    }

    private void handlePage(int page) {
        pdfView.jumpTo(page);
    }
}
