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

package io.nosqlbench.engine.api.activityimpl.tracking;

import io.nosqlbench.engine.api.activityimpl.marker.longheap.LongTreeTracker;
import io.nosqlbench.engine.api.activityimpl.marker.longheap.LongTreeTrackerAtomic;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class LongTreeTrackerTest2 {
    private final static Logger logger = LogManager.getLogger(LongTreeTrackerTest2.class);

//    @Test
//    public void testCoMask() {
//        LongTreeTracker t = new LongTreeTracker();
//        assertThat(t.comaskOfBit(0b1000L)).isEqualTo(0b1100L);
//        assertThat(t.comaskOfBit(0b0100L)).isEqualTo(0b1100L);
//        assertThat(t.comaskOfBit(1L<<61)).isEqualTo(1L<<61|1L<<60);
//    }

//    @Test
//    public void testParentBit() {
//        LongTreeTracker t = new LongTreeTracker();
//        assertThat(t.parentOf(0)).isEqualTo(1);
//        assertThat(t.parentOf(1)).isEqualTo(1);
//        assertThat(t.parentOf(2)).isEqualTo(2);
//        assertThat(t.parentOf(23)).isEqualTo(11);
//    }

    @Test
    public void testLowestCompleted() {
        LongTreeTracker t;

        t= new LongTreeTracker(0);
        assertThat(t.getLowestCompleted()).isEqualTo(-1L);

        t = new LongTreeTracker(Long.MIN_VALUE);
        assertThat(t.getLowestCompleted()).isEqualTo(0L);

        t = new LongTreeTracker(Long.MIN_VALUE>>>2);
        assertThat(t.getLowestCompleted()).isEqualTo(2L);
        t.setCompleted(0);
        assertThat(t.getLowestCompleted()).isEqualTo(0L);
    }

    @Test
    public void testHighestCompleted() {
        LongTreeTracker t;

        t = new LongTreeTracker(0L);
        assertThat(t.getHighestCompleted()).isEqualTo(-1);

        t = new LongTreeTracker(Long.MIN_VALUE);
        assertThat(t.getHighestCompleted()).isEqualTo(0L);

        t = new LongTreeTracker(Long.MIN_VALUE>>>3);
        assertThat(t.getHighestCompleted()).isEqualTo(3L);
        t.setCompleted(9);
        assertThat(t.getHighestCompleted()).isEqualTo(9L);
    }

    @Test
    public void testTotalCompleted() {
        LongTreeTracker t;

        t = new LongTreeTracker(0L);
        assertThat(t.getTotalCompleted()).isEqualTo(0);

        t = new LongTreeTracker(Long.MIN_VALUE);
        assertThat(t.getTotalCompleted()).isEqualTo(1L);

        t = new LongTreeTracker();
        t.setCompleted(3);
        t.setCompleted(5);
        t.setCompleted(7);
        assertThat(t.getTotalCompleted()).isEqualTo(3);

    }

    @Test
    public void testApply() {
        LongTreeTracker t = new LongTreeTracker(0L);
        t.setCompleted(0);
        logger.debug(t);
        t.setCompleted(1);
        logger.debug(t);
        t.setCompleted(2);
        logger.debug(t);
        t.setCompleted(5);
        logger.debug(t);
        t.setCompleted(6);
        logger.debug(t);
        t.setCompleted(3);
        logger.debug(t);
        t.setCompleted(4);
        logger.debug(t);
        t.setCompleted(7);
        logger.debug(t);
    }

    @Test
    public void testFullCycle() {
        LongTreeTracker t = new LongTreeTracker();
        for (int i = 0; i < 32 ; i++) {
            t.setCompleted(i);
        }
        logger.debug(t);
        assertThat(t.getImage()).isEqualTo(-2L);
    }

    @Test
    public void testCompleted() {
        LongTreeTracker t1 = new LongTreeTracker(0);
        assertThat(t1.isCompleted(0)).isFalse();
        t1.setCompleted(3);
        assertThat(t1.isCompleted(0)).isFalse();
        t1.setCompleted(2);
        assertThat(t1.isCompleted(0)).isFalse();
        t1.setCompleted(1);
        assertThat(t1.isCompleted(0)).isFalse();
        t1.setCompleted(0);
        assertThat(t1.isCompleted(0)).isTrue();

    }

    @Test
    public void testBitString() {
        LongTreeTracker t = new LongTreeTracker(2L);
        logger.debug(t);
    }

    /**
     * Last result on a mobile i7 CPU:
     * <pre>
     *  count: 1073741824
     *  duration ms: 13730.785213
     *  rate/ms: 78199.593639
     *  rate/s: 78199593.638928
     * </pre>
     */

    @Test
    @Disabled
    public void speedcheckThreadLocal() {
        long t1=System.nanoTime();
        LongTreeTracker t = new LongTreeTracker();
        int count=1024*1024*1024;
        for(int i=0;i<count;i++) {
            int j = i % 32;
            t.setCompleted(j);
        }

        long t2=System.nanoTime();
        double duration = ((double) t2 - (double) t1)/1000000.0d;
        double rate = ((double) count) / duration;
        System.out.format("count: %d\n",count);
        System.out.format("duration ms: %f\n", duration);
        System.out.format("rate/ms: %f\n", rate);
        System.out.format("rate/s: %f\n", rate * 1000.0d);
    }

    @Test
    @Disabled
    public void speedcheckConcurrentLocal() {
        long t1=System.nanoTime();
        LongTreeTracker t = new LongTreeTrackerAtomic();
        int count=1024*1024*1024;
        for(int i=0;i<count;i++) {
            int j = i % 32;
            t.setCompleted(j);
        }

        long t2=System.nanoTime();
        double duration = ((double) t2 - (double) t1)/1000000.0d;
        double rate = ((double) count) / duration;
        System.out.format("count: %d\n",count);
        System.out.format("duration ms: %f\n", duration);
        System.out.format("rate/ms: %f\n", rate);
        System.out.format("rate/s: %f\n", rate * 1000.0d);
    }

}
