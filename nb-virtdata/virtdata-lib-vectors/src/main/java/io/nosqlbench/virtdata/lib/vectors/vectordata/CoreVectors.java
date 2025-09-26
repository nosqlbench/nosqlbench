package io.nosqlbench.virtdata.lib.vectors.vectordata;

import io.nosqlbench.nbdatatools.api.concurrent.ProgressIndicatingFuture;
import io.nosqlbench.vectordata.VectorTestData;
import io.nosqlbench.vectordata.discovery.TestDataView;
import io.nosqlbench.vectordata.downloader.Catalog;
import io.nosqlbench.vectordata.downloader.DatasetEntry;
import io.nosqlbench.vectordata.spec.datasets.types.DatasetView;
import io.nosqlbench.virtdata.api.annotations.ThreadSafeMapper;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.function.LongFunction;

public abstract class CoreVectors<T> implements LongFunction<T> {

    protected final TestDataView tdv;
    protected final DatasetView<T> dataset;

    public CoreVectors(String datasetAndProfile, boolean prebuffer) {
        Catalog catalog = VectorTestData.catalogs().configure().catalog();
        DatasetEntry datasetEntry = catalog.findExact(datasetAndProfile)
            .orElseThrow(() -> new RuntimeException("Cannot find dataset and/or profile '" + datasetAndProfile + "'"));
        tdv = datasetEntry.select().profile(datasetAndProfile);
        dataset = getRandomAccessData();

        if (prebuffer) {
            CompletableFuture<Void> pbfuture = dataset.prebuffer();
            if (pbfuture instanceof ProgressIndicatingFuture<Void> indicator) {
//                try {
                    indicator.monitorProgress(1000);
//                } catch (InterruptedException | ExecutionException | TimeoutException e) {
//                    throw new RuntimeException(e);
//                }
            }
        }
    }

    protected abstract DatasetView<T> getRandomAccessData();

    @Override
    public T apply(long value) {
        return dataset.get(value);
    }

}
