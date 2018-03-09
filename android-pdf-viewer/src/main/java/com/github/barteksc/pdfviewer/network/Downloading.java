package com.github.barteksc.pdfviewer.network;

import java.io.File;

public interface Downloading {

    /**
     * @param url
     * @param customName custom name of the file
     */
    void download(String url, String customName);

    void download(String url);

    interface Listener {
        void onSuccess(String url, File pdfFile);

        void onFailure(Exception e);

        void onProgressUpdate(int progress, int total);
    }
}
