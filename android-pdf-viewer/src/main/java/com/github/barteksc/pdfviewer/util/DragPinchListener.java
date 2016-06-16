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
package com.github.barteksc.pdfviewer.util;

import android.graphics.PointF;
import android.os.Handler;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;

/**
 *         This class manage MotionEvents. Use it on your view with
 *         setOnTouchListener(dragManager);
 *
 *         Use {@link #setOnDragListener(OnDragListener)} and {@link #setOnPinchListener(OnPinchListener)}
 *         to receive events when a drag or pinch event occurs.
 */
public class DragPinchListener implements OnTouchListener {

    /**
     * Max time a finger can stay pressed before this
     * action is considered as a non-click (in ms)
     */
    private static final long MAX_CLICK_TIME = 500;

    /**
     * Max distance a finger can move before this action
     * is considered as a non-click (in px)
     */
    private static final float MAX_CLICK_DISTANCE = 5;

    /**
     * Max time between 2 clicks to be considered as a
     * double click
     */
    private static final float MAX_DOUBLE_CLICK_TIME = 280;

    private static final int POINTER1 = 0, POINTER2 = 1;
    /**
     * Handler used for perfom click (only if it's not a double tap)
     */
    private final Handler handlerClick = new Handler();
    private View mView;
    private final Runnable runnableClick = new Runnable() {
        @Override
        public void run() {
            mView.performClick();
        }
    };

    /** Implement this interface to receive Drag events */
    public interface OnDragListener {

        /**
         * @param dx The differential X offset
         * @param dy The differential Y offset
         */
        void onDrag(float dx, float dy);

        /** Called when a drag event starts */
        void startDrag(float x, float y);

        /** Called when a drag event stops */
        void endDrag(float x, float y);

    }

    /** Implement this interface to receive Pinch events */
    public interface OnPinchListener {

        /**
         * @param dr    The differential ratio
         * @param pivot The pivot point on which the redim occurs
         */
        void onPinch(float dr, PointF pivot);

    }

    /** Implement this interface to receive Double Tap events */
    public interface OnDoubleTapListener {

        /**
         * Called when a double tap happens.
         * @param x X-offset of event.
         * @param y Y-offset of event.
         */
        void onDoubleTap(float x, float y);

    }

    enum State {NONE, ZOOM, DRAG}

    private State state = State.NONE;

    private float dragLastX, dragLastY;

    private float pointer2LastX, pointer2LastY;

    private float zoomLastDistance;

    private OnDragListener onDragListener;

    private OnPinchListener onPinchListener;

    private OnDoubleTapListener onDoubleTapListener;

    private float lastDownX, lastDownY;

    private long lastClickTime;

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        mView = v;
        switch (event.getAction()) {

            // NORMAL CASE : FIRST POINTER DOWN
            case MotionEvent.ACTION_DOWN:
                // Start dragging
                startDrag(event);
                state = State.DRAG;
                lastDownX = event.getX();
                lastDownY = event.getY();
                break;

            // NORMAL CASE : SECOND POINTER DOWN
            case MotionEvent.ACTION_POINTER_2_DOWN:
                startDrag(event);
                startZoom(event);
                state = State.ZOOM;
                break;

            // NORMAL CASE : SECOND POINTER UP
            case MotionEvent.ACTION_POINTER_2_UP:
                // End zooming, goes back to dragging
                state = State.DRAG;
                break;

            // NORMAL CASE : FIRST POINTER UP
            case MotionEvent.ACTION_UP:
                // End everything
                state = State.NONE;
                endDrag();

                // Treat clicks
                if (isClick(event, lastDownX, lastDownY, event.getX(), event.getY())) {
                    long time = System.currentTimeMillis();
                    handlerClick.removeCallbacks(runnableClick);
                    if (onDoubleTapListener != null) {
                        if (time - lastClickTime < MAX_DOUBLE_CLICK_TIME) {
                            onDoubleTapListener.onDoubleTap(event.getX(), event.getY());
                            lastClickTime = 0;
                        } else {
                            lastClickTime = System.currentTimeMillis();
                            handlerClick.postDelayed(runnableClick, MAX_CLICK_TIME);
                        }
                    } else {
                        handlerClick.postDelayed(runnableClick, 0);
                    }
                }
                break;

            // TRICKY CASE : FIRST POINTER UP WHEN SECOND STILL DOWN
            case MotionEvent.ACTION_POINTER_1_UP:

                dragLastX = pointer2LastX;
                dragLastY = pointer2LastY;
                state = State.DRAG;
                break;

            // TRICKY CASE : FIRST POINTER UP THEN DOWN WHILE SECOND POINTER STILL UP
            case MotionEvent.ACTION_POINTER_1_DOWN:
                pointer2LastX = event.getX(POINTER1);
                pointer2LastY = event.getY(POINTER1);

                startDrag(event);
                startZoom(event);
                state = State.ZOOM;
                break;

            // NORMAL CASE : MOVE
            case MotionEvent.ACTION_MOVE:

                switch (state) {
                    case ZOOM:
                        pointer2LastX = event.getX(POINTER2);
                        pointer2LastY = event.getY(POINTER2);
                        zoom(event);

                    case DRAG:
                        drag(event);
                        break;
                    default:
                        break;
                }
                break;
        }

        return true;
    }

    private void endDrag() {
        onDragListener.endDrag(dragLastX, dragLastY);
    }

    private void startZoom(MotionEvent event) {
        zoomLastDistance = distance(event);
    }

    private void zoom(MotionEvent event) {
        float zoomCurrentDistance = distance(event);

        if (onPinchListener != null) {
            onPinchListener.onPinch(zoomCurrentDistance / zoomLastDistance, //
                    new PointF(event.getX(POINTER1), event.getY(POINTER1)));
        }

        zoomLastDistance = zoomCurrentDistance;
    }

    private void startDrag(MotionEvent event) {
        dragLastX = event.getX(POINTER1);
        dragLastY = event.getY(POINTER1);
        onDragListener.startDrag(dragLastX, dragLastY);
    }

    private void drag(MotionEvent event) {
        float dragCurrentX = event.getX(POINTER1);
        float dragCurrentY = event.getY(POINTER1);

        if (onDragListener != null) {
            onDragListener.onDrag(dragCurrentX - dragLastX,
                    dragCurrentY - dragLastY);
        }

        dragLastX = dragCurrentX;
        dragLastY = dragCurrentY;
    }

    /** Calculates the distance between the 2 current pointers */
    private float distance(MotionEvent event) {
        if (event.getPointerCount() < 2) {
            return 0;
        }
        return PointF.length(event.getX(POINTER1) - event.getX(POINTER2), //
                event.getY(POINTER1) - event.getY(POINTER2));
    }

    /**
     * Test if a MotionEvent with the given start and end offsets
     * can be considered as a "click".
     * @param upEvent The final finger-up event.
     * @param xDown   The x-offset of the down event.
     * @param yDown   The y-offset of the down event.
     * @param xUp     The x-offset of the up event.
     * @param yUp     The y-offset of the up event.
     * @return true if it's a click, false otherwise
     */
    private boolean isClick(MotionEvent upEvent, float xDown, float yDown, float xUp, float yUp) {
        if (upEvent == null) return false;
        long time = upEvent.getEventTime() - upEvent.getDownTime();
        float distance = PointF.length( //
                xDown - xUp, //
                yDown - yUp);
        return time < MAX_CLICK_TIME && distance < MAX_CLICK_DISTANCE;
    }

    public void setOnDragListener(OnDragListener onDragListener) {
        this.onDragListener = onDragListener;
    }

    public void setOnPinchListener(OnPinchListener onPinchListener) {
        this.onPinchListener = onPinchListener;
    }

    public void setOnDoubleTapListener(OnDoubleTapListener onDoubleTapListener) {
        this.onDoubleTapListener = onDoubleTapListener;
    }

}