package io.nosqlbench.virtdata.userlibs.apps.valuesapp;

/*
 * Copyright (c) 2022 nosqlbench
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


public class LongHistogram implements PostProcessor {

    long[] buckets;
    long mask;

    public LongHistogram(int bits) {
        if (bits>16) {
            throw new RuntimeException("bits > 16 may cause OOM");
        }
        mask=0L;
        for (int i = 1; i <= bits; i++) {
            mask&=1L<<i;
        }
        System.out.println("mask:" + mask);
    }

    @Override
    public void process(Object[] values) {
        for (Object value : values) {
            long v = (long) value;

        }

    }

    @Override
    public void close() {

    }
}
