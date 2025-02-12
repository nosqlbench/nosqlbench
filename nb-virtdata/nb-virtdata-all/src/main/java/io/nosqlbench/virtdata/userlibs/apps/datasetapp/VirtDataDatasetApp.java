/*
 * Copyright (c) 2025 nosqlbench
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
 *
 */

package io.nosqlbench.virtdata.userlibs.apps.datasetapp;

import io.jhdf.HdfFile;
import io.jhdf.WritableHdfFile;
import org.apache.commons.math3.ml.distance.EuclideanDistance;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.IntStream;

public class VirtDataDatasetApp {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        // Get user inputs
        System.out.print("Enter the number of train vectors (n): ");
        int n = scanner.nextInt();
        System.out.print("Enter the dimensionality of each vector (p): ");
        int p = scanner.nextInt();
        System.out.print("Enter the number of test vectors (x): ");
        int x = scanner.nextInt();
        System.out.print("Enter the number of contiguous vectors per ID: ");
        int vectorsPerId = scanner.nextInt();
        System.out.print("Enter the output HDF5 file name: ");
        String outputFile = scanner.next();
        System.out.print("Create dataset with predicates? (y/n): ");
        boolean usePredicates = scanner.next().trim().equalsIgnoreCase("y");

        // Generate dataset
        float[][] trainData = generateRandomMatrix(n, p);
        float[][] testData = generateRandomMatrix(x, p);
        int[] trainIds = usePredicates ? generateTrainIds(n, vectorsPerId) : null;
        List<int[]> testIds = usePredicates ? generateTestIds(x, n / vectorsPerId) : null;

        // Compute KNN
        int[][] neighbors = new int[x][100];
        float[][] distances = new float[x][100];
        computeKnn(trainData, testData, trainIds, testIds, neighbors, distances, usePredicates);

        // Write to HDF5
        writeToHdf5(outputFile, trainData, testData, neighbors, distances, trainIds, testIds, usePredicates);

        System.out.println("Dataset saved to " + outputFile);
    }

    private static float[][] generateRandomMatrix(int rows, int cols) {
        Random rand = new Random();
        float[][] matrix = new float[rows][cols];
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                matrix[i][j] = rand.nextFloat();
            }
        }
        return matrix;
    }

    private static int[] generateTrainIds(int n, int vectorsPerId) {
        int numIds = n / vectorsPerId;
        int[] trainIds = new int[n];
        for (int i = 0; i < numIds; i++) {
            Arrays.fill(trainIds, i * vectorsPerId, (i + 1) * vectorsPerId, i + 1);
        }
        return trainIds;
    }

    private static List<int[]> generateTestIds(int x, int maxId) {
        Random rand = new Random();
        List<int[]> testIds = new ArrayList<>();
        for (int i = 0; i < x; i++) {
            int numIds = rand.nextInt(5) + 1; // 1 to 5 ids per test vector
            int[] ids = rand.ints(numIds, 1, maxId + 1).toArray();
            testIds.add(ids);
        }
        return testIds;
    }

    private static void computeKnn(float[][] trainData, float[][] testData, int[] trainIds, List<int[]> testIds,
                                   int[][] neighbors, float[][] distances, boolean usePredicates) {
        EuclideanDistance distance = new EuclideanDistance();
        for (int i = 0; i < testData.length; i++) {
            float[] query = testData[i];

            List<Integer> validIndices = new ArrayList<>();
            if (usePredicates) {
                Set<Integer> validIds = new HashSet<>();
                for (int id : testIds.get(i)) validIds.add(id);
                for (int j = 0; j < trainIds.length; j++) {
                    if (validIds.contains(trainIds[j])) validIndices.add(j);
                }
            } else {
                validIndices = IntStream.range(0, trainData.length).collect(ArrayList::new, ArrayList::add, ArrayList::addAll);
            }

            List<float[]> validTrainData = new ArrayList<>();
            for (int index : validIndices) validTrainData.add(trainData[index]);

            float[] queryDistances = new float[validTrainData.size()];
            for (int j = 0; j < validTrainData.size(); j++) {
                queryDistances[j] = (float) distance.compute(floats_to_doubles(query), floats_to_doubles(validTrainData.get(j)));
            }

            Integer[] sortedIndices = IntStream.range(0, queryDistances.length)
                .boxed()
                .sorted(Comparator.comparingDouble(index -> queryDistances[index]))
                .toArray(Integer[]::new);

            for (int k = 0; k < 100; k++) {
                if (k < sortedIndices.length) {
                    neighbors[i][k] = validIndices.get(sortedIndices[k]);
                    distances[i][k] = queryDistances[sortedIndices[k]];
                } else {
                    neighbors[i][k] = -1;
                    distances[i][k] = Float.POSITIVE_INFINITY;
                }
            }
        }
    }

    private static double[] floats_to_doubles(float[] floats) {
        double[] doubles = new double[floats.length];
        for (int j = 0; j < floats.length; j++) {
            doubles[j] = floats[j];
        }
        return doubles;
    }

    private static void writeToHdf5(String outputFile, float[][] trainData, float[][] testData,
                                    int[][] neighbors, float[][] distances, int[] trainIds, List<int[]> testIds,
                                    boolean usePredicates) {
        try {
            File file = new File(outputFile);
            Path path = Path.of(outputFile);
            if (file.exists()) Files.delete(path);
            WritableHdfFile hdfFile = HdfFile.write(path);
            hdfFile.putGroup("/");

            hdfFile.putDataset("train", trainData);
            hdfFile.putDataset("test", testData);
            hdfFile.putDataset("neighbors", neighbors);
            hdfFile.putDataset("distances", distances);

            if (usePredicates) {
                hdfFile.putDataset("train_ids", trainIds);
                int[][] testIdsArray = testIds.stream().map(arr -> Arrays.copyOf(arr, 5)).toArray(int[][]::new);
                hdfFile.putDataset("test_ids", testIdsArray);
            }

            hdfFile.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
