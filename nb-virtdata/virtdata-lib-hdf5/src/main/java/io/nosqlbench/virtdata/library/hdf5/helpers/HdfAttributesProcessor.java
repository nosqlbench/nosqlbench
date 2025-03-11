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

package io.nosqlbench.virtdata.library.hdf5.helpers;

import io.jhdf.HdfFile;

/**
 * Interface for processing attributes from HDF5 files.
 * Defines constants for common HDF5 dataset and attribute names used in nosqlbench contexts.
 */
public interface HdfAttributesProcessor {
    /** Number of nearest neighbors */
    public static final String NEIGHBORS = "neighbors";
    /** Dimensionality of the vector space */
    public static final String DIMENSIONS = "dimensions";
    /** Training dataset vectors */
    public static final String TRAIN_VECTORS = "train_vectors";
    /** Test dataset vectors */
    public static final String TEST_VECTORS = "test_vectors";
    /** Machine learning model information */
    public static final String MODEL = "model";
    /** Distance metric used for similarity calculations */
    public static final String DISTANCE_FUNCTION = "distance_function";
    /** Encoding format for vector components */
    public static final String COMPONENT_ENCODING = "component_encoding";

    /**
     * Process attributes from an HDF5 file.
     * @param hdfFile The HDF5 file to process attributes from
     */
    public void processAttributes(HdfFile hdfFile);
}
