/**
 * Copyright 2016 Bartosz Schiller
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.github.barteksc.pdfviewer;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.PointF;
import android.graphics.Rect;
import android.graphics.RectF;
import android.net.Uri;
import android.os.AsyncTask;
import android.util.AttributeSet;
import android.util.Log;
import android.view.SurfaceView;

import com.github.barteksc.pdfviewer.exception.FileNotFoundException;
import com.github.barteksc.pdfviewer.listener.OnDrawListener;
import com.github.barteksc.pdfviewer.listener.OnErrorListener;
import com.github.barteksc.pdfviewer.listener.OnLoadCompleteListener;
import com.github.barteksc.pdfviewer.listener.OnPageChangeListener;
import com.github.barteksc.pdfviewer.model.PagePart;
import com.github.barteksc.pdfviewer.util.ArrayUtils;
import com.github.barteksc.pdfviewer.util.Constants;
import com.github.barteksc.pdfviewer.util.NumberUtils;
import com.shockwave.pdfium.PdfDocument;
import com.shockwave.pdfium.PdfiumCore;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import static com.github.barteksc.pdfviewer.util.Constants.Cache.CACHE_SIZE;

/**
 * It supports animations, zoom, cache, and swipe.
 * <p>
 * To fully understand this class you must know its principles :
 * - The PDF document is seen as if we always want to draw all the pages.
 * - The thing is that we only draw the visible parts.
 * - All parts are the same size, this is because we can't interrupt a native page rendering,
 * so we need these renderings to be as fast as possible, and be able to interrupt them
 * as soon as we can.
 * - The parts are loaded when the current offset or the current zoom level changes
 * <p>
 * Important :
 * - DocumentPage = A page of the PDF document.
 * - UserPage = A page as defined by the user.
 * By default, they're the same. But the user can change the pages order
 * using {@link #load(String, boolean, String, OnLoadCompleteListener, OnErrorListener, int[])}. In this
 * particular case, a userPage of 5 can refer to a documentPage of 17.
 */
public class PDFView extends SurfaceView {

    private static final String TAG = PDFView.class.getSimpleName();

    public static final float DEFAULT_MAX_SCALE = 3.0f;
    public static final float DEFAULT_MID_SCALE = 1.75f;
    public static final float DEFAULT_MIN_SCALE = 1.0f;

    private float minZoom = DEFAULT_MIN_SCALE;
    private float midZoom = DEFAULT_MID_SCALE;
    private float maxZoom = DEFAULT_MAX_SCALE;

    /**
     * Rendered parts go to the cache manager
     */
    private CacheManager cacheManager;

    /**
     * Animation manager manage all offset and zoom animation
     */
    private AnimationManager animationManager;

    /**
     * Drag manager manage all touch events
     */
    private DragPinchManager dragPinchManager;

    /**
     * The pages the user want to display in order
     * (ex: 0, 2, 2, 8, 8, 1, 1, 1)
     */
    private int[] originalUserPages;

    /**
     * The same pages but with a filter to avoid repetition
     * (ex: 0, 2, 8, 1)
     */
    private int[] filteredUserPages;

    /**
     * The same pages but with a filter to avoid repetition
     * (ex: 0, 1, 1, 2, 2, 3, 3, 3)
     */
    private int[] filteredUserPageIndexes;

    /**
     * Number of pages in the loaded PDF document
     */
    private int documentPageCount;

    /**
     * The index of the current sequence
     */
    private int currentPage;

    /**
     * The index of the current sequence
     */
    private int currentFilteredPage;

    /**
     * The actual width and height of the pages in the PDF document
     */
    private int pageWidth, pageHeight;

    /**
     * The optimal width and height of the pages to fit the component size
     */
    private float optimalPageWidth, optimalPageHeight;

    /**
     * If you picture all the pages side by side in their optimal width,
     * and taking into account the zoom level, the current offset is the
     * position of the left border of the screen in this big picture
     */
    private float currentXOffset = 0;

    /**
     * If you picture all the pages side by side in their optimal width,
     * and taking into account the zoom level, the current offset is the
     * position of the left border of the screen in this big picture
     */
    private float currentYOffset = 0;

    /**
     * The zoom level, always >= 1
     */
    private float zoom = 1f;

    /**
     * Coordinates of the left mask on the screen
     */
    private RectF leftMask;

    /**
     * Coordinates of the right mask on the screen
     */
    private RectF rightMask;

    /**
     * True if the PDFView has been recycled
     */
    private boolean recycled = true;

    /**
     * Current state of the view
     */
    private State state = State.DEFAULT;

    /**
     * Async task used during the loading phase to decode a PDF document
     */
    private DecodingAsyncTask decodingAsyncTask;

    /**
     * Async task always playing in the background and proceeding rendering tasks
     */
    private RenderingAsyncTask renderingAsyncTask;

    /**
     * Call back object to call when the PDF is loaded
     */
    private OnLoadCompleteListener onLoadCompleteListener;

    private OnErrorListener onErrorListener;

    /**
     * Call back object to call when the page has changed
     */
    private OnPageChangeListener onPageChangeListener;

    /**
     * Call back object to call when the above layer is to drawn
     */
    private OnDrawListener onDrawListener;

    /**
     * Paint object for drawing
     */
    private Paint paint;

    /**
     * Paint object for drawing mask
     */
    private Paint maskPaint;

    /**
     * Paint object for drawing debug stuff
     */
    private Paint debugPaint;

    /**
     * Paint object for minimap background
     */
    private Paint paintMinimapBack;

    private Paint paintMinimapFront;

    /**
     * True if should draw map on the top right corner
     */
    private boolean miniMapRequired;

    /**
     * Bounds of the minimap
     */
    private RectF minimapBounds;

    /**
     * Bounds of the minimap
     */
    private RectF minimapScreenBounds;

    private int defaultPage = 0;

    private boolean userWantsMinimap = false;

    /**
     * True if should scroll through pages vertically instead of horizontally
     */
    private boolean swipeVertical = false;

    /**
     * True if should show a page with animation
     */
    private boolean showPageWithAnimation = true;

    /**
     * Pdfium core for loading and rendering PDFs
     */
    private PdfiumCore pdfiumCore;

    private PdfDocument pdfDocument;

    private ScrollBar scrollBar;

    /**
     * True if bitmap should use ARGB_8888 format and take more memory
     * False if bitmap should be compressed by using RGB_565 format and take less memory
     */
    private boolean bestQuality = false;

    /**
     * True if annotations should be rendered
     * False otherwise
     */
    private boolean annotationRendering = false;

    /**
     * Storing already opened pages. Used form optimizing Pdfium calls
     */
    private List<Integer> openedPages = new ArrayList<>();

    /**
     * Construct the initial view
     */
    public PDFView(Context context, AttributeSet set) {
        super(context, set);

        if (isInEditMode()) {
            return;
        }

        miniMapRequired = false;
        cacheManager = new CacheManager();
        animationManager = new AnimationManager(this);
        dragPinchManager = new DragPinchManager(this);

        paint = new Paint();
        debugPaint = new Paint();
        debugPaint.setStyle(Style.STROKE);
        paintMinimapBack = new Paint();
        paintMinimapBack.setStyle(Style.FILL);
        paintMinimapBack.setColor(Color.BLACK);
        paintMinimapBack.setAlpha(50);
        paintMinimapFront = new Paint();
        paintMinimapFront.setStyle(Style.FILL);
        paintMinimapFront.setColor(Color.BLACK);
        paintMinimapFront.setAlpha(50);

        // A surface view does not call
        // onDraw() as a default but we need it.
        setWillNotDraw(false);

        pdfiumCore = new PdfiumCore(context);
    }

    private void load(String path, boolean isAsset, String password, OnLoadCompleteListener listener, OnErrorListener onErrorListener) {
        load(path, isAsset, password, listener, onErrorListener, null);
    }

    private void load(String path, boolean isAsset, String password, OnLoadCompleteListener onLoadCompleteListener, OnErrorListener onErrorListener, int[] userPages) {

        if (!recycled) {
            throw new IllegalStateException("Don't call load on a PDF View without recycling it first.");
        }

        // Manage UserPages if not null
        if (userPages != null) {
            this.originalUserPages = userPages;
            this.filteredUserPages = ArrayUtils.deleteDuplicatedPages(originalUserPages);
            this.filteredUserPageIndexes = ArrayUtils.calculateIndexesInDuplicateArray(originalUserPages);
        }

        this.onLoadCompleteListener = onLoadCompleteListener;
        this.onErrorListener = onErrorListener;

        recycled = false;
        // Start decoding document
        decodingAsyncTask = new DecodingAsyncTask(path, isAsset, password, this, pdfiumCore);
        decodingAsyncTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);

    }

    /**
     * Go to the given page.
     *
     * @param page Page number starting from 1.
     */
    public void jumpTo(int page) {
        showPage(page - 1);
    }

    void showPage(int pageNb) {
        if (recycled) {
            return;
        }
        state = State.SHOWN;

        // Check the page number and makes the
        // difference between UserPages and DocumentPages
        pageNb = determineValidPageNumberFrom(pageNb);
        currentPage = pageNb;
        currentFilteredPage = pageNb;
        if (filteredUserPageIndexes != null) {
            if (pageNb >= 0 && pageNb < filteredUserPageIndexes.length) {
                pageNb = filteredUserPageIndexes[pageNb];
                currentFilteredPage = pageNb;
            }
        }

        // Reset the zoom and center the page on the screen
        resetZoom();
        if (showPageWithAnimation) {
            if (swipeVertical) {
                animationManager.startYAnimation(currentYOffset, calculateCenterOffsetForPage(pageNb));
            } else {
                animationManager.startXAnimation(currentXOffset, calculateCenterOffsetForPage(pageNb));
            }
        } else {
            if (swipeVertical) {
                moveTo(getCurrentXOffset(), calculateCenterOffsetForPage(pageNb));
            } else {
                moveTo(calculateCenterOffsetForPage(pageNb), getCurrentYOffset());
            }
        }

        loadPages();

        if (scrollBar != null) {
            scrollBar.pageChanged(currentPage);
        }

        if (onPageChangeListener != null) {
            onPageChangeListener.onPageChanged(currentPage + 1, getPageCount());
        }
    }

    public int getPageCount() {
        if (originalUserPages != null) {
            return originalUserPages.length;
        }
        return documentPageCount;
    }

    public void enableSwipe(boolean enableSwipe) {
        dragPinchManager.setSwipeEnabled(enableSwipe);
    }

    public void enableDoubletap(boolean enableDoubletap) {
        this.dragPinchManager.enableDoubletap(enableDoubletap);
    }

    private void setOnPageChangeListener(OnPageChangeListener onPageChangeListener) {
        this.onPageChangeListener = onPageChangeListener;
    }

    OnPageChangeListener getOnPageChangeListener() {
        return this.onPageChangeListener;
    }

    private void setOnDrawListener(OnDrawListener onDrawListener) {
        this.onDrawListener = onDrawListener;
    }

    public void recycle() {

        // Stop tasks
        if (renderingAsyncTask != null) {
            renderingAsyncTask.cancel(true);
        }
        if (decodingAsyncTask != null) {
            decodingAsyncTask.cancel(true);
        }

        // Clear caches
        cacheManager.recycle();

        if (pdfiumCore != null && pdfDocument != null) {
            pdfiumCore.closeDocument(pdfDocument);
        }

        originalUserPages = null;
        filteredUserPages = null;
        filteredUserPageIndexes = null;
        openedPages.clear();
        pdfDocument = null;
        recycled = true;
        state = State.DEFAULT;
    }

    public boolean isRecycled() {
        return recycled;
    }

    @Override
    protected void onDetachedFromWindow() {
        recycle();
        super.onDetachedFromWindow();
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        if (isInEditMode()) {
            return;
        }
        animationManager.stopAll();
        calculateOptimalWidthAndHeight();
        loadPages();
        if (swipeVertical)
            moveTo(currentXOffset, calculateCenterOffsetForPage(currentFilteredPage));
        else
            moveTo(calculateCenterOffsetForPage(currentFilteredPage), currentYOffset);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        if (isInEditMode()) {
            return;
        }
        // As I said in this class javadoc, we can think of this canvas as a huge
        // strip on which we draw all the images. We actually only draw the rendered
        // parts, of course, but we render them in the place they belong in this huge
        // strip.

        // That's where Canvas.translate(x, y) becomes very helpful.
        // This is the situation :
        //  _______________________________________________
        // |   			 |					 			   |
        // | the actual  |					The big strip  |
        // |	canvas	 | 								   |
        // |_____________|								   |
        // |_______________________________________________|
        //
        // If the rendered part is on the bottom right corner of the strip
        // we can draw it but we won't see it because the canvas is not big enough.

        // But if we call translate(-X, -Y) on the canvas just before drawing the object :
        //  _______________________________________________
        // |   			  					  _____________|
        // |   The big strip     			 |			   |
        // |		    					 |	the actual |
        // |								 |	canvas	   |
        // |_________________________________|_____________|
        //
        // The object will be on the canvas.
        // This technique is massively used in this method, and allows
        // abstraction of the screen position when rendering the parts.

        // Draws background
        Drawable bg = getBackground();
        if (bg == null) {
            canvas.drawColor(Color.WHITE);
        } else {
            bg.draw(canvas);
        }

        if (recycled) {
            return;
        }

        if (state != State.SHOWN) {
            return;
        }

        // Moves the canvas before drawing any element
        float currentXOffset = this.currentXOffset;
        float currentYOffset = this.currentYOffset;
        canvas.translate(currentXOffset, currentYOffset);

        // Draws thumbnails
        for (PagePart part : cacheManager.getThumbnails()) {
            drawPart(canvas, part);
        }

        // Draws parts
        for (PagePart part : cacheManager.getPageParts()) {
            drawPart(canvas, part);
        }

        // Draws the user layer
        if (onDrawListener != null) {
            canvas.translate(toCurrentScale(currentFilteredPage * optimalPageWidth), 0);

            onDrawListener.onLayerDrawn(canvas, //
                    toCurrentScale(optimalPageWidth), //
                    toCurrentScale(optimalPageHeight),
                    currentPage);

            canvas.translate(-toCurrentScale(currentFilteredPage * optimalPageWidth), 0);
        }

        // Restores the canvas position
        canvas.translate(-currentXOffset, -currentYOffset);

        // Draws mask around current page
        canvas.drawRect(leftMask, maskPaint);
        canvas.drawRect(rightMask, maskPaint);

        // If minimap shown draws it
        if (userWantsMinimap && miniMapRequired) {
            drawMiniMap(canvas);
        }
    }

    public void onLayerUpdate() {
        invalidate();
    }

    /**
     * Draw a given PagePart on the canvas
     */
    private void drawPart(Canvas canvas, PagePart part) {
        // Can seem strange, but avoid lot of calls
        RectF pageRelativeBounds = part.getPageRelativeBounds();
        Bitmap renderedBitmap = part.getRenderedBitmap();

        // Move to the target page
        float localTranslationX = 0;
        float localTranslationY = 0;
        if (swipeVertical)
            localTranslationY = toCurrentScale(part.getUserPage() * optimalPageHeight);
        else
            localTranslationX = toCurrentScale(part.getUserPage() * optimalPageWidth);
        canvas.translate(localTranslationX, localTranslationY);

        Rect srcRect = new Rect(0, 0, renderedBitmap.getWidth(), //
                renderedBitmap.getHeight());

        float offsetX = toCurrentScale(pageRelativeBounds.left * optimalPageWidth);
        float offsetY = toCurrentScale(pageRelativeBounds.top * optimalPageHeight);
        float width = toCurrentScale(pageRelativeBounds.width() * optimalPageWidth);
        float height = toCurrentScale(pageRelativeBounds.height() * optimalPageHeight);

        // If we use float values for this rectangle, there will be
        // a possible gap between page parts, especially when
        // the zoom level is high.
        RectF dstRect = new RectF((int) offsetX, (int) offsetY, //
                (int) (offsetX + width), //
                (int) (offsetY + height));

        // Check if bitmap is in the screen
        float translationX = currentXOffset + localTranslationX;
        float translationY = currentYOffset + localTranslationY;
        if (translationX + dstRect.left >= getWidth() || translationX + dstRect.right <= 0 ||
                translationY + dstRect.top >= getHeight() || translationY + dstRect.bottom <= 0) {
            canvas.translate(-localTranslationX, -localTranslationY);
            return;
        }

        canvas.drawBitmap(renderedBitmap, srcRect, dstRect, paint);

        if (Constants.DEBUG_MODE) {
            debugPaint.setColor(part.getUserPage() % 2 == 0 ? Color.RED : Color.BLUE);
            canvas.drawRect(dstRect, debugPaint);
        }

        // Restore the canvas position
        canvas.translate(-localTranslationX, -localTranslationY);

    }

    private void drawMiniMap(Canvas canvas) {
        canvas.drawRect(minimapBounds, paintMinimapBack);
        canvas.drawRect(minimapScreenBounds, paintMinimapFront);
    }

    /**
     * Load all the parts around the center of the screen,
     * taking into account X and Y offsets, zoom level, and
     * the current page displayed
     */
    public void loadPages() {
        if (optimalPageWidth == 0 || optimalPageHeight == 0) {
            return;
        }

        // Cancel all current tasks
        renderingAsyncTask.removeAllTasks();
        cacheManager.makeANewSet();

        // Find current index in filtered user pages
        int index = currentPage;
        if (filteredUserPageIndexes != null) {
            index = filteredUserPageIndexes[currentPage];
        }

        // Loop through the pages like [...][4][2][0][1][3][...]
        // loading as many parts as it can.
        int parts = 0;
        for (int i = 0; i <= Constants.LOADED_SIZE / 2 && parts < CACHE_SIZE; i++) {
            parts += loadPage(index + i, CACHE_SIZE - parts);
            if (i != 0 && parts < CACHE_SIZE) {
                parts += loadPage(index - i, CACHE_SIZE - parts);
            }
        }

        invalidate();
    }

    /**
     * Render a page, creating 1 to <i>nbOfPartsLoadable</i> page parts. <br><br>
     * <p>
     * This is one of the trickiest method of this library. It finds
     * the DocumentPage associated with the given UserPage, loads its
     * thumbnail, cut this page into 256x256 blocs considering the
     * current zoom level, calculate the bloc containing the center of
     * the screen, and start loading these parts in a spiral {@link SpiralLoopManager},
     * only if the given part is not already in the Cache, in which case it
     * moves the part up in the cache.
     *
     * @param userPage          The user page to load.
     * @param nbOfPartsLoadable Maximum number of parts it can load.
     * @return The number of parts loaded.
     */
    private int loadPage(final int userPage, final int nbOfPartsLoadable) {

        // Finds the document page associated with the given userPage
        int documentPage = userPage;
        if (filteredUserPages != null) {
            if (userPage < 0 || userPage >= filteredUserPages.length) {
                return 0;
            } else {
                documentPage = filteredUserPages[userPage];
            }
        }
        final int documentPageFinal = documentPage;
        if (documentPage < 0 || userPage >= documentPageCount) {
            return 0;
        }

        if (!openedPages.contains(documentPage)) {
            openedPages.add(documentPage);
            pdfiumCore.openPage(pdfDocument, documentPage);
        }

        // Render thumbnail of the page
        if (!cacheManager.containsThumbnail(userPage, documentPage, //
                (int) (optimalPageWidth * Constants.THUMBNAIL_RATIO), //
                (int) (optimalPageHeight * Constants.THUMBNAIL_RATIO), //
                new RectF(0, 0, 1, 1))) {
            renderingAsyncTask.addRenderingTask(userPage, documentPage, //
                    (int) (optimalPageWidth * Constants.THUMBNAIL_RATIO), //
                    (int) (optimalPageHeight * Constants.THUMBNAIL_RATIO), //
                    new RectF(0, 0, 1, 1), true, 0, bestQuality, annotationRendering);
        }

        // When we want to render a 256x256 bloc, we also need to provide
        // the bounds (left, top, right, bottom) of the rendered part in
        // the PDF page. These four coordinates are ratios (0 -> 1), where
        // (0,0) is the top left corner of the PDF page, and (1,1) is the
        // bottom right corner.
        float ratioX = 1f / optimalPageWidth;
        float ratioY = 1f / optimalPageHeight;
        final float partHeight = (Constants.PART_SIZE * ratioY) / zoom;
        final float partWidth = (Constants.PART_SIZE * ratioX) / zoom;
        final int nbRows = (int) Math.ceil(1f / partHeight);
        final int nbCols = (int) Math.ceil(1f / partWidth);
        final float pageRelativePartWidth = 1f / (float) nbCols;
        final float pageRelativePartHeight = 1f / (float) nbRows;

        // To improve user experience, we need to start displaying the
        // 256x256 blocs with the middle of the screen. Imagine the cut
        // page as a grid. This part calculates which cell of this grid
        // is currently in the middle of the screen, given the current
        // zoom level and the offsets.
        float middleOfScreenX = (-currentXOffset + getWidth() / 2);
        float middleOfScreenY = (-currentYOffset + getHeight() / 2);
        float middleOfScreenPageX;
        float middleOfScreenPageY;
        if (!swipeVertical) {
            middleOfScreenPageX = middleOfScreenX - userPage * toCurrentScale(optimalPageWidth);
            middleOfScreenPageY = middleOfScreenY;
        } else {
            middleOfScreenPageY = middleOfScreenY - userPage * toCurrentScale(optimalPageHeight);
            middleOfScreenPageX = middleOfScreenX;
        }
        float middleOfScreenPageXRatio = middleOfScreenPageX / toCurrentScale(optimalPageWidth);
        float middleOfScreenPageYRatio = middleOfScreenPageY / toCurrentScale(optimalPageHeight);
        int startingRow = (int) (middleOfScreenPageYRatio * nbRows);
        int startingCol = (int) (middleOfScreenPageXRatio * nbCols);

        // Avoid outside values
        startingRow = NumberUtils.limit(startingRow, 0, nbRows);
        startingCol = NumberUtils.limit(startingCol, 0, nbCols);

        // Prepare the loop listener
        class SpiralLoopListenerImpl implements SpiralLoopManager.SpiralLoopListener {
            int nbItemTreated = 0;

            @Override
            public boolean onLoop(int row, int col) {

                // Create relative page bounds
                float relX = pageRelativePartWidth * col;
                float relY = pageRelativePartHeight * row;
                float relWidth = pageRelativePartWidth;
                float relHeight = pageRelativePartHeight;

                // Adjust width and height to
                // avoid being outside the page
                float renderWidth = Constants.PART_SIZE / relWidth;
                float renderHeight = Constants.PART_SIZE / relHeight;
                if (relX + relWidth > 1) {
                    relWidth = 1 - relX;
                }
                if (relY + relHeight > 1) {
                    relHeight = 1 - relY;
                }
                renderWidth *= relWidth;
                renderHeight *= relHeight;
                RectF pageRelativeBounds = new RectF(relX, relY, relX + relWidth, relY + relHeight);

                if (renderWidth != 0 && renderHeight != 0) {

                    // Check it the calculated part is already contained in the Cache
                    // If it is, this call will insure the part will go to the right
                    // place in the cache and won't be deleted if the cache need space.
                    if (!cacheManager.upPartIfContained(userPage, documentPageFinal, //
                            renderWidth, renderHeight, pageRelativeBounds, nbItemTreated)) {

                        // If not already in cache, register the rendering
                        // task for further execution.
                        renderingAsyncTask.addRenderingTask(userPage, documentPageFinal, //
                                renderWidth, renderHeight, pageRelativeBounds, false, nbItemTreated, bestQuality, annotationRendering);
                    }

                }

                nbItemTreated++;
                if (nbItemTreated >= nbOfPartsLoadable) {
                    // Return false to stop the loop
                    return false;
                }
                return true;
            }
        }

        // Starts the loop
        SpiralLoopListenerImpl spiralLoopListener;
        new SpiralLoopManager(spiralLoopListener = new SpiralLoopListenerImpl())//
                .startLoop(nbRows, nbCols, startingRow, startingCol);

        return spiralLoopListener.nbItemTreated;
    }

    /**
     * Called when the PDF is loaded
     */
    public void loadComplete(PdfDocument pdfDocument) {
        this.documentPageCount = pdfiumCore.getPageCount(pdfDocument);

        int firstPageIdx = 0;
        if (originalUserPages != null) {
            firstPageIdx = originalUserPages[0];
        }

        // We assume all the pages are the same size
        this.pdfDocument = pdfDocument;
        pdfiumCore.openPage(pdfDocument, firstPageIdx);
        openedPages.add(firstPageIdx);
        this.pageWidth = pdfiumCore.getPageWidth(pdfDocument, firstPageIdx);
        this.pageHeight = pdfiumCore.getPageHeight(pdfDocument, firstPageIdx);
        state = State.LOADED;
        calculateOptimalWidthAndHeight();

        renderingAsyncTask = new RenderingAsyncTask(this, pdfiumCore, pdfDocument);
        renderingAsyncTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);

        if (scrollBar != null) {
            scrollBar.pdfLoaded();
        }

        // Notify the listener
        jumpTo(defaultPage);
        if (onLoadCompleteListener != null) {
            onLoadCompleteListener.loadComplete(documentPageCount);
        }
    }

    public void loadError(Throwable t) {
        recycle();
        invalidate();
        if (this.onErrorListener != null) {
            this.onErrorListener.onError(t);
        } else {
            Log.e("PDFView", "load pdf error", t);
        }
    }

    /**
     * Called when a rendering task is over and
     * a PagePart has been freshly created.
     *
     * @param part The created PagePart.
     */
    public void onBitmapRendered(PagePart part) {
        if (part.isThumbnail()) {
            cacheManager.cacheThumbnail(part);
        } else {
            cacheManager.cachePart(part);
        }
        invalidate();
    }

    /**
     * Given the UserPage number, this method restrict it
     * to be sure it's an existing page. It takes care of
     * using the user defined pages if any.
     *
     * @param userPage A page number.
     * @return A restricted valid page number (example : -2 => 0)
     */
    private int determineValidPageNumberFrom(int userPage) {
        if (userPage <= 0) {
            return 0;
        }
        if (originalUserPages != null) {
            if (userPage >= originalUserPages.length) {
                return originalUserPages.length - 1;
            }
        } else {
            if (userPage >= documentPageCount) {
                return documentPageCount - 1;
            }
        }
        return userPage;
    }

    /**
     * Calculate the x/y-offset needed to have the given
     * page centered on the screen. It doesn't take into
     * account the zoom level.
     *
     * @param pageNb The page number.
     * @return The x/y-offset to use to have the pageNb centered.
     */
    private float calculateCenterOffsetForPage(int pageNb) {
        if (swipeVertical) {
            float imageY = -(pageNb * optimalPageHeight);
            imageY += getHeight() / 2 - optimalPageHeight / 2;
            return imageY;
        } else {
            float imageX = -(pageNb * optimalPageWidth);
            imageX += getWidth() / 2 - optimalPageWidth / 2;
            return imageX;
        }
    }

    /**
     * Calculate the optimal width and height of a page
     * considering the area width and height
     */
    private void calculateOptimalWidthAndHeight() {
        if (state == State.DEFAULT || getWidth() == 0) {
            return;
        }

        float maxWidth = getWidth(), maxHeight = getHeight();
        float w = pageWidth, h = pageHeight;
        float ratio = w / h;
        w = maxWidth;
        h = (float) Math.floor(maxWidth / ratio);
        if (h > maxHeight) {
            h = maxHeight;
            w = (float) Math.floor(maxHeight * ratio);
        }

        optimalPageWidth = w;
        optimalPageHeight = h;

        calculateMasksBounds();
        calculateMinimapBounds();
    }

    /**
     * Place the minimap background considering the optimal width and height
     * and the MINIMAP_MAX_SIZE.
     */
    private void calculateMinimapBounds() {
        float ratioX = Constants.MINIMAP_MAX_SIZE / optimalPageWidth;
        float ratioY = Constants.MINIMAP_MAX_SIZE / optimalPageHeight;
        float ratio = Math.min(ratioX, ratioY);
        float minimapWidth = optimalPageWidth * ratio;
        float minimapHeight = optimalPageHeight * ratio;
        minimapBounds = new RectF(getWidth() - 5 - minimapWidth, 5, getWidth() - 5, 5 + minimapHeight);
        calculateMinimapAreaBounds();
    }

    /**
     * Place the minimap current rectangle considering the minimap bounds
     * the zoom level, and the current X/Y offsets
     */
    private void calculateMinimapAreaBounds() {
        if (minimapBounds == null) {
            return;
        }

        if (zoom == 1f) {
            miniMapRequired = false;
        } else {
            // Calculates the bounds of the current displayed area
            float x = (-currentXOffset - toCurrentScale(currentFilteredPage * optimalPageWidth)) //
                    / toCurrentScale(optimalPageWidth) * minimapBounds.width();
            float width = getWidth() / toCurrentScale(optimalPageWidth) * minimapBounds.width();
            float y = -currentYOffset / toCurrentScale(optimalPageHeight) * minimapBounds.height();
            float height = getHeight() / toCurrentScale(optimalPageHeight) * minimapBounds.height();
            minimapScreenBounds = new RectF(minimapBounds.left + x, minimapBounds.top + y, //
                    minimapBounds.left + x + width, minimapBounds.top + y + height);
            minimapScreenBounds.intersect(minimapBounds);
            miniMapRequired = true;
        }
    }

    /**
     * Place the left and right masks around the current page.
     */
    private void calculateMasksBounds() {
        leftMask = new RectF(0, 0, getWidth() / 2 - toCurrentScale(optimalPageWidth) / 2, getHeight());
        rightMask = new RectF(getWidth() / 2 + toCurrentScale(optimalPageWidth) / 2, 0, getWidth(), getHeight());
    }

    /**
     * Move to the given X and Y offsets, but check them ahead of time
     * to be sure not to go outside the the big strip.
     *
     * @param offsetX The big strip X offset to use as the left border of the screen.
     * @param offsetY The big strip Y offset to use as the right border of the screen.
     */
    public void moveTo(float offsetX, float offsetY) {
        if (swipeVertical) {
            // Check X offset
            if (toCurrentScale(optimalPageWidth) < getWidth()) {
                offsetX = getWidth() / 2 - toCurrentScale(optimalPageWidth) / 2;
            } else {
                if (offsetX > 0) {
                    offsetX = 0;
                } else if (offsetX + toCurrentScale(optimalPageWidth) < getWidth()) {
                    offsetX = getWidth() - toCurrentScale(optimalPageWidth);
                }
            }

            // Check Y offset
            if (isZooming()) {
                if (toCurrentScale(optimalPageHeight) < getHeight()) {
                    miniMapRequired = false;
                    offsetY = getHeight() / 2 - toCurrentScale((currentFilteredPage + 0.5f) * optimalPageHeight);
                } else {
                    miniMapRequired = true;
                    if (offsetY + toCurrentScale(currentFilteredPage * optimalPageHeight) > 0) {
                        offsetY = -toCurrentScale(currentFilteredPage * optimalPageHeight);
                    } else if (offsetY + toCurrentScale((currentFilteredPage + 1) * optimalPageHeight) < getHeight()) {
                        offsetY = getHeight() - toCurrentScale((currentFilteredPage + 1) * optimalPageHeight);
                    }
                }

            } else {
                float maxY = calculateCenterOffsetForPage(currentFilteredPage + 1);
                float minY = calculateCenterOffsetForPage(currentFilteredPage - 1);
                if (offsetY < maxY) {
                    offsetY = maxY;
                } else if (offsetY > minY) {
                    offsetY = minY;
                }
            }
        } else {
            // Check Y offset
            if (toCurrentScale(optimalPageHeight) < getHeight()) {
                offsetY = getHeight() / 2 - toCurrentScale(optimalPageHeight) / 2;
            } else {
                if (offsetY > 0) {
                    offsetY = 0;
                } else if (offsetY + toCurrentScale(optimalPageHeight) < getHeight()) {
                    offsetY = getHeight() - toCurrentScale(optimalPageHeight);
                }
            }

            // Check X offset
            if (isZooming()) {
                if (toCurrentScale(optimalPageWidth) < getWidth()) {
                    miniMapRequired = false;
                    offsetX = getWidth() / 2 - toCurrentScale((currentFilteredPage + 0.5f) * optimalPageWidth);
                } else {
                    miniMapRequired = true;
                    if (offsetX + toCurrentScale(currentFilteredPage * optimalPageWidth) > 0) {
                        offsetX = -toCurrentScale(currentFilteredPage * optimalPageWidth);
                    } else if (offsetX + toCurrentScale((currentFilteredPage + 1) * optimalPageWidth) < getWidth()) {
                        offsetX = getWidth() - toCurrentScale((currentFilteredPage + 1) * optimalPageWidth);
                    }
                }

            } else {
                float maxX = calculateCenterOffsetForPage(currentFilteredPage + 1);
                float minX = calculateCenterOffsetForPage(currentFilteredPage - 1);
                if (offsetX < maxX) {
                    offsetX = maxX;
                } else if (offsetX > minX) {
                    offsetX = minX;
                }
            }
        }

        currentXOffset = offsetX;
        currentYOffset = offsetY;
        calculateMinimapAreaBounds();
        invalidate();
    }

    /**
     * Move relatively to the current position.
     *
     * @param dx The X difference you want to apply.
     * @param dy The Y difference you want to apply.
     * @see #moveTo(float, float)
     */
    public void moveRelativeTo(float dx, float dy) {
        moveTo(currentXOffset + dx, currentYOffset + dy);
    }

    /**
     * Change the zoom level
     */
    public void zoomTo(float zoom) {
        this.zoom = zoom;
        calculateMasksBounds();
    }

    /**
     * Change the zoom level, relatively to a pivot point.
     * It will call moveTo() to make sure the given point stays
     * in the middle of the screen.
     *
     * @param zoom  The zoom level.
     * @param pivot The point on the screen that should stays.
     */
    public void zoomCenteredTo(float zoom, PointF pivot) {
        float dzoom = zoom / this.zoom;
        zoomTo(zoom);
        float baseX = currentXOffset * dzoom;
        float baseY = currentYOffset * dzoom;
        baseX += (pivot.x - pivot.x * dzoom);
        baseY += (pivot.y - pivot.y * dzoom);
        moveTo(baseX, baseY);
    }

    /**
     * @see #zoomCenteredTo(float, PointF)
     */
    public void zoomCenteredRelativeTo(float dzoom, PointF pivot) {
        zoomCenteredTo(zoom * dzoom, pivot);
    }

    public int getCurrentPage() {
        return currentPage;
    }

    public float getCurrentXOffset() {
        return currentXOffset;
    }

    public float getCurrentYOffset() {
        return currentYOffset;
    }

    public float toRealScale(float size) {
        return size / zoom;
    }

    public float toCurrentScale(float size) {
        return size * zoom;
    }

    public float getZoom() {
        return zoom;
    }

    public boolean isZooming() {
        return zoom != minZoom;
    }

    public float getOptimalPageWidth() {
        return optimalPageWidth;
    }

    public float getOptimalPageHeight() {
        return optimalPageHeight;
    }

    private void setUserWantsMinimap(boolean userWantsMinimap) {
        this.userWantsMinimap = userWantsMinimap;
    }

    private void setDefaultPage(int defaultPage) {
        this.defaultPage = defaultPage;
    }

    public void resetZoom() {
        zoomTo(minZoom);
    }

    public void resetZoomWithAnimation() {
        animationManager.startZoomAnimation(zoom, minZoom);
    }

    public void zoomWithAnimation(float scale) {
        animationManager.startZoomAnimation(zoom, scale);
    }

    public void setScrollBar(ScrollBar scrollBar) {
        this.scrollBar = scrollBar;
        scrollBar.addToPDFView(this);
    }

    public float getMinZoom() {
        return minZoom;
    }

    public void setMinZoom(float minZoom) {
        this.minZoom = minZoom;
    }

    public float getMidZoom() {
        return midZoom;
    }

    public void setMidZoom(float midZoom) {
        this.midZoom = midZoom;
    }

    public float getMaxZoom() {
        return maxZoom;
    }

    public void setMaxZoom(float maxZoom) {
        this.maxZoom = maxZoom;
    }

    public void useBestQuality(boolean bestQuality) {
        this.bestQuality = bestQuality;
    }

    public void enableAnnotationRendering(boolean annotationRendering) {
        this.annotationRendering = annotationRendering;
    }

    public PdfDocument.Meta getDocumentMeta() {
        if (pdfDocument == null) {
            return null;
        }
        return pdfiumCore.getDocumentMeta(pdfDocument);
    }

    public List<PdfDocument.Bookmark> getTableOfContents() {
        if (pdfDocument == null) {
            return new ArrayList<>();
        }
        return pdfiumCore.getTableOfContents(pdfDocument);
    }

    /**
     * Use an asset file as the pdf source
     */
    public Configurator fromAsset(String assetName) {
        InputStream stream = null;
        try {
            stream = getContext().getAssets().open(assetName);
            return new Configurator(assetName, true);
        } catch (IOException e) {
            throw new FileNotFoundException(assetName + " does not exist.", e);
        } finally {
            try {
                if (stream != null) {
                    stream.close();
                }
            } catch (IOException e) {

            }
        }
    }

    /**
     * Use a file as the pdf source
     */
    public Configurator fromFile(File file) {
        if (!file.exists()) {
            throw new FileNotFoundException(file.getAbsolutePath() + " does not exist.");
        }
        return new Configurator(file.getAbsolutePath(), false);
    }

    /**
     * Use URI as the pdf source, for use with content providers
     */
    public Configurator fromUri(Uri uri) {
        return new Configurator(uri.toString(), false);
    }

    private enum State {DEFAULT, LOADED, SHOWN}

    public class Configurator {

        private final String path;

        private final boolean isAsset;

        private int[] pageNumbers = null;

        private boolean enableSwipe = true;

        private boolean enableDoubletap = true;

        private OnDrawListener onDrawListener;

        private OnLoadCompleteListener onLoadCompleteListener;

        private OnErrorListener onErrorListener;

        private OnPageChangeListener onPageChangeListener;

        private int defaultPage = 1;

        private boolean showMinimap = false;

        private boolean swipeVertical = false;

        private boolean showPageWithAnimation = true;

        private boolean annotationRendering = false;

        private int maskColor = Color.BLACK;

        private int maskAlpha = Constants.MASK_ALPHA;

        private String password = null;

        private Configurator(String path, boolean isAsset) {
            this.path = path;
            this.isAsset = isAsset;
        }

        public Configurator pages(int... pageNumbers) {
            this.pageNumbers = pageNumbers;
            return this;
        }

        public Configurator enableSwipe(boolean enableSwipe) {
            this.enableSwipe = enableSwipe;
            return this;
        }

        public Configurator enableDoubletap(boolean enableDoubletap) {
            this.enableDoubletap = enableDoubletap;
            return this;
        }

        public Configurator enableAnnotationRendering(boolean annotationRendering) {
            this.annotationRendering = annotationRendering;
            return this;
        }

        public Configurator onDraw(OnDrawListener onDrawListener) {
            this.onDrawListener = onDrawListener;
            return this;
        }

        public Configurator onLoad(OnLoadCompleteListener onLoadCompleteListener) {
            this.onLoadCompleteListener = onLoadCompleteListener;
            return this;
        }

        public Configurator onError(OnErrorListener onErrorListener) {
            this.onErrorListener = onErrorListener;
            return this;
        }

        public Configurator onPageChange(OnPageChangeListener onPageChangeListener) {
            this.onPageChangeListener = onPageChangeListener;
            return this;
        }

        public Configurator defaultPage(int defaultPage) {
            this.defaultPage = defaultPage;
            return this;
        }

        public Configurator swipeVertical(boolean swipeVertical) {
            this.swipeVertical = swipeVertical;
            return this;
        }

        public Configurator showPageWithAnimation(boolean showPageWithAnimation) {
            this.showPageWithAnimation = showPageWithAnimation;
            return this;
        }

        public Configurator password(String password) {
            this.password = password;
            return this;
        }

        /**
         * @param maskColor - mask color (default Color.BLACK)
         * @param maskAlpha - alpha value in [0,255] (default 20)
         * @return
         */
        public Configurator mask(int maskColor, int maskAlpha) {
            this.maskColor = maskColor;
            this.maskAlpha = maskAlpha;
            return this;
        }

        public void load() {
            PDFView.this.recycle();
            PDFView.this.setOnDrawListener(onDrawListener);
            PDFView.this.setOnPageChangeListener(onPageChangeListener);
            PDFView.this.enableSwipe(enableSwipe);
            PDFView.this.enableDoubletap(enableDoubletap);
            PDFView.this.setDefaultPage(defaultPage);
            PDFView.this.setUserWantsMinimap(showMinimap);
            PDFView.this.setSwipeVertical(swipeVertical);
            PDFView.this.setShowPageWithAnimation(showPageWithAnimation);
            PDFView.this.enableAnnotationRendering(annotationRendering);
            PDFView.this.dragPinchManager.setSwipeVertical(swipeVertical);
            PDFView.this.maskPaint = new Paint();
            PDFView.this.maskPaint.setColor(maskColor);
            PDFView.this.maskPaint.setAlpha(maskAlpha);
            if (pageNumbers != null) {
                PDFView.this.load(path, isAsset, password, onLoadCompleteListener, onErrorListener, pageNumbers);
            } else {
                PDFView.this.load(path, isAsset, password, onLoadCompleteListener, onErrorListener);
            }
        }

        public Configurator showMinimap(boolean showMinimap) {
            this.showMinimap = showMinimap;
            return this;
        }
    }

    public boolean isSwipeVertical() {
        return swipeVertical;
    }

    public void setSwipeVertical(boolean swipeVertical) {
        this.swipeVertical = swipeVertical;
    }

    public void setShowPageWithAnimation(boolean showPageWithAnimation) {
        this.showPageWithAnimation = showPageWithAnimation;
    }
}
