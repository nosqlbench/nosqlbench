/*
 * Copyright (c) 2024 nosqlbench
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

package io.nosqlbench.nb.api.lifecycle;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * <P>This object pooling class provides a simple way to pool objects which:
 * <UL>
 * <LI>Are expensive to create relative to how they are used</LI>
 * <LI>Need to be used my multiple threads</LI>
 * <LI>Are not thread safe</LI>
 * </UL>
 * <p>
 * The overhead of using this pool should be weighed against the simpler case of just creating the object where it is
 * needed. Generally speaking, avoid using this pool unless you know that object caching or pooling is needed from
 * profiling.
 * </P>
 *
 * <P>This pool keeps count of how many active elements are in use by caller threads. In the event that the available
 * pool of objects is more than 2X the size of the active ones, it is reduced to 1/2 its current size, <em>after</em>
 * the last element is released. This means that the pool will size down automatically when there are transient or
 * episodic spikes.
 * </P>
 *
 * <P>The pool also handles object resetting via the {@link Consumer} provided. Although it is expressed as a consumer,
 * it doesn't strictly consume references. It simply does whatever the caller needs in order to effectively reset one of
 * the pooled objects as it is returned to the resource pool.</P>
 *
 * <P>Safe usage of this class is achieved by wrapping it with a try-with-resources clause so that automatic
 * resource management takes care of returning and reusing objects. The type returned by {@link #get()} is a
 * reference wrapper. For example, you can create and use a pool of {@link StringBuilder}s like this:
 *
 * <pre><code>
 *         ObjectPool<StringBuilder> pool = new ObjectPool<>(StringBuilder::new, sb -> sb.setLength());
 *         try (var handle = pool.get()) {
 *             StringBuilder sb=handle.ref;
 *             sb.append("abc");
 *             sb.append("xyz");
 *             System.out.println(sb.toString());
 *         } catch (Exception e) {
 *             throw new RuntimeException(e);
 *         }
 * </code></pre>
 * <p>
 * At the end of the try-with-resources, the StringBuilder will be returned to the pool after its length has been
 * reset to 0.
 * </P>
 *
 * @param <T>
 */
public class ObjectPool<T> implements Supplier<ObjectPool.Borrowed<T>> {

    private final Logger logger = LogManager.getLogger(ObjectPool.class);
    private final Supplier<T> supplier;
    private final Consumer<T> reset;
    private final ConcurrentLinkedQueue<Borrowed<T>> pool = new ConcurrentLinkedQueue<>();
    private int active = 0;

    public ObjectPool(Supplier<T> supplier, Consumer<T> reset) {
        this.supplier = supplier;
        this.reset = reset;
    }

    public ObjectPool<T> allocate(int count) {
        for (int i = 0; i < count; i++) {
            pool.add(new Borrowed<>(this, supplier.get()));
        }
        return this;
    }

    public void release(Borrowed<T> handle) {
        active--;
        reset.accept(handle.ref);
        pool.add(handle);
        if (active < pool.size() >> 1) {
            logger.debug(() -> "truncating resource pool to match active use: active=" + active + "pool=" + pool.size());
            while (active < pool.size()) {
                pool.poll();
            }
        }
    }

    public int getActive() {
        return active;
    }

    public int getInactive() {
        return pool.size();
    }

    @SuppressWarnings("resource")
    @Override
    public Borrowed<T> get() {
        var handle = pool.poll();
        while (handle == null) {
            allocate(1);
            handle = pool.poll();
        }
        active++;
        return handle;
    }

    public static class Borrowed<T> implements AutoCloseable {
        private final ObjectPool<T> pool;
        public final T ref;

        public Borrowed(ObjectPool<T> pool, T ref) {
            this.pool = pool;
            this.ref = ref;
        }

        @Override
        public void close() throws Exception {
            pool.release(this);
        }
    }


}
