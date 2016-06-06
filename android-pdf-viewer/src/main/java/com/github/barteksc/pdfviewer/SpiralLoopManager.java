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

/**
 * This class allows to run a loop like :
 * <pre>
 * _____ _____ _____
 * |     |     |     |
 * |  7  |  6  |  5  |
 * |_____|_____|_____|
 * |     |     |     |
 * |  8  |  1  |  4  |
 * |_____|_____|_____|
 * |     |     |     |
 * |  9  |  2  |  3  |
 * |_____|_____|_____|
 *
 * </pre>
 * <p/>
 * Usage :
 * <pre>
 * new SpiralLoopManager(new SpiralLoopListener(){
 * public boolean onLoop(int row, int col) {
 * // Treatment
 * // Return true if you want to continue
 * return true;
 * }
 * }).startLoop(5, 5, 2, 2);
 * </pre>
 */
class SpiralLoopManager {

    public interface SpiralLoopListener {
        /**
         * Called on loop update
         * @param row The row number (starting with 0)
         * @param col The col number (starting with 0)
         * @return true if you want to continue, false otherwise
         */
        boolean onLoop(int row, int col);
    }

    private SpiralLoopListener listener;

    public SpiralLoopManager(SpiralLoopListener listener) {
        if (listener == null) {
            throw new IllegalArgumentException("SpiralLoopListener must not be null");
        }
        this.listener = listener;
    }

    public void startLoop(int nbRows, int nbCols, int startRow, int startCol) {

        int totalNbCells = nbCols * nbRows;
        int nbMarkedCells = 0;

        int row = startRow, col = startCol;
        int progress = 1;
        int variation = 1;

        // First row
        listener.onLoop(row, col);
        nbMarkedCells++;

        while (nbMarkedCells < totalNbCells) {

            // Progress horizontal
            for (int i = 0; i < progress; i++) {
                row += variation;
                if (isValidCell(row, col, nbRows, nbCols)) {
                    nbMarkedCells++;
                    boolean canContinue = listener.onLoop(row, col);
                    if (!canContinue) return;
                }
            }

            // Progress vertical
            for (int i = 0; i < progress; i++) {
                col += variation;
                if (isValidCell(row, col, nbRows, nbCols)) {
                    nbMarkedCells++;
                    boolean canContinue = listener.onLoop(row, col);
                    if (!canContinue) return;
                }
            }

            // Change size of progress
            progress++;

            // Change sign of variation
            variation *= -1;
        }
    }

    private boolean isValidCell(int row, int col, int nbRows, int nbCols) {
        return !(row < 0 || row >= nbRows || col < 0 || col >= nbCols);
    }
}
