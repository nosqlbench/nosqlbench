/*
 *
 *    Copyright 2016 jshook
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 * /
 */

package io.nosqlbench.engine.api.metrics;

import org.testng.annotations.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class NicerHistogramTest {

    @Test
    public void testNicerHistogramValues() {
        NicerHistogram nh = new NicerHistogram("testhisto",new DeltaHdrHistogramReservoir("testhisto",4));
        for (int i = 1; i <= 100; i++) {
            nh.update(i);
        }
        ConvenientSnapshot snapshot = nh.getSnapshot();
        assertThat(snapshot.getMax()).isEqualTo(100);

        nh.getDeltaSnapshot(500); // Just to reset
        for (int i=1; i<= 200; i++ ) {
            nh.update(i);
        }
        ConvenientSnapshot deltaSnapshot1 = nh.getDeltaSnapshot(500);
        assertThat(deltaSnapshot1.getMax()).isEqualTo(200);

        ConvenientSnapshot cachedSnapshot = nh.getSnapshot();
        assertThat(cachedSnapshot.getMax()).isEqualTo(200);
        for (int i=1; i<= 300; i++ ) {
            nh.update(i);
        }
        ConvenientSnapshot stillCachedSnapshot = nh.getSnapshot();
        assertThat(stillCachedSnapshot.getMax()).isEqualTo(200);

        try {
            Thread.sleep(501);
        } catch (InterruptedException ignored) {
        }

        ConvenientSnapshot notCachedAnyMoreSnapshot = nh.getSnapshot();
        notCachedAnyMoreSnapshot.getMax();
        assertThat(notCachedAnyMoreSnapshot.getMax()).isEqualTo(300);


    }

}