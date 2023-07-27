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
import ncsa.hdf.hdf5lib.exceptions.HDF5LibraryException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Hdf5Reader implements HdfReader {
    private static final Logger logger = LogManager.getLogger(Hdf5Reader.class);
    private VectorWriter writer;
    private final LoaderConfig config;
    private final ExecutorService executorService;
    public Hdf5Reader(LoaderConfig config) {
        this.config = config;
        executorService = Executors.newFixedThreadPool(config.getThreads());
    }

    @Override
    public void setWriter(VectorWriter writer) {
        this.writer = writer;
    }

    @Override
    public void read() throws HDF5LibraryException {
        String sourceFile = config.getSourceFile();
        int fileId = H5.H5Fopen(sourceFile, HDF5Constants.H5F_ACC_RDONLY, HDF5Constants.H5P_DEFAULT);
        for (Map<String,String> dataset : config.getDatasets()) {
            executorService.submit(() -> {
                // Your lambda code that runs in a separate thread for each object
                logger.info("Processing dataset: " + dataset.get("name"));
                try {
                    int datasetId = H5.H5Dopen(fileId, dataset.get("name"));
                    // Get the dataspace of the dataset
                    int dataspaceId = H5.H5Dget_space(datasetId);

                    // Get the number of dimensions in the dataspace
                    int numDimensions = H5.H5Sget_simple_extent_ndims(dataspaceId);
                    long[] dims = new long[numDimensions];
                } catch (HDF5LibraryException e) {
                    logger.error(e);
                }
            });
        }
    }
}
