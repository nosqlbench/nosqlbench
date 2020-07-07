package io.nosqlbench.engine.core.experimental;

import org.junit.Test;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class CompletableTests {

    @Test
    public void testCompletionStages() {
        CompletableFuture<Object> f = new CompletableFuture<>();
        ExecutorService executorService = Executors.newCachedThreadPool();
        CompletableFuture<Object> objectCompletableFuture = f.completeAsync(() -> "foo", executorService);
        boolean bar = objectCompletableFuture.complete("bar");

    }
}
