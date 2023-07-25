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

package io.nosqlbench.adapters.api.metrics;

import io.nosqlbench.adapters.api.metrics.EndToEndMetricsAdapterUtil.MSG_SEQ_ERROR_SIMU_TYPE;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MessageSequenceNumberSendingHandlerTest {
    MessageSequenceNumberSendingHandler sequenceNumberSendingHandler = new MessageSequenceNumberSendingHandler();

    @Test
    void shouldAddMonotonicSequence() {
        for (long l = 1; 100 >= l; l++)
            assertEquals(l, this.sequenceNumberSendingHandler.getNextSequenceNumber(Collections.emptySet()));
    }

    @Test
    void shouldInjectMessageLoss() {
        assertEquals(1L, this.sequenceNumberSendingHandler.getNextSequenceNumber(Collections.emptySet()));
        assertEquals(3L, this.sequenceNumberSendingHandler.getNextSequenceNumber(Collections.singleton(MSG_SEQ_ERROR_SIMU_TYPE.MsgLoss), 100));
    }

    @Test
    void shouldInjectMessageDuplication() {
        assertEquals(1L, this.sequenceNumberSendingHandler.getNextSequenceNumber(Collections.emptySet()));
        assertEquals(1L, this.sequenceNumberSendingHandler.getNextSequenceNumber(Collections.singleton(MSG_SEQ_ERROR_SIMU_TYPE.MsgDup), 100));
    }

    @Test
    void shouldInjectMessageOutOfOrder() {
        assertEquals(1L, this.sequenceNumberSendingHandler.getNextSequenceNumber(Collections.emptySet()));
        assertEquals(4L, this.sequenceNumberSendingHandler.getNextSequenceNumber(Collections.singleton(MSG_SEQ_ERROR_SIMU_TYPE.OutOfOrder), 100));
        assertEquals(2L, this.sequenceNumberSendingHandler.getNextSequenceNumber(Collections.emptySet()));
        assertEquals(3L, this.sequenceNumberSendingHandler.getNextSequenceNumber(Collections.emptySet()));
        assertEquals(5L, this.sequenceNumberSendingHandler.getNextSequenceNumber(Collections.emptySet()));
        assertEquals(6, this.sequenceNumberSendingHandler.getNextSequenceNumber(Collections.emptySet()));
    }

    @Test
    void shouldInjectOneOfTheSimulatedErrorsRandomly() {
        final Set<MSG_SEQ_ERROR_SIMU_TYPE> allErrorTypes = new HashSet<>(Arrays.asList(MSG_SEQ_ERROR_SIMU_TYPE.values()));

        assertEquals(1L, this.sequenceNumberSendingHandler.getNextSequenceNumber(Collections.emptySet()));
        long previousSequenceNumber = 1L;
        int outOfSequenceInjectionCounter = 0;
        int messageDupCounter = 0;
        int messageLossCounter = 0;
        int successCounter = 0;
        for (int i = 0; 1000 > i; i++) {
            final long nextSequenceNumber = this.sequenceNumberSendingHandler.getNextSequenceNumber(allErrorTypes);
            if (nextSequenceNumber >= (previousSequenceNumber + 3)) outOfSequenceInjectionCounter++;
            else if (nextSequenceNumber <= previousSequenceNumber) messageDupCounter++;
            else if (nextSequenceNumber >= (previousSequenceNumber + 2)) messageLossCounter++;
            else if (nextSequenceNumber == (previousSequenceNumber + 1)) successCounter++;
            previousSequenceNumber = nextSequenceNumber;
        }
        assertTrue(0 < outOfSequenceInjectionCounter);
        assertTrue(0 < messageDupCounter);
        assertTrue(0 < messageLossCounter);
        assertEquals(1000, outOfSequenceInjectionCounter + messageDupCounter + messageLossCounter + successCounter);
    }

}
