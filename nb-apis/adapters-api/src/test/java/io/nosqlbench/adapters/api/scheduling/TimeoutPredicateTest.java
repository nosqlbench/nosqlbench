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

package io.nosqlbench.adapters.api.scheduling;

import org.junit.jupiter.api.Test;

import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;

public class TimeoutPredicateTest {

    @Test
    public void testNeverCompletablePreciate() {
        int interval=10;
        int timeout=500;

        TimeoutPredicate<Boolean> wontMakeIt = TimeoutPredicate.of(
            ()->false,
            l -> l,
            Duration.ofMillis(timeout),
            Duration.ofMillis(interval),
            true
        );

        TimeoutPredicate.Result<Boolean> resultNow = wontMakeIt.test();
        assertThat(resultNow.duration_ns()).isEqualTo(0L);
        assertThat(resultNow.value()).isFalse();
        assertThat(resultNow.status()).isEqualTo(TimeoutPredicate.Status.pending);

        resultNow = wontMakeIt.test();
        assertThat(resultNow.duration_ns()).isBetween(10*1_000_000L,50*1_000_000L);
        assertThat(resultNow.value()).isFalse();
        assertThat(resultNow.status()).isEqualTo(TimeoutPredicate.Status.pending);

        while (resultNow.status()== TimeoutPredicate.Status.pending) {
            resultNow=wontMakeIt.test();
        }

        assertThat(resultNow.status()).isEqualTo(TimeoutPredicate.Status.incomplete);

    }

    @Test
    public void testImmediatelyCompletablePreciate() {
        int interval=10;
        int timeout=5000;
        TimeoutPredicate<Boolean> canMakeIt = TimeoutPredicate.of(
            ()->true,
            l -> l,
            Duration.ofMillis(timeout),
            Duration.ofMillis(interval),
            true
        );

        TimeoutPredicate.Result<Boolean> resultNow = canMakeIt.test();
        assertThat(resultNow.duration_ns()).isEqualTo(0L);
        assertThat(resultNow.value()).isTrue();
        assertThat(resultNow.status()).isEqualTo(TimeoutPredicate.Status.complete);

    }

    @Test
    public void testEventuallyCompletePredicate() {

        int interval=250;
        int timeout=5000;
        long now = System.currentTimeMillis();
        long inASec = now+1000;
        TimeoutPredicate<Long> canMakeIt = TimeoutPredicate.of(
            System::currentTimeMillis,
            l -> l>inASec,
            Duration.ofMillis(timeout),
            Duration.ofMillis(interval),
            true
        );

        TimeoutPredicate.Result<Long> result = canMakeIt.test();
        System.out.println(result);

        while (result.status()== TimeoutPredicate.Status.pending) {
//            canMakeIt.blockUntilNextInterval();
            result=canMakeIt.test();
            System.out.println(canMakeIt);
            System.out.println(result);
        }

        assertThat(result.status()).isEqualTo(TimeoutPredicate.Status.complete);
    }

}
