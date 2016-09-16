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

public class MathUtils {

    static private final int BIG_ENOUGH_INT = 16 * 1024;
    static private final double BIG_ENOUGH_FLOOR = BIG_ENOUGH_INT;
    static private final double BIG_ENOUGH_CEIL = 16384.999999999996;

    private MathUtils() {
        // Prevents instantiation
    }

    /**
     * Limits the given <b>number</b> between the other values
     * @param number  The number to limit.
     * @param between The smallest value the number can take.
     * @param and     The biggest value the number can take.
     * @return The limited number.
     */
    public static int limit(int number, int between, int and) {
        if (number <= between) {
            return between;
        }
        if (number >= and) {
            return and;
        }
        return number;
    }

    /**
     * Limits the given <b>number</b> between the other values
     * @param number  The number to limit.
     * @param between The smallest value the number can take.
     * @param and     The biggest value the number can take.
     * @return The limited number.
     */
    public static float limit(float number, float between, float and) {
        if (number <= between) {
            return between;
        }
        if (number >= and) {
            return and;
        }
        return number;
    }

    public static float max(float number, float max) {
        if (number > max) {
            return max;
        }
        return number;
    }

    public static float min(float number, float min) {
        if (number < min) {
            return min;
        }
        return number;
    }

    public static int max(int number, int max) {
        if (number > max) {
            return max;
        }
        return number;
    }

    public static int min(int number, int min) {
        if (number < min) {
            return min;
        }
        return number;
    }

    /**
     * Methods from libGDX - https://github.com/libgdx/libgdx
     */

    /** Returns the largest integer less than or equal to the specified float. This method will only properly floor floats from
     * -(2^14) to (Float.MAX_VALUE - 2^14). */
    static public int floor(float value) {
        return (int) (value + BIG_ENOUGH_FLOOR) - BIG_ENOUGH_INT;
    }

    /** Returns the smallest integer greater than or equal to the specified float. This method will only properly ceil floats from
     * -(2^14) to (Float.MAX_VALUE - 2^14). */
    static public int ceil(float value) {
        return (int) (value + BIG_ENOUGH_CEIL) - BIG_ENOUGH_INT;
    }
}
