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


import io.nosqlbench.vectordata.Catalog;
import io.nosqlbench.vectordata.CatalogSources;
import io.nosqlbench.vectordata.TestDataView;
import io.nosqlbench.vectordata.VectorDataSettings;
import io.nosqlbench.vectordata.VectorReader;
import java.util.function.LongFunction;

public abstract class CoreVectors<T> implements LongFunction<T> {

    protected final TestDataView tdv;
    protected final VectorReader<T> dataset;

    public CoreVectors(String datasetAndProfile, boolean prebuffer) {
        this(datasetAndProfile, prebuffer, VectorDataSettings.defaults());
    }

    protected CoreVectors(String datasetAndProfile, boolean prebuffer, VectorDataSettings settings) {
        Catalog catalog = Catalog.of(CatalogSources.defaults(), settings);
        tdv = catalog.openProfile(datasetAndProfile);
        dataset = getRandomAccessData();

        if (prebuffer) {
            dataset.prebuffer();
        }
    }

    protected abstract VectorReader<T> getRandomAccessData();

    @Override
    public T apply(long value) {
        return dataset.get(value);
    }

}
