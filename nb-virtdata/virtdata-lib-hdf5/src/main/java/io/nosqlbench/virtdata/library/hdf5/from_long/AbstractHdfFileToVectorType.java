/*
 * Copyright (c) nosqlbench
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

package io.nosqlbench.virtdata.library.hdf5.from_long;

import io.jhdf.HdfFile;
import io.jhdf.api.Attribute;
import io.jhdf.api.Dataset;
import io.jhdf.api.Group;
import io.jhdf.api.Node;
import io.nosqlbench.nb.api.nbio.NBIO;
import io.nosqlbench.virtdata.library.hdf5.helpers.HdfAttributesProcessor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Map;

/**
 * Abstract base class for reading data from HDF5 files.
 * Provides common functionality for accessing and processing HDF5 datasets and their attributes.
 */
public abstract class AbstractHdfFileToVectorType implements HdfAttributesProcessor {
    protected final HdfFile hdfFile;           // The HDF5 file handle
    protected final Dataset dataset;           // The specific dataset within the HDF5 file
    protected final String datasetName;        // Name/path of the dataset
    protected final int[] dims;                // Dimensions of the dataset
    protected String dataType;                 // Data type of the dataset
    protected final static Logger logger = LogManager.getLogger(AbstractHdfFileToVectorType.class);

    /**
     * Constructs a new instance for reading data from an HDF5 file.
     *
     * @param filename The name of the HDF5 file to read
     * @param datasetName The path to the dataset within the HDF5 file
     */
    public AbstractHdfFileToVectorType(String filename, String datasetName) {
        hdfFile = new HdfFile(NBIO.all().search(filename).one().asPath());
        this.datasetName = datasetName;
        dataset = hdfFile.getDatasetByPath(datasetName);
        dims = dataset.getDimensions();
        processAttributes(hdfFile);
        this.dataType = (dataType == null) ? dataset.getJavaType().getSimpleName().toLowerCase() : dataType;
    }

    /**
     * Retrieves data from the dataset at a specific position.
     * For multi-dimensional datasets, returns a slice of data.
     *
     * @param l The position to read from
     * @return The data at the specified position
     */
    protected Object getDataFrom(long l) {
        long[] sliceOffset = new long[dims.length];
        sliceOffset[0] = (l % dims[0]);
        int[] sliceDimensions = new int[dims.length];
        // We always want to read a single value
        sliceDimensions[0] = 1;
        if (dims.length > 1) {
            sliceDimensions[1] = dims[1];
            return dataset.getData(sliceOffset, sliceDimensions);
        } else {
            return dataset.getData();
        }
    }

    /**
     * Processes attributes of the HDF5 file by traversing its node hierarchy.
     *
     * @param hdfFile The HDF5 file to process
     */
    @Override
    public void processAttributes(HdfFile hdfFile) {
        processNode(hdfFile);
    }

    /**
     * Recursively processes a node and its children in the HDF5 file hierarchy.
     *
     * @param node The node to process
     */
    private void processNode(Node node) {
        logger.info("Node: {}", node.getPath());
        readAttributes(node);

        // If the node is a group, iterate through its children
        if (node instanceof Group group) {
            for (Node child : group.getChildren().values()) {
                processNode(child);
            }
        }
    }

    /**
     * Reads and processes attributes of a node, updating the dataType if a
     * COMPONENT_ENCODING attribute is found for the target dataset.
     *
     * @param node The node whose attributes should be read
     */
    private void readAttributes(Node node) {
        Map<String, Attribute> attributes = node.getAttributes();
        if (attributes.isEmpty()) {
            logger.info(() -> "No attributes");
        } else {
            logger.info(() -> "Attributes:");
            for (Map.Entry<String, Attribute> entry : attributes.entrySet()) {
                logger.info("{} = {}", entry.getKey(), entry.getValue().getData());
                if (entry.getKey().equals(COMPONENT_ENCODING) && (node.getPath().equalsIgnoreCase(datasetName))) {
                    dataType = entry.getValue().getData().toString();
                }
            }
        }
    }

}
