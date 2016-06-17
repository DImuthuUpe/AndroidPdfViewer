

# Android PdfViewer

Library for displaying PDF documents on Android, with `animations`, `gestures`, `zoom` and `double tap` support.
It is based on [PdfiumAndroid](https://github.com/barteksc/PdfiumAndroid) for decoding PDF files. Works on API 11 and higher.
Licensed under Apache License 2.0.

## What's new in 1.1.0?
* added method `pdfView.fromUri(Uri)` for opening files from content providers
* updated PdfiumAndroid to 1.0.3, which should fix bug with exception
* updated sample with demonstration of `fromUri()` method
* some minor fixes

Version 1.1.1 fixes bug with strange behavior when indices passed to `.pages()` don't start with `0`.


Next release is coming soon, it will introduce continuous scroll through whole document
and some incompatibilities with current API (only few small).

## Installation

Add to _build.gradle_:

`compile 'com.github.barteksc:android-pdf-viewer:1.1.1'`

Library is available in jcenter repository, probably it'll be in Maven Central soon.

## Include PDFView in your layout

``` xml
<com.github.barteksc.pdfviewer.PDFView
        android:id="@+id/pdfView"
        android:layout_width="match_parent"
        android:layout_height="match_parent"/>
```

## Load a PDF file

All available options with default values:
``` java
pdfView.fromAsset(pdfName)
    .pages(0, 2, 1, 3, 3, 3) //all pages are displayed by default
    .enableSwipe(true)
    .enableDoubletap(true)
    .swipeVertical(false)
    .defaultPage(1)
    .showMinimap(false)
    .onDraw(onDrawListener)
    .onLoad(onLoadCompleteListener)
    .onPageChange(onPageChangeListener)
    .onError(onErrorListener)
    .load();
```

* `enableSwipe` is optional, it allows you to block changing pages using swipe
* `pages` is optional, it allows you to filter and order the pages of the PDF as you need
* `onDraw` is also optional, and allows you to draw something on a provided canvas, above the current page

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

## Possible questions
### Why resulting apk is so big?
Android PdfViewer depends on PdfiumAndroid, which is set of native libraries (almost 16 MB) for many architectures.
Apk must contain all this libraries to run on every device available on market.
Fortunately, Google Play allows us to upload multiple apks, e.g. one per every architecture.
There is good article on automatically splitting your application into multiple apks,
available [here](http://ph0b.com/android-studio-gradle-and-ndk-integration/).
Most important section is _Improving multiple APKs creation and versionCode handling with APK Splits_, but whole article is worth reading.
You only need to do this in your application, no need for forking PdfiumAndroid or so.

## One more thing
If you have any suggestions on making this lib better, write me, create issue or write some code and send pull request.

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
