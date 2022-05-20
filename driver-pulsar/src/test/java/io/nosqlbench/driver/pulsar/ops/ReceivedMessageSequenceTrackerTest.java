package io.nosqlbench.driver.pulsar.ops;

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


import static org.junit.jupiter.api.Assertions.assertEquals;

import com.codahale.metrics.Counter;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class ReceivedMessageSequenceTrackerTest {
    Counter msgErrOutOfSeqCounter = new Counter();
    Counter msgErrDuplicateCounter = new Counter();
    Counter msgErrLossCounter = new Counter();
    ReceivedMessageSequenceTracker messageSequenceTracker = new ReceivedMessageSequenceTracker(msgErrOutOfSeqCounter, msgErrDuplicateCounter, msgErrLossCounter, 20, 20);

    @Test
    void shouldCountersBeZeroWhenSequenceDoesntContainGaps() {
        // when
        for (long l = 0; l < 100L; l++) {
            messageSequenceTracker.sequenceNumberReceived(l);
        }
        messageSequenceTracker.close();
        // then
        assertEquals(0, msgErrOutOfSeqCounter.getCount());
        assertEquals(0, msgErrDuplicateCounter.getCount());
        assertEquals(0, msgErrLossCounter.getCount());
    }

    @ParameterizedTest
    @ValueSource(longs = {10L, 11L, 19L, 20L, 21L, 100L})
    void shouldDetectMsgLossWhenEverySecondMessageIsLost(long totalMessages) {
        doShouldDetectMsgLoss(totalMessages, 2);
    }

    @ParameterizedTest
    @ValueSource(longs = {10L, 11L, 19L, 20L, 21L, 100L})
    void shouldDetectMsgLossWhenEveryThirdMessageIsLost(long totalMessages) {
        doShouldDetectMsgLoss(totalMessages, 3);
    }

    @ParameterizedTest
    @ValueSource(longs = {20L, 21L, 40L, 41L, 42L, 43L, 100L})
    void shouldDetectMsgLossWhenEvery21stMessageIsLost(long totalMessages) {
        doShouldDetectMsgLoss(totalMessages, 21);
    }

    private void doShouldDetectMsgLoss(long totalMessages, int looseEveryNthMessage) {
        int messagesLost = 0;
        // when
        boolean lastMessageWasLost = false;
        for (long l = 0; l < totalMessages; l++) {
            if (l % looseEveryNthMessage == 1) {
                messagesLost++;
                lastMessageWasLost = true;
                continue;
            } else {
                lastMessageWasLost = false;
            }
            messageSequenceTracker.sequenceNumberReceived(l);
        }
        if (lastMessageWasLost) {
            messageSequenceTracker.sequenceNumberReceived(totalMessages);
        }
        messageSequenceTracker.close();
        // then
        assertEquals(0, msgErrOutOfSeqCounter.getCount());
        assertEquals(0, msgErrDuplicateCounter.getCount());
        assertEquals(messagesLost, msgErrLossCounter.getCount());
    }

    @ParameterizedTest
    @ValueSource(longs = {10L, 11L, 19L, 20L, 21L, 100L})
    void shouldDetectMsgDuplication(long totalMessages) {
        int messagesDuplicated = 0;
        // when
        for (long l = 0; l < totalMessages; l++) {
            if (l % 2 == 1) {
                messagesDuplicated++;
                messageSequenceTracker.sequenceNumberReceived(l);
            }
            messageSequenceTracker.sequenceNumberReceived(l);
        }
        if (totalMessages % 2 == 0) {
            messageSequenceTracker.sequenceNumberReceived(totalMessages);
        }
        if (totalMessages < 2 * messageSequenceTracker.getMaxTrackOutOfOrderSequenceNumbers()) {
            messageSequenceTracker.close();
        }

        // then
        assertEquals(0, msgErrOutOfSeqCounter.getCount());
        assertEquals(messagesDuplicated, msgErrDuplicateCounter.getCount());
        assertEquals(0, msgErrLossCounter.getCount());
    }

    @Test
    void shouldDetectSingleMessageOutOfSequence() {
        // when
        for (long l = 0; l < 10L; l++) {
            messageSequenceTracker.sequenceNumberReceived(l);
        }
        messageSequenceTracker.sequenceNumberReceived(10L);
        messageSequenceTracker.sequenceNumberReceived(12L);
        messageSequenceTracker.sequenceNumberReceived(11L);
        for (long l = 13L; l < 100L; l++) {
            messageSequenceTracker.sequenceNumberReceived(l);
        }

        // then
        assertEquals(1, msgErrOutOfSeqCounter.getCount());
        assertEquals(0, msgErrDuplicateCounter.getCount());
        assertEquals(0, msgErrLossCounter.getCount());
    }

    @Test
    void shouldDetectMultipleMessagesOutOfSequence() {
        // when
        for (long l = 0; l < 10L; l++) {
            messageSequenceTracker.sequenceNumberReceived(l);
        }
        messageSequenceTracker.sequenceNumberReceived(10L);
        messageSequenceTracker.sequenceNumberReceived(14L);
        messageSequenceTracker.sequenceNumberReceived(13L);
        messageSequenceTracker.sequenceNumberReceived(11L);
        messageSequenceTracker.sequenceNumberReceived(12L);
        for (long l = 15L; l < 100L; l++) {
            messageSequenceTracker.sequenceNumberReceived(l);
        }

        // then
        assertEquals(2, msgErrOutOfSeqCounter.getCount());
        assertEquals(0, msgErrDuplicateCounter.getCount());
        assertEquals(0, msgErrLossCounter.getCount());
    }

    @Test
    void shouldDetectIndividualMessageLoss() {
        // when
        for (long l = 0; l < 100L; l++) {
            if (l != 11L) {
                messageSequenceTracker.sequenceNumberReceived(l);
            }
        }
        messageSequenceTracker.close();

        // then
        assertEquals(0, msgErrOutOfSeqCounter.getCount());
        assertEquals(0, msgErrDuplicateCounter.getCount());
        assertEquals(1, msgErrLossCounter.getCount());
    }

    @Test
    void shouldDetectGapAndMessageDuplication() {
        // when
        for (long l = 0; l < 100L; l++) {
            if (l != 11L) {
                messageSequenceTracker.sequenceNumberReceived(l);
            }
            if (l == 12L) {
                messageSequenceTracker.sequenceNumberReceived(l);
            }
        }
        messageSequenceTracker.close();

        // then
        assertEquals(0, msgErrOutOfSeqCounter.getCount());
        assertEquals(1, msgErrDuplicateCounter.getCount());
        assertEquals(1, msgErrLossCounter.getCount());
    }

    @Test
    void shouldDetectGapAndMessageDuplicationTimes2() {
        // when
        for (long l = 0; l < 100L; l++) {
            if (l != 11L) {
                messageSequenceTracker.sequenceNumberReceived(l);
            }
            if (l == 12L) {
                messageSequenceTracker.sequenceNumberReceived(l);
                messageSequenceTracker.sequenceNumberReceived(l);
            }
        }
        messageSequenceTracker.close();

        // then
        assertEquals(0, msgErrOutOfSeqCounter.getCount());
        assertEquals(2, msgErrDuplicateCounter.getCount());
        assertEquals(1, msgErrLossCounter.getCount());
    }


    @Test
    void shouldDetectDelayedOutOfOrderDelivery() {
        // when
        for (long l = 0; l < 5 * messageSequenceTracker.getMaxTrackOutOfOrderSequenceNumbers(); l++) {
            if (l != 10) {
                messageSequenceTracker.sequenceNumberReceived(l);
            }
            if (l == messageSequenceTracker.getMaxTrackOutOfOrderSequenceNumbers() * 2) {
                messageSequenceTracker.sequenceNumberReceived(10);
            }
        }
        messageSequenceTracker.close();

        // then
        assertEquals(1, msgErrOutOfSeqCounter.getCount());
        assertEquals(0, msgErrDuplicateCounter.getCount());
        assertEquals(0, msgErrLossCounter.getCount());
    }

    @Test
    void shouldDetectDelayedOutOfOrderDeliveryOf2ConsecutiveSequenceNumbers() {
        // when
        for (long l = 0; l < 5 * messageSequenceTracker.getMaxTrackOutOfOrderSequenceNumbers(); l++) {
            if (l != 10 && l != 11) {
                messageSequenceTracker.sequenceNumberReceived(l);
            }
            if (l == messageSequenceTracker.getMaxTrackOutOfOrderSequenceNumbers() * 2) {
                messageSequenceTracker.sequenceNumberReceived(10);
                messageSequenceTracker.sequenceNumberReceived(11);
            }
        }
        messageSequenceTracker.close();

        // then
        assertEquals(2, msgErrOutOfSeqCounter.getCount());
        assertEquals(0, msgErrDuplicateCounter.getCount());
        assertEquals(0, msgErrLossCounter.getCount());
    }
}
