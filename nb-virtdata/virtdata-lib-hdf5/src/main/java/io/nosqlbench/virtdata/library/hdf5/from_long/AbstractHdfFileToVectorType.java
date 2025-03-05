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

public abstract class AbstractHdfFileToVectorType implements HdfAttributesProcessor {
    protected final HdfFile hdfFile;
    protected final Dataset dataset;
    protected final String datasetName;
    protected final int[] dims;
    protected String dataType;
    protected final static Logger logger = LogManager.getLogger(AbstractHdfFileToVectorType.class);

    public AbstractHdfFileToVectorType(String filename, String datasetName) {
        hdfFile = new HdfFile(NBIO.all().search(filename).one().asPath());
        this.datasetName = datasetName;
        dataset = hdfFile.getDatasetByPath(datasetName);
        dims = dataset.getDimensions();
        processAttributes(hdfFile);
        this.dataType = (dataType == null) ? dataset.getJavaType().getSimpleName().toLowerCase() : dataType;
    }

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

    @Override
    public void processAttributes(HdfFile hdfFile) {
        processNode(hdfFile);
    }

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
