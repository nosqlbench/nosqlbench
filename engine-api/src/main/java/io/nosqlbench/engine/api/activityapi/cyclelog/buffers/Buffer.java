/*
 * Copyright (c) 2022-2023 nosqlbench
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

package io.nosqlbench.engine.api.activityapi.cyclelog.buffers;

import java.util.ArrayList;
import java.util.List;

/**
 * This is a lightweight buffer implementation that allows for buffer
 * flipping and callbacks when the buffer is full.
 * @param <T> The type held in this buffer
 */
public abstract class Buffer<T extends Comparable> {

    private int position;
    private final int limit;
    protected ArrayList<T> data;

    public Buffer(int size) {
        data = new ArrayList<>(size);
        this.limit=size;
    }

    protected void onFull() {
    }

    protected abstract int compare(T one, T other);

    public int remaining() {
        return limit-position;
    }

    public Buffer<T> put(T element) {
        data.add(element);
        if (data.size()==limit) {
            onFull();
        }
        return this;
    }

//    @Override
//    public int compareTo(Buffer<T> other) {
//        int diff = Integer.compare(this.data.size(), other.data.size());
//        if (diff!=0) return diff;
//
//        for (int i = 0; i < data.size(); i++) {
//            diff = data.get(i).compareTo(other.data.get(i));
//            if (diff!=0) {
//                return diff;
//            }
//        }
//        return 0;
//    }

    public List<T> getFlippedData() {
        return data;
    }

    @Override
    public String toString() {
        return "position=" + this.position + ", limit=" + this.limit + ", capacity=" + (data!=null ? data.size() : "NULLDATA");
    }
}
