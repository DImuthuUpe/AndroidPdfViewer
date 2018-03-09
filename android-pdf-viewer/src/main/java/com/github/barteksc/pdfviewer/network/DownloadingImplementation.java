
package com.github.barteksc.pdfviewer.network;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Handler;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

public class DownloadingImplementation implements Downloading {

    private static final int BUFFER_LENGTH = 1024;
    private static final int NOTIFY_PERIOD = 150 * 1024;

    private Context context;
    private Handler uiThread;
    private Listener listener = new OnDownloadingListener();

    public DownloadingImplementation(Context context, Handler uiThread, Listener listener) {
        this.context = context;
        this.uiThread = uiThread;
        this.listener = listener;

    }

    @Override
    public void download(final String url, final String fileName) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                FileOutputStream fileOutput = null;
                try {
                    File file = new File(context.getFilesDir(), fileName);

                    if (!isOnline(context)){
                        if (file.exists()){
                            notifySuccess(url, file);
                            return;
                        } else {
                            //in this case it will be empty so we need to delete
                            file.delete();
                            notifyFailure(new Exception("No connection"));
                            return;
                        }
                    }

                    fileOutput = new FileOutputStream(file);
                    HttpURLConnection urlConnection = null;
                    URL urlObj = new URL(url);
                    urlConnection = (HttpURLConnection) urlObj.openConnection();
                    int totalSize = urlConnection.getContentLength();
                    int downloadedSize = 0;
                    int counter = 0;
                    byte[] buffer = new byte[BUFFER_LENGTH];
                    int bufferLength;
                    InputStream in = new BufferedInputStream(urlConnection.getInputStream());

                    while ((bufferLength = in.read(buffer)) > 0) {
                        fileOutput.write(buffer, 0, bufferLength);
                        downloadedSize += bufferLength;
                        counter += bufferLength;
                        if (listener != null && counter > NOTIFY_PERIOD) {
                            notifyProgress(downloadedSize, totalSize);
                            counter = 0;
                        }
                    }

                    notifySuccess(url, file);
                    urlConnection.disconnect();
                    fileOutput.close();


                } catch (MalformedURLException e) {
                    notifyFailure(e);
                } catch (IOException e) {
                    notifyFailure(e);
                } finally {
                    if (fileOutput != null){
                        try {
                            fileOutput.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        }).start();
    }

    /**
     * Will get a file name from url
     * @param url
     */
    @Override
    public void download(String url) {
        download(url, url.substring(url.lastIndexOf('/') + 1));
    }

    private void notifySuccess(final String url, final File destinationPath) {
        if (uiThread == null) {
            return;
        }

        uiThread.post(new Runnable() {
            @Override
            public void run() {
                listener.onSuccess(url, destinationPath);
            }
        });
    }

    private void notifyFailure(final Exception e) {
        if (uiThread == null) {
            return;
        }

        uiThread.post(new Runnable() {
            @Override
            public void run() {
                listener.onFailure(e);
            }
        });
    }

    private void notifyProgress(final int downloadedSize, final int totalSize) {
        if (uiThread == null) {
            return;
        }

        uiThread.post(new Runnable() {
            @Override
            public void run() {
                listener.onProgressUpdate(downloadedSize, totalSize);
            }
        });
    }

    public static String extractFileNameFromURL(String url) {
        return url.substring(url.lastIndexOf('/') + 1);
    }

    /**
     *
     * @param context
     * @return true if device online and false if offline
     */
    private boolean isOnline(Context context) {
        ConnectivityManager cm =
                (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        return netInfo != null && netInfo.isConnectedOrConnecting();
    }

    protected class OnDownloadingListener implements Listener {

        /**
         * This method will notify main thread that downloading is successful ended.
         * return
         * @param url (for example if person want to save it in a hashMap as a key)
         * and
         * @param file
         */
        public void onSuccess(String url, File file) {
            /* Empty */
        }

        /**
         * this will return any error like no network or can't create new file and so on
         * @param e
         */
        public void onFailure(Exception e) {
            /* Empty */
        }

        /**
         * Wil return current
         * @param progress (current size)
         * and
         * @param total size left
         */
        public void onProgressUpdate(int progress, int total) {
            /* Empty */
        }
    }
}
