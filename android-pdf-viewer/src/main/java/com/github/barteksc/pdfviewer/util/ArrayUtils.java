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

import java.util.ArrayList;
import java.util.List;

public class ArrayUtils {

    private ArrayUtils() {
        // Prevents instantiation
    }

    /** Transforms (0,1,2,2,3) to (0,1,2,3) */
    public static int[] deleteDuplicatedPages(int[] pages) {
        List<Integer> result = new ArrayList<>();
        int lastInt = -1;
        for (Integer currentInt : pages) {
            if (lastInt != currentInt) {
                result.add(currentInt);
            }
            lastInt = currentInt;
        }
        int[] arrayResult = new int[result.size()];
        for (int i = 0; i < result.size(); i++) {
            arrayResult[i] = result.get(i);
        }
        return arrayResult;
    }

    /** Transforms (0, 4, 4, 6, 6, 6, 3) into (0, 1, 1, 2, 2, 2, 3) */
    public static int[] calculateIndexesInDuplicateArray(int[] originalUserPages) {
        int[] result = new int[originalUserPages.length];
        if (originalUserPages.length == 0) {
            return result;
        }

        int index = 0;
        result[0] = index;
        for (int i = 1; i < originalUserPages.length; i++) {
            if (originalUserPages[i] != originalUserPages[i - 1]) {
                index++;
            }
            result[i] = index;
        }

        return result;
    }

    public static String arrayToString(int[] array) {
        StringBuilder builder = new StringBuilder("[");
        for (int i = 0; i < array.length; i++) {
            builder.append(array[i]);
            if (i != array.length - 1) {
                builder.append(",");
            }
        }
        builder.append("]");
        return builder.toString();
    }
}
