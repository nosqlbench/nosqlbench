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

import com.codahale.metrics.Counter;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ReceivedMessageSequenceTrackerTest {
    Counter msgErrOutOfSeqCounter = new Counter();
    Counter msgErrDuplicateCounter = new Counter();
    Counter msgErrLossCounter = new Counter();
    ReceivedMessageSequenceTracker messageSequenceTracker = new ReceivedMessageSequenceTracker(this.msgErrOutOfSeqCounter, this.msgErrDuplicateCounter, this.msgErrLossCounter, 20, 20);

    @Test
    void shouldCountersBeZeroWhenSequenceDoesntContainGaps() {
        // when
        for (long l = 0; 100L > l; l++) this.messageSequenceTracker.sequenceNumberReceived(l);
        this.messageSequenceTracker.close();
        // then
        assertEquals(0, this.msgErrOutOfSeqCounter.getCount());
        assertEquals(0, this.msgErrDuplicateCounter.getCount());
        assertEquals(0, this.msgErrLossCounter.getCount());
    }

    @ParameterizedTest
    @ValueSource(longs = {10L, 11L, 19L, 20L, 21L, 100L})
    void shouldDetectMsgLossWhenEverySecondMessageIsLost(final long totalMessages) {
        this.doShouldDetectMsgLoss(totalMessages, 2);
    }

    @ParameterizedTest
    @ValueSource(longs = {10L, 11L, 19L, 20L, 21L, 100L})
    void shouldDetectMsgLossWhenEveryThirdMessageIsLost(final long totalMessages) {
        this.doShouldDetectMsgLoss(totalMessages, 3);
    }

    @ParameterizedTest
    @ValueSource(longs = {20L, 21L, 40L, 41L, 42L, 43L, 100L})
    void shouldDetectMsgLossWhenEvery21stMessageIsLost(final long totalMessages) {
        this.doShouldDetectMsgLoss(totalMessages, 21);
    }

    private void doShouldDetectMsgLoss(final long totalMessages, final int looseEveryNthMessage) {
        int messagesLost = 0;
        // when
        boolean lastMessageWasLost = false;
        for (long l = 0; l < totalMessages; l++) {
            if (1 == (l % looseEveryNthMessage)) {
                messagesLost++;
                lastMessageWasLost = true;
                continue;
            }
            lastMessageWasLost = false;
            this.messageSequenceTracker.sequenceNumberReceived(l);
        }
        if (lastMessageWasLost) this.messageSequenceTracker.sequenceNumberReceived(totalMessages);
        this.messageSequenceTracker.close();
        // then
        assertEquals(0, this.msgErrOutOfSeqCounter.getCount());
        assertEquals(0, this.msgErrDuplicateCounter.getCount());
        assertEquals(messagesLost, this.msgErrLossCounter.getCount());
    }

    @ParameterizedTest
    @ValueSource(longs = {10L, 11L, 19L, 20L, 21L, 100L})
    void shouldDetectMsgDuplication(final long totalMessages) {
        int messagesDuplicated = 0;
        // when
        for (long l = 0; l < totalMessages; l++) {
            if (1 == (l % 2)) {
                messagesDuplicated++;
                this.messageSequenceTracker.sequenceNumberReceived(l);
            }
            this.messageSequenceTracker.sequenceNumberReceived(l);
        }
        if (0 == (totalMessages % 2)) this.messageSequenceTracker.sequenceNumberReceived(totalMessages);
        if (totalMessages < (2L * this.messageSequenceTracker.getMaxTrackOutOfOrderSequenceNumbers()))
            this.messageSequenceTracker.close();

        // then
        assertEquals(0, this.msgErrOutOfSeqCounter.getCount());
        assertEquals(messagesDuplicated, this.msgErrDuplicateCounter.getCount());
        assertEquals(0, this.msgErrLossCounter.getCount());
    }

    @Test
    void shouldDetectSingleMessageOutOfSequence() {
        // when
        for (long l = 0; 10L > l; l++) this.messageSequenceTracker.sequenceNumberReceived(l);
        this.messageSequenceTracker.sequenceNumberReceived(10L);
        this.messageSequenceTracker.sequenceNumberReceived(12L);
        this.messageSequenceTracker.sequenceNumberReceived(11L);
        for (long l = 13L; 100L > l; l++) this.messageSequenceTracker.sequenceNumberReceived(l);

        // then
        assertEquals(1, this.msgErrOutOfSeqCounter.getCount());
        assertEquals(0, this.msgErrDuplicateCounter.getCount());
        assertEquals(0, this.msgErrLossCounter.getCount());
    }

    @Test
    void shouldDetectMultipleMessagesOutOfSequence() {
        // when
        for (long l = 0; 10L > l; l++) this.messageSequenceTracker.sequenceNumberReceived(l);
        this.messageSequenceTracker.sequenceNumberReceived(10L);
        this.messageSequenceTracker.sequenceNumberReceived(14L);
        this.messageSequenceTracker.sequenceNumberReceived(13L);
        this.messageSequenceTracker.sequenceNumberReceived(11L);
        this.messageSequenceTracker.sequenceNumberReceived(12L);
        for (long l = 15L; 100L > l; l++) this.messageSequenceTracker.sequenceNumberReceived(l);

        // then
        assertEquals(2, this.msgErrOutOfSeqCounter.getCount());
        assertEquals(0, this.msgErrDuplicateCounter.getCount());
        assertEquals(0, this.msgErrLossCounter.getCount());
    }

    @Test
    void shouldDetectIndividualMessageLoss() {
        // when
        for (long l = 0; 100L > l; l++) if (11L != l) this.messageSequenceTracker.sequenceNumberReceived(l);
        this.messageSequenceTracker.close();

        // then
        assertEquals(0, this.msgErrOutOfSeqCounter.getCount());
        assertEquals(0, this.msgErrDuplicateCounter.getCount());
        assertEquals(1, this.msgErrLossCounter.getCount());
    }

    @Test
    void shouldDetectGapAndMessageDuplication() {
        // when
        for (long l = 0; 100L > l; l++) {
            if (11L != l) this.messageSequenceTracker.sequenceNumberReceived(l);
            if (12L == l) this.messageSequenceTracker.sequenceNumberReceived(12L);
        }
        this.messageSequenceTracker.close();

        // then
        assertEquals(0, this.msgErrOutOfSeqCounter.getCount());
        assertEquals(1, this.msgErrDuplicateCounter.getCount());
        assertEquals(1, this.msgErrLossCounter.getCount());
    }

    @Test
    void shouldDetectGapAndMessageDuplicationTimes2() {
        // when
        for (long l = 0; 100L > l; l++) {
            if (11L != l) this.messageSequenceTracker.sequenceNumberReceived(l);
            if (12L == l) {
                this.messageSequenceTracker.sequenceNumberReceived(12L);
                this.messageSequenceTracker.sequenceNumberReceived(l);
            }
        }
        this.messageSequenceTracker.close();

        // then
        assertEquals(0, this.msgErrOutOfSeqCounter.getCount());
        assertEquals(2, this.msgErrDuplicateCounter.getCount());
        assertEquals(1, this.msgErrLossCounter.getCount());
    }


    @Test
    void shouldDetectDelayedOutOfOrderDelivery() {
        // when
        for (long l = 0; l < (5L * this.messageSequenceTracker.getMaxTrackOutOfOrderSequenceNumbers()); l++) {
            if (10 != l) this.messageSequenceTracker.sequenceNumberReceived(l);
            if (l == (this.messageSequenceTracker.getMaxTrackOutOfOrderSequenceNumbers() * 2L))
                this.messageSequenceTracker.sequenceNumberReceived(10);
        }
        this.messageSequenceTracker.close();

        // then
        assertEquals(1, this.msgErrOutOfSeqCounter.getCount());
        assertEquals(0, this.msgErrDuplicateCounter.getCount());
        assertEquals(0, this.msgErrLossCounter.getCount());
    }

    @Test
    void shouldDetectDelayedOutOfOrderDeliveryOf2ConsecutiveSequenceNumbers() {
        // when
        for (long l = 0; l < (5L * this.messageSequenceTracker.getMaxTrackOutOfOrderSequenceNumbers()); l++) {
            if ((10 != l) && (11 != l)) this.messageSequenceTracker.sequenceNumberReceived(l);
            if (l == (this.messageSequenceTracker.getMaxTrackOutOfOrderSequenceNumbers() * 2L)) {
                this.messageSequenceTracker.sequenceNumberReceived(10);
                this.messageSequenceTracker.sequenceNumberReceived(11);
            }
        }
        this.messageSequenceTracker.close();

        // then
        assertEquals(2, this.msgErrOutOfSeqCounter.getCount());
        assertEquals(0, this.msgErrDuplicateCounter.getCount());
        assertEquals(0, this.msgErrLossCounter.getCount());
    }
}
