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
package com.github.barteksc.pdfviewer.listener;

import com.github.barteksc.pdfviewer.link.LinkHandler;
import com.github.barteksc.pdfviewer.model.LinkTapEvent;

public class Callbacks {

    /**
     * Call back object to call when document loading error occurs
     */
    private OnErrorListener onErrorListener;

    /**
     * Call back object to call when the document is initially rendered
     */
    private OnRenderListener onRenderListener;

    /**
     * Call back object to call when clicking link
     */
    private LinkHandler linkHandler;

    public void setOnError(OnErrorListener onErrorListener) {
        this.onErrorListener = onErrorListener;
    }

    public OnErrorListener getOnError() {
        return onErrorListener;
    }

    public void setOnRender(OnRenderListener onRenderListener) {
        this.onRenderListener = onRenderListener;
    }

    public void callOnRender(int pagesCount) {
        if (onRenderListener != null) {
            onRenderListener.onInitiallyRendered(pagesCount);
        }
    }

    public void setLinkHandler(LinkHandler linkHandler) {
        this.linkHandler = linkHandler;
    }

    public void callLinkHandler(LinkTapEvent event) {
        if (linkHandler != null) {
            linkHandler.handleLinkEvent(event);
        }
    }
}
