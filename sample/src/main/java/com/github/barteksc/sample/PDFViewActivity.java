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
package com.github.barteksc.sample;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;

import com.github.barteksc.pdfviewer.PDFView;
import com.github.barteksc.pdfviewer.listener.OnFileDownloadCompleteListener;
import com.github.barteksc.pdfviewer.listener.OnLoadCompleteListener;
import com.github.barteksc.pdfviewer.listener.OnPageChangeListener;

import java.io.File;


public class PDFViewActivity extends AppCompatActivity implements OnPageChangeListener, OnLoadCompleteListener ,OnFileDownloadCompleteListener{
    PDFView pdfView;
    private Button btnLoad;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        pdfView= (PDFView) findViewById(R.id.pdfView);
        btnLoad= (Button) findViewById(R.id.btnLoad);
        btnLoad.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pdfView.fromUrl("http://www.anweitong.com/upload/document/standard/national_standards/138793918364316200.pdf")
                        .enableSwipe(true) // allows to block changing pages using swipe
                        .defaultPage(0)
                        .onLoad(PDFViewActivity.this) // called after document is loaded and starts to be rendered
                        .onPageChange(PDFViewActivity.this)
                        .swipeHorizontal(false)
                        .enableAntialiasing(true)
                        .onFileDownload(PDFViewActivity.this)
                        .loadFromUrl();
            }
        });
    }

    @Override
    public void loadComplete(int nbPages) {

    }

    @Override
    public void onPageChanged(int page, int pageCount) {

    }

    @Override
    public void onDownloadComplete(File file) {

    }
}
