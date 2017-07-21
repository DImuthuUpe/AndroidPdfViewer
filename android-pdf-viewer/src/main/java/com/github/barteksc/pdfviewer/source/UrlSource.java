package com.github.barteksc.pdfviewer.source;

import android.content.Context;
import android.os.ParcelFileDescriptor;

import com.shockwave.pdfium.PdfDocument;
import com.shockwave.pdfium.PdfiumCore;

import java.io.File;
import java.io.IOException;

/**
 * Created by 邵鸿轩 on 2017/7/21.
 */

public class UrlSource implements DocumentSource{
    private File file;

    public UrlSource(File file) {
        this.file = file;
    }

    @Override
    public PdfDocument createDocument(Context context, PdfiumCore core, String password) throws IOException {
        ParcelFileDescriptor pfd = ParcelFileDescriptor.open(file, ParcelFileDescriptor.MODE_READ_ONLY);
        return core.newDocument(pfd, password);
    }
}
