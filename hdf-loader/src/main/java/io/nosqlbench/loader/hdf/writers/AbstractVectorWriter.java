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

package io.nosqlbench.loader.hdf.writers;

import java.util.concurrent.LinkedBlockingQueue;

public abstract class AbstractVectorWriter implements VectorWriter {
    protected LinkedBlockingQueue<float[]> queue;

    public void setQueue(LinkedBlockingQueue<float[]> queue) {
        this.queue = queue;
    }

    @Override
    public void run() {
        while (true) {
            try {
                float[] vector = queue.take();
                if (vector.length==0) {
                    break;
                }
                writeVector(vector);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }

    protected abstract void writeVector(float[] vector);

}
