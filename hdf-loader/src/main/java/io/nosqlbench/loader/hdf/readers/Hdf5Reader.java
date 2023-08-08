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

import io.jhdf.HdfFile;
import io.jhdf.api.Dataset;
import io.jhdf.api.Group;
import io.jhdf.api.Node;
import io.jhdf.object.datatype.DataType;
import io.nosqlbench.loader.hdf.config.LoaderConfig;
import io.nosqlbench.loader.hdf.embedding.EmbeddingGenerator;
import io.nosqlbench.loader.hdf.writers.VectorWriter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;

import static io.nosqlbench.loader.hdf.embedding.EmbeddingGeneratorFactory.getGenerator;

public class Hdf5Reader implements HdfReader {
    private static final Logger logger = LogManager.getLogger(Hdf5Reader.class);
    public static final String ALL = "all";
    private VectorWriter writer;
    private final LoaderConfig config;
    private final ExecutorService executorService;
    private final LinkedBlockingQueue<float[]> queue;
    private List<String> datasets;
    public Hdf5Reader(LoaderConfig config) {
        this.config = config;
        executorService = Executors.newCachedThreadPool();
        queue = new LinkedBlockingQueue<>(config.getQueueSize());
    }

    @Override
    public void setWriter(VectorWriter writer) {
        this.writer = writer;
        writer.setQueue(queue);
        Thread t = new Thread(writer);
        t.start();
    }

    public void extractDatasets(Group parent) {
        Map<String, Node> nodes = parent.getChildren();
        for (String key : nodes.keySet()) {
            Node node = nodes.get(key);
            if (node instanceof Dataset) {
                datasets.add(((Dataset)node).getPath());
            } else if (node.isGroup()) {
                extractDatasets((Group) node);
            }
        }
    }

    @Override
    public void read()   {
        HdfFile hdfFile = new HdfFile(Paths.get(config.getSourceFile()));
        datasets = config.getDatasets();
        if (datasets.get(0).equalsIgnoreCase(ALL)) {
            extractDatasets(hdfFile);
        }
        List<Future<?>> futures = new ArrayList<>();
        for (String ds : datasets) {
            if (ds.equalsIgnoreCase(ALL)) {
                continue;
            }
            Future<?> future = executorService.submit(() -> {
                logger.info("Processing dataset: " + ds);
                Dataset dataset = hdfFile.getDatasetByPath(ds);
                DataType dataType = dataset.getDataType();

                int[] dims = dataset.getDimensions();
                Object data = dataset.getData();

                String type = dataset.getJavaType().getSimpleName();
                EmbeddingGenerator generator = getGenerator(dataset.getJavaType().getSimpleName());
                float[][] vectors = generator.generateEmbeddingFrom(data, dims);
                for (int i = 0; i < dims[0]; i++) {
                    try {
                        queue.put(vectors[i]);
                    } catch (InterruptedException e) {
                        logger.error(e.getMessage(), e);
                    }
                }
            });
            futures.add(future);
        }
        for (Future<?> future : futures) {
            try {
                future.get();
            } catch (Exception e) {
                logger.error(e.getMessage(), e);
            }
        }
        hdfFile.close();
        writer.shutdown();
    }
}
