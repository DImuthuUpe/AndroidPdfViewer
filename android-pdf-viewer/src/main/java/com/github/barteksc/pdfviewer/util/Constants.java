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
package com.github.barteksc.pdfviewer.util;

public interface Constants {

    boolean DEBUG_MODE = false;

    /** Size of the minimum, in percent of the component size */
    float MINIMAP_MAX_SIZE = 200f;

    /** Number of pages loaded (default 3) */
    int LOADED_SIZE = 3;

    /** Between 0 and 1, the thumbnails quality (default 0.2) */
    float THUMBNAIL_RATIO = 0.3f;

    /**
     * The size of the rendered parts (default 256)
     * Tinier : a little bit slower to have the whole page rendered but more reactive.
     * Bigger : user will have to wait longer to have the first visual results
     */
    float PART_SIZE = 256;

    /** Transparency of masks around the main page (between 0 and 255, default 50) */
    int MASK_ALPHA = 20;

    /** The size of the grid of loaded images around the current point */
    int GRID_SIZE = 10;

    interface Cache {

        /** The size of the cache (number of bitmaps kept) */
        int CACHE_SIZE = (int) Math.pow(GRID_SIZE, 2d);

        int THUMBNAILS_CACHE_SIZE = 4;
    }

    interface Pinch {

        float MAXIMUM_ZOOM = 10;

        float MINIMUM_ZOOM = 1;

        /**
         * A move must be quicker than this duration and longer than
         * this distance to be considered as a quick move
         */
        int QUICK_MOVE_THRESHOLD_TIME = 250, //

        QUICK_MOVE_THRESHOLD_DISTANCE = 50;

    }

}
