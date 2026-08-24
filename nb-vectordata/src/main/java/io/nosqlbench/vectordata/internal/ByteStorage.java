/*
 * Copyright (c) 2026 The NoSQLBench Authors.
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
package io.nosqlbench.vectordata.internal;

import io.nosqlbench.vectordata.CacheStats;
import io.nosqlbench.vectordata.PrebufferProgress;

import java.nio.ByteBuffer;

/** Thread-safe source of arbitrary byte ranges. */
public interface ByteStorage extends AutoCloseable {
    long size();
    ByteBuffer read(long offset, int length);
    void prebuffer(PrebufferProgress progress);
    boolean isComplete();
    CacheStats stats();
    @Override default void close() { }
}
