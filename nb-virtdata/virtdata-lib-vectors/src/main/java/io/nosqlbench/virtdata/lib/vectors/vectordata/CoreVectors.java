package io.nosqlbench.virtdata.lib.vectors.vectordata;

/*
 * Copyright (c) nosqlbench
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */


import io.nosqlbench.nbdatatools.api.concurrent.ProgressIndicatingFuture;
import io.nosqlbench.vectordata.VectorTestData;
import io.nosqlbench.vectordata.discovery.ProfileSelector;
import io.nosqlbench.vectordata.discovery.TestDataView;
import io.nosqlbench.vectordata.downloader.Catalog;
import io.nosqlbench.vectordata.downloader.DatasetEntry;
import io.nosqlbench.vectordata.spec.datasets.types.DatasetView;
import java.util.concurrent.CompletableFuture;
import java.util.function.LongFunction;

public abstract class CoreVectors<T> implements LongFunction<T> {

    protected final TestDataView tdv;
    protected final DatasetView<T> dataset;

    public CoreVectors(String datasetAndProfile, boolean prebuffer) {
        Catalog catalog = VectorTestData.catalogs().configure().catalog();
        tdv = catalog.profile(datasetAndProfile);
        dataset = getRandomAccessData();

        if (prebuffer) {
            CompletableFuture<Void> pbfuture = dataset.prebuffer();
            if (pbfuture instanceof ProgressIndicatingFuture<Void> indicator) {
                    indicator.monitorProgress(1000);
            }
        }
    }

    protected abstract DatasetView<T> getRandomAccessData();

    @Override
    public T apply(long value) {
        return dataset.get(value);
    }

}
