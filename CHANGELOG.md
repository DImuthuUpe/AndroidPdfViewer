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
