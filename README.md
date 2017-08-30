

# Android PdfViewer

__AndroidPdfViewer 1.x is available on [AndroidPdfViewerV1](https://github.com/barteksc/AndroidPdfViewerV1)
repo, where can be developed independently. Version 1.x uses different engine for drawing document on canvas,
so if you don't like 2.x version, try 1.x.__

Library for displaying PDF documents on Android, with `animations`, `gestures`, `zoom` and `double tap` support.
It is based on [PdfiumAndroid](https://github.com/barteksc/PdfiumAndroid) for decoding PDF files. Works on API 11 (Android 3.0) and higher.
Licensed under Apache License 2.0.

## What's new in 2.7.0?
* Merge pull request by [owurman](https://github.com/owurman) with added OnTapListener
* Merge bugfix by [lzwandnju](https://github.com/lzwandnju) to prevent `ArithmeticException: divide by zero`

## Changes in 2.0 API
* `Configurator#defaultPage(int)` and `PDFView#jumpTo(int)` now require page index (i.e. starting from 0)
* `OnPageChangeListener#onPageChanged(int, int)` is called with page index (i.e. starting from 0)
* removed scrollbar
* added scroll handle as a replacement for scrollbar, use with `Configurator#scrollHandle()`
* added `OnPageScrollListener` listener due to continuous scroll, register with `Configurator#onPageScroll()`
* default scroll direction is vertical, so `Configurator#swipeVertical()` was changed to `Configurator#swipeHorizontal()`
* removed minimap and mask configuration

## Installation

Add to _build.gradle_:

`compile 'com.github.barteksc:android-pdf-viewer:2.7.0'`

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
pdfView.fromUri(Uri)
or
pdfView.fromFile(File)
or
pdfView.fromBytes(byte[])
or
pdfView.fromStream(InputStream) // stream is written to bytearray - native code cannot use Java Streams
or
pdfView.fromSource(DocumentSource)
or
pdfView.fromAsset(String)
    .pages(0, 2, 1, 3, 3, 3) // all pages are displayed by default
    .enableSwipe(true) // allows to block changing pages using swipe
    .swipeHorizontal(false)
    .enableDoubletap(true)
    .defaultPage(0)
    // allows to draw something on the current page, usually visible in the middle of the screen
    .onDraw(onDrawListener)
    // allows to draw something on all pages, separately for every page. Called only for visible pages
    .onDrawAll(onDrawListener)
    .onLoad(onLoadCompleteListener) // called after document is loaded and starts to be rendered
    .onPageChange(onPageChangeListener)
    .onPageScroll(onPageScrollListener)
    .onError(onErrorListener)
    .onRender(onRenderListener) // called after document is rendered for the first time
    // called on single tap, return true if handled, false to toggle scroll handle visibility
    .onTap(onTapListener)
    .enableAnnotationRendering(false) // render annotations (such as comments, colors or forms)
    .password(null)
    .scrollHandle(null)
    .enableAntialiasing(true) // improve rendering a little bit on low-res screens
    // spacing between pages in dp. To define spacing color, set view background
    .spacing(0) 
    .load();
```

* `pages` is optional, it allows you to filter and order the pages of the PDF as you need

## Scroll handle

Scroll handle is replacement for **ScrollBar** from 1.x branch.

From version 2.1.0 putting **PDFView** in **RelativeLayout** to use **ScrollHandle** is not required, you can use any layout.

To use scroll handle just register it using method `Configurator#scrollHandle()`.
This method accepts implementations of **ScrollHandle** interface.

There is default implementation shipped with AndroidPdfViewer, and you can use it with
`.scrollHandle(new DefaultScrollHandle(this))`.
**DefaultScrollHandle** is placed on the right (when scrolling vertically) or on the bottom (when scrolling horizontally).
By using constructor with second argument (`new DefaultScrollHandle(this, true)`), handle can be placed left or top.

You can also create custom scroll handles, just implement **ScrollHandle** interface.
All methods are documented as Javadoc comments on interface [source](https://github.com/barteksc/AndroidPdfViewer/tree/master/android-pdf-viewer/src/main/java/com/github/barteksc/pdfviewer/scroll/ScrollHandle.java).

## Document sources
Version 2.3.0 introduced _document sources_, which are just providers for PDF documents.
Every provider implements **DocumentSource** interface.
Predefined providers are available in **com.github.barteksc.pdfviewer.source** package and can be used as
samples for creating custom ones.

Predefined providers can be used with shorthand methods:
```
pdfView.fromUri(Uri)
pdfView.fromFile(File)
pdfView.fromBytes(byte[])
pdfView.fromStream(InputStream)
pdfView.fromAsset(String)
```
Custom providers may be used with `pdfView.fromSource(DocumentSource)` method.

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

### Why I cannot open PDF from URL?
Downloading files is long running process which must be aware of Activity lifecycle, must support some configuration, 
data cleanup and caching, so creating such module will probably end up as new library.

### How can I show last opened page after configuration change?
You have to store current page number and then set it with `pdfView.defaultPage(page)`, refer to sample app

### How can I fit document to screen width (eg. on orientation change)?
Use this code snippet:
``` java
Configurator.onRender(new OnRenderListener() {
    @Override
    public void onInitiallyRendered(int pages, float pageWidth, float pageHeight) {
        pdfView.fitToWidth(); // optionally pass page number
    }
});
```

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
