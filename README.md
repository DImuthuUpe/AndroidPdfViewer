

# Android PdfViewer

Library for displaying PDF documents on Android, with `animations`, `gestures`, `zoom` and `double tap` support.
It is based on [PdfiumAndroid](https://github.com/barteksc/PdfiumAndroid) for decoding PDF files. Works on API 11 and higher.
Licensed under Apache License 2.0.

## Installation

Add to _build.gradle_:

`compile 'com.github.barteksc:android-pdf-viewer:1.0.0'`

Library is available in jcenter repository, probably it'll be in Maven Central soon.

## Include PDFView in your layout

``` xml
<com.github.barteksc.pdfviewer.PDFView
        android:id="@+id/pdfView"
        android:layout_width="match_parent"
        android:layout_height="match_parent"/>
```

## Load a PDF file

``` java
pdfView.fromAsset(pdfName)
    .pages(0, 2, 1, 3, 3, 3)
    .defaultPage(1)
    .showMinimap(false)
    .enableSwipe(true)
    .onDraw(onDrawListener)
    .onLoad(onLoadCompleteListener)
    .onPageChange(onPageChangeListener)
    .onError(onErrorListener)
    .load();
```

* ```pages``` is optional, it allows you to filter and order the pages of the PDF as you need
* ```onDraw``` is also optional, and allows you to draw something on a provided canvas, above the current page

## Show scrollbar

Use **ScrollBar** class to place scrollbar view near **PDFView**

1. in layout XML (it's important that the parent view is **RelativeLayout**)

    ``` xml
    <RelativeLayout xmlns:android="http://schemas.android.com/apk/res/android"
        android:layout_width="match_parent"
        android:layout_height="match_parent">

        <com.github.barteksc.pdfviewer.PDFView
            android:id="@+id/pdfView"
            android:layout_width="match_parent"
            android:layout_height="match_parent"
            android:layout_toLeftOf="@+id/scrollBar"/>

        <com.github.barteksc.pdfviewer.ScrollBar
            android:id="@+id/scrollBar"
            android:layout_width="wrap_content"
            android:layout_height="match_parent"
            android:layout_alignParentRight="true"
            android:layout_alignParentEnd="true" />

    </RelativeLayout>
    ```
2. in activity or fragment
    ``` java

    @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);

            ...

            PDFView pdfView = (PDFView) findViewById(R.id.pdfView);
            ScrollBar scrollBar = (ScrollBar) findViewById(R.id.scrollBar);
            pdfView.setScrollBar(scrollBar);
        }

    ```

Scrollbar styling:
``` xml
    <com.github.barteksc.pdfviewpager.view.ScrollBar
        android:layout_width="wrap_content"
        android:layout_height="match_parent"
        app:sb_handlerColor="..." <!-- scrollbar handler color -->
        app:sb_indicatorColor="..." <!-- background color of current page indicator -->
        app:sb_indicatorTextColor="..." <!-- text color of current page indicator -->
        android:background="..." <!-- scrollbar background -->
        />
```

**ScrollBarPageIndicator** is added to scrollbar automatically and is shown while dragging scrollbar handler,
 displaying number of page on current position.

## Additional options

### Bitmap quality
By default, generated bitmaps are _compressed_ with `RGB_565` format to reduce memory consumption.
Rendering with `ARGB_8888` can be forced by using `pdfView.useBestQuality(true)` method.

### Double tap zooming
There are three zoom levels: min (default 1), mid (default 1.75) and max (default 3). On first double tap,
view is zoomed to mid level, on second to max level, and on third returns to min level.
If you are between mid and max levels, double tapping causes zooming to max and so on.

Zoom levels can be changed using following methods:

``` java
void setMinZoom(float zoom);
void setMidZoom(float zoom);
void setMaxZoom(float zoom);
```

## License

Created with the help of android-pdfview by [Joan Zapata](http://joanzapata.com/)
```
Copyright 2016 Bartosz Schiller

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
```
