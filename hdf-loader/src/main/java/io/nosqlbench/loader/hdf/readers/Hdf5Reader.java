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
 *
 */

package io.nosqlbench.loader.hdf.readers;

import io.nosqlbench.loader.hdf.config.LoaderConfig;
import io.nosqlbench.loader.hdf.writers.VectorWriter;

import ncsa.hdf.hdf5lib.H5;
import ncsa.hdf.hdf5lib.HDF5Constants;
import ncsa.hdf.hdf5lib.exceptions.HDF5Exception;
import ncsa.hdf.hdf5lib.exceptions.HDF5LibraryException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;

public class Hdf5Reader implements HdfReader {
    private static final Logger logger = LogManager.getLogger(Hdf5Reader.class);
    public static final String ALL = "all";
    private VectorWriter writer;
    private final LoaderConfig config;
    private final ExecutorService executorService;
    private final LinkedBlockingQueue<float[]> queue;
    public Hdf5Reader(LoaderConfig config) {
        this.config = config;
        executorService = Executors.newFixedThreadPool(config.getThreads());
        queue = new LinkedBlockingQueue<>(config.getQueueSize());
    }

    @Override
    public void setWriter(VectorWriter writer) {
        this.writer = writer;
    }

    @Override
    public void read() throws HDF5LibraryException {
        String sourceFile = config.getSourceFile();
        int fileId = H5.H5Fopen(sourceFile, HDF5Constants.H5F_ACC_RDONLY, HDF5Constants.H5P_DEFAULT);
        List<String> datasets = config.getDatasets();
        if (datasets.get(0).equalsIgnoreCase(ALL)) {
            try {
                int numObjects = H5.H5Fget_obj_count(fileId, HDF5Constants.H5F_OBJ_ALL);
                String[] objNames = new String[numObjects];
                int[] objTypes = new int[numObjects];
                long[] refArray = new long[numObjects];
                //H5.H5Fget_obj_ids(fileId, HDF5Constants.H5F_OBJ_ALL, numObjects, objNames, objTypes);
                H5.H5Gget_obj_info_all(fileId, null, objNames, objTypes, refArray);

                for (int i = 0; i < numObjects; i++) {
                    String objName = objNames[i];
                    int objType = objTypes[i];
                    if (objType == HDF5Constants.H5G_DATASET) {
                        datasets.add(objName);
                    }
                }
            } catch (HDF5Exception e) {
                logger.error("Error getting all datasets from file: " + sourceFile, e);
            }
        }
        for (String dataset : config.getDatasets()) {
            if (dataset.equalsIgnoreCase(ALL)) {
                continue;
            }
            executorService.submit(() -> {
                // Your lambda code that runs in a separate thread for each object
                logger.info("Processing dataset: " + dataset);
                try {
                    int datasetId = H5.H5Dopen(fileId, dataset);
                    // Get the dataspace of the dataset
                    int dataspaceId = H5.H5Dget_space(datasetId);
                    // Get the number of dimensions in the dataspace
                    int numDimensions = H5.H5Sget_simple_extent_ndims(dataspaceId);
                    float[] vector = new float[numDimensions];
                    long[] dims = new long[numDimensions];
                    // Get the datatype of the dataset
                    int datatypeId = H5.H5Dget_type(datasetId);
                    // Get the size of each dimension
                    H5.H5Sget_simple_extent_dims(dataspaceId, dims, null);

                    // Read the data from the dataset
                    double[] data = new double[(int) dims[0]];
                    H5.H5Dread(datasetId, datatypeId, HDF5Constants.H5S_ALL, HDF5Constants.H5S_ALL,
                        HDF5Constants.H5P_DEFAULT, data);

                    // Close the dataspace, datatype, and dataset
                    H5.H5Sclose(dataspaceId);
                    H5.H5Tclose(datatypeId);
                    H5.H5Dclose(datasetId);

                    // Now you have the data, and you can convert it into vector embeddings
                    //INDArray dataArray = Nd4j.create(data);
                    //WordVectors wordVectors = new WordVectorsImpl();
                    //wordVectors.setLookupTable(dataArray);
                    //WordVectorSerializer.writeWordVectors(wordVectors, "vector_embeddings.txt");

                    queue.put(vector);
                } catch (HDF5Exception e) {
                    logger.error(e);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            });
        }
    }
}
