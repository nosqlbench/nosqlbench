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

package io.nosqlbench.engine.api.activityimpl.input;

public class LinkedInputTest {

    // TODO: Reintegrate these tests as follow the leader tests via output and input
//    @Test
//    public void shouldStayAtOrBehindLinkedInput() {
//        Input goes2Kfast = new TargetRateInput(ActivityDef.parseActivityDef("alias=goes2k;targetrate=2000"));
//        LinkedInput goesAsFast = new LinkedInput(ActivityDef.parseActivityDef("alias=asfast"),goes2Kfast);
//
//        long last2kFast = 0L;
//        long lastAsFast = 0L;
//        for (int i = 0; i < 100; i++) {
//            last2kFast = goes2Kfast.getCycle();
//            assertThat(goesAsFast.canAdvance()).isTrue();
//            lastAsFast = goesAsFast.getCycle();
//            assertThat(goesAsFast.canAdvance()).isFalse();
//            assertThat(goesAsFast.canAdvance()).isFalse();
//        }
//    }
//
//    @Test
//    public void shouldBlockUntilLinkedAdvances() {
//        ContiguousInput goes2Kfast = new TargetRateInput(ActivityDef.parseActivityDef("targetrate=2000"));
//        LinkedInput goesAsFast = new LinkedInput(ActivityDef.parseActivityDef("alias=asfast"),goes2Kfast);
//
//        AtomicLong asFastValue = new AtomicLong(0L);
//        Runnable linked = new Runnable() {
//            @Override
//            public void run() {
//                long value = goes2Kfast.getCycle();
//                asFastValue.set(value);
//            }
//        };
//
//        Thread thread = new Thread(linked);
//        thread.start();
//        assertThat(thread.isAlive()).isTrue();
//        try {
//            Thread.sleep(200);
//        } catch (InterruptedException ignored) {
//        }
//        assertThat(asFastValue.get()).isEqualTo(0L);
//        goes2Kfast.getCycle();
//
//        try {
//            Thread.sleep(100);
//        } catch (InterruptedException ignored) {
//            assertThat(asFastValue.get()).isEqualTo(1L);
//        }
//    }
//
//    @Test(enabled=false)
//    public void microBenchDiffRate() {
//        TargetRateInput fastInput = new TargetRateInput(ActivityDef.parseActivityDef("targetrate=10000000"));
//        LinkedInput slowInput = new LinkedInput(ActivityDef.parseActivityDef("alias=asfast"),fastInput);
//        Timer fastInputTimer = new NicerTimer("fastinput", new DeltaHdrHistogramReservoir("fastinput",4));
//        Timer slowInputTimer = new NicerTimer("slowinput", new DeltaHdrHistogramReservoir("slowinput",4));
//
//        long cycles=1000000;
//        long updateAt=cycles / 10;
//
//        Runnable fastInputRunnable = new Runnable() {
//            @Override
//            public void run() {
//                long value=-1L;
//                for (int i = 0; i < cycles ; i++) {
//                    try (Timer.Context c = fastInputTimer.time()) {
//                        value = fastInput.getCycle();
//                        if ((value % updateAt)==0) {
//                            System.out.println("fastone:" + value + " @" + System.nanoTime());
//                            System.out.flush();
//                        }
//                    }
//                }
//                System.out.println("fastone: lastvalue:" + value);
//                System.out.flush();
//            }
//        };
//
//        Runnable slowInputRunnable = new Runnable() {
//            @Override
//            public void run() {
//                long value=-1L;
//                for (int i = 0; i < cycles; i++) {
//                    try (Timer.Context c = slowInputTimer.time()) {
//                        value = slowInput.getCycle();
//                        if ((value % updateAt)==0) {
//                            System.out.println("slowone:" + value + " @" + System.nanoTime());
//                            System.out.flush();
//                        }
//                    }
//                }
//                System.out.println("slowone: lastvalue:" + value);
//                System.out.flush();
//            }
//        };
//
//        Thread fastInputThread = new Thread(fastInputRunnable);
//        Thread slowInputThread = new Thread(slowInputRunnable);
//
//        slowInputThread.start();
//        fastInputThread.start();
//
//        while (fastInputThread.isAlive()) {
//            try {
//                Thread.sleep(100);
//            } catch (InterruptedException ignored) {
//            }
//        }
//
//        System.out.println("fastone: 999th nano latency: " + fastInputTimer.getSnapshot().get999thPercentile());
//        System.out.println("slowone: 999th nano latency: " + slowInputTimer.getSnapshot().get999thPercentile());
//
//        System.out.println("fastone: timer counts: " + fastInputTimer.getCount());
//        System.out.println("slowone: timer counts: " + slowInputTimer.getCount());
//    }

}
