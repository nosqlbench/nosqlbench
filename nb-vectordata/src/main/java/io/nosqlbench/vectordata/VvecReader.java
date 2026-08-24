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
package io.nosqlbench.vectordata;

/** Random access to variable-dimension vectors backed by an IDXFOR sidecar. */
public interface VvecReader<A> {
    long count();
    int dimensionAt(long index);
    A get(long index);
    void prebuffer(PrebufferProgress progress);
    default void prebuffer() { prebuffer(PrebufferProgress.NONE); }
    boolean isComplete();
    CacheStats cacheStats();
    ElementType elementType();
}
