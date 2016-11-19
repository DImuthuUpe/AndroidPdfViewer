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
