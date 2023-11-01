/*
 * Copyright (c) 2023 nosqlbench
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.nosqlbench.scenarios.simframe;

public class ZScore {

    public static void main(String[] args) {
        // Example data
        double[][] data = {
            {1, 2, 3},
            {4, 5, 6},
            {7, 8, 9}
        };

        // Normalize data
        double[][] normalizedData = zScoreNormalization(data);

        // Print normalized data
        for (double[] row : normalizedData) {
            for (double num : row) {
                System.out.print(num + " ");
            }
            System.out.println();
        }
    }

    public static double[][] zScoreNormalization(double[][] data) {
        int numRows = data.length;
        int numCols = data[0].length;

        double[] means = new double[numCols];
        double[] stdDevs = new double[numCols];

        // Calculate means
        for (double[] row : data) {
            for (int j = 0; j < numCols; j++) {
                means[j] += row[j];
            }
        }
        for (int j = 0; j < numCols; j++) {
            means[j] /= numRows;
        }

        // Calculate standard deviations
        for (double[] row : data) {
            for (int j = 0; j < numCols; j++) {
                stdDevs[j] += Math.pow(row[j] - means[j], 2);
            }
        }
        for (int j = 0; j < numCols; j++) {
            stdDevs[j] = Math.sqrt(stdDevs[j] / numRows);
            // Prevent division by zero
            if (stdDevs[j] == 0) {
                stdDevs[j] = 1;
            }
        }

        // Normalize data
        double[][] normalizedData = new double[numRows][numCols];
        for (int i = 0; i < numRows; i++) {
            for (int j = 0; j < numCols; j++) {
                normalizedData[i][j] = (data[i][j] - means[j]) / stdDevs[j];
            }
        }

        return normalizedData;
    }
}
