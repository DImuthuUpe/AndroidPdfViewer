## 3.2.0-beta.1 (2019-08-18)
* Merge PR #714 with optimized page load
* Merge PR #776 with fix for max & min zoom level
* Merge PR #722 with fix for showing right position when view size changed
* Merge PR #703 with fix for too many threads
* Merge PR #702 with fix for memory leak
* Merge PR #689 with possibility to disable long click
* Merge PR #628 with fix for hiding scroll handle
* Merge PR #627 with `fitEachPage` option
* Merge PR #638 and #406 with fixed NPE
* Merge PR #780 with README fix
* Update compile SDK and support library to 28
* Update Gradle and Gradle Plugin

## 3.1.0-beta.1 (2018-06-29)
* Merge pull request #557 for snapping pages (scrolling page by page)
* merge pull request #618 for night mode
* Merge pull request #566 for `OnLongTapListener`
* Update PdfiumAndroid to 1.9.0, which uses `c++_shared` instead of `gnustl_static`
* Update Gradle Plugin
* Update compile SDK and support library to 26
* Change minimum SDK to 14

## 3.0.0-beta.5 (2018-01-06)
* Fix issue with `Configurator#pages()` from #486
* Fix `IllegalStateException` from #464
* Fix not detecting links reported in #447

## 3.0.0-beta.4 (2017-12-15)
* Fix not loaded pages when using animated `PDFView#jumpTo()`
* Fix NPE in `canScrollVertically()` and `canScrollHorizontally()`

## 3.0.0-beta.3 (2017-11-18)
* Fix bug preventing `OnErrorListener` from being called

## 3.0.0-beta.2 (2017-11-15)
* Fix rendering with maximum zoom
* Improve fit policies
* Update PdfiumAndroid to 1.8.1

## 3.0.0-beta.1 (2017-11-12)
* Add support for documents with different page sizes
* Add support for links
* Add support for defining page fit policy (fit width, height or both)
* Update sample.pdf to contain different page sizes

## 2.8.1 (2017-11-11)
* Fix bug with rendering `PDFView` in Android Studio Layout Editor

## 2.8.0 (2017-10-31)
* Add handling of invalid pages, inspired by pull request #433. Exception on page opening crashed application until now,
currently `OnPageErrorListener` set with `.onPageError()` is called. Invalid page color can be set using `.invalidPageColor()`
* Implement `canScrollVertically()` and `canScrollHorizontally()` methods to work e.g. with `SwipeRefreshLayout`
* Fix bug when `Configurator#load()` method was called before view has been measured, which resulted in empty canvas

## 2.7.0 (2017-08-30)
* Merge pull request by [owurman](https://github.com/owurman) with added OnTapListener
* Merge bugfix by [lzwandnju](https://github.com/lzwandnju) to prevent `ArithmeticException: divide by zero`

## 2.7.0-beta.1 (2017-07-05)
* Updates PdfiumAndroid to 1.7.0 which reduces memory usage about twice and improves performance by using RGB 565 format (when not using `pdfView.useBestQuality(true)`)

## 2.7.0-beta (2017-06-16)
* Update PdfiumAndroid to 1.6.1, which fixed font rendering (issue #253)
* Add `.spacing(int)` method to add spacing (in dp) between document pages
* Fix drawing with `.onDraw(onDrawListener)`
* Add `.onDrawAll(onDrawListener)` method to draw on all pages
* Add small rendering improvements
* Fix rendering when duplicated pages are passed to `.pages(..)`

## 2.6.1 (2017-06-08)
* Fix disappearing scroll handle

## 2.6.0 (2017-06-04)
* Fix fling on single-page documents
* Greatly improve overall fling experience

## 2.5.1 (2017-04-08)
* Temporarily downgrade PdfiumAndroid until #253 will be fixed

## 2.5.0 (2017-03-23)
* Update PdfiumAndroid to 1.6.0, which is based on newest Pdfium from Android 7.1.1. It should fix many rendering and fonts problems
* Add method `pdfView.fitToWidth()`, which called in `OnRenderListener.onInitiallyRendered()` will fit document to width of the screen (inspired by [1stmetro](https://github.com/1stmetro))
* Add change from pull request by [isanwenyu](https://github.com/isanwenyu) to get rid of rare IllegalArgumentException while rendering
* Add `OnRenderListener`, that will be called once, right before document is drawn on the screen
* Add `Configurator.enableAntialiasing()` to improve rendering on low-res screen a little bit (as suggested by [majkimester](majkimester))
* Modify engine to not block UI when big documents are loaded
* Change `Constants` interface and inner interfaces to static public classes, to allow modifying core config values

## 2.4.0 (2016-12-30)
* Merge pull request by [hansinator85](https://github.com/hansinator85) which allows to enable/disable rendering during scale
* Make rendering during scale disabled by default (looks better)
* Merge pull request by [cesquivias](https://github.com/cesquivias) which replaces RenderingAsyncTask with Handler to simply code and work with testing frameworks

## 2.3.0 (2016-11-19)
* Add mechanism for providing documents from different sources - more info in README
* Update PdfiumAndroid to 1.5.0
* Thanks to document sources and PdfiumAndroid update, in-memory documents are supported
* Fix not working OnClickListener on PDFView
* **com.github.barteksc.exception.FileNotFoundException** is deprecated and all usages was removed.
All exceptions are delivered to old Configurator#onError() listener.

## 2.2.0 (2016-11-15)
* Merge pull request by [skarempudi](https://github.com/skarempudi) which fixes SDK 23 permission problems in sample app
* Merge pull request by skarempudi for showing info on phones without file manager
* Add feature from 1.x - canvas is set to drawable from View#getBackground()

## 2.1.0 (2016-09-16)
* fixed loading document from subfolder in assets directory
* fixed scroll handle NPE after document loading error (improvement of 2.0.3 fix)
* fixed incorrect scroll handle position with additional views in RelativeLayout
* improved cache usage and fixed bug with rendering when zooming
* if you are using custom scroll handle: scroll handle implementation changed a little bit, check DefaultScrollHandle source for details

## 2.0.3 (2016-08-30)
* Fix scroll handle NPE after document loading error

## 2.0.2 (2016-08-27)
* Fix exceptions caused by improperly finishing rendering task

## 2.0.1 (2016-08-16)
* Fix NPE when onDetachFromWindow is called

## 2.0.0 (2016-08-14)
* few API changes
* improved rendering speed and accuracy
* added continuous scroll - now it behaves like Adobe Reader and others
* added `fling` scroll gesture for velocity based scrolling
* added scroll handle as a replacement for scrollbar

### Changes in 2.0 API
* `Configurator#defaultPage(int)` and `PDFView#jumpTo(int)` now require page index (i.e. starting from 0)
* `OnPageChangeListener#onPageChanged(int, int)` is called with page index (i.e. starting from 0)
* removed scrollbar
* added scroll handle as a replacement for scrollbar, use with `Configurator#scrollHandle()`
* added `OnPageScrollListener` listener due to continuous scroll, register with `Configurator#onPageScroll()`
* default scroll direction is vertical, so `Configurator#swipeVertical()` was changed to `Configurator#swipeHorizontal()`
* removed minimap and mask configuration

## 1.4.0 (2016-07-25)
* Fix NPE and IndexOutOfBound bugs when rendering parts
* Merge pull request by [paulo-sato-daitan](https://github.com/paulo-sato-daitan) for disabling page change animation
* Merge pull request by [Miha-x64](https://github.com/Miha-x64) for drawing background if set on `PDFView`

## 1.3.0 (2016-07-13)
* update PdfiumAndroid to 1.4.0 with support for rendering annotations
* merge pull request by [usef](https://github.com/usef) for rendering annotations

## 1.2.0 (2016-07-11)
* update PdfiumAndroid to 1.3.1 with support for bookmarks, Table Of Contents and documents with password:
  * added method `PDFView#getDocumentMeta()`, which returns document metadata
  * added method `PDFView#getTableOfContents()`, which returns whole tree of bookmarks in PDF document
  * added method `Configurator#password(String)`
* added horizontal mode to **ScrollBar** - use `ScrollBar#setHorizontal(true)` or `app:sb_horizontal="true"` in XML
* block interaction with `PDFView` when document is not loaded - prevent some exceptions
* fix `PDFView` exceptions in layout preview (edit mode)

## 1.1.2 (2016-06-27)
* update PdfiumAndroid to 1.1.0, which fixes displaying multiple `PDFView`s at the same time and few errors with loading PDF documents.

## 1.1.1 (2016-06-17)
* fixes bug with strange behavior when indices passed to `.pages()` don't start with `0`.

## 1.1.0 (2016-06-16)
* added method `pdfView.fromUri(Uri)` for opening files from content providers
* updated PdfiumAndroid to 1.0.3, which should fix bug with exception
* updated sample with demonstration of `fromUri()` method
* some minor fixes

## 1.0.0 (2016-06-06)
* Initial release
